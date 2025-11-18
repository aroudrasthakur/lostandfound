package com.uta.lostfound.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

enum class MatchStatus {
    PENDING,    // Match request sent, waiting for approval
    APPROVED,   // Both users approved, match confirmed
    REJECTED    // One user rejected the match
}

@Parcelize
data class Match(
    val id: String = "",
    val itemId: String = "",              // The item being matched (can be lost or found)
    val itemOwnerId: String = "",         // User who posted the item
    val claimantUserId: String = "",      // User who is claiming/found the item
    val requesterId: String = "",         // User who initiated the match request
    val status: MatchStatus = MatchStatus.PENDING,
    val itemOwnerApproved: Boolean = false,
    val claimantApproved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val approvedAt: Long? = null,
    val notificationSent: Boolean = false
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "itemId" to itemId,
            "itemOwnerId" to itemOwnerId,
            "claimantUserId" to claimantUserId,
            "requesterId" to requesterId,
            "status" to status.name,
            "itemOwnerApproved" to itemOwnerApproved,
            "claimantApproved" to claimantApproved,
            "timestamp" to timestamp,
            "approvedAt" to (approvedAt ?: 0),
            "notificationSent" to notificationSent
        )
    }
}
