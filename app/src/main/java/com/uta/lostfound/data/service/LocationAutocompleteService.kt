package com.uta.lostfound.data.service

import android.content.Context
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import com.uta.lostfound.R

data class PlaceSuggestion(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String,
    val fullAddress: String
)

class LocationAutocompleteService(context: Context) {
    private val placesClient: PlacesClient
    private val token: AutocompleteSessionToken = AutocompleteSessionToken.newInstance()
    private var isUsingMockData = false
    
    companion object {
        private const val TAG = "LocationAutocomplete"
    }
    
    // UTA campus location (approximate)
    private val utaBounds = RectangularBounds.newInstance(
        LatLng(32.7250, -97.1200), // Southwest corner
        LatLng(32.7400, -97.1050)  // Northeast corner
    )
    
    init {
        // Initialize Places API
        if (!Places.isInitialized()) {
            val apiKey = context.getString(R.string.google_maps_key)
            Log.d(TAG, "Initializing Places API with key: ${apiKey.take(20)}...")
            try {
                Places.initialize(context.applicationContext, apiKey)
                Log.d(TAG, "Places API initialized successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize Places API: ${e.message}")
                isUsingMockData = true
            }
        }
        placesClient = Places.createClient(context)
    }
    
    /**
     * Search for location suggestions based on user query
     */
    suspend fun searchLocations(query: String): Result<List<PlaceSuggestion>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }
        
        // If already using mock data, skip API call
        if (isUsingMockData) {
            Log.d(TAG, "Using mock data for query: $query")
            return Result.success(getMockUTALocations(query))
        }
        
        return try {
            Log.d(TAG, "Calling Google Places API for: $query")
            
            val request = FindAutocompletePredictionsRequest.builder()
                .setLocationBias(utaBounds)
                .setSessionToken(token)
                .setQuery(query)
                .build()
            
            val response = placesClient.findAutocompletePredictions(request).await()
            
            val suggestions = response.autocompletePredictions
                .take(5) // Limit to 5 best matches
                .map { prediction ->
                    PlaceSuggestion(
                        placeId = prediction.placeId,
                        primaryText = prediction.getPrimaryText(null).toString(),
                        secondaryText = prediction.getSecondaryText(null).toString(),
                        fullAddress = prediction.getFullText(null).toString()
                    )
                }
            
            Log.d(TAG, "✓ Google Places API returned ${suggestions.size} results")
            Result.success(suggestions)
        } catch (e: Exception) {
            Log.e(TAG, "✗ Google Places API failed: ${e.message}")
            
            // Check specific error types
            when {
                e.message?.contains("API_KEY_INVALID") == true -> {
                    Log.e(TAG, "Invalid API key - switching to mock data")
                    isUsingMockData = true
                }
                e.message?.contains("PERMISSION_DENIED") == true -> {
                    Log.e(TAG, "Permission denied - check API restrictions")
                }
                e.message?.contains("BILLING_NOT_ENABLED") == true -> {
                    Log.e(TAG, "Billing not enabled on Google Cloud project")
                }
                e.message?.contains("REQUEST_DENIED") == true -> {
                    Log.e(TAG, "Request denied - check if Places API is enabled")
                }
            }
            
            // Fall back to mock UTA campus locations for testing
            Log.d(TAG, "Falling back to mock data")
            val mockSuggestions = getMockUTALocations(query)
            if (mockSuggestions.isNotEmpty()) {
                Result.success(mockSuggestions)
            } else {
                Result.failure(e)
            }
        }
    }
    
    /**
     * Provide mock UTA campus locations as fallback
     */
    private fun getMockUTALocations(query: String): List<PlaceSuggestion> {
        val utaLocations = listOf(
            PlaceSuggestion("1", "Central Library", "University of Texas at Arlington", "Central Library, UTA Campus, Arlington, TX"),
            PlaceSuggestion("2", "Engineering Research Building", "500 UTA Blvd", "Engineering Research Building, 500 UTA Blvd, Arlington, TX 76019"),
            PlaceSuggestion("3", "University Center", "300 W 1st St", "University Center, 300 W 1st St, Arlington, TX 76019"),
            PlaceSuggestion("4", "Science Hall", "UTA Campus", "Science Hall, UTA Campus, Arlington, TX 76019"),
            PlaceSuggestion("5", "College Park Center", "601 S Center St", "College Park Center, 601 S Center St, Arlington, TX 76013"),
            PlaceSuggestion("6", "Nedderman Hall", "416 Yates St", "Nedderman Hall, 416 Yates St, Arlington, TX 76019"),
            PlaceSuggestion("7", "Maverick Activities Center", "UTA Campus", "Maverick Activities Center, UTA Campus, Arlington, TX"),
            PlaceSuggestion("8", "Life Science Building", "UTA Campus", "Life Science Building, UTA Campus, Arlington, TX 76019"),
            PlaceSuggestion("9", "Student Recreation Center", "UTA Campus", "Student Recreation Center, UTA Campus, Arlington, TX"),
            PlaceSuggestion("10", "Fine Arts Building", "502 S Cooper St", "Fine Arts Building, 502 S Cooper St, Arlington, TX 76019")
        )
        
        return utaLocations.filter { 
            it.primaryText.contains(query, ignoreCase = true) || 
            it.secondaryText.contains(query, ignoreCase = true) 
        }.take(5)
    }
    
    /**
     * Validate if a place ID exists and get full details
     */
    suspend fun validatePlace(placeId: String): Result<Place> {
        return try {
            val placeFields = listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG
            )
            
            val request = FetchPlaceRequest.builder(placeId, placeFields).build()
            val response = placesClient.fetchPlace(request).await()
            
            Result.success(response.place)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Validate if a text query matches any known location
     */
    suspend fun isValidLocation(query: String): Boolean {
        if (query.isBlank()) return false
        
        return try {
            val suggestions = searchLocations(query).getOrNull()
            !suggestions.isNullOrEmpty()
        } catch (e: Exception) {
            false
        }
    }
}
