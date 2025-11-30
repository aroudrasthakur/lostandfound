package com.uta.lostfound.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.ItemCategory
import com.uta.lostfound.data.model.ItemStatus
import com.uta.lostfound.data.repository.ItemRepository
import com.uta.lostfound.data.repository.MetricsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

data class ReportUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val category: ItemCategory? = null,
    val status: ItemStatus = ItemStatus.LOST,
    val date: Timestamp = Timestamp(Date())
)

class ReportViewModel(application: Application) : AndroidViewModel(application) {
    private val itemRepository = ItemRepository(application.applicationContext)
    private val metricsRepository = MetricsRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState
    
    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }
    
    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }
    
    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }
    
    fun updateCategory(category: ItemCategory) {
        _uiState.value = _uiState.value.copy(category = category)
    }
    
    fun updateStatus(status: ItemStatus) {
        _uiState.value = _uiState.value.copy(status = status)
    }
    
    fun updateDate(date: Timestamp) {
        _uiState.value = _uiState.value.copy(date = date)
    }

    fun submitReport(imageUri: Uri?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Get current user info
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Please login to report items"
                )
                return@launch
            }
            
            val state = _uiState.value
            val result = if (state.status == ItemStatus.LOST) {
                itemRepository.reportLostItem(
                    Item(
                        id = "",
                        userId = currentUser.uid,
                        userName = currentUser.displayName ?: currentUser.email ?: "Unknown User",
                        title = state.title,
                        description = state.description,
                        category = state.category?.name ?: ItemCategory.OTHER.name,
                        location = state.location,
                        date = state.date.seconds * 1000,
                        imageUrl = "",
                        status = state.status,
                        isActive = true
                    ),
                    imageUri
                )
            } else {
                itemRepository.reportFoundItem(
                    Item(
                        id = "",
                        userId = currentUser.uid,
                        userName = currentUser.displayName ?: currentUser.email ?: "Unknown User",
                        title = state.title,
                        description = state.description,
                        category = state.category?.name ?: ItemCategory.OTHER.name,
                        location = state.location,
                        date = state.date.seconds * 1000,
                        imageUrl = "",
                        status = state.status,
                        isActive = true
                    ),
                    imageUri
                )
            }
            
            if (result.isSuccess) {
                // Update metrics after successful item report
                metricsRepository.updateMetrics()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to report item"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = ReportUiState()
    }
}
