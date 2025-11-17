package com.uta.lostfound.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.uta.lostfound.data.model.Match
import kotlinx.coroutines.tasks.await

class NotificationRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getUserMatches(userId: String): Result<List<Match>> {
        return try {
            val matches = mutableListOf<Match>()
            
            // Get matches where user lost an item
            val lostMatches = firestore.collection("matches")
                .whereEqualTo("lostUserId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Match::class.java) }
            
            // Get matches where user found an item
            val foundMatches = firestore.collection("matches")
                .whereEqualTo("foundUserId", userId)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(Match::class.java) }
            
            matches.addAll(lostMatches)
            matches.addAll(foundMatches)
            
            Result.success(matches.sortedByDescending { it.timestamp })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMatchById(matchId: String): Result<Match> {
        return try {
            val doc = firestore.collection("matches")
                .document(matchId)
                .get()
                .await()
            
            val match = doc.toObject(Match::class.java) ?: throw Exception("Match not found")
            Result.success(match)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
