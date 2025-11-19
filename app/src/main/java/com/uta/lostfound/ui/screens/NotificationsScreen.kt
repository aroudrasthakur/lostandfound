package com.uta.lostfound.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uta.lostfound.data.model.Notification
import com.uta.lostfound.viewmodel.NotificationsViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsTab(
    userId: String,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToItemDetails: (String) -> Unit = {},
    viewModel: NotificationsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(userId) {
        viewModel.loadNotifications(userId)
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header with refresh button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.loadNotifications(userId) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }
        }
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadNotifications(userId) }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.notifications.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No notifications yet",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.notifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            onMarkAsRead = { viewModel.markAsRead(notification.id) },
                            onDelete = { viewModel.deleteNotification(notification.id, userId) },
                            onNavigateToUserProfile = onNavigateToUserProfile,
                            onNavigateToItemDetails = onNavigateToItemDetails
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    notification: Notification,
    onMarkAsRead: () -> Unit,
    onDelete: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    onNavigateToItemDetails: (String) -> Unit
) {
    var showConfirmDelete by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val maxSwipeDistance = 200f
    
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Background with delete icon
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .matchParentSize()
                .background(MaterialTheme.colorScheme.errorContainer)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (showConfirmDelete) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        onClick = {
                            onDelete()
                        }
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Confirm Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            showConfirmDelete = false
                            scope.launch {
                                offsetX.animateTo(0f, animationSpec = tween(300))
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cancel Delete",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                IconButton(
                    onClick = {
                        showConfirmDelete = true
                    }
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        
        // Foreground card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    translationX = offsetX.value
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (offsetX.value >= maxSwipeDistance * 0.6f) {
                                    // Swipe completed - show confirm/cancel
                                    offsetX.animateTo(maxSwipeDistance, animationSpec = tween(300))
                                    showConfirmDelete = true
                                } else {
                                    // Snap back
                                    offsetX.animateTo(0f, animationSpec = tween(300))
                                    showConfirmDelete = false
                                }
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            scope.launch {
                                val newOffset = (offsetX.value + dragAmount).coerceIn(0f, maxSwipeDistance)
                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                },
            colors = CardDefaults.cardColors(
                containerColor = if (notification.read) {
                    MaterialTheme.colorScheme.surface
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            onClick = {
                if (!notification.read) {
                    onMarkAsRead()
                }
                
                // Navigate based on notification type
                when (notification.type) {
                    "match_request" -> {
                        // Navigate to item details page for match requests
                        if (notification.itemId.isNotBlank()) {
                            onNavigateToItemDetails(notification.itemId)
                        }
                    }
                    else -> {
                        // Navigate to sender's profile for other notifications
                        if (notification.senderUserId.isNotBlank() && 
                            !notification.senderUserId.startsWith("mock_user_")) {
                            onNavigateToUserProfile(notification.senderUserId)
                        }
                    }
                }
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Notification icon
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = if (notification.read) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Notification content
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = getNotificationTitle(notification.type),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.read) FontWeight.Normal else FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = getNotificationMessage(notification),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = formatTimestamp(notification.timestamp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getNotificationTitle(type: String): String {
    return when (type) {
        "have_item" -> "Someone Has Your Lost Item!"
        "claim_item" -> "Someone Wants to Claim Your Found Item!"
        "match_request" -> "Match Request Received!"
        "match_approved" -> "Match Approved!"
        "match_rejected" -> "Match Request Rejected"
        else -> "New Notification"
    }
}

private fun getNotificationMessage(notification: Notification): String {
    return when (notification.type) {
        "have_item" -> "${notification.senderName} says they have your lost item \"${notification.itemTitle}\". Contact them soon!"
        "claim_item" -> "${notification.senderName} wants to claim the item \"${notification.itemTitle}\" that you found. Please get in touch!"
        "match_request" -> "${notification.senderName} wants to match with your item \"${notification.itemTitle}\". Tap to review and approve!"
        "match_approved" -> "Great news! Your match request for \"${notification.itemTitle}\" has been approved. The item is now classified as matched!"
        "match_rejected" -> "Sorry, your match request for \"${notification.itemTitle}\" was not approved by ${notification.senderName}."
        else -> "${notification.senderName} sent you a notification about \"${notification.itemTitle}\""
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000} minutes ago"
        diff < 86400000 -> "${diff / 3600000} hours ago"
        diff < 604800000 -> "${diff / 86400000} days ago"
        else -> {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dateFormat.format(Date(timestamp))
        }
    }
}
