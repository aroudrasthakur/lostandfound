package com.uta.lostfound.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user", // "user" or "admin"
    val fcmToken: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "email" to email,
            "role" to role,
            "fcmToken" to fcmToken,
            "createdAt" to createdAt
        )
    }
}
