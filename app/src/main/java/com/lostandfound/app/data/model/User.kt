package com.lostandfound.app.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val phoneNumber: String = "",
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "uid" to uid,
            "email" to email,
            "displayName" to displayName,
            "photoUrl" to photoUrl,
            "phoneNumber" to phoneNumber,
            "createdAt" to createdAt
        )
    }
}
