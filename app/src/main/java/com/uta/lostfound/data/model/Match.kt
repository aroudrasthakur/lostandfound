package com.uta.lostfound.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Match(
    val id: String = "",
    val lostItemId: String = "",
    val foundItemId: String = "",
    val lostUserId: String = "",
    val foundUserId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val notificationSent: Boolean = false
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "lostItemId" to lostItemId,
            "foundItemId" to foundItemId,
            "lostUserId" to lostUserId,
            "foundUserId" to foundUserId,
            "timestamp" to timestamp,
            "notificationSent" to notificationSent
        )
    }
}
