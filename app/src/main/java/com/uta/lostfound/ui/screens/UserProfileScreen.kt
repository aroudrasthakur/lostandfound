package com.uta.lostfound.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uta.lostfound.viewmodel.LoginViewModel
import com.uta.lostfound.viewmodel.UserProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    userId: String,
    onNavigateBack: () -> Unit,
    loginViewModel: LoginViewModel = viewModel(),
    viewModel: UserProfileViewModel = viewModel()
) {
    val loginUiState by loginViewModel.uiState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    
    val currentUser = loginUiState.currentUser
    val isAdmin = currentUser?.role == "admin"
    val isOwnProfile = currentUser?.uid == userId
    
    var showRestrictDialog by remember { mutableStateOf(false) }
    var showUnrestrictDialog by remember { mutableStateOf(false) }
    var showBanDialog by remember { mutableStateOf(false) }
    
    // Load user profile
    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }
    
    // Navigate back on successful ban
    LaunchedEffect(uiState.banSuccess) {
        if (uiState.banSuccess) {
            onNavigateBack()
        }
    }
    
    val user = uiState.user
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { viewModel.loadUser(userId) }) {
                            Text("Retry")
                        }
                    }
                }
                user != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Profile Icon
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(100.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // User Name
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Email
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Role Badge
                        if (user.role == "admin") {
                            AssistChip(
                                onClick = {},
                                label = { Text("Admin") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Settings,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        } else {
                            AssistChip(
                                onClick = {},
                                label = { Text("User") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                        
                        // Restricted Status
                        if (user.isRestricted) {
                            Spacer(modifier = Modifier.height(8.dp))
                            AssistChip(
                                onClick = {},
                                label = { Text("Restricted") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Warning,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    labelColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Account Details Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Account Details",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                // Member Since
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Member Since",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                    Text(
                                        text = dateFormat.format(Date(user.createdAt)),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // User ID
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "User ID",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = user.uid.take(8) + "...",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Admin Actions
                        if (isAdmin && !isOwnProfile && user.role != "admin") {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Admin Actions",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )
                                    
                                    if (!user.isRestricted) {
                                        OutlinedButton(
                                            onClick = { showRestrictDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.Warning,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Restrict User from Posting")
                                        }
                                    } else {
                                        OutlinedButton(
                                            onClick = { showUnrestrictDialog = true },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Remove Restriction")
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    Button(
                                        onClick = { showBanDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.error
                                        )
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Ban User Permanently")
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "⚠️ Banning will delete all user data including posted items",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Restrict Dialog
    if (showRestrictDialog && user != null) {
        AlertDialog(
            onDismissRequest = { showRestrictDialog = false },
            title = { Text("Restrict User") },
            text = { Text("Are you sure you want to restrict ${user.name} from posting new items? They will still be able to view items but cannot create new posts.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restrictUser(userId)
                        showRestrictDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Restrict")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestrictDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Unrestrict Dialog
    if (showUnrestrictDialog && user != null) {
        AlertDialog(
            onDismissRequest = { showUnrestrictDialog = false },
            title = { Text("Remove Restriction") },
            text = { Text("Remove posting restriction from ${user.name}? They will be able to create new posts again.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.unrestrictUser(userId)
                        showUnrestrictDialog = false
                    }
                ) {
                    Text("Remove Restriction")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnrestrictDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Ban Dialog
    if (showBanDialog && user != null) {
        AlertDialog(
            onDismissRequest = { showBanDialog = false },
            title = { Text("Ban User Permanently") },
            text = { Text("⚠️ WARNING: This will permanently delete ${user.name}'s account and ALL their posted items. This action CANNOT be undone. Are you absolutely sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.banUser(userId)
                        showBanDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Ban Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBanDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
