package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ImportPlayerMappingCard(
    importedName: String,
    existingPlayers: List<String>,
    currentMapping: String?, // Null means "New Player", otherwise maps to existing name
    unavailablePlayers: Set<String> = emptySet(), // Players already mapped elsewhere
    isAutoMatched: Boolean = false,
    onMappingChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    // Mapped name or "New Player" text
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
                    // Imported Name (Left)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Imported",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person, 
                                null, 
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                importedName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Arrow
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    // Target Name (Right - Clickable)
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.AutoFixHigh,
                                            null,
                                            modifier = Modifier.size(10.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "Auto-matched",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 8.sp,
                                            lineHeight = 8.sp
                                        )
                                    }
                                }
                                Text(
                                    displayText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1
                                )
                            }
                            Icon(
                                Icons.Default.ArrowDropDown,
                                null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                
                // Dropdown Menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    // Option: Create New Player
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
                            expanded = false
                        },
                        colors = if (isNewPlayer) MenuDefaults.itemColors(
                            textColor = MaterialTheme.colorScheme.primary,
                            leadingIconColor = MaterialTheme.colorScheme.primary
                        ) else MenuDefaults.itemColors()
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    
                    Text(
                        "Map to existing:",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Existing Players List
                    existingPlayers.forEach { player ->
                        if (player != importedName) { 
                             val isUnavailable = player in unavailablePlayers && player != currentMapping
                             
                             DropdownMenuItem(
                                text = { 
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            player,
                                            color = if (isUnavailable) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f) 
                                                    else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isUnavailable) {
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "(Already Mapped)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                                            )
                                        }
                                    }
                                },
                                enabled = !isUnavailable,
                                onClick = {
                                    onMappingChanged(player)
                                    expanded = false
                                },
                                trailingIcon = if (currentMapping == player) {
                                    { Icon(Icons.Default.Check, null) }
                                } else null
                            )
                        }
                    }
                }
            }
        }
    }
}
