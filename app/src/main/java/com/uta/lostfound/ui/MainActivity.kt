package com.uta.lostfound.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.uta.lostfound.ui.navigation.NavGraph
import com.uta.lostfound.ui.navigation.Screen
import com.uta.lostfound.ui.theme.LostAndFoundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            LostAndFoundTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Start at login screen - Firebase auth check disabled until google-services.json is updated
                    NavGraph(startDestination = Screen.Login.route)
                }
            }
        }
    }
}
