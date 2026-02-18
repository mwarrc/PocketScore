package com.mwarrc.pocketscore.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

// Light theme colors
val PrimaryLight = Color(0xFF1B1B1F)
val SecondaryLight = Color(0xFF5E5E62)
val TertiaryLight = Color(0xFF7E767D)

// Dark theme colors
val PrimaryDark = Color(0xFFE3E2E6)
val SecondaryDark = Color(0xFFC7C6CA)
val TertiaryDark = Color(0xFFE5DEE4)

// Material 3 player color palette
private val PlayerColorPalette = listOf(
    Color(0xFF6750A4), // M3 Primary Purple
    Color(0xFF006A6A), // Teal
    Color(0xFF8B5000), // Orange
    Color(0xFF0061A4), // Blue
    Color(0xFF984061), // Pink/Berry
    Color(0xFF625B71), // Slate
    Color(0xFF006D3B), // Green
    Color(0xFF7E5700)  // Golden/Tan
)

/**
 * Gets a consistent Material 3 color for a player based on their name.
 * 
 * Uses deterministic hash-based selection to ensure the same name
 * always gets the same color across sessions.
 * 
 * @param name Player name
 * @return Material 3 color from the player palette
 */
@Composable
fun getMaterialPlayerColor(name: String): Color {
    if (name.isEmpty()) return MaterialTheme.colorScheme.primary
    
    val index = abs(name.hashCode()) % PlayerColorPalette.size
    return PlayerColorPalette[index]
}

/**
 * Gets a player color by index.
 * 
 * Useful for manual color assignment or previews.
 * 
 * @param index Color index (wraps around if out of bounds)
 * @return Color from the player palette
 */
fun getPlayerColorByIndex(index: Int): Color {
    return PlayerColorPalette[abs(index) % PlayerColorPalette.size]
}

/**
 * Returns the number of available player colors.
 */
fun getPlayerColorCount(): Int = PlayerColorPalette.size