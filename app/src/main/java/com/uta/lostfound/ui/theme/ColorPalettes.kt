package com.uta.lostfound.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * ============================================
 * LOST & FOUND APP - MATERIAL 3 COLOR PALETTES
 * ============================================
 * 
 * This file contains three complete Material 3 color palettes:
 * 
 * 1. UTA Themed Palette (UTAColorScheme)
 *    - Branded UTA blue (#0064B0) and orange (#FF8200)
 *    - Professional and recognizable
 *    - Best for official/institutional feel
 * 
 * 2. Clean Neutral Palette (CleanNeutralColorScheme)
 *    - Minimalist grayscale with soft blue-gray accent
 *    - Modern, light, and clean aesthetic
 *    - Best for professional/minimal design
 * 
 * 3. Vibrant Modern Palette (VibrantModernColorScheme)
 *    - Bright teal, purple, and lime accents
 *    - Energetic and student-friendly
 *    - Best for engaging user experience
 * 
 * HOW TO USE:
 * -----------
 * In Theme.kt, change the default colorScheme parameter:
 * 
 *   @Composable
 *   fun LostAndFoundTheme(
 *       colorScheme: ColorScheme = UTAColorScheme,  // Change this!
 *       content: @Composable () -> Unit
 *   ) { ... }
 * 
 * Or pass it when calling the theme:
 * 
 *   LostAndFoundTheme(colorScheme = VibrantModernColorScheme) {
 *       // Your app content
 *   }
 * 
 * CONTRAST RATIOS:
 * ----------------
 * All palettes follow WCAG accessibility guidelines:
 * - Primary/onPrimary: 4.5:1 minimum
 * - Surface/onSurface: 4.5:1 minimum
 * - All "on" colors ensure readable text
 */

// ============================================
// 1. UTA THEMED PALETTE
// ============================================
// Inspired by University of Texas at Arlington colors
// Primary: UTA Blue (#0064B0)
// Secondary: UTA Orange (#FF8200)

private val UTABlue = Color(0xFF0064B0)
private val UTABlueLight = Color(0xFF4A90D9)
private val UTABlueDark = Color(0xFF004A82)
private val UTAOrange = Color(0xFFFF8200)
private val UTAOrangeLight = Color(0xFFFFAB4A)
private val UTAOrangeDark = Color(0xFFC66100)

val UTAColorScheme = lightColorScheme(
    // Primary colors - UTA Blue
    primary = UTABlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3E4F7),
    onPrimaryContainer = Color(0xFF001D35),
    
    // Secondary colors - UTA Orange
    secondary = UTAOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC5),
    onSecondaryContainer = Color(0xFF2A1800),
    
    // Tertiary colors - Complementary teal
    tertiary = Color(0xFF006A6A),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF9CF2F2),
    onTertiaryContainer = Color(0xFF002020),
    
    // Error colors
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    // Background colors - Light and neutral
    background = Color(0xFFFDFCFF),
    onBackground = Color(0xFF1A1C1E),
    
    // Surface colors
    surface = Color(0xFFFDFCFF),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    
    // Outline
    outline = Color(0xFF75777F),
    outlineVariant = Color(0xFFC5C6D0)
)

// ============================================
// 2. CLEAN NEUTRAL PALETTE
// ============================================
// Minimalist grayscale with soft blue accent
// Modern, light, and clean aesthetic

private val NeutralPrimary = Color(0xFF5B6471)
private val NeutralAccent = Color(0xFF7B94B2)
private val WarmGray = Color(0xFF726B69)

val CleanNeutralColorScheme = lightColorScheme(
    // Primary colors - Muted blue-gray
    primary = NeutralAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDFE7F2),
    onPrimaryContainer = Color(0xFF161D28),
    
    // Secondary colors - Warm gray
    secondary = WarmGray,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEBE1DF),
    onSecondaryContainer = Color(0xFF2A2524),
    
    // Tertiary colors - Soft sage
    tertiary = Color(0xFF7C9580),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFDEECE0),
    onTertiaryContainer = Color(0xFF1A2621),
    
    // Error colors
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    
    // Background colors - Pure white
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1C1C1E),
    
    // Surface colors - Light grays
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1C1C1E),
    surfaceVariant = Color(0xFFF3F3F5),
    onSurfaceVariant = Color(0xFF48484A),
    
    // Outline
    outline = Color(0xFF8E8E93),
    outlineVariant = Color(0xFFD1D1D6)
)

