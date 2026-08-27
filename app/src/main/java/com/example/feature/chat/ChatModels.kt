package com.example.feature.chat

import com.google.firebase.Timestamp

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val timestamp: Timestamp? = null,
    val imageUrl: String = "",
    val isDeleted: Boolean = false,
    val replyTo: ReplyTo? = null,
    val reactions: Map<String, List<String>> = emptyMap()
)

data class ReplyTo(
    val messageId: String = "",
    val senderName: String = "",
    val text: String = ""
)

data class PrivateChatMeta(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null,
    val unreadCount: Map<String, Int> = emptyMap()
)
