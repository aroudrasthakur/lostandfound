package com.uta.lostfound.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Metrics(
    val id: String = "", // Format: yyyy-mm
    val lostCount: Int = 0,
    val foundCount: Int = 0,
    val matchedCount: Int = 0,
    val totalItemsCount: Int = 0,
    val totalUsers: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "lostCount" to lostCount,
            "foundCount" to foundCount,
            "matchedCount" to matchedCount,
            "totalItemsCount" to totalItemsCount,
            "totalUsers" to totalUsers,
            "lastUpdated" to lastUpdated
        )
    }
}
