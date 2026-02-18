package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.ui.feature.settings.InfoContent

/**
 * Section for setting app limits.
 * Currently allows configuring the maximum number of players for a game.
 *
 * @param settings The current [AppSettings].
 * @param onUpdateSettings Callback to update the settings.
 */
@Composable
fun LimitsSection(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    var infoDialogContent by remember { mutableStateOf<InfoContent?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Limits",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    onClick = {
                        infoDialogContent = InfoContent(
                            title = "Max Players",
                            description = "Configures the upper limit for the number of players that can be added to a single match. Increasing this allows for larger tournaments, while decreasing it helps keep the home screen selection pool clean.",
                            icon = Icons.Default.Group
                        )
                    },
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Group, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Max Players", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Allowed in setup: ${settings.maxPlayers}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Slider(
                value = settings.maxPlayers.toFloat(),
                onValueChange = { value ->
                    onUpdateSettings { it.copy(maxPlayers = value.toInt()) }
                },
                valueRange = 8f..100f,
                steps = 24,
                modifier = Modifier.fillMaxWidth()
            )
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
