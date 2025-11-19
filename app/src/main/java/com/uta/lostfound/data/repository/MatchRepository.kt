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
                    // Return if user is the requester (sender waiting for approval)
                    // OR if user is the approver who hasn't approved yet (receiver who needs to approve)
                    if (match.requesterId == userId ||
                        (match.itemOwnerId == userId && !match.itemOwnerApproved) ||
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

    suspend fun approveMatch(matchId: String, userId: String, approverName: String): Result<Unit> {
        return try {
            android.util.Log.d("MatchRepository", "Approving match: $matchId by user: $userId")
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
            
            android.util.Log.d("MatchRepository", "Match details - itemId: ${match.itemId}, requesterId: ${match.requesterId}")
            
            // Try to get item from both collections to determine which one it's in
            var itemDoc = firestore.collection("lost_items").document(match.itemId).get().await()
            var itemCollection = "lost_items"
            
            if (!itemDoc.exists()) {
                itemDoc = firestore.collection("found_items").document(match.itemId).get().await()
                itemCollection = "found_items"
            }
            
            val itemTitle = itemDoc.getString("title") ?: "Unknown Item"
            
            android.util.Log.d("MatchRepository", "Item found in collection: $itemCollection, title: $itemTitle")
            
            // Update approval status
            val updates = mutableMapOf<String, Any>()
            when (userId) {
                match.itemOwnerId -> updates["itemOwnerApproved"] = true
                match.claimantUserId -> updates["claimantApproved"] = true
            }
            
            // Approve immediately when item owner approves (single approval system)
            updates["status"] = MatchStatus.APPROVED.name
            updates["approvedAt"] = System.currentTimeMillis()
            updates["notificationSent"] = true
            
            android.util.Log.d("MatchRepository", "Moving item from $itemCollection to matched_items")
            
            // Get the full item data
            val itemData = itemDoc.data ?: emptyMap()
            val matchedItemData = itemData.toMutableMap().apply {
                put("isMatched", true)
                put("matchId", matchId)
                put("isActive", false)
                put("updatedAt", System.currentTimeMillis())
                put("matchedAt", System.currentTimeMillis())
            }
            
            // Move item to matched_items collection
            firestore.collection("matched_items")
                .document(match.itemId)
                .set(matchedItemData)
                .await()
            
            android.util.Log.d("MatchRepository", "Item added to matched_items collection")
            
            // Delete item from original collection (lost_items or found_items)
            firestore.collection(itemCollection)
                .document(match.itemId)
                .delete()
                .await()
            
            android.util.Log.d("MatchRepository", "Item removed from $itemCollection collection")
            
            android.util.Log.d("MatchRepository", "Sending notification to requester: ${match.requesterId}")
            
            // Send approval notification to the requester
            notificationRepository.sendMatchApprovedNotification(
                recipientUserId = match.requesterId,
                approverUserId = userId,
                approverName = approverName,
                itemId = match.itemId,
                itemTitle = itemTitle,
                matchId = matchId
            )
            
            android.util.Log.d("MatchRepository", "Updating match document")
            matchesCollection.document(matchId).update(updates).await()
            
            android.util.Log.d("MatchRepository", "Match approval completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("MatchRepository", "Error approving match", e)
            Result.failure(e)
        }
    }

    suspend fun rejectMatch(matchId: String, rejecterUserId: String, rejecterName: String): Result<Unit> {
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
            
            // Get item details for notification
            val itemDoc = itemsCollection.document(match.itemId).get().await()
            val itemTitle = itemDoc.getString("title") ?: "Unknown Item"
            
            matchesCollection.document(matchId).update(
                mapOf(
                    "status" to MatchStatus.REJECTED.name
                )
            ).await()
            
            // Send rejection notification to the requester
            notificationRepository.sendMatchRejectedNotification(
                recipientUserId = match.requesterId,
                rejecterUserId = rejecterUserId,
                rejecterName = rejecterName,
                itemId = match.itemId,
                itemTitle = itemTitle,
                matchId = matchId
            )
            
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
