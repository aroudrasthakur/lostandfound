package com.uta.lostfound.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.ItemStatus
import com.uta.lostfound.utils.FirebaseDataSeeder
import com.uta.lostfound.viewmodel.FoundItemsViewModel
import com.uta.lostfound.viewmodel.LoginViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToReportLost: () -> Unit,
    onNavigateToReportFound: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToItemDetails: (String) -> Unit,
    onNavigateToAdminDashboard: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToUserProfile: (String) -> Unit,
    loginViewModel: LoginViewModel = viewModel(),
    foundItemsViewModel: FoundItemsViewModel = viewModel()
) {
    val loginUiState by loginViewModel.uiState.collectAsState()
    val foundItemsState by foundItemsViewModel.uiState.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    var showAddMenu by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("UTA Lost & Found") },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    Box {
                        IconButton(onClick = { showAddMenu = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Item")
                        }
                        DropdownMenu(
                            expanded = showAddMenu,
                            onDismissRequest = { showAddMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Report Lost Item") },
                                onClick = {
                                    showAddMenu = false
                                    onNavigateToReportLost()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Search, contentDescription = null)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add a Discovered Item") },
                                onClick = {
                                    showAddMenu = false
                                    onNavigateToReportFound()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                }
                            )
                        }
                    }
                    if (loginUiState.currentUser?.role == "admin") {
                        IconButton(onClick = onNavigateToAdminDashboard) {
                            Icon(Icons.Default.Settings, contentDescription = "Admin Dashboard")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Lost Items") },
                    label = { Text("Lost") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = "Found Items") },
                    label = { Text("Found") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> ItemsList(
                    items = foundItemsState.items.filter { it.status.name == "LOST" },
                    isLoading = foundItemsState.isLoading,
                    error = foundItemsState.error,
                    emptyMessage = "No lost items reported yet",
                    onItemClick = onNavigateToItemDetails,
                    onUserClick = onNavigateToUserProfile,
                    onRefresh = { foundItemsViewModel.loadFoundItems() }
                )
                1 -> ItemsList(
                    items = foundItemsState.items.filter { it.status.name == "FOUND" },
                    isLoading = foundItemsState.isLoading,
                    error = foundItemsState.error,
                    emptyMessage = "No found items reported yet",
                    onItemClick = onNavigateToItemDetails,
                    onUserClick = onNavigateToUserProfile,
                    onRefresh = { foundItemsViewModel.loadFoundItems() }
                )
                2 -> ProfileTab(
                    user = loginUiState.currentUser,
                    onLogout = {
                        loginViewModel.signOut()
                        onNavigateToLogin()
                    }
                )
            }
        }
    }
}

@Composable
fun ItemsList(
    items: List<Item>,
    isLoading: Boolean,
    error: String?,
    emptyMessage: String,
    onItemClick: (String) -> Unit,
    onUserClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            error != null -> {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            items.isEmpty() -> {
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items) { item ->
                        ItemCard(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            onUserClick = { onUserClick(item.userId) }
                        )
                    }
                }
            }
        }
        
        // Refresh FAB
        FloatingActionButton(
            onClick = onRefresh,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = "Refresh"
            )
        }
    }
}

@Composable
fun ItemCard(
    item: Item,
    onClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Image with placeholder for empty URLs
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ),
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
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "No image",
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Location: ${item.location}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // User name (clickable)
                Text(
                    text = "Posted by: ${item.userName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    modifier = Modifier.clickable(
                        onClick = onUserClick,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Text(
                    text = dateFormat.format(Date(item.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ProfileTab(
    user: com.uta.lostfound.data.model.User?,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var isSeeding by remember { mutableStateOf(false) }
    var seedMessage by remember { mutableStateOf<String?>(null) }
    var showMessage by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Profile",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (user != null) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (user.role == "admin") {
                Spacer(modifier = Modifier.height(8.dp))
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
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Development Tools Section (Admin Only)
        if (user?.role == "admin") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Development Tools",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Add sample data to test the app",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                isSeeding = true
                                scope.launch {
                                    FirebaseDataSeeder.seedMockData().fold(
                                        onSuccess = { message ->
                                            seedMessage = message
                                            showMessage = true
                                            isSeeding = false
                                        },
                                        onFailure = { error ->
                                            seedMessage = "Error: ${error.message}"
                                            showMessage = true
                                            isSeeding = false
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isSeeding
                        ) {
                            if (isSeeding) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Add Mock Data")
                            }
                        }
                        
                        OutlinedButton(
                            onClick = {
                                isSeeding = true
                                scope.launch {
                                    FirebaseDataSeeder.clearMockData().fold(
                                        onSuccess = { message ->
                                            seedMessage = message
                                            showMessage = true
                                            isSeeding = false
                                        },
                                        onFailure = { error ->
                                            seedMessage = "Error: ${error.message}"
                                            showMessage = true
                                            isSeeding = false
                                        }
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isSeeding
                        ) {
                            Text("Clear Mock Data")
                        }
                    }
                    
                    if (showMessage && seedMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = seedMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (seedMessage!!.startsWith("✓")) 
                                MaterialTheme.colorScheme.primary 
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
        
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("Sign Out")
        }
    }
}
