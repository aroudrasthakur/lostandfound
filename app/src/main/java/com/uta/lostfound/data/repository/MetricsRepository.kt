package com.uta.lostfound.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uta.lostfound.data.model.Metrics
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class MetricsRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getCurrentMonthMetrics(): Result<Metrics> {
        return try {
            val monthId = getCurrentMonthId()
            val doc = firestore.collection("metrics")
                .document(monthId)
                .get()
                .await()
            
            val metrics = if (doc.exists()) {
                doc.toObject(Metrics::class.java) ?: getDefaultMetrics(monthId)
            } else {
                getDefaultMetrics(monthId)
            }
            
            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getMonthlyMetrics() = getCurrentMonthMetrics()

    suspend fun updateMetrics(): Result<Metrics> {
        return try {
            val monthId = getCurrentMonthId()
            
            // Count all lost items (active and inactive)
            val allLostItems = firestore.collection("lost_items")
                .get()
                .await()
                .documents.mapNotNull { it.toObject(com.uta.lostfound.data.model.Item::class.java) }
            
            // Count all found items (active and inactive)
            val allFoundItems = firestore.collection("found_items")
                .get()
                .await()
                .documents.mapNotNull { it.toObject(com.uta.lostfound.data.model.Item::class.java) }
            
            // Count matched items (items with isMatched = true)
            val matchedLostCount = allLostItems.count { it.isMatched }
            val matchedFoundCount = allFoundItems.count { it.isMatched }
            val matchedCount = matchedLostCount + matchedFoundCount
            
            // Count active, non-matched items
            val activeLostCount = allLostItems.count { it.isActive && !it.isMatched }
            val activeFoundCount = allFoundItems.count { it.isActive && !it.isMatched }
            
            // Total active items (lost + found)
            val lostCount = activeLostCount
            val foundCount = activeFoundCount
            
            // Count total users
            val totalUsers = firestore.collection("users")
                .get()
                .await()
                .size()
            
            // Unclaimed items are the active, non-matched items
            val unclaimedCount = lostCount + foundCount
            
            val metrics = Metrics(
                id = monthId,
                lostCount = lostCount,
                foundCount = foundCount,
                unclaimedCount = unclaimedCount,
                matchedCount = matchedCount,
                totalUsers = totalUsers,
                lastUpdated = System.currentTimeMillis()
            )
            
            firestore.collection("metrics")
                .document(monthId)
                .set(metrics.toMap())
                .await()
            
            Result.success(metrics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAllMetrics(): Result<List<Metrics>> {
        return try {
            val metricsList = firestore.collection("metrics")
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Metrics::class.java) }
                .sortedByDescending { it.id }
            
            Result.success(metricsList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCurrentMonthId(): String {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getDefaultMetrics(monthId: String): Metrics {
        return Metrics(
            id = monthId,
            lostCount = 0,
            foundCount = 0,
            unclaimedCount = 0,
            matchedCount = 0,
            totalUsers = 0
        )
    }
}
