package com.uta.lostfound.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.ItemStatus
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ItemRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun reportLostItem(item: Item, imageUri: Uri?): Result<Item> {
        return try {
            val itemId = firestore.collection("lost_items").document().id
            val imageUrl = imageUri?.let { uploadImage(it, itemId, "lost") } ?: ""
            
            val newItem = item.copy(
                id = itemId,
                imageUrl = imageUrl,
                status = ItemStatus.LOST,
                createdAt = System.currentTimeMillis()
            )
            
            firestore.collection("lost_items")
                .document(itemId)
                .set(newItem.toMap())
                .await()
            
            Result.success(newItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportFoundItem(item: Item, imageUri: Uri?): Result<Item> {
        return try {
            val itemId = firestore.collection("found_items").document().id
            val imageUrl = imageUri?.let { uploadImage(it, itemId, "found") } ?: ""
            
            val newItem = item.copy(
                id = itemId,
                imageUrl = imageUrl,
                status = ItemStatus.FOUND,
                createdAt = System.currentTimeMillis()
            )
            
            firestore.collection("found_items")
                .document(itemId)
                .set(newItem.toMap())
                .await()
            
            Result.success(newItem)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getFoundItems(): Result<List<Item>> {
        return try {
            val snapshot = firestore.collection("found_items")
                .whereEqualTo("isActive", true)
                .whereEqualTo("isMatched", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Item::class.java)?.copy(status = ItemStatus.FOUND)
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllFoundItems() = getFoundItems()

    suspend fun getLostItems(): Result<List<Item>> {
        return try {
            val snapshot = firestore.collection("lost_items")
                .whereEqualTo("isActive", true)
                .whereEqualTo("isMatched", false)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            
            val items = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Item::class.java)?.copy(status = ItemStatus.LOST)
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllLostItems() = getLostItems()

    suspend fun getUserItems(userId: String): Result<List<Item>> {
        return try {
            val lostItems = firestore.collection("lost_items")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
                .documents.mapNotNull { it.toObject(Item::class.java)?.copy(status = ItemStatus.LOST) }
            
            val foundItems = firestore.collection("found_items")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isActive", true)
                .get()
                .await()
                .documents.mapNotNull { it.toObject(Item::class.java)?.copy(status = ItemStatus.FOUND) }
            
            Result.success(lostItems + foundItems)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getItemById(itemId: String, status: ItemStatus): Result<Item> {
        return try {
            val collection = if (status == ItemStatus.LOST) "lost_items" else "found_items"
            val doc = firestore.collection(collection)
                .document(itemId)
                .get()
                .await()
            
            val item = doc.toObject(Item::class.java)?.copy(status = status)
                ?: throw Exception("Item not found")
            Result.success(item)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getLostItem(itemId: String) = getItemById(itemId, ItemStatus.LOST)
    suspend fun getFoundItem(itemId: String) = getItemById(itemId, ItemStatus.FOUND)

    suspend fun deleteItem(itemId: String, status: ItemStatus): Result<Unit> {
        return try {
            val collection = if (status == ItemStatus.LOST) "lost_items" else "found_items"
            firestore.collection(collection)
                .document(itemId)
                .update("isActive", false)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteLostItem(itemId: String) = deleteItem(itemId, ItemStatus.LOST)
    suspend fun deleteFoundItem(itemId: String) = deleteItem(itemId, ItemStatus.FOUND)

    private suspend fun uploadImage(imageUri: Uri, itemId: String, type: String): String {
        val fileName = "${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference
            .child("items/$type/$itemId/$fileName")
        
        storageRef.putFile(imageUri).await()
        return storageRef.downloadUrl.await().toString()
    }
}
