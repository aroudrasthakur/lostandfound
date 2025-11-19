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
            
            // Count lost items from lost_items collection
            val lostCount = firestore.collection("lost_items")
                .get()
                .await()
                .size()
            
            // Count found items from found_items collection
            val foundCount = firestore.collection("found_items")
                .get()
                .await()
                .size()
            
            // Count matched items from matched_items collection
            val matchedCount = firestore.collection("matched_items")
                .get()
                .await()
                .size()
            
            // Total items count (lost + found + matched)
            val totalItemsCount = lostCount + foundCount + matchedCount
            
            // Count total users
            val totalUsers = firestore.collection("users")
                .get()
                .await()
                .size()
            
            val metrics = Metrics(
                id = monthId,
                lostCount = lostCount,
                foundCount = foundCount,
                matchedCount = matchedCount,
                totalItemsCount = totalItemsCount,
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
            matchedCount = 0,
            totalItemsCount = 0,
            totalUsers = 0
        )
    }
}
