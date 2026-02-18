package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
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
import com.mwarrc.pocketscore.domain.model.AppTheme
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout
import com.mwarrc.pocketscore.ui.feature.settings.InfoContent

/**
 * Section for appearance settings.
 * Allows configuring app theme (Dark/Light/System) and default scoreboard layout.
 *
 * @param settings The current [AppSettings].
 * @param onUpdateSettings Callback to update the settings.
 */
@Composable
fun AppearanceSection(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    var infoDialogContent by remember { mutableStateOf<InfoContent?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(
            text = "Appearance",
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
                            title = "Theme Mode",
                            description = "Change the visual style of the application. 'System' follows your Android device settings, while 'Light' and 'Dark' provide fixed appearances. Dark mode is recommended for battery saving and eye comfort in low light.",
                            icon = Icons.Default.Palette
                        )
                    },
                    color = Color.Transparent,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Palette, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Theme Mode", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Switch between Light and Dark",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf("System", "Light", "Dark")
                val selectedIndex = when (settings.appTheme) {
                    AppTheme.SYSTEM -> 0
                    AppTheme.LIGHT -> 1
                    AppTheme.DARK -> 2
                }
                options.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = {
                            val newVal = when (index) {
                                0 -> AppTheme.SYSTEM
                                1 -> AppTheme.LIGHT
                                2 -> AppTheme.DARK
                                else -> AppTheme.SYSTEM
                            }
                            onUpdateSettings { it.copy(appTheme = newVal) }
                        },
                        selected = index == selectedIndex,
                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    onClick = {
                        infoDialogContent = InfoContent(
                            title = "Default Layout",
                            description = "Sets your preferred viewing mode for the match scoreboard. 'Grid' is compact and shows many players at once, while 'List' provides more detailed information per player and is easier to read in fast-paced games.",
                            icon = Icons.Default.ViewModule
                        )
                    },
                    color = Color.Transparent,
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ViewModule, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Default Layout", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Preferred scoreboard view",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf("Grid", "List")
                options.forEachIndexed { index, label ->
                    val isGridChoice = label == "Grid"
                    // Check logic: if it IS grid choice and current is grid -> selected
                    // OR if it is list choice and current is list -> selected
                    val isSelected =
                        (settings.defaultLayout == ScoreboardLayout.GRID && isGridChoice) ||
                                (settings.defaultLayout == ScoreboardLayout.LIST && !isGridChoice)

                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = options.size
                        ),
                        onClick = {
                            onUpdateSettings {
                                it.copy(defaultLayout = if (isGridChoice) ScoreboardLayout.GRID else ScoreboardLayout.LIST)
                            }
                        },
                        selected = isSelected,
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isGridChoice) Icons.Outlined.GridView else Icons.Outlined.ViewAgenda,
                                    null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(label, style = MaterialTheme.typography.labelSmall)
                            }
                        }
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
