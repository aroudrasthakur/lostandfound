package com.uta.lostfound.utils

import android.content.Context
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.uta.lostfound.R
import kotlinx.coroutines.tasks.await

object ApiKeyValidator {
    private const val TAG = "ApiKeyValidator"
    
    /**
     * Test if the Google Places API key is valid and working
     */
    suspend fun validateGooglePlacesApiKey(context: Context): ValidationResult {
        return try {
            val apiKey = context.getString(R.string.google_maps_key)
            
            // Check if it's the placeholder
            if (apiKey.contains("YOUR_GOOGLE_PLACES_API_KEY_HERE") || apiKey.isBlank()) {
                return ValidationResult.NotConfigured("API key not configured in strings.xml")
            }
            
            Log.d(TAG, "Testing API key: ${apiKey.take(20)}...")
            
            // Initialize Places API
            if (!Places.isInitialized()) {
                Places.initialize(context.applicationContext, apiKey)
            }
            
            val placesClient: PlacesClient = Places.createClient(context)
            val token = AutocompleteSessionToken.newInstance()
            
            // Make a simple test request
            val request = FindAutocompletePredictionsRequest.builder()
                .setSessionToken(token)
                .setQuery("library")
                .build()
            
            val response = placesClient.findAutocompletePredictions(request).await()
            
            if (response.autocompletePredictions.isNotEmpty()) {
                Log.d(TAG, "✓ API key is VALID - Got ${response.autocompletePredictions.size} results")
                return ValidationResult.Valid(
                    "API key is working! Found ${response.autocompletePredictions.size} places"
                )
            } else {
                Log.w(TAG, "API key might be valid but returned no results")
                return ValidationResult.Valid("API key accepted but returned no results")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "✗ API key validation FAILED: ${e.message}", e)
            
            val errorMessage = when {
                e.message?.contains("API_KEY_INVALID") == true -> 
                    "Invalid API key"
                e.message?.contains("PERMISSION_DENIED") == true -> 
                    "Permission denied - check API restrictions"
                e.message?.contains("BILLING_NOT_ENABLED") == true -> 
                    "Billing not enabled on Google Cloud project"
                e.message?.contains("REQUEST_DENIED") == true -> 
                    "Request denied - check if Places API is enabled"
                else -> 
                    "Error: ${e.message}"
            }
            
            return ValidationResult.Invalid(errorMessage)
        }
    }
    
    sealed class ValidationResult {
        data class Valid(val msg: String) : ValidationResult()
        data class Invalid(val msg: String) : ValidationResult()
        data class NotConfigured(val msg: String) : ValidationResult()
        
        fun isValid(): Boolean = this is Valid
        
        fun getResultMessage(): String = when (this) {
            is Valid -> msg
            is Invalid -> msg
            is NotConfigured -> msg
        }
    }
}
