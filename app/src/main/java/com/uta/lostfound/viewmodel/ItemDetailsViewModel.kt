package com.uta.lostfound.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.ItemStatus
import com.uta.lostfound.data.repository.ItemRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ItemDetailsUiState(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val error: String? = null,
    val deleteSuccess: Boolean = false
)

class ItemDetailsViewModel : ViewModel() {
    private val itemRepository = ItemRepository()
    
    private val _uiState = MutableStateFlow(ItemDetailsUiState())
    val uiState: StateFlow<ItemDetailsUiState> = _uiState
    
    fun loadItem(itemId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Try both collections
            var result = itemRepository.getLostItem(itemId)
            if (result.isFailure) {
                result = itemRepository.getFoundItem(itemId)
            }
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    item = result.getOrNull(),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load item"
                )
            }
        }
    }

    fun deleteItem(itemId: String, status: ItemStatus) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = if (status == ItemStatus.LOST) {
                itemRepository.deleteLostItem(itemId)
            } else {
                itemRepository.deleteFoundItem(itemId)
            }
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    deleteSuccess = true,
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to delete item"
                )
            }
        }
    }
}
