package com.uta.lostfound.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.uta.lostfound.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance()

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Sign in failed")
            
            // Get FCM token
            val fcmToken = messaging.token.await()
            
            // Get user data from Firestore
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()
            
            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java) ?: throw Exception("User data not found")
            } else {
                // Create user document if doesn't exist
                val newUser = User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: "",
                    email = firebaseUser.email ?: email,
                    fcmToken = fcmToken
                )
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(newUser.toMap())
                    .await()
                newUser
            }
            
            // Update FCM token
            if (user.fcmToken != fcmToken) {
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .update("fcmToken", fcmToken)
                    .await()
            }
            
            Result.success(user.copy(fcmToken = fcmToken))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String): Result<User> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("User creation failed")
            
            // Get FCM token
            val fcmToken = messaging.token.await()
            
            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email,
                role = "user",
                fcmToken = fcmToken
            )
            
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user.toMap())
                .await()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): User? {
        val firebaseUser = currentUser ?: return null
        return try {
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()
            userDoc.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }

    suspend fun updateFCMToken(token: String): Result<Unit> {
        return try {
            val uid = currentUser?.uid ?: throw Exception("No user logged in")
            firestore.collection("users")
                .document(uid)
                .update("fcmToken", token)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
