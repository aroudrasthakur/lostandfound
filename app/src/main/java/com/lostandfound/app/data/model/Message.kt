package com.lostandfound.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "chatId" to chatId,
            "senderId" to senderId,
            "senderName" to senderName,
            "receiverId" to receiverId,
            "text" to text,
            "timestamp" to timestamp,
            "isRead" to isRead
        )
    }
}

@Parcelize
data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val itemId: String = "",
    val itemTitle: String = ""
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "participants" to participants,
            "participantNames" to participantNames,
            "lastMessage" to lastMessage,
            "lastMessageTime" to lastMessageTime,
            "itemId" to itemId,
            "itemTitle" to itemTitle
        )
    }
}
