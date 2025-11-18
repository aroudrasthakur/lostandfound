package com.uta.lostfound.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uta.lostfound.data.model.User
import com.uta.lostfound.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.delay

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null
)

class LoginViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    init {
        // Check if user is authenticated without blocking
        val firebaseUser = authRepository.currentUser
        if (firebaseUser != null) {
            // User is authenticated, fetch full user data in background
            checkAuthStatus()
        } else {
            // No user logged in, set state immediately
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            var retries = 0
            val maxRetries = 3
            
            while (retries < maxRetries) {
                try {
                    _uiState.value = _uiState.value.copy(isLoading = true)
                    
                    // Add 10 second timeout for first attempt, 5 seconds for retries
                    val timeoutMs = if (retries == 0) 10000L else 5000L
                    val user = withTimeout(timeoutMs) {
                        authRepository.getCurrentUser()
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoggedIn = user != null,
                        currentUser = user
                    )
                    return@launch // Success, exit
                    
                } catch (e: Exception) {
                    retries++
                    if (retries >= maxRetries) {
                        // All retries failed - clear auth state and show login screen
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isLoggedIn = false,
                            error = null // Don't show error on initial load
                        )
                    } else {
                        // Wait before retry
                        delay(1000L)
                    }
                }
            }
        }
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authRepository.signInWithEmail(email, password)
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUser = result.getOrNull(),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Sign in failed"
                )
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = authRepository.signUpWithEmail(email, password, name)
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUser = result.getOrNull(),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Sign up failed"
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = LoginUiState()
    }
}
