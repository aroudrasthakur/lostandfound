package com.uta.lostfound.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.util.Log
import com.uta.lostfound.data.service.LocationAutocompleteService
import com.uta.lostfound.data.service.PlaceSuggestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationAutocompleteField(
    value: String,
    onValueChange: (String) -> Unit,
    onLocationSelected: (PlaceSuggestion) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Location",
    placeholder: String = "Start typing to search...",
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val context = LocalContext.current
    val locationService = remember { LocationAutocompleteService(context) }
    val scope = rememberCoroutineScope()
    
    var suggestions by remember { mutableStateOf<List<PlaceSuggestion>>(emptyList()) }
    var showSuggestions by remember { mutableStateOf(false) }
    var searchJob by remember { mutableStateOf<Job?>(null) }
    var isSearching by remember { mutableStateOf(false) }
    var isValidated by remember { mutableStateOf(false) }
    var selectedSuggestion by remember { mutableStateOf<PlaceSuggestion?>(null) }
    
    // Debounced search
    LaunchedEffect(value) {
        if (value.isBlank()) {
            suggestions = emptyList()
            showSuggestions = false
            isValidated = false
            selectedSuggestion = null
            return@LaunchedEffect
        }
        
        // If user modified the validated selection, reset validation
        if (selectedSuggestion != null && value != selectedSuggestion?.fullAddress) {
            isValidated = false
            selectedSuggestion = null
        }
        
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(300) // Debounce delay
            isSearching = true
            Log.d("LocationAutocomplete", "Searching for: $value")
            
            locationService.searchLocations(value).fold(
                onSuccess = { results ->
                    Log.d("LocationAutocomplete", "Found ${results.size} suggestions")
                    suggestions = results
                    showSuggestions = results.isNotEmpty()
                    isSearching = false
                },
                onFailure = { error ->
                    Log.e("LocationAutocomplete", "Search failed: ${error.message}")
                    suggestions = emptyList()
                    showSuggestions = false
                    isSearching = false
                }
            )
        }
    }
    
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
            leadingIcon = {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = if (isValidated) MaterialTheme.colorScheme.primary 
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                when {
                    isSearching -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    value.isNotEmpty() -> {
                        IconButton(onClick = { 
                            onValueChange("")
                            suggestions = emptyList()
                            showSuggestions = false
                            isValidated = false
                            selectedSuggestion = null
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                }
            },
            supportingText = if (errorMessage != null) {
                { Text(errorMessage) }
            } else if (isValidated) {
                { Text("✓ Valid location", color = MaterialTheme.colorScheme.primary) }
            } else null
        )
        
        // Dropdown suggestions
        if (showSuggestions && suggestions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 250.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn {
                    items(suggestions) { suggestion ->
                        ListItem(
                            headlineContent = { 
                                Text(
                                    text = suggestion.primaryText,
                                    style = MaterialTheme.typography.bodyLarge
                                ) 
                            },
                            supportingContent = { 
                                Text(
                                    text = suggestion.secondaryText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ) 
                            },
                            leadingContent = {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier.clickable {
                                onValueChange(suggestion.fullAddress)
                                onLocationSelected(suggestion)
                                selectedSuggestion = suggestion
                                isValidated = true
                                showSuggestions = false
                                suggestions = emptyList()
                            }
                        )
                        if (suggestion != suggestions.last()) {
                            Divider()
                        }
                    }
                }
            }
        }
    }
}