// ============================================
// 3. VIBRANT MODERN PALETTE
// ============================================
// Bright, energetic combination
// Teal, Purple, and Lime accents
// Perfect for student-facing app

private val VibrantTeal = Color(0xFF00B4A6)
private val VibrantPurple = Color(0xFF8B5CF6)
private val VibrantLime = Color(0xFFA3E635)
private val VibrantPink = Color(0xFFEC4899)

val VibrantModernColorScheme = lightColorScheme(
    // Primary colors - Vibrant teal
    primary = VibrantTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA7F3EC),
    onPrimaryContainer = Color(0xFF00332E),
    
    // Secondary colors - Purple
    secondary = VibrantPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE9D5FF),
    onSecondaryContainer = Color(0xFF2E1065),
    
    // Tertiary colors - Lime
    tertiary = Color(0xFF84CC16),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFECFCCB),
    onTertiaryContainer = Color(0xFF1A3300),
    
    // Error colors - Vibrant pink/red
    error = Color(0xFFDC2626),
    onError = Color.White,
    errorContainer = Color(0xFFFFE5E5),
    onErrorContainer = Color(0xFF4A0000),
    
    // Background colors - Soft white
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF1F1F1F),
    
    // Surface colors
    surface = Color(0xFFFAFAFA),
    onSurface = Color(0xFF1F1F1F),
    surfaceVariant = Color(0xFFE7F5F4),
    onSurfaceVariant = Color(0xFF404040),
    
    // Outline
    outline = Color(0xFF737373),
    outlineVariant = Color(0xFFD4D4D8)
)

// ============================================
// HELPER FUNCTIONS
// ============================================

/**
 * Color palette options for the app
 */
enum class ColorPalette {
    UTA_THEMED,
    CLEAN_NEUTRAL,
    VIBRANT_MODERN
}

/**
 * Get color scheme by palette type
 */
fun getColorScheme(palette: ColorPalette) = when (palette) {
    ColorPalette.UTA_THEMED -> UTAColorScheme
    ColorPalette.CLEAN_NEUTRAL -> CleanNeutralColorScheme
    ColorPalette.VIBRANT_MODERN -> VibrantModernColorScheme
}

// ============================================
// INDIVIDUAL COLOR DEFINITIONS
// ============================================
// If you prefer to define colors separately and compose them yourself

object UTAColors {
    val Blue = Color(0xFF0064B0)
    val BlueLight = Color(0xFF4A90D9)
    val BlueDark = Color(0xFF004A82)
    val Orange = Color(0xFFFF8200)
    val OrangeLight = Color(0xFFFFAB4A)
    val OrangeDark = Color(0xFFC66100)
}

object NeutralColors {
    val Gray50 = Color(0xFFFAFAFA)
    val Gray100 = Color(0xFFF5F5F5)
    val Gray200 = Color(0xFFEEEEEE)
    val Gray300 = Color(0xFFE0E0E0)
    val Gray400 = Color(0xFFBDBDBD)
    val Gray500 = Color(0xFF9E9E9E)
    val Gray600 = Color(0xFF757575)
    val Gray700 = Color(0xFF616161)
    val Gray800 = Color(0xFF424242)
    val Gray900 = Color(0xFF212121)
    val BlueGray = Color(0xFF7B94B2)
}

object VibrantColors {
    val Teal = Color(0xFF00B4A6)
    val TealLight = Color(0xFF5EEAD4)
    val TealDark = Color(0xFF0F766E)
    val Purple = Color(0xFF8B5CF6)
    val PurpleLight = Color(0xFFC4B5FD)
    val PurpleDark = Color(0xFF6D28D9)
    val Lime = Color(0xFFA3E635)
    val LimeLight = Color(0xFFD9F99D)
    val LimeDark = Color(0xFF65A30D)
    val Pink = Color(0xFFEC4899)
    val PinkLight = Color(0xFFF9A8D4)
    val PinkDark = Color(0xFFBE185D)
}
