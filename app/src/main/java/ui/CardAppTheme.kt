package com.example.listsqre_revamped.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF826A5D),            // Muted brown
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFFC3B091),          // Faded beige
    onSecondary = Color(0xFF000000),
    background = Color(0xFFF5EBDD),         // Off-white with warmth
    onBackground = Color(0xFF3C2F2F),
    surface = Color(0xFFE8D9C4),            // Aged paper color
    onSurface = Color(0xFF3C2F2F)
)

val LightFABContainer = Color(0xFF826A5D)   // Same as primary
val LightFABContent = Color(0xFFFFFFFF)     // Same as onPrimary

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBFA6A0),            // Dusty rose
    onPrimary = Color(0xFF1E1E1E),
    secondary = Color(0xFF7A6E5B),          // Coffee brown
    onSecondary = Color(0xFFEDE3D2),
    background = Color(0xFF2E2B29),         // Deep sepia
    onBackground = Color(0xFFDAD2C9),       // Light beige
    surface = Color(0xFF3F3B36),            // Muted charcoal
    onSurface = Color(0xFFEFE7DD)
)

val DarkFABContainer = Color(0xFFBFA6A0)    // Same as primary
val DarkFABContent = Color(0xFF1E1E1E)      // Same as onPrimary

val Typography = Typography(
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.5.sp
    )
)

@Composable
fun ThemedFAB(onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val containerColor = if (isDark) DarkFABContainer else LightFABContainer
    val contentColor = if (isDark) DarkFABContent else LightFABContent

    FloatingActionButton(
        onClick = onClick,
        containerColor = containerColor,
        contentColor = contentColor
    ) {
        Icon(Icons.Default.Add, contentDescription = "Add")
    }
}

@Composable
fun CardAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}