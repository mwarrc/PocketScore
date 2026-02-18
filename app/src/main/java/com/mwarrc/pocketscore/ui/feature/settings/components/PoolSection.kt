package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.ui.feature.settings.getBallColor
import com.mwarrc.pocketscore.ui.feature.settings.InfoContent

/**
 * Section for configuring pool game settings.
 * Includes options for pool ball management and ball values.
 *
 * @param settings The current [AppSettings].
 * @param onUpdateSettings Callback to update the settings.
 * @param onNavigateToBallValues Callback to navigate to the ball values screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoolSection(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onNavigateToBallValues: () -> Unit
) {
    var infoDialogContent by remember { mutableStateOf<InfoContent?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Pool Management",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
        )

        SettingsItem(
            title = "Pool Ball Management",
            subtitle = "Enable insights & elimination for billiards",
            icon = Icons.Default.Analytics,
            onIconClick = {
                infoDialogContent = InfoContent(
                    title = "Pool Ball Management",
                    description = "Enables specialized tracking for pool/billiards games. It adds a secondary interface to the match screen where you can track which balls are remaining, calculate probabilities, and automatically adjust scores based on ball values.",
                    icon = Icons.Default.Analytics
                )
            },
            trailing = {
                Switch(
                    checked = settings.poolBallManagementEnabled,
                    onCheckedChange = { enabled ->
                        onUpdateSettings { it.copy(poolBallManagementEnabled = enabled) }
                    }
                )
            }
        )

        if (settings.poolBallManagementEnabled) {
            Surface(
                onClick = onNavigateToBallValues,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Extension,
                                null,
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Pool Ball Rules",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            val activePreset = settings.ballValuePresets.find {
                                it.values == settings.ballValues
                            }
                            if (activePreset != null) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        activePreset.name,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Mini visual preview of currently set values
                            (1..7).forEach { num ->
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(getBallColor(num)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${settings.ballValues[num]}",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (num in listOf(1, 2, 8, 13)) Color.Black else Color.White
                                    )
                                }
                            }
                            Text(
                                "...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.Default.PlayArrow,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }

    infoDialogContent?.let { info ->
        SettingInfoDialog(
            title = info.title,
            description = info.description,
            icon = info.icon,
            onDismiss = { infoDialogContent = null }
        )
    }
}
