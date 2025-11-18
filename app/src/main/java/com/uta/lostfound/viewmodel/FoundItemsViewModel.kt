package com.uta.lostfound.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class FoundItemsUiState(
    val isLoading: Boolean = false,
    val items: List<Item> = emptyList(),
    val error: String? = null
)

class FoundItemsViewModel : ViewModel() {
    private val itemRepository = ItemRepository()
    
    private val _uiState = MutableStateFlow(FoundItemsUiState())
    val uiState: StateFlow<FoundItemsUiState> = _uiState

    // Don't load items in init - let the UI trigger the load when ready
    init {
        // Initialize with empty state, ready to load
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    fun loadFoundItems() {
        // Prevent concurrent loads
        if (_uiState.value.isLoading) return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Load both lost and found items
            val lostResult = itemRepository.getLostItems()
            val foundResult = itemRepository.getFoundItems()
            
            val allItems = mutableListOf<Item>()
            
            if (lostResult.isSuccess) {
                allItems.addAll(lostResult.getOrNull() ?: emptyList())
            }
            
            if (foundResult.isSuccess) {
                allItems.addAll(foundResult.getOrNull() ?: emptyList())
            }
            
            _uiState.value = if (lostResult.isSuccess || foundResult.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    items = allItems,
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = foundResult.exceptionOrNull()?.message ?: "Failed to load items"
                )
            }
        }
    }
}
