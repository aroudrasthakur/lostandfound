package com.uta.lostfound.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uta.lostfound.data.model.Match
import com.uta.lostfound.data.model.MatchStatus
import kotlinx.coroutines.tasks.await
import java.util.UUID

class MatchRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val matchesCollection = firestore.collection("matches")
    private val itemsCollection = firestore.collection("items")
    private val notificationRepository = NotificationRepository()

    suspend fun createMatchRequest(
        itemId: String,
        itemOwnerId: String,
        claimantUserId: String,
        requesterId: String,
        itemTitle: String,
        requesterName: String
    ): Result<Match> {
        return try {
            val matchId = UUID.randomUUID().toString()
            
            val match = Match(
                id = matchId,
                itemId = itemId,
                itemOwnerId = itemOwnerId,
                claimantUserId = claimantUserId,
                requesterId = requesterId,
                status = MatchStatus.PENDING,
                itemOwnerApproved = requesterId == itemOwnerId,
                claimantApproved = requesterId == claimantUserId,
                timestamp = System.currentTimeMillis()
            )
            
            matchesCollection.document(matchId).set(match.toMap()).await()
            
            // Send notification to the other user
            val recipientId = if (requesterId == itemOwnerId) claimantUserId else itemOwnerId
            notificationRepository.sendMatchRequestNotification(
                recipientUserId = recipientId,
                senderUserId = requesterId,
                senderName = requesterName,
                itemId = itemId,
                itemTitle = itemTitle,
                matchId = matchId
            )
            
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatchRequestsForItem(itemId: String): Result<List<Match>> {
        return try {
            val snapshot = matchesCollection
                .whereEqualTo("itemId", itemId)
                .whereIn("status", listOf(MatchStatus.PENDING.name, MatchStatus.APPROVED.name))
                .get()
                .await()
            
            val matches = snapshot.documents.mapNotNull { doc ->
                try {
                    Match(
                        id = doc.getString("id") ?: "",
                        itemId = doc.getString("itemId") ?: "",
                        itemOwnerId = doc.getString("itemOwnerId") ?: "",
                        claimantUserId = doc.getString("claimantUserId") ?: "",
                        requesterId = doc.getString("requesterId") ?: "",
                        status = MatchStatus.valueOf(doc.getString("status") ?: "PENDING"),
                        itemOwnerApproved = doc.getBoolean("itemOwnerApproved") ?: false,
                        claimantApproved = doc.getBoolean("claimantApproved") ?: false,
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        approvedAt = doc.getLong("approvedAt")?.takeIf { it > 0 },
                        notificationSent = doc.getBoolean("notificationSent") ?: false
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            Result.success(matches)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPendingMatchForUser(itemId: String, userId: String): Result<Match?> {
        return try {
            val snapshot = matchesCollection
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("status", MatchStatus.PENDING.name)
                .get()
                .await()
            
            val match = snapshot.documents.mapNotNull { doc ->
                try {
                    val match = Match(
                        id = doc.getString("id") ?: "",
                        itemId = doc.getString("itemId") ?: "",
                        itemOwnerId = doc.getString("itemOwnerId") ?: "",
                        claimantUserId = doc.getString("claimantUserId") ?: "",
                        requesterId = doc.getString("requesterId") ?: "",
                        status = MatchStatus.valueOf(doc.getString("status") ?: "PENDING"),
                        itemOwnerApproved = doc.getBoolean("itemOwnerApproved") ?: false,
                        claimantApproved = doc.getBoolean("claimantApproved") ?: false,
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        approvedAt = doc.getLong("approvedAt")?.takeIf { it > 0 },
                        notificationSent = doc.getBoolean("notificationSent") ?: false
                    )
                    // Only return if this user is involved and hasn't approved yet
                    if ((match.itemOwnerId == userId && !match.itemOwnerApproved) ||
                        (match.claimantUserId == userId && !match.claimantApproved)) {
                        match
                    } else null
                } catch (e: Exception) {
                    null
                }
            }.firstOrNull()
            
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveMatch(matchId: String, userId: String): Result<Unit> {
        return try {
            val matchDoc = matchesCollection.document(matchId).get().await()
            
            val match = Match(
                id = matchDoc.getString("id") ?: "",
                itemId = matchDoc.getString("itemId") ?: "",
                itemOwnerId = matchDoc.getString("itemOwnerId") ?: "",
                claimantUserId = matchDoc.getString("claimantUserId") ?: "",
                requesterId = matchDoc.getString("requesterId") ?: "",
                status = MatchStatus.valueOf(matchDoc.getString("status") ?: "PENDING"),
                itemOwnerApproved = matchDoc.getBoolean("itemOwnerApproved") ?: false,
                claimantApproved = matchDoc.getBoolean("claimantApproved") ?: false,
                timestamp = matchDoc.getLong("timestamp") ?: 0L,
                approvedAt = matchDoc.getLong("approvedAt")?.takeIf { it > 0 },
                notificationSent = matchDoc.getBoolean("notificationSent") ?: false
            )
            
            // Update approval status
            val updates = mutableMapOf<String, Any>()
            when (userId) {
                match.itemOwnerId -> updates["itemOwnerApproved"] = true
                match.claimantUserId -> updates["claimantApproved"] = true
            }
            
            // Check if both approved
            val bothApproved = (if (userId == match.itemOwnerId) true else match.itemOwnerApproved) &&
                               (if (userId == match.claimantUserId) true else match.claimantApproved)
            
            if (bothApproved) {
                updates["status"] = MatchStatus.APPROVED.name
                updates["approvedAt"] = System.currentTimeMillis()
                
                // Update item status to matched
                itemsCollection.document(match.itemId).update(
                    mapOf(
                        "isMatched" to true,
                        "matchId" to matchId,
                        "isActive" to false,
                        "updatedAt" to System.currentTimeMillis()
                    )
                ).await()
            }
            
            matchesCollection.document(matchId).update(updates).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectMatch(matchId: String): Result<Unit> {
        return try {
            matchesCollection.document(matchId).update(
                mapOf(
                    "status" to MatchStatus.REJECTED.name
                )
            ).await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatchedItems(userId: String): Result<List<String>> {
        return try {
            val snapshot = matchesCollection
                .whereEqualTo("status", MatchStatus.APPROVED.name)
                .get()
                .await()
            
            val itemIds = snapshot.documents.mapNotNull { doc ->
                val itemOwnerId = doc.getString("itemOwnerId")
                val claimantUserId = doc.getString("claimantUserId")
                if (itemOwnerId == userId || claimantUserId == userId) {
                    doc.getString("itemId")
                } else null
            }
            
            Result.success(itemIds)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
