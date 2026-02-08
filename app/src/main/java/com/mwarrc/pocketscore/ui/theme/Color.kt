package com.mwarrc.pocketscore.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme

val PrimaryLight = Color(0xFF1B1B1F)
val SecondaryLight = Color(0xFF5E5E62)
val TertiaryLight = Color(0xFF7E767D)

val PrimaryDark = Color(0xFFE3E2E6)
val SecondaryDark = Color(0xFFC7C6CA)
val TertiaryDark = Color(0xFFE5DEE4)

@Composable
fun getMaterialPlayerColor(name: String): Color {
    val colors = listOf(
        Color(0xFF6750A4), // M3 Primary Purple
        Color(0xFF006A6A), // Teal
        Color(0xFF8B5000), // Orange
        Color(0xFF0061A4), // Blue
        Color(0xFF984061), // Pink/Berry
        Color(0xFF625B71), // Slate
        Color(0xFF006D3B), // Green
        Color(0xFF7E5700)  // Golden/Tan
    )
    if (name.isEmpty()) return MaterialTheme.colorScheme.primary
    val index = Math.abs(name.hashCode()) % colors.size
    return colors[index]
}