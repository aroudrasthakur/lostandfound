package com.uta.lostfound.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Notification(
    val id: String = "",
    val recipientUserId: String = "",
    val senderUserId: String = "",
    val senderName: String = "",
    val itemTitle: String = "",
    val itemId: String = "",
    val matchId: String = "",
    val type: String = "", // "have_item", "claim_item", or "match_request"
    val timestamp: Long = 0L,
    val read: Boolean = false
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "recipientUserId" to recipientUserId,
            "senderUserId" to senderUserId,
            "senderName" to senderName,
            "itemTitle" to itemTitle,
            "itemId" to itemId,
            "matchId" to matchId,
            "type" to type,
            "timestamp" to timestamp,
            "read" to read
        )
    }
}
