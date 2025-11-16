package com.lostandfound.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lostandfound.app.data.model.Chat
import com.lostandfound.app.data.model.Message
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MessageRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun sendMessage(message: Message): Result<Message> {
        return try {
            val messageId = firestore.collection("messages").document().id
            val newMessage = message.copy(id = messageId)
            
            firestore.collection("messages")
                .document(messageId)
                .set(newMessage.toMap())
                .await()
            
            // Update chat's last message
            updateChatLastMessage(message.chatId, message.text)
            
            Result.success(newMessage)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getMessagesForChat(chatId: String): Flow<List<Message>> = callbackFlow {
        val subscription = firestore.collection("messages")
            .whereEqualTo("chatId", chatId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val messages = snapshot?.documents?.mapNotNull { 
                    it.toObject(Message::class.java) 
                } ?: emptyList()
                
                trySend(messages)
            }
        
        awaitClose { subscription.remove() }
    }

    suspend fun createOrGetChat(
        userId1: String,
        userId2: String,
        userName1: String,
        userName2: String,
        itemId: String,
        itemTitle: String
    ): Result<Chat> {
        return try {
            // Check if chat already exists
            val existingChats = firestore.collection("chats")
                .whereArrayContains("participants", userId1)
                .get()
                .await()
            
            val existingChat = existingChats.documents
                .mapNotNull { it.toObject(Chat::class.java) }
                .firstOrNull { chat ->
                    chat.participants.contains(userId2) && chat.itemId == itemId
                }
            
            if (existingChat != null) {
                return Result.success(existingChat)
            }
            
            // Create new chat
            val chatId = firestore.collection("chats").document().id
            val newChat = Chat(
                id = chatId,
                participants = listOf(userId1, userId2),
                participantNames = mapOf(userId1 to userName1, userId2 to userName2),
                itemId = itemId,
                itemTitle = itemTitle
            )
            
            firestore.collection("chats")
                .document(chatId)
                .set(newChat.toMap())
                .await()
            
            Result.success(newChat)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserChats(userId: String): Flow<List<Chat>> = callbackFlow {
        val subscription = firestore.collection("chats")
            .whereArrayContains("participants", userId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val chats = snapshot?.documents?.mapNotNull { 
                    it.toObject(Chat::class.java) 
                } ?: emptyList()
                
                trySend(chats)
            }
        
        awaitClose { subscription.remove() }
    }

    private suspend fun updateChatLastMessage(chatId: String, lastMessage: String) {
        try {
            firestore.collection("chats")
                .document(chatId)
                .update(
                    mapOf(
                        "lastMessage" to lastMessage,
                        "lastMessageTime" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            // Handle error silently
        }
    }

    suspend fun markMessageAsRead(messageId: String): Result<Unit> {
        return try {
            firestore.collection("messages")
                .document(messageId)
                .update("isRead", true)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
