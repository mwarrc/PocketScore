package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A sophisticated mapping card used to link imported player names to existing local players.
 * 
 * Features:
 * - **Auto-Matching Support**: Visual indicator when names are automatically detected.
 * - **Identity Management**: Option to creating a "New Player" or linking to an existing one.
 * - **Conflict Prevention**: Disables existing players that have already been mapped to avoid duplicates.
 * - **Intuitive UI**: Clear directional flow from imported identity to local identity.
 * 
 * @param importedName The raw player name found in the shared data.
 * @param existingPlayers List of player names currently in the user's roster.
 * @param currentMapping The currently selected target name. `null` indicates a "New Player" creation.
 * @param unavailablePlayers Set of player names already linked to other imported identities.
 * @param isAutoMatched Whether the system automatically suggested this mapping.
 * @param onMappingChanged Callback triggered when the target identity is updated.
 * @param modifier Modifier for the card container.
 */
@Composable
fun ImportPlayerMappingCard(
    importedName: String,
    existingPlayers: List<String>,
    currentMapping: String?, 
    unavailablePlayers: Set<String> = emptySet(), 
    isAutoMatched: Boolean = false,
    onMappingChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Labeling and identity status
    val displayText = currentMapping ?: "New Player"
    val isNewPlayer = currentMapping == null

    Column(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp, 
                if (isAutoMatched) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) 
                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Imported Identity (Read-only)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Imported",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person, 
                                contentDescription = null, 
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = importedName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Center: Directional Indicator
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "maps to",
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    // Right: Local Identity Selection (Interactive)
                    Surface(
                        onClick = { expanded = true },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isNewPlayer) MaterialTheme.colorScheme.secondaryContainer 
                                else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                if (isAutoMatched) {
                                    AutoMatchBadge()
                                }
                                Text(
                                    text = displayText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Show Options",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Selection Menu
                MappingDropdownMenu(
                    expanded = expanded,
                    onDismiss = { expanded = false },
                    isNewPlayer = isNewPlayer,
                    currentMapping = currentMapping,
                    existingPlayers = existingPlayers,
                    importedName = importedName,
                    unavailablePlayers = unavailablePlayers,
                    onMappingChanged = onMappingChanged
                )
            }
        }
    }
}

@Composable
private fun AutoMatchBadge() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.AutoFixHigh,
            contentDescription = null,
            modifier = Modifier.size(10.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = "Auto-matched",
            style = MaterialTheme.typography.labelSmall,
            fontSize = 8.sp,
            lineHeight = 8.sp
        )
    }
}

@Composable
private fun MappingDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isNewPlayer: Boolean,
    currentMapping: String?,
    existingPlayers: List<String>,
    importedName: String,
    unavailablePlayers: Set<String>,
    onMappingChanged: (String?) -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.7f)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        // Option 1: Create as unique player
        DropdownMenuItem(
            text = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Text("Create as New Player")
                }
            },
            onClick = {
                onMappingChanged(null)
                onDismiss()
            },
            colors = if (isNewPlayer) MenuDefaults.itemColors(
                textColor = MaterialTheme.colorScheme.primary,
                leadingIconColor = MaterialTheme.colorScheme.primary
            ) else MenuDefaults.itemColors()
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
        
        Text(
            text = "Map to existing:",
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // List of eligible local players
        existingPlayers.forEach { player ->
            if (player != importedName) { 
                val isUnavailable = player in unavailablePlayers && player != currentMapping
                
                DropdownMenuItem(
                    text = { 
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = player,
                                color = if (isUnavailable) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f) 
                                        else MaterialTheme.colorScheme.onSurface
                            )
                            if (isUnavailable) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "(Already Mapped)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                )
                            }
                        }
                    },
                    enabled = !isUnavailable,
                    onClick = {
                        onMappingChanged(player)
                        onDismiss()
                    },
                    trailingIcon = if (currentMapping == player) {
                        { Icon(Icons.Default.Check, null) }
                    } else null
                )
            }
        }
    }
}
