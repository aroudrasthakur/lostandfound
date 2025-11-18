package com.uta.lostfound.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.uta.lostfound.data.model.Item
import com.uta.lostfound.data.model.ItemCategory
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
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    
    // Load items when HomeScreen is first composed
    LaunchedEffect(Unit) {
        foundItemsViewModel.loadFoundItems()
    }
    
    // Filter categories for quick access
    val categories = listOf("All", "Electronics", "Documents", "Bags", "Keys", "Other")
    
    Scaffold(
        topBar = {
            Column {
                // Top App Bar with increased visual hierarchy
                TopAppBar(
                    title = { 
                        Text(
                            "UTA Lost & Found",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    actions = {
                        if (loginUiState.currentUser?.role == "admin") {
                            IconButton(onClick = onNavigateToAdminDashboard) {
                                Icon(Icons.Default.Settings, contentDescription = "Admin Dashboard")
                            }
                        }
                    }
                )
                
                // Search Bar with Plus Icon
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = onNavigateToSearch)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Search items...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    // Plus button with dropdown for adding items
                    Box {
                        IconButton(
                            onClick = { showAddMenu = !showAddMenu },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Item",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
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
                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                }
                            )
                        }
                    }
                }
                
                // Category Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = if (category == "All") selectedCategory == null else selectedCategory == category,
                            onClick = {
                                selectedCategory = if (category == "All") null else category
                            },
                            label = { Text(category) }
                        )
                    }
                }
                
                // Thin divider to separate header from content
                Divider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        },
        bottomBar = {
            // Clean bottom navigation with clear selected state
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { 
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = "Lost Items",
                            modifier = Modifier.size(24.dp)
                        ) 
                    },
                    label = { 
                        Text(
                            "Lost",
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { 
                        Icon(
                            Icons.Default.CheckCircle, 
                            contentDescription = "Found Items",
                            modifier = Modifier.size(24.dp)
                        ) 
                    },
                    label = { 
                        Text(
                            "Found",
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { 
                        Icon(
                            Icons.Default.Notifications, 
                            contentDescription = "Notifications",
                            modifier = Modifier.size(24.dp)
                        ) 
                    },
                    label = { 
                        Text(
                            "Notifications",
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    },
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { 
                        Icon(
                            Icons.Default.Person, 
                            contentDescription = "Profile",
                            modifier = Modifier.size(24.dp)
                        ) 
                    },
                    label = { 
                        Text(
                            "Profile",
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    },
                    alwaysShowLabel = true
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter items based on selected category
            val filteredItems = remember(foundItemsState.items, selectedCategory) {
                if (selectedCategory == null) {
                    foundItemsState.items
                } else {
                    foundItemsState.items.filter { 
                        it.category.equals(selectedCategory, ignoreCase = true) 
                    }
                }
            }
            
            when (selectedTab) {
                0 -> ItemsList(
                    items = filteredItems.filter { it.status.name == "LOST" },
                    isLoading = foundItemsState.isLoading,
                    error = foundItemsState.error,
                    emptyMessage = "No lost items reported yet",
                    onItemClick = onNavigateToItemDetails,
                    onUserClick = onNavigateToUserProfile,
                    onRefresh = { foundItemsViewModel.loadFoundItems() }
                )
                1 -> ItemsList(
                    items = filteredItems.filter { it.status.name == "FOUND" },
                    isLoading = foundItemsState.isLoading,
                    error = foundItemsState.error,
                    emptyMessage = "No found items reported yet",
                    onItemClick = onNavigateToItemDetails,
                    onUserClick = onNavigateToUserProfile,
                    onRefresh = { foundItemsViewModel.loadFoundItems() }
                )
                2 -> {
                    // Notifications Tab
                    loginUiState.currentUser?.let { user ->
                        NotificationsTab(
                            userId = user.uid,
                            onNavigateToUserProfile = onNavigateToUserProfile,
                            onNavigateToItemDetails = onNavigateToItemDetails
                        )
                    } ?: run {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Please log in to view notifications")
                        }
                    }
                }
                3 -> ProfileTab(
                    user = loginUiState.currentUser,
                    onLogout = {
                        loginViewModel.signOut()
                        onNavigateToLogin()
                    },
                    onViewFullProfile = {
                        loginUiState.currentUser?.uid?.let { userId ->
                            onNavigateToUserProfile(userId)
                        }
                    }
                )
            }
        }
    }
}

// ItemsList with simple refresh button in corner (Material Design 3)
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
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: $error",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = onRefresh) {
                        Text("Retry")
                    }
                }
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
                // Increased spacing between cards for cleaner look
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
        
        // Small refresh button in bottom right corner (not overlapping content)
        if (!isLoading && items.isNotEmpty()) {
            SmallFloatingActionButton(
                onClick = onRefresh,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// Enhanced ItemCard with improved typography and layout
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
        // Subtle elevation with medium corner radius
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Reduced image size, left aligned
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small
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
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Bold item title
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Category as pill badge with light gray background
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = Color(0xFFE0E0E0),
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Text(
                        text = "[${item.category.uppercase()}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF424242),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Location with pin icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Compact "Posted by" and date on one line
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Posted by ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.userName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable(
                            onClick = onUserClick,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                    )
                    Text(
                        text = " • ${dateFormat.format(Date(item.date))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileTab(
    user: com.uta.lostfound.data.model.User?,
    onLogout: () -> Unit,
    onViewFullProfile: () -> Unit = {}
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // View Full Profile Button
            OutlinedButton(
                onClick = onViewFullProfile,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Full Profile")
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
