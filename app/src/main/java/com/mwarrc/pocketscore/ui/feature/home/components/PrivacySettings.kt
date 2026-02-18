package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.AppSettings

/**
 * A configuration card for managing session privacy and temporary data persistence.
 * 
 * In PocketScore, "Guest Sessions" allow users to track matches without 
 * polluting their primary roster or history. This component provides:
 * - **Toggling**: Enable/Disable temporary session mode.
 * - **Granular Control**: Opt-in to saving results or player names even in guest mode.
 * - **Visual Feedback**: Distinct color themes (Tertiary) to signal non-standard persistence.
 * 
 * @param settings Current application settings defining privacy behavior.
 * @param onUpdateSettings Functional update for app configuration.
 * @param modifier Modifier for the card container.
 */
@Composable
fun PrivacySettingsCard(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    val isGuestActive = settings.isGuestSession
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = if (isGuestActive) {
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        border = BorderStroke(
            1.dp, 
            if (isGuestActive) {
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Toggle Row
            PrivacyHeaderRow(
                isGuestActive = isGuestActive,
                settings = settings,
                onCheckedChange = { isEnabled ->
                    onUpdateSettings { it.copy(isGuestSession = isEnabled) }
                }
            )

            // Extended Persistence Logic
            AnimatedVisibility(
                visible = isGuestActive,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                PrivacyExceptionsPanel(
                    saveRecords = settings.guestSaveRecords,
                    savePlayers = settings.guestSavePlayers,
                    onUpdate = onUpdateSettings
                )
            }
        }
    }
}

@Composable
private fun PrivacyHeaderRow(
    isGuestActive: Boolean,
    settings: AppSettings,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Visual Identifier
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isGuestActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceContainerHigh,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isGuestActive) Icons.Default.Groups else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = if (isGuestActive) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            // Textual Status
            Column {
                Text(
                    text = "Guest Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                val privacyStatus = when {
                    !isGuestActive -> "History recording active"
                    settings.guestSaveRecords && settings.guestSavePlayers -> "Saving records & players"
                    settings.guestSaveRecords -> "Saving game records"
                    settings.guestSavePlayers -> "Saving player names"
                    else -> "Session is fully private"
                }
                Text(
                    text = privacyStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isGuestActive) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Switch(
            checked = isGuestActive,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onTertiary,
                checkedTrackColor = MaterialTheme.colorScheme.tertiary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun PrivacyExceptionsPanel(
    saveRecords: Boolean,
    savePlayers: Boolean,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Label
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Info, 
                contentDescription = null, 
                modifier = Modifier.size(14.dp), 
                tint = MaterialTheme.colorScheme.tertiary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "STORAGE EXCEPTIONS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.tertiary,
                letterSpacing = 1.sp
            )
        }
        
        // Granular Toggles
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PrivacyToggleRow(
                title = "Log Results",
                description = "Keep match records in local History",
                checked = saveRecords,
                onCheckedChange = { checked ->
                     onUpdate { it.copy(guestSaveRecords = checked) }
                }
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
            
            PrivacyToggleRow(
                title = "Remember Guests",
                description = "Keep names in your Active Roster",
                checked = savePlayers,
                onCheckedChange = { checked ->
                     onUpdate { it.copy(guestSavePlayers = checked) }
                }
            )
        }
    }
}

@Composable
private fun PrivacyToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = MaterialTheme.colorScheme.tertiary,
                uncheckedColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}
