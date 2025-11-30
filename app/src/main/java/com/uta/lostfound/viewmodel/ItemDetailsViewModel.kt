package com.uta.lostfound.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.ItemStatus
import com.uta.lostfound.data.model.Match
import com.uta.lostfound.data.model.MatchStatus
import com.uta.lostfound.data.repository.ItemRepository
import com.uta.lostfound.data.repository.MatchRepository
import com.uta.lostfound.data.repository.MetricsRepository
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
    private val metricsRepository = MetricsRepository()
    
    private val _uiState = MutableStateFlow(ItemDetailsUiState())
    val uiState: StateFlow<ItemDetailsUiState> = _uiState
    
    fun loadItem(itemId: String, userId: String) {
        viewModelScope.launch {
            android.util.Log.d("ItemDetailsViewModel", "loadItem called - itemId: $itemId, userId: $userId")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Try both collections
            var result = itemRepository.getLostItem(itemId)
            if (result.isFailure) {
                result = itemRepository.getFoundItem(itemId)
            }
            
            // Preserve matchApproved state when reloading
            val currentMatchApproved = _uiState.value.matchApproved
            android.util.Log.d("ItemDetailsViewModel", "Preserving matchApproved state: $currentMatchApproved")
            
            _uiState.value = if (result.isSuccess) {
                val item = result.getOrNull()
                android.util.Log.d("ItemDetailsViewModel", "Item loaded - isMatched: ${item?.isMatched}, matchId: ${item?.matchId}")
                _uiState.value.copy(
                    isLoading = false,
                    item = item,
                    error = null,
                    matchApproved = currentMatchApproved
                )
            } else {
                android.util.Log.e("ItemDetailsViewModel", "Failed to load item: ${result.exceptionOrNull()?.message}")
                _uiState.value.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to load item",
                    matchApproved = currentMatchApproved
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
            android.util.Log.d("ItemDetailsViewModel", "loadPendingMatch called for itemId: $itemId")
            val result = matchRepository.getPendingMatchForUser(itemId, userId)
            if (result.isSuccess) {
                val match = result.getOrNull()
                android.util.Log.d("ItemDetailsViewModel", "Pending match result - match: $match, status: ${match?.status}")
                
                // Preserve matchApproved state
                val currentMatchApproved = _uiState.value.matchApproved
                
                _uiState.value = _uiState.value.copy(
                    pendingMatch = match,
                    matchRequestSent = match?.requesterId == userId && match.status == MatchStatus.PENDING,
                    matchApproved = currentMatchApproved
                )
                
                android.util.Log.d("ItemDetailsViewModel", "State after loadPendingMatch - matchApproved: ${_uiState.value.matchApproved}, pendingMatch: ${_uiState.value.pendingMatch}")
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
            
            if (result.isSuccess) {
                // Update metrics after successful item deletion
                metricsRepository.updateMetrics()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    deleteSuccess = true,
                    error = null
                )
            } else {
                _uiState.value = _uiState.value.copy(
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
    
    fun approveMatch(matchId: String, userId: String, approverName: String) {
        viewModelScope.launch {
            android.util.Log.d("ItemDetailsViewModel", "approveMatch called - matchId: $matchId, userId: $userId")
            _uiState.value = _uiState.value.copy(isLoading = true, matchError = null)
            
            val result = matchRepository.approveMatch(matchId, userId, approverName)
            
            android.util.Log.d("ItemDetailsViewModel", "approveMatch result - success: ${result.isSuccess}")
            
            if (result.isSuccess) {
                // Update metrics after successful match approval
                metricsRepository.updateMetrics()
                
                // Reload the item to get updated status
                val itemId = _uiState.value.item?.id ?: ""
                android.util.Log.d("ItemDetailsViewModel", "Reloading item: $itemId")
                
                if (itemId.isNotEmpty()) {
                    loadItem(itemId, userId)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    matchApproved = true,
                    pendingMatch = null,
                    matchError = null
                )
                
                android.util.Log.d("ItemDetailsViewModel", "State updated - matchApproved: ${_uiState.value.matchApproved}")
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to approve match"
                android.util.Log.e("ItemDetailsViewModel", "Error approving match: $error")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    matchError = error
                )
            }
        }
    }
    
    fun rejectMatch(matchId: String, rejecterUserId: String, rejecterName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, matchError = null)
            
            val result = matchRepository.rejectMatch(matchId, rejecterUserId, rejecterName)
            
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(
                    isLoading = false,
                    pendingMatch = null,
                    matchRequestSent = false,
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
