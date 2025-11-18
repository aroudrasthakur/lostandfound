package com.uta.lostfound.util

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Migration utility to add isMatched field to existing items
 * Run this once to update all existing items in the database
 */
object DatabaseMigration {
    
    private val firestore = FirebaseFirestore.getInstance()
    
    suspend fun addIsMatchedFieldToAllItems(): Result<String> {
        return try {
            var updatedCount = 0
            
            // Update all lost_items
            val lostItems = firestore.collection("lost_items")
                .get()
                .await()
            
            lostItems.documents.forEach { doc ->
                val data = doc.data
                if (data != null && !data.containsKey("isMatched")) {
                    firestore.collection("lost_items")
                        .document(doc.id)
                        .update("isMatched", false, "matchId", "")
                        .await()
                    updatedCount++
                }
            }
            
            // Update all found_items
            val foundItems = firestore.collection("found_items")
                .get()
                .await()
            
            foundItems.documents.forEach { doc ->
                val data = doc.data
                if (data != null && !data.containsKey("isMatched")) {
                    firestore.collection("found_items")
                        .document(doc.id)
                        .update("isMatched", false, "matchId", "")
                        .await()
                    updatedCount++
                }
            }
            
            Result.success("Successfully updated $updatedCount items")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
