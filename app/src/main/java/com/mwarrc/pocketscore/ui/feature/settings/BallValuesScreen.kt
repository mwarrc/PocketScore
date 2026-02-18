package com.mwarrc.pocketscore.ui.feature.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.BallValuePreset
import com.mwarrc.pocketscore.ui.feature.settings.components.BallPreviewItem
import com.mwarrc.pocketscore.ui.feature.settings.components.DeletePresetConfirmDialog
import com.mwarrc.pocketscore.ui.feature.settings.components.SavePresetDialog
import com.mwarrc.pocketscore.ui.feature.settings.components.ValueEditor

/**
 * Screen for configuring the point values of pool balls (1–15).
 *
 * ## Features
 * - **Preset selector** – choose from saved presets via filter chips; custom presets can be
 *   deleted. Default presets are protected.
 * - **Live table preview** – shows all 15 balls with their current values and accurate
 *   visual designs (Solid / Stripe / 8-Ball).
 * - **Stepper editors** – each ball has a dedicated +/− card so values are easy to adjust
 *   without a keyboard.
 * - **Non-destructive save** – "Apply" saves the values and stays on the screen; "Save as
 *   Preset" stores the current config for future use. The back button warns if there are
 *   unsaved changes.
 *
 * @param settings         Current application settings.
 * @param onUpdateSettings Callback to persist updated settings.
 * @param onBack           Callback to navigate back to the Settings screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BallValuesScreen(
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onBack: () -> Unit
) {
    // ── State ────────────────────────────────────────────────────────────────
    /** Working copy of ball values as strings for the editors. */
    var editingValues by remember {
        mutableStateOf(settings.ballValues.mapValues { it.value.toString() })
    }

    /** Whether the working copy differs from the persisted values. */
    val hasUnsavedChanges = editingValues != settings.ballValues.mapValues { it.value.toString() }

    /** The preset whose values exactly match the current working copy, or null if custom. */
    val matchingPreset = settings.ballValuePresets.find { preset ->
        preset.values.mapValues { it.value.toString() } == editingValues
    }

    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<BallValuePreset?>(null) }
    var newPresetName by remember { mutableStateOf("") }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /** Converts the current [editingValues] to a typed map and persists it. */
    fun applyChanges() {
        val finalValues = editingValues.mapValues { it.value.toIntOrNull() ?: 0 }
        onUpdateSettings { it.copy(ballValues = finalValues) }
    }

    // ── UI ───────────────────────────────────────────────────────────────────
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .displayCutoutPadding(),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Ball Value Rules",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = matchingPreset?.name ?: "Custom Configuration",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (matchingPreset != null)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Quick "Save as preset" shortcut in the app bar
                        FilledTonalIconButton(
                            onClick = { showSavePresetDialog = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Save as new preset",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // ── Preset Selector ──────────────────────────────────────────
                PresetSelectorBar(
                    presets = settings.ballValuePresets,
                    editingValues = editingValues,
                    onPresetSelected = { preset ->
                        editingValues = preset.values.mapValues { it.value.toString() }
                    },
                    onDeletePreset = { showDeleteConfirm = it }
                )

                // ── Scrollable Content ───────────────────────────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Table Preview
                    TablePreviewCard(editingValues = editingValues)

                    // Value Editors
                    BallValueEditorGrid(
                        editingValues = editingValues,
                        onValueChange = { num, newVal ->
                            editingValues = editingValues + (num to newVal)
                        }
                    )

                    // High-value warning
                    if (editingValues.values.any { (it.toIntOrNull() ?: 0) > 100 }) {
                        HighValueWarningBanner()
                    }

                    // Extra space so the sticky button doesn't overlap content
                    Spacer(Modifier.height(88.dp))
                }
            }
        }

        // ── Sticky action bar ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = hasUnsavedChanges,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Save as preset (secondary action)
                FilledTonalButton(
                    onClick = { showSavePresetDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Preset", fontWeight = FontWeight.Bold)
                }

                // Apply (primary action – stays on screen)
                Button(
                    onClick = { applyChanges() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Apply",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────
    if (showSavePresetDialog) {
        SavePresetDialog(
            presetName = newPresetName,
            onNameChange = { newPresetName = it },
            onConfirm = {
                if (newPresetName.isNotBlank()) {
                    val finalValues = editingValues.mapValues { it.value.toIntOrNull() ?: 0 }
                    val newPreset = BallValuePreset(newPresetName, finalValues, isDefault = false)
                    onUpdateSettings {
                        it.copy(
                            ballValuePresets = it.ballValuePresets + newPreset,
                            ballValues = finalValues
                        )
                    }
                    // Reset editing state to match the newly saved preset
                    editingValues = finalValues.mapValues { it.value.toString() }
                    showSavePresetDialog = false
                    newPresetName = ""
                }
            },
            onDismiss = {
                showSavePresetDialog = false
                newPresetName = ""
            }
        )
    }

    showDeleteConfirm?.let { preset ->
        DeletePresetConfirmDialog(
            presetName = preset.name,
            onConfirm = {
                onUpdateSettings { it.copy(ballValuePresets = it.ballValuePresets - preset) }
                showDeleteConfirm = null
            },
            onDismiss = { showDeleteConfirm = null }
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Private sub-composables
// ────────────────────────────────────────────────────────────────────────────

/**
 * Horizontally scrollable row of [FilterChip]s for selecting a ball-value preset.
 *
 * @param presets         All available presets from [AppSettings].
 * @param editingValues   The current working values (used to determine which chip is selected).
 * @param onPresetSelected Callback when a preset chip is tapped.
 * @param onDeletePreset  Callback when the delete icon on a custom preset is tapped.
 */
@Composable
private fun PresetSelectorBar(
    presets: List<BallValuePreset>,
    editingValues: Map<Int, String>,
    onPresetSelected: (BallValuePreset) -> Unit,
    onDeletePreset: (BallValuePreset) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                "PRESETS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { preset ->
                    val isSelected = editingValues == preset.values.mapValues { it.value.toString() }
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPresetSelected(preset) },
                        label = { Text(preset.name) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        trailingIcon = if (!preset.isDefault) {
                            {
                                IconButton(
                                    onClick = { onDeletePreset(preset) },
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Delete preset ${preset.name}",
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        } else null,
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }
        }
    }
}

/**
 * Card showing a live preview of all 15 pool balls with their current values.
 * Balls are arranged in three rows of five.
 *
 * @param editingValues The current working values used to render each [BallPreviewItem].
 */
@Composable
private fun TablePreviewCard(editingValues: Map<Int, String>) {
    val total = editingValues.values.sumOf { it.toIntOrNull() ?: 0 }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Table Preview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                // Total points pill
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "$total pts",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Ball grid – 3 rows × 5 balls
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                (1..15).chunked(5).forEach { rowBalls ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        rowBalls.forEach { num ->
                            val ballValue = (editingValues[num] ?: "0").toIntOrNull() ?: 0
                            BallPreviewItem(num, ballValue)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Grid of [ValueEditor] cards for all 15 balls, laid out in rows of three.
 *
 * @param editingValues The current working values.
 * @param onValueChange Callback invoked with the ball number and new string value.
 */
@Composable
private fun BallValueEditorGrid(
    editingValues: Map<Int, String>,
    onValueChange: (Int, String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "EDIT VALUES",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.sp
        )

        (1..15).chunked(3).forEach { chunk ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                chunk.forEach { num ->
                    ValueEditor(
                        number = num,
                        value = editingValues[num] ?: "0",
                        onValueChange = { newVal -> onValueChange(num, newVal) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Pad the last row if it has fewer than 3 items
                repeat(3 - chunk.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * A warning banner shown when any ball value exceeds 100.
 */
@Composable
private fun HighValueWarningBanner() {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "Warning: Some ball values are unusually high (> 100).",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}
