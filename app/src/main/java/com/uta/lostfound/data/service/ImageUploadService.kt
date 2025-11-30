package com.uta.lostfound.data.service

import android.content.Context
import android.net.Uri
import android.util.Base64
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

/**
 * Service for uploading images to ImgBB (free image hosting)
 * 
 * To get your free API key:
 * 1. Go to https://imgbb.com/
 * 2. Create a free account
 * 3. Go to https://api.imgbb.com/
 * 4. Copy your API key
 */
class ImageUploadService(private val context: Context) {
    
    companion object {
        // TODO: Replace with your ImgBB API key from https://api.imgbb.com/
        private const val IMGBB_API_KEY = "API_KEY"
        private const val IMGBB_UPLOAD_URL = "https://api.imgbb.com/1/upload"
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    /**
     * Upload an image to ImgBB and return the URL
     * @param imageUri The URI of the image to upload
     * @return The URL of the uploaded image, or null if upload failed
     */
    suspend fun uploadImage(imageUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            // Convert image to Base64
            val base64Image = convertImageToBase64(imageUri) ?: return@withContext null
            
            // Build the request
            val requestBody = FormBody.Builder()
                .add("key", IMGBB_API_KEY)
                .add("image", base64Image)
                .build()
            
            val request = Request.Builder()
                .url(IMGBB_UPLOAD_URL)
                .post(requestBody)
                .build()
            
            // Execute the request
            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                val imgbbResponse = gson.fromJson(responseBody, ImgBBResponse::class.java)
                
                if (imgbbResponse.success) {
                    return@withContext imgbbResponse.data?.url
                }
            }
            
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Convert an image URI to Base64 string
     */
    private fun convertImageToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            
            inputStream?.use { input ->
                val buffer = ByteArray(4096)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead)
                }
            }
            
            val imageBytes = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(imageBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// ImgBB API Response classes
data class ImgBBResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ImgBBData?
)

data class ImgBBData(
    @SerializedName("url") val url: String,
    @SerializedName("display_url") val displayUrl: String,
    @SerializedName("delete_url") val deleteUrl: String?
)
