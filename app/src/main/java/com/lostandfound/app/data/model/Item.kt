package com.lostandfound.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Item(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "",
    val itemType: ItemType = ItemType.LOST,
    val location: String = "",
    val date: Long = System.currentTimeMillis(),
    val imageUrls: List<String> = emptyList(),
    val status: ItemStatus = ItemStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "userName" to userName,
            "title" to title,
            "description" to description,
            "category" to category,
            "itemType" to itemType.name,
            "location" to location,
            "date" to date,
            "imageUrls" to imageUrls,
            "status" to status.name,
            "createdAt" to createdAt,
            "updatedAt" to updatedAt
        )
    }
}

enum class ItemType {
    LOST,
    FOUND
}

enum class ItemStatus {
    ACTIVE,
    RESOLVED,
    DELETED
}
