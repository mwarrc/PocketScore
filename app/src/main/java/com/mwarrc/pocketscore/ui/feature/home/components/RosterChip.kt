package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.ui.theme.getMaterialPlayerColor

/**
 * A selectable chip representing a player in the roster pool.
 * 
 * Features:
 * - **Selection State**: Visual highlight when selected.
 * - **Selection Order**: Displays a numeric badge showing the order of selection.
 * - **Identity Colors**: Subtly indicates the player's unique color theme.
 * 
 * @param name The player's display name.
 * @param isSelected Whether the player is currently part of the active session roster.
 * @param selectionOrder The sequence number in which this player was added (1-indexed).
 * @param onClick Callback triggered when the chip is tapped.
 */
@Composable
fun RosterChip(
    name: String,
    isSelected: Boolean,
    selectionOrder: Int? = null,
    onClick: () -> Unit
) {
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
            // Selection Badge / Identity Indicator
            if (isSelected) {
                if (selectionOrder != null) {
                    SelectionIndexBadge(index = selectionOrder)
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            } else {
                // Dimmed identity indicator
                Surface(
                    modifier = Modifier.size(10.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                ) { Box {} }
            }
            
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SelectionIndexBadge(index: Int) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .background(MaterialTheme.colorScheme.onPrimary, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
