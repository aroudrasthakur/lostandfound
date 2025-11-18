package com.uta.lostfound.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.ItemCategory
import com.uta.lostfound.data.model.User
import com.uta.lostfound.data.repository.SearchRepository
import com.uta.lostfound.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SearchUiState(
    val isLoading: Boolean = false,
    val searchResults: List<Item> = emptyList(),
    val userResults: List<User> = emptyList(),
    val error: String? = null,
    val query: String = "",
    val searchMode: String = "Items" // "Items" or "Users"
)

class SearchViewModel : ViewModel() {
    private val searchRepository = SearchRepository()
    private val userRepository = UserRepository()
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState
    
    fun setSearchMode(mode: String) {
        _uiState.value = _uiState.value.copy(
            searchMode = mode,
            searchResults = emptyList(),
            userResults = emptyList()
        )
        if (_uiState.value.query.isNotBlank()) {
            if (mode == "Users") {
                searchUsers(_uiState.value.query)
            } else {
                searchAll(_uiState.value.query)
            }
        }
    }
    
    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        if (query.isNotBlank()) {
            if (_uiState.value.searchMode == "Users") {
                searchUsers(query)
            } else {
                searchAll(query)
            }
        } else {
            _uiState.value = _uiState.value.copy(
                searchResults = emptyList(),
                userResults = emptyList()
            )
        }
    }
    
    fun searchUsers(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = userRepository.searchUsers(query)
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    userResults = result.getOrNull() ?: emptyList(),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "User search failed"
                )
            }
        }
    }
    
    fun searchAll(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = searchRepository.searchItems(query)
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    searchResults = result.getOrNull() ?: emptyList(),
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Search failed"
                )
            }
        }
    }
    
    fun searchLostItems(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = searchRepository.searchItems(query)
            
            _uiState.value = if (result.isSuccess) {
                val lostItems = result.getOrNull()?.filter { it.status.name == "LOST" } ?: emptyList()
                _uiState.value.copy(
                    isLoading = false,
                    searchResults = lostItems,
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Search failed"
                )
            }
        }
    }
    
    fun searchFoundItems(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val result = searchRepository.searchItems(query)
            
            _uiState.value = if (result.isSuccess) {
                val foundItems = result.getOrNull()?.filter { it.status.name == "FOUND" } ?: emptyList()
                _uiState.value.copy(
                    isLoading = false,
                    searchResults = foundItems,
                    error = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Search failed"
                )
            }
        }
    }
    
    fun searchByCategory(category: ItemCategory) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Search both collections and combine results
            val lostResult = searchRepository.searchByCategory(category.name, com.uta.lostfound.data.model.ItemStatus.LOST)
            val foundResult = searchRepository.searchByCategory(category.name, com.uta.lostfound.data.model.ItemStatus.FOUND)
            
            val allResults = (lostResult.getOrNull() ?: emptyList()) + (foundResult.getOrNull() ?: emptyList())
            
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                searchResults = allResults,
                error = null
            )
        }
    }
}
