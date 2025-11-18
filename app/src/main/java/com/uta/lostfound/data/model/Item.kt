package com.uta.lostfound.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val location: String = "",
    val date: Long = System.currentTimeMillis(),
    val imageUrl: String = "",
    val userId: String = "",
    val userName: String = "",
    val status: ItemStatus = ItemStatus.LOST,
    val isActive: Boolean = true,
    val isMatched: Boolean = false,
    val matchId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "title" to title,
            "description" to description,
            "category" to category,
            "location" to location,
            "date" to date,
            "imageUrl" to imageUrl,
            "userId" to userId,
            "userName" to userName,
            "status" to status.name,
            "isActive" to isActive,
            "isMatched" to isMatched,
            "matchId" to matchId,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}

enum class ItemStatus {
    LOST,
    FOUND
}

enum class ItemCategory(val displayName: String) {
    ELECTRONICS("Electronics"),
    CLOTHING("Clothing"),
    ACCESSORIES("Accessories"),
    BOOKS("Books"),
    KEYS("Keys"),
    BAGS("Bags"),
    DOCUMENTS("Documents"),
    OTHER("Other");
    
    companion object {
        fun fromString(value: String): ItemCategory {
            return values().find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}
