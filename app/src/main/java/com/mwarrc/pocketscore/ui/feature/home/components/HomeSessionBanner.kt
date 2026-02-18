package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings

/**
 * A banner that informs the user about the current session's privacy and persistence settings.
 * 
 * In PocketScore, "Guest Mode" or "Private Sessions" are used when the user wants to play
 * without permanently affecting their global roster or match history. 
 * This banner provides quick visual feedback on whether data is being saved.
 * 
 * @param settings The current application settings defining session behavior.
 * @param onSettingsClick Callback triggered when the banner is tapped, usually to change session rules.
 * @param modifier Modifier for the banner container.
 */
@Composable
fun HomeSessionBanner(
    settings: AppSettings,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = settings.isGuestSession,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        // Find the human-readable name of current game rules (e.g. "8-Ball", "Snooker")
        val activePresetName = settings.ballValuePresets.find { 
            it.values == settings.ballValues 
        }?.name ?: "Custom Rules"

        Surface(
            onClick = onSettingsClick,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Session Information
                Column(modifier = Modifier.weight(1f)) {
                    val bannerTitle = when {
                        settings.guestSaveRecords && settings.guestSavePlayers -> "Guest Scoreboard"
                        settings.guestSaveRecords || settings.guestSavePlayers -> "Guest Session"
                        else -> "Private Session"
                    }
                    
                    val bannerSubtitle = when {
                        settings.guestSaveRecords && settings.guestSavePlayers -> "Saving records & names"
                        settings.guestSaveRecords -> "Saving game records"
                        settings.guestSavePlayers -> "Saving player names"
                        else -> "Nothing is being saved"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Groups,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = bannerTitle,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                    Text(
                        text = bannerSubtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                }
                
                // Right: Active Rule Preset Badge
                Surface(
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = activePresetName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}
