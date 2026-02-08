package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import com.mwarrc.pocketscore.ui.theme.getMaterialPlayerColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActiveRosterSection(
    savedPlayerNames: List<String>,
    currentNames: List<String>,
    onSelectName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (savedPlayerNames.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header
        Column(modifier = Modifier.padding(start = 4.dp)) {
            Text(
                "Active Roster",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            val currentCount = currentNames.count { it.trim().isNotEmpty() }
            if (currentCount > 0) {
                Text(
                    "$currentCount players selected • Tap to toggle",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // Roster Pool
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sortedNames = savedPlayerNames.sortedByDescending { savedName ->
                currentNames.any { it.trim().equals(savedName.trim(), ignoreCase = true) }
            }
            
            sortedNames.forEach { name ->
                val isSelected = currentNames.any { it.trim().equals(name.trim(), ignoreCase = true) }
                
                RosterChip(
                    name = name,
                    isSelected = isSelected,
                    onClick = { onSelectName(name) }
                )
            }
        }
    }
}

@Composable
fun RosterChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val playerColor = getMaterialPlayerColor(name)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()


    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        tonalElevation = if (isSelected) 2.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = Modifier.height(44.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                // Minimal circle instead of player color
                Surface(
                    modifier = Modifier.size(10.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                ) { Box {} }
            }
            
            Text(
                name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
