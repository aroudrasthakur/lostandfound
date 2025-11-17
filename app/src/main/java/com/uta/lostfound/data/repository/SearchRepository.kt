package com.uta.lostfound.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.ItemStatus
import kotlinx.coroutines.tasks.await

class SearchRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun searchItems(query: String, searchInLost: Boolean = true, searchInFound: Boolean = true): Result<List<Item>> {
        return try {
            val results = mutableListOf<Item>()
            
            if (searchInLost) {
                val lostItems = firestore.collection("lost_items")
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(Item::class.java)?.copy(status = ItemStatus.LOST) }
                    .filter { item ->
                        item.title.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true) ||
                        item.location.contains(query, ignoreCase = true)
                    }
                results.addAll(lostItems)
            }
            
            if (searchInFound) {
                val foundItems = firestore.collection("found_items")
                    .whereEqualTo("isActive", true)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(Item::class.java)?.copy(status = ItemStatus.FOUND) }
                    .filter { item ->
                        item.title.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true) ||
                        item.category.contains(query, ignoreCase = true) ||
                        item.location.contains(query, ignoreCase = true)
                    }
                results.addAll(foundItems)
            }
            
            Result.success(results.sortedByDescending { it.createdAt })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchByCategory(category: String, status: ItemStatus): Result<List<Item>> {
        return try {
            val collection = if (status == ItemStatus.LOST) "lost_items" else "found_items"
            val items = firestore.collection(collection)
                .whereEqualTo("category", category)
                .whereEqualTo("isActive", true)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Item::class.java)?.copy(status = status) }
            
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchByLocation(location: String, status: ItemStatus): Result<List<Item>> {
        return try {
            val collection = if (status == ItemStatus.LOST) "lost_items" else "found_items"
            val items = firestore.collection(collection)
                .whereEqualTo("isActive", true)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Item::class.java)?.copy(status = status) }
                .filter { it.location.contains(location, ignoreCase = true) }
            
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
