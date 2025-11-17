package com.uta.lostfound.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryVariant,
    onPrimaryContainer = OnPrimary,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = Color(0xFFE8E4FF),
    onSecondaryContainer = Color(0xFF1A1A1A),
    tertiary = Tertiary,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE0F7F5),
    onTertiaryContainer = Color(0xFF1A1A1A),
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Color(0xFFE0E0E0),
    outlineVariant = Color(0xFFF5F5F5),
    error = Error,
    onError = OnError,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB00020)
)

@Composable
fun LostAndFoundTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
