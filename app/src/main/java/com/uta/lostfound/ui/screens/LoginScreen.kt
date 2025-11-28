package com.uta.lostfound.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uta.lostfound.viewmodel.LoginViewModel
import com.uta.lostfound.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val isEmailValid = email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val isPasswordValid = password.isNotBlank() && password.length >= 6
    val isFormValid = isEmailValid && isPasswordValid
    
    // Navigate to home when login succeeds
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            onNavigateToHome()
        }
    }
    
    // Show loading screen while checking authentication status
    if (uiState.isLoading && !uiState.isLoggedIn && email.isBlank()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }
    
    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "UTA Lost & Found",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF001F3F))
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Splash icon at top of login screen
                Image(
                    painter = painterResource(id = R.drawable.splash_icon),
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 8.dp)
                )
            Spacer(modifier = Modifier.height(32.dp))
            
            // Login Form Card
            LoginFormCard(
                email = email,
                password = password,
                passwordVisible = passwordVisible,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                onSignInClick = { viewModel.signIn(email, password) },
                isLoading = uiState.isLoading,
                error = uiState.error,
                isFormValid = isFormValid,
                isEmailValid = isEmailValid,
                isPasswordValid = isPasswordValid
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Bottom Auth Prompt
            BottomAuthPrompt(
                normalText = "Don't have an account? ",
                clickableText = "Sign Up",
                onClick = onNavigateToSignUp
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun LoginFormCard(
    email: String,
    password: String,
    passwordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onSignInClick: () -> Unit,
    isLoading: Boolean,
    error: String?,
    isFormValid: Boolean,
    isEmailValid: Boolean,
    isPasswordValid: Boolean
) {
    val focusManager = LocalFocusManager.current
    
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                placeholder = { Text("your.email@example.com") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                isError = email.isNotEmpty() && !isEmailValid,
                supportingText = {
                    if (email.isNotEmpty() && !isEmailValid) {
                        Text("Please enter a valid email address")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Password") },
                placeholder = { Text("Enter your password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null)
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (isFormValid && !isLoading) {
                            onSignInClick()
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = onPasswordVisibilityToggle) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                singleLine = true,
                isError = password.isNotEmpty() && !isPasswordValid,
                supportingText = {
                    if (password.isNotEmpty() && !isPasswordValid) {
                        Text("Password must be at least 6 characters")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = { /* TODO: Implement forgot password */ }
                ) {
                    Text(
                        "Forgot password?",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            // Error Message
            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sign In Button
            Button(
                onClick = onSignInClick,
                enabled = !isLoading && isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Sign In",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomAuthPrompt(
    normalText: String,
    clickableText: String,
    onClick: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                color = Color.White
            )
        ) {
            append(normalText)
        }
        
        pushStringAnnotation(tag = "CLICKABLE", annotation = "CLICKABLE")
        withStyle(
            style = SpanStyle(
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )
        ) {
            append(clickableText)
        }
        pop()
    }
    
    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Center
        ),
        onClick = { offset ->
            annotatedString.getStringAnnotations(
                tag = "CLICKABLE",
                start = offset,
                end = offset
            ).firstOrNull()?.let {
                onClick()
            }
        }
    )
}
