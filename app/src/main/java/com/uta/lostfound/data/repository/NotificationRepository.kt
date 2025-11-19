package com.uta.lostfound.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.uta.lostfound.data.model.Match
import com.uta.lostfound.data.model.Notification
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUserMatches(userId: String): Result<List<Match>> {
        return try {
            val matches = mutableListOf<Match>()
            
            // Get matches where user lost an item
            val lostMatches = firestore.collection("matches")
                .whereEqualTo("lostUserId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Match::class.java) }
            
            // Get matches where user found an item
            val foundMatches = firestore.collection("matches")
                .whereEqualTo("foundUserId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Match::class.java) }
            
            matches.addAll(lostMatches)
            matches.addAll(foundMatches)
            
            Result.success(matches.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatchById(matchId: String): Result<Match> {
        return try {
            val doc = firestore.collection("matches")
                .document(matchId)
                .get()
                .await()
            
            val match = doc.toObject(Match::class.java) ?: throw Exception("Match not found")
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendItemNotification(
        recipientUserId: String,
        senderUserId: String,
        senderName: String,
        itemTitle: String,
        notificationType: String // "have_item" or "claim_item"
    ): Result<Unit> {
        return try {
            val notification = hashMapOf(
                "recipientUserId" to recipientUserId,
                "senderUserId" to senderUserId,
                "senderName" to senderName,
                "itemTitle" to itemTitle,
                "type" to notificationType,
                "timestamp" to System.currentTimeMillis(),
                "read" to false
            )
            
            firestore.collection("notifications")
                .add(notification)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMatchRequestNotification(
        recipientUserId: String,
        senderUserId: String,
        senderName: String,
        itemId: String,
        itemTitle: String,
        matchId: String
    ): Result<Unit> {
        return try {
            val notification = hashMapOf(
                "recipientUserId" to recipientUserId,
                "senderUserId" to senderUserId,
                "senderName" to senderName,
                "itemTitle" to itemTitle,
                "itemId" to itemId,
                "matchId" to matchId,
                "type" to "match_request",
                "timestamp" to System.currentTimeMillis(),
                "read" to false
            )
            
            firestore.collection("notifications")
                .add(notification)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMatchApprovedNotification(
        recipientUserId: String,
        approverUserId: String,
        approverName: String,
        itemId: String,
        itemTitle: String,
        matchId: String
    ): Result<Unit> {
        return try {
            val notification = hashMapOf(
                "recipientUserId" to recipientUserId,
                "senderUserId" to approverUserId,
                "senderName" to approverName,
                "itemTitle" to itemTitle,
                "itemId" to itemId,
                "matchId" to matchId,
                "type" to "match_approved",
                "timestamp" to System.currentTimeMillis(),
                "read" to false
            )
            
            firestore.collection("notifications")
                .add(notification)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun sendMatchRejectedNotification(
        recipientUserId: String,
        rejecterUserId: String,
        rejecterName: String,
        itemId: String,
        itemTitle: String,
        matchId: String
    ): Result<Unit> {
        return try {
            val notification = hashMapOf(
                "recipientUserId" to recipientUserId,
                "senderUserId" to rejecterUserId,
                "senderName" to rejecterName,
                "itemTitle" to itemTitle,
                "itemId" to itemId,
                "matchId" to matchId,
                "type" to "match_rejected",
                "timestamp" to System.currentTimeMillis(),
                "read" to false
            )
            
            firestore.collection("notifications")
                .add(notification)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserNotifications(userId: String): Result<List<Notification>> {
        return try {
            val snapshot = firestore.collection("notifications")
                .whereEqualTo("recipientUserId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val notifications = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notification::class.java)?.copy(id = doc.id)
            }
            
            Result.success(notifications)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun markNotificationAsRead(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .update("read", true)
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("notifications")
                .document(notificationId)
                .delete()
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
