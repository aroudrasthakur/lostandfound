package com.uta.lostfound.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.Metrics
import com.uta.lostfound.data.repository.ItemRepository
import com.uta.lostfound.data.repository.MetricsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val metrics: Metrics? = null,
    val lostItems: List<Item> = emptyList(),
    val foundItems: List<Item> = emptyList(),
    val error: String? = null
)

class AdminViewModel : ViewModel() {
    private val metricsRepository = MetricsRepository()
    private val itemRepository = ItemRepository()
    
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState

    fun loadMetrics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = metricsRepository.getMonthlyMetrics()
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    metrics = result.getOrNull(),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load metrics"
                )
            }
        }
    }
    
    fun loadLostItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = itemRepository.getAllLostItems()
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    lostItems = result.getOrNull() ?: emptyList(),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load lost items"
                )
            }
        }
    }
    
    fun loadFoundItems() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = itemRepository.getAllFoundItems()
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    foundItems = result.getOrNull() ?: emptyList(),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load found items"
                )
            }
        }
    }

    fun deleteLostItem(itemId: String) {
        viewModelScope.launch {
            val result = itemRepository.deleteLostItem(itemId)
            
            if (result.isSuccess) {
                loadLostItems()
                loadMetrics()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to delete item"
                )
            }
        }
    }
    
    fun deleteFoundItem(itemId: String) {
        viewModelScope.launch {
            val result = itemRepository.deleteFoundItem(itemId)
            
            if (result.isSuccess) {
                loadFoundItems()
                loadMetrics()
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to delete item"
                )
            }
        }
    }
}
