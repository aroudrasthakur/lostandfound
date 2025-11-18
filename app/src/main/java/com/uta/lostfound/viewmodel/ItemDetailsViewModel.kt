package com.uta.lostfound.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.ItemStatus
import com.uta.lostfound.data.model.Match
import com.uta.lostfound.data.repository.ItemRepository
import com.uta.lostfound.data.repository.MatchRepository
import com.uta.lostfound.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ItemDetailsUiState(
    val isLoading: Boolean = false,
    val item: Item? = null,
    val error: String? = null,
    val deleteSuccess: Boolean = false,
    val notificationSent: Boolean = false,
    val notificationError: String? = null,
    val pendingMatch: Match? = null,
    val matchRequestSent: Boolean = false,
    val matchApproved: Boolean = false,
    val matchError: String? = null
)

class ItemDetailsViewModel : ViewModel() {
    private val itemRepository = ItemRepository()
    private val notificationRepository = NotificationRepository()
    private val matchRepository = MatchRepository()
    
    private val _uiState = MutableStateFlow(ItemDetailsUiState())
    val uiState: StateFlow<ItemDetailsUiState> = _uiState
    
    fun loadItem(itemId: String, userId: String) {
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
            
            // Load pending match if exists
            if (result.isSuccess) {
                loadPendingMatch(itemId, userId)
            }
        }
    }
    
    private fun loadPendingMatch(itemId: String, userId: String) {
        viewModelScope.launch {
            val result = matchRepository.getPendingMatchForUser(itemId, userId)
            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(pendingMatch = result.getOrNull())
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
    
    fun sendItemNotification(recipientUserId: String, senderUserId: String, senderName: String, itemTitle: String, notificationType: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, notificationError = null)
            
            val result = notificationRepository.sendItemNotification(
                recipientUserId = recipientUserId,
                senderUserId = senderUserId,
                senderName = senderName,
                itemTitle = itemTitle,
                notificationType = notificationType
            )
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    notificationSent = true,
                    notificationError = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    notificationError = result.exceptionOrNull()?.message ?: "Failed to send notification"
                )
            }
        }
    }
    
    fun resetNotificationState() {
        _uiState.value = _uiState.value.copy(
            notificationSent = false,
            notificationError = null
        )
    }
    
    fun createMatchRequest(
        itemId: String,
        itemOwnerId: String,
        claimantUserId: String,
        requesterId: String,
        itemTitle: String,
        requesterName: String
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, matchError = null)
            
            val result = matchRepository.createMatchRequest(
                itemId = itemId,
                itemOwnerId = itemOwnerId,
                claimantUserId = claimantUserId,
                requesterId = requesterId,
                itemTitle = itemTitle,
                requesterName = requesterName
            )
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    matchRequestSent = true,
                    matchError = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    matchError = result.exceptionOrNull()?.message ?: "Failed to send match request"
                )
            }
        }
    }
    
    fun approveMatch(matchId: String, userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, matchError = null)
            
            val result = matchRepository.approveMatch(matchId, userId)
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    matchApproved = true,
                    pendingMatch = null,
                    matchError = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    matchError = result.exceptionOrNull()?.message ?: "Failed to approve match"
                )
            }
        }
    }
    
    fun rejectMatch(matchId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, matchError = null)
            
            val result = matchRepository.rejectMatch(matchId)
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    pendingMatch = null,
                    matchError = null
                )
            } else {
                _uiState.value.copy(
                    isLoading = false,
                    matchError = result.exceptionOrNull()?.message ?: "Failed to reject match"
                )
            }
        }
    }
    
    fun resetMatchState() {
        _uiState.value = _uiState.value.copy(
            matchRequestSent = false,
            matchApproved = false,
            matchError = null
        )
    }
}
