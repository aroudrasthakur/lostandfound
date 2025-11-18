package com.uta.lostfound.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.uta.lostfound.viewmodel.ItemDetailsViewModel
import com.uta.lostfound.viewmodel.LoginViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsScreen(
    itemId: String,
    onNavigateBack: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    loginViewModel: LoginViewModel = viewModel(),
    viewModel: ItemDetailsViewModel = viewModel()
) {
    val context = LocalContext.current
    val loginUiState by loginViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showNotificationSuccessDialog by remember { mutableStateOf(false) }
    var showMatchRequestDialog by remember { mutableStateOf(false) }
    var showMatchSuccessDialog by remember { mutableStateOf(false) }
    
    // Load item details
    LaunchedEffect(itemId, loginUiState.currentUser?.uid) {
        val userId = loginUiState.currentUser?.uid ?: ""
        viewModel.loadItem(itemId, userId)
    }
    
    // Navigate back on successful delete
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onNavigateBack()
        }
    }
    
    // Show success dialog when notification is sent
    LaunchedEffect(uiState.notificationSent) {
        if (uiState.notificationSent) {
            showNotificationSuccessDialog = true
        }
    }
    
    // Show dialog when match request is sent
    LaunchedEffect(uiState.matchRequestSent) {
        if (uiState.matchRequestSent) {
            showMatchRequestDialog = true
        }
    }
    
    // Show dialog when match is approved
    LaunchedEffect(uiState.matchApproved) {
        if (uiState.matchApproved) {
            showMatchSuccessDialog = true
        }
    }
    
    val item = uiState.item
    val currentUser = loginUiState.currentUser
    val isOwner = currentUser?.uid == item?.userId
    val isAdmin = currentUser?.role == "admin"
    val canDelete = isOwner || isAdmin
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Item Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (canDelete && item != null) {
                        // Edit button (admin only)
                        if (isAdmin && !isOwner) {
                            IconButton(onClick = { /* TODO: Implement edit functionality */ }) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = "Error: ${uiState.error}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                item != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Image with placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.imageUrl.isNotBlank()) {
                                AsyncImage(
                                    model = item.imageUrl,
                                    contentDescription = item.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = "No image",
                                        modifier = Modifier.size(80.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "No image available",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Status Badge
                            Row(
                                modifier = Modifier.padding(bottom = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AssistChip(
                                    onClick = {},
                                    label = { 
                                        Text(
                                            text = item.status.name,
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                )
                                
                                // Show matched badge if item is matched
                                if (item.isMatched) {
                                    AssistChip(
                                        onClick = {},
                                        label = { 
                                            Text(
                                                text = "✓ MATCHED",
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }
                            
                            // Title
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            // Category
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            // Description
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Location
                            Text(
                                text = "Location",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = item.location,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Date
                            Text(
                                text = "Date ${if (item.status.name == "LOST") "Lost" else "Found"}",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                            Text(
                                text = dateFormat.format(Date(item.date)),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            
                            // Posted by (clickable)
                            Text(
                                text = "Posted By",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = item.userName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .padding(bottom = 24.dp)
                                    .clickable(
                                        onClick = { onNavigateToUserProfile(item.userId) },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    )
                            )
                            
                            // Show different UI based on item state
                            when {
                                // If item is already matched, show matched status
                                item.isMatched -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "✓",
                                                style = MaterialTheme.typography.headlineMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "This item has been matched!",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }
                                }
                                
                                // If there's a pending match for the current user to approve
                                uiState.pendingMatch != null && currentUser != null -> {
                                    val pendingMatch = uiState.pendingMatch!!
                                    val needsApproval = (pendingMatch.itemOwnerId == currentUser.uid && !pendingMatch.itemOwnerApproved) ||
                                                       (pendingMatch.claimantUserId == currentUser.uid && !pendingMatch.claimantApproved)
                                    
                                    if (needsApproval) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                                            )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp)
                                            ) {
                                                Text(
                                                    text = "Match Request Pending",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                                Text(
                                                    text = "Someone is interested in this item. Review and approve if this is a valid match.",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                                    modifier = Modifier.padding(bottom = 16.dp)
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.rejectMatch(pendingMatch.id)
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        enabled = !uiState.isLoading
                                                    ) {
                                                        Text("Reject")
                                                    }
                                                    Button(
                                                        onClick = {
                                                            viewModel.approveMatch(
                                                                matchId = pendingMatch.id,
                                                                userId = currentUser.uid
                                                            )
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        enabled = !uiState.isLoading
                                                    ) {
                                                        if (uiState.isLoading) {
                                                            CircularProgressIndicator(
                                                                modifier = Modifier.size(20.dp),
                                                                color = MaterialTheme.colorScheme.onPrimary
                                                            )
                                                        } else {
                                                            Text("Approve Match")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        // Match request sent, waiting for other party
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Match request sent. Waiting for approval...",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // Show match request button if not owner and not matched
                                !isOwner && currentUser != null -> {
                                    Column {
                                        Button(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = Uri.parse("mailto:")
                                                    putExtra(Intent.EXTRA_SUBJECT, "Regarding: ${item.title}")
                                                    putExtra(Intent.EXTRA_TEXT, 
                                                        "Hi,\n\nI saw your ${item.status.name.lowercase()} item posting for \"${item.title}\".\n\n")
                                                }
                                                context.startActivity(Intent.createChooser(intent, "Send Email"))
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(
                                                Icons.Default.Email,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Contact Owner")
                                        }
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Match request button
                                        Button(
                                            onClick = {
                                                viewModel.createMatchRequest(
                                                    itemId = item.id,
                                                    itemOwnerId = item.userId,
                                                    claimantUserId = currentUser.uid,
                                                    requesterId = currentUser.uid,
                                                    requesterName = currentUser.name,
                                                    itemTitle = item.title
                                                )
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            enabled = !uiState.isLoading,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.secondary
                                            )
                                        ) {
                                            if (uiState.isLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(20.dp),
                                                    color = MaterialTheme.colorScheme.onSecondary
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.Info,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(if (item.status.name == "LOST") "I Have This Item" else "This Is My Item")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Delete Confirmation Dialog
    if (showDeleteDialog && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Item") },
            text = { Text("Are you sure you want to delete \"${item.title}\"? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteItem(itemId, item.status)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Notification Success Dialog
    if (showNotificationSuccessDialog && item != null) {
        AlertDialog(
            onDismissRequest = {
                showNotificationSuccessDialog = false
                viewModel.resetNotificationState()
            },
            title = { Text("Notification Sent") },
            text = { 
                Text(
                    if (item.status.name == "LOST") {
                        "The owner has been notified that you have their item. They will contact you soon!"
                    } else {
                        "The finder has been notified of your claim. They will contact you soon!"
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationSuccessDialog = false
                        viewModel.resetNotificationState()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Match Request Sent Dialog
    if (showMatchRequestDialog && item != null) {
        AlertDialog(
            onDismissRequest = {
                showMatchRequestDialog = false
                viewModel.resetMatchState()
            },
            title = { Text("Match Request Sent") },
            text = { 
                Text(
                    "Your match request has been sent to the item owner. You'll be notified when they respond."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMatchRequestDialog = false
                        viewModel.resetMatchState()
                        onNavigateBack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Match Success Dialog
    if (showMatchSuccessDialog && item != null) {
        AlertDialog(
            onDismissRequest = {
                showMatchSuccessDialog = false
                viewModel.resetMatchState()
            },
            title = { Text("Match Confirmed!") },
            text = { 
                Text(
                    "Congratulations! This item has been successfully matched. Both parties can now coordinate the exchange."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMatchSuccessDialog = false
                        viewModel.resetMatchState()
                        onNavigateBack()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
    
    // Notification Error Snackbar
    uiState.notificationError?.let { error ->
        LaunchedEffect(error) {
            // You could show a Snackbar here if needed
            viewModel.resetNotificationState()
        }
    }
    
    // Match Error Dialog
    uiState.matchError?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.resetMatchState() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                Button(onClick = { viewModel.resetMatchState() }) {
                    Text("OK")
                }
            }
        )
    }
}
