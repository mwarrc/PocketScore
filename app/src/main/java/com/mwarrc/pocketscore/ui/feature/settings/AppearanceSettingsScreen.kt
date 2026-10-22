package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.BorderAll
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.ViewAgenda
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.AppTheme
import com.mwarrc.pocketscore.domain.model.KeyboardHeight
import com.mwarrc.pocketscore.domain.model.ScoreboardLayout
import com.mwarrc.pocketscore.ui.feature.settings.components.SettingsItem
import com.mwarrc.pocketscore.ui.theme.displayName
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettingsScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onNavigateToFont: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .displayCutoutPadding(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Display & Appearance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                windowInsets = WindowInsets(top = 32.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .navigationBarsPadding()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Theme Section
            item {
                Text(
                    "App Theme",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.padding(horizontal = 6.dp))
                            Text(
                                "Color Mode",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val themes = listOf(AppTheme.LIGHT to "Light", AppTheme.DARK to "Dark", AppTheme.SYSTEM to "System")
                            themes.forEachIndexed { index, (theme, label) ->
                                SegmentedButton(
                                    selected = settings.appTheme == theme,
                                    onClick = { onUpdateSettings { it.copy(appTheme = theme) } },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = themes.size)
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                }
            }

            // Typography Section
            item {
                Text(
                    "Typography",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    onClick = onNavigateToFont,
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.TextFields,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "App Font",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                settings.appFont.displayName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }

            // Scoreboard Layout
            item {
                Text(
                    "Scoreboard Layout",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ViewModule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.padding(horizontal = 6.dp))
                            Text(
                                "Default Game View",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            val layouts = listOf(
                                ScoreboardLayout.GRID to ("Grid View" to Icons.Outlined.GridView),
                                ScoreboardLayout.LIST to ("List View" to Icons.Outlined.ViewAgenda)
                            )
                            layouts.forEachIndexed { index, (layout, data) ->
                                val (label, icon) = data
                                SegmentedButton(
                                    selected = settings.defaultLayout == layout,
                                    onClick = { onUpdateSettings { it.copy(defaultLayout = layout) } },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = layouts.size),
                                    icon = { Icon(icon, null, modifier = Modifier.size(18.dp)) }
                                ) {
                                    Text(label)
                                }
                            }
                        }
                    }
                }
            }

            // Global UI Zoom
            item {
                Text(
                    "Interface Scale (Zoom)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Global Zoom Factor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "Adjust size of scores, buttons, and text",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            val displayValue = String.format(Locale.US, "%.2fx", settings.globalScale)
                            val isDefault = abs(settings.globalScale - 1.0f) < 0.02f
                            TextButton(
                                onClick = { onUpdateSettings { it.copy(globalScale = 1.0f) } },
                                enabled = !isDefault
                            ) {
                                Text(if (isDefault) "Default (1.0x)" else "Reset ($displayValue)")
                            }
                        }
                        
                        var localSliderValue by remember(settings.globalScale) { mutableFloatStateOf(settings.globalScale) }
                        Slider(
                            value = localSliderValue,
                            onValueChange = { localSliderValue = Math.round(it * 20f) / 20f },
                            onValueChangeFinished = { onUpdateSettings { it.copy(globalScale = localSliderValue) } },
                            valueRange = AppSettings.MIN_GLOBAL_SCALE..AppSettings.MAX_GLOBAL_SCALE,
                            steps = 13,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Custom Keyboard
            item {
                Text(
                    "Custom Number Pad",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            item {
                SettingsItem(
                    title = "Use Custom Scoring Keyboard",
                    subtitle = "Optimized fast input keypad for scores",
                    icon = Icons.Default.Keyboard,
                    trailing = {
                        Switch(
                            checked = settings.useCustomKeyboard,
                            onCheckedChange = { enabled ->
                                onUpdateSettings { it.copy(useCustomKeyboard = enabled) }
                            }
                        )
                    }
                )
            }

            if (settings.useCustomKeyboard) {
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Height,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.padding(horizontal = 6.dp))
                                Text(
                                    "Keyboard Height",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                val heights = listOf(
                                    KeyboardHeight.COMPACT to "Compact",
                                    KeyboardHeight.MEDIUM to "Medium",
                                    KeyboardHeight.LARGE to "Large"
                                )
                                heights.forEachIndexed { index, (height, label) ->
                                    SegmentedButton(
                                        selected = settings.keyboardHeight == height,
                                        onClick = { onUpdateSettings { it.copy(keyboardHeight = height) } },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = heights.size)
                                    ) {
                                        Text(label)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    SettingsItem(
                        title = "Keyboard Key Borders",
                        subtitle = "Show outlines around individual keypads",
                        icon = Icons.Default.BorderAll,
                        trailing = {
                            Switch(
                                checked = settings.keyboardBordersEnabled,
                                onCheckedChange = { enabled ->
                                    onUpdateSettings { it.copy(keyboardBordersEnabled = enabled) }
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}
