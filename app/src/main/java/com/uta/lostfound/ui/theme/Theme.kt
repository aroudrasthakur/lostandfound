package com.uta.lostfound.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

/**
 * Main theme for the Lost & Found app
 * 
 * To switch color palettes, change the colorScheme parameter:
 * - UTAColorScheme: UTA branded blue and orange
 * - CleanNeutralColorScheme: Minimalist grayscale with soft accent
 * - VibrantModernColorScheme: Bright teal, purple, and lime
 * 
 * All palettes are defined in ColorPalettes.kt
 */
@Composable
fun LostAndFoundTheme(
    // Change this to switch between color palettes
    colorScheme: androidx.compose.material3.ColorScheme = UTAColorScheme,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
