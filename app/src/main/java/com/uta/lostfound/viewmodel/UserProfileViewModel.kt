package com.uta.lostfound.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uta.lostfound.data.model.User
import com.uta.lostfound.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val isLoading: Boolean = false,
    val user: User? = null,
    val error: String? = null,
    val restrictSuccess: Boolean = false,
    val unrestrictSuccess: Boolean = false,
    val banSuccess: Boolean = false
)

class UserProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    
    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authRepository.getUserById(userId)
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    user = result.getOrNull(),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load user"
                )
            }
        }
    }
    
    fun restrictUser(userId: String) {
        viewModelScope.launch {
            val result = authRepository.restrictUser(userId)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(restrictSuccess = true)
                loadUser(userId) // Reload user data
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to restrict user"
                )
            }
        }
    }
    
    fun unrestrictUser(userId: String) {
        viewModelScope.launch {
            val result = authRepository.unrestrictUser(userId)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(unrestrictSuccess = true)
                loadUser(userId) // Reload user data
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to unrestrict user"
                )
            }
        }
    }
    
    fun banUser(userId: String) {
        viewModelScope.launch {
            val result = authRepository.banUser(userId)
            
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(banSuccess = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Failed to ban user"
                )
            }
        }
    }
}
