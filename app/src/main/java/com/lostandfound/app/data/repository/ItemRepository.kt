package com.lostandfound.app.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.lostandfound.app.data.model.Item
import com.lostandfound.app.data.model.ItemStatus
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ItemRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun createItem(item: Item, imageUris: List<Uri>): Result<Item> {
        return try {
            val itemId = firestore.collection("items").document().id
            val imageUrls = uploadImages(imageUris, itemId)
            
            val newItem = item.copy(
                id = itemId,
                imageUrls = imageUrls,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            
            firestore.collection("items")
                .document(itemId)
                .set(newItem.toMap())
                .await()
            
            Result.success(newItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateItem(item: Item, newImageUris: List<Uri>): Result<Item> {
        return try {
            val additionalUrls = if (newImageUris.isNotEmpty()) {
                uploadImages(newImageUris, item.id)
            } else {
                emptyList()
            }
            
            val updatedItem = item.copy(
                imageUrls = item.imageUrls + additionalUrls,
                updatedAt = System.currentTimeMillis()
            )
            
            firestore.collection("items")
                .document(item.id)
                .set(updatedItem.toMap())
                .await()
            
            Result.success(updatedItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteItem(itemId: String): Result<Unit> {
        return try {
            firestore.collection("items")
                .document(itemId)
                .update("status", ItemStatus.DELETED.name)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItem(itemId: String): Result<Item> {
        return try {
            val doc = firestore.collection("items")
                .document(itemId)
                .get()
                .await()
            
            val item = doc.toObject(Item::class.java) ?: throw Exception("Item not found")
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllItems(): Result<List<Item>> {
        return try {
            val snapshot = firestore.collection("items")
                .whereEqualTo("status", ItemStatus.ACTIVE.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { it.toObject(Item::class.java) }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserItems(userId: String): Result<List<Item>> {
        return try {
            val snapshot = firestore.collection("items")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", ItemStatus.ACTIVE.name)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { it.toObject(Item::class.java) }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchItems(query: String): Result<List<Item>> {
        return try {
            val snapshot = firestore.collection("items")
                .whereEqualTo("status", ItemStatus.ACTIVE.name)
                .get()
                .await()
            
            val items = snapshot.documents
                .mapNotNull { it.toObject(Item::class.java) }
                .filter { item ->
                    item.title.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true) ||
                    item.category.contains(query, ignoreCase = true) ||
                    item.location.contains(query, ignoreCase = true)
                }
            
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadImages(imageUris: List<Uri>, itemId: String): List<String> {
        val imageUrls = mutableListOf<String>()
        
        for (uri in imageUris) {
            val fileName = "${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference
                .child("items/$itemId/$fileName")
            
            storageRef.putFile(uri).await()
            val downloadUrl = storageRef.downloadUrl.await()
            imageUrls.add(downloadUrl.toString())
        }
        
        return imageUrls
    }
}
