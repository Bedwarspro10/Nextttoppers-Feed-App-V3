package com.example.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.di.AppContainer
import com.example.core.network.ConnectivityRepository
import com.example.data.models.AppUser
import com.example.data.repositories.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false
)

class ChatViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    connectivityRepository: ConnectivityRepository? = null
) : ViewModel() {

    private val _communityMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val communityMessages: StateFlow<List<ChatMessage>> = _communityMessages.asStateFlow()

    private val _privateChats = MutableStateFlow<List<PrivateChatMeta>>(emptyList())
    val privateChats: StateFlow<List<PrivateChatMeta>> = _privateChats.asStateFlow()

    private val _currentPrivateMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentPrivateMessages: StateFlow<List<ChatMessage>> = _currentPrivateMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isPrivateRoomLoading = MutableStateFlow(false)
    val isPrivateRoomLoading = _isPrivateRoomLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // uid -> resolved profile cache, used to show real names/avatars for the other
    // participant in private chats. Populated lazily via resolveUser(uid).
    private val _userCache = MutableStateFlow<Map<String, AppUser>>(emptyMap())
    val userCache: StateFlow<Map<String, AppUser>> = _userCache.asStateFlow()

    // Which message (if any) the composer is currently replying to.
    private val _replyTarget = MutableStateFlow<ChatMessage?>(null)
    val replyTarget: StateFlow<ChatMessage?> = _replyTarget.asStateFlow()

    val isOnline: StateFlow<Boolean> = connectivityRepository
        ?.isOnline
        ?.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
        ?: MutableStateFlow(true).asStateFlow()

    private var communityListener: ListenerRegistration? = null
    private var privateMetaListener: ListenerRegistration? = null
    private var currentPrivateChatListener: ListenerRegistration? = null
    private val resolvedUids = mutableSetOf<String>()

    init {
        listenToCommunityMessages()
        listenToPrivateChatMeta()
    }

    private fun listenToCommunityMessages() {
        communityListener?.remove()
        _isLoading.value = true
        communityListener = firestore.collection("communityMessages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(100)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.message
                    _isLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java)?.copy(id = it.id) }
                    _communityMessages.value = msgs
                    _isLoading.value = false
                    _error.value = null
                    msgs.forEach { resolveUser(it.senderId) }
                }
            }
    }

    /** Re-attaches the community listener after an error. Safe to call repeatedly. */
    fun retryCommunity() {
        listenToCommunityMessages()
    }

    private fun listenToPrivateChatMeta() {
        val uid = auth.currentUser?.uid ?: return
        privateMetaListener?.remove()
        privateMetaListener = firestore.collection("privateChatMeta")
            .whereArrayContains("participants", uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val chats = snapshot.documents.mapNotNull { it.toObject(PrivateChatMeta::class.java)?.copy(id = it.id) }
                    _privateChats.value = chats.sortedByDescending { it.lastMessageTime }
                    chats.forEach { meta ->
                        meta.participants.firstOrNull { it != uid }?.let { resolveUser(it) }
                    }
                }
            }
    }

    fun openPrivateChat(chatId: String) {
        currentPrivateChatListener?.remove()
        _currentPrivateMessages.value = emptyList()
        _isPrivateRoomLoading.value = true
        clearReplyTarget()

        currentPrivateChatListener = firestore.collection("privateChats").document(chatId).collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .limitToLast(100)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = e.message
                    _isPrivateRoomLoading.value = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val msgs = snapshot.documents.mapNotNull { it.toObject(ChatMessage::class.java)?.copy(id = it.id) }
                    _currentPrivateMessages.value = msgs
                    _isPrivateRoomLoading.value = false
                    _error.value = null

                    // Reset unread count for this user
                    resetUnreadCount(chatId)
                }
            }
    }

    fun closePrivateChat() {
        currentPrivateChatListener?.remove()
        currentPrivateChatListener = null
        _currentPrivateMessages.value = emptyList()
        clearReplyTarget()
    }

    /** Looks up a user's profile once and caches it. No-op if already cached/in-flight. */
    fun resolveUser(uid: String) {
        if (uid.isBlank() || resolvedUids.contains(uid)) return
        resolvedUids.add(uid)
        viewModelScope.launch {
            val user = userRepository.getUserById(uid)
            if (user != null) {
                _userCache.value = _userCache.value + (uid to user)
            }
        }
    }

    fun setReplyTarget(message: ChatMessage) {
        _replyTarget.value = message
    }

    fun clearReplyTarget() {
        _replyTarget.value = null
    }

    fun sendCommunityMessage(text: String) {
        if (text.isBlank()) return
        val user = auth.currentUser ?: return

        val docRef = firestore.collection("communityMessages").document()
        val reply = _replyTarget.value
        val msg = ChatMessage(
            id = docRef.id,
            text = text,
            senderId = user.uid,
            senderName = user.displayName ?: "User",
            senderPhoto = user.photoUrl?.toString() ?: "",
            timestamp = Timestamp.now(),
            replyTo = reply?.let { ReplyTo(messageId = it.id, senderName = it.senderName, text = it.text) }
        )
        docRef.set(msg)
        clearReplyTarget()
    }

    fun sendPrivateMessage(chatId: String, text: String, otherUserId: String) {
        if (text.isBlank()) return
        val user = auth.currentUser ?: return

        val msgRef = firestore.collection("privateChats").document(chatId).collection("messages").document()
        val reply = _replyTarget.value
        val msg = ChatMessage(
            id = msgRef.id,
            text = text,
            senderId = user.uid,
            senderName = user.displayName ?: "User",
            senderPhoto = user.photoUrl?.toString() ?: "",
            timestamp = Timestamp.now(),
            replyTo = reply?.let { ReplyTo(messageId = it.id, senderName = it.senderName, text = it.text) }
        )
        msgRef.set(msg)
        clearReplyTarget()

        // Update meta
        val metaRef = firestore.collection("privateChatMeta").document(chatId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(metaRef)
            if (snapshot.exists()) {
                val currentUnread = snapshot.get("unreadCount") as? Map<String, Long> ?: emptyMap()
                val otherUnread = (currentUnread[otherUserId] ?: 0) + 1

                val newUnread = currentUnread.toMutableMap()
                newUnread[otherUserId] = otherUnread

                transaction.update(metaRef, mapOf(
                    "lastMessage" to text,
                    "lastMessageTime" to Timestamp.now(),
                    "unreadCount" to newUnread
                ))
            } else {
                val newMeta = PrivateChatMeta(
                    id = chatId,
                    participants = listOf(user.uid, otherUserId),
                    lastMessage = text,
                    lastMessageTime = Timestamp.now(),
                    unreadCount = mapOf(otherUserId to 1)
                )
                transaction.set(metaRef, newMeta)
            }
        }
    }

    /** Soft-deletes a community message. Only meaningful for the sender's own messages. */
    fun deleteCommunityMessage(messageId: String) {
        if (messageId.isBlank()) return
        firestore.collection("communityMessages").document(messageId)
            .update(mapOf("isDeleted" to true, "text" to ""))
    }

    /** Soft-deletes a private message. Only meaningful for the sender's own messages. */
    fun deletePrivateMessage(chatId: String, messageId: String) {
        if (chatId.isBlank() || messageId.isBlank()) return
        firestore.collection("privateChats").document(chatId).collection("messages").document(messageId)
            .update(mapOf("isDeleted" to true, "text" to ""))
    }

    /** Toggles the current user's reaction on a community message using the existing `reactions` field. */
    fun toggleCommunityReaction(messageId: String, emoji: String) {
        toggleReaction(firestore.collection("communityMessages").document(messageId), emoji)
    }

    /** Toggles the current user's reaction on a private message using the existing `reactions` field. */
    fun togglePrivateReaction(chatId: String, messageId: String, emoji: String) {
        toggleReaction(
            firestore.collection("privateChats").document(chatId).collection("messages").document(messageId),
            emoji
        )
    }

    private fun toggleReaction(docRef: com.google.firebase.firestore.DocumentReference, emoji: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(docRef)
            @Suppress("UNCHECKED_CAST")
            val currentReactions = (snapshot.get("reactions") as? Map<String, List<String>>) ?: emptyMap()
            val currentUsers = currentReactions[emoji] ?: emptyList()
            val newUsers = if (currentUsers.contains(uid)) currentUsers - uid else currentUsers + uid
            val newReactions = currentReactions.toMutableMap()
            if (newUsers.isEmpty()) newReactions.remove(emoji) else newReactions[emoji] = newUsers
            transaction.update(docRef, "reactions", newReactions)
        }
    }

    private fun resetUnreadCount(chatId: String) {
        val uid = auth.currentUser?.uid ?: return
        val metaRef = firestore.collection("privateChatMeta").document(chatId)
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(metaRef)
            if (snapshot.exists()) {
                val currentUnread = snapshot.get("unreadCount") as? Map<String, Long> ?: emptyMap()
                if ((currentUnread[uid] ?: 0) > 0) {
                    val newUnread = currentUnread.toMutableMap()
                    newUnread[uid] = 0
                    transaction.update(metaRef, "unreadCount", newUnread)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        communityListener?.remove()
        privateMetaListener?.remove()
        currentPrivateChatListener?.remove()
    }

    fun getCurrentUserId(): String = auth.currentUser?.uid ?: ""

    companion object {
        fun provideFactory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val userRepository = UserRepository(container.firebaseAuth, container.firestore)
                    return ChatViewModel(
                        firestore = container.firestore,
                        auth = container.firebaseAuth,
                        userRepository = userRepository,
                        connectivityRepository = container.connectivityRepository
                    ) as T
                }
            }
    }
}
