package com.example.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.core.di.AppContainer
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isTyping: Boolean = false
)

class ChatViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _communityMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val communityMessages: StateFlow<List<ChatMessage>> = _communityMessages.asStateFlow()

    private val _privateChats = MutableStateFlow<List<PrivateChatMeta>>(emptyList())
    val privateChats: StateFlow<List<PrivateChatMeta>> = _privateChats.asStateFlow()

    private val _currentPrivateMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentPrivateMessages: StateFlow<List<ChatMessage>> = _currentPrivateMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var communityListener: ListenerRegistration? = null
    private var privateMetaListener: ListenerRegistration? = null
    private var currentPrivateChatListener: ListenerRegistration? = null

    init {
        listenToCommunityMessages()
        listenToPrivateChatMeta()
    }

    private fun listenToCommunityMessages() {
        communityListener?.remove()
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
                }
            }
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
                }
            }
    }

    fun openPrivateChat(chatId: String) {
        currentPrivateChatListener?.remove()
        _currentPrivateMessages.value = emptyList()
        _isLoading.value = true
        
        currentPrivateChatListener = firestore.collection("privateChats").document(chatId).collection("messages")
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
                    _currentPrivateMessages.value = msgs
                    _isLoading.value = false
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
    }

    fun sendCommunityMessage(text: String) {
        if (text.isBlank()) return
        val user = auth.currentUser ?: return
        
        val docRef = firestore.collection("communityMessages").document()
        val msg = ChatMessage(
            id = docRef.id,
            text = text,
            senderId = user.uid,
            senderName = user.displayName ?: "User",
            senderPhoto = user.photoUrl?.toString() ?: "",
            timestamp = Timestamp.now()
        )
        docRef.set(msg)
    }

    fun sendPrivateMessage(chatId: String, text: String, otherUserId: String) {
        if (text.isBlank()) return
        val user = auth.currentUser ?: return
        
        val msgRef = firestore.collection("privateChats").document(chatId).collection("messages").document()
        val msg = ChatMessage(
            id = msgRef.id,
            text = text,
            senderId = user.uid,
            senderName = user.displayName ?: "User",
            senderPhoto = user.photoUrl?.toString() ?: "",
            timestamp = Timestamp.now()
        )
        msgRef.set(msg)
        
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
                    return ChatViewModel(container.firestore, container.firebaseAuth) as T
                }
            }
    }
}
