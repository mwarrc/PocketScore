package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog for saving the current ball values as a new preset.
 *
 * @param presetName Current value of the preset name input.
 * @param onNameChange Callback when the preset name input changes.
 * @param onConfirm Callback when the save button is clicked.
 * @param onDismiss Callback when the dialog is dismissed.
 */
@Composable
fun SavePresetDialog(
    presetName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Custom Preset", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Save this configuration for quick access later.",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = presetName,
                    onValueChange = onNameChange,
                    label = { Text("Preset Name") },
                    singleLine = true,
                    placeholder = { Text("e.g., Kenya Classic v2") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = presetName.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

/**
 * Dialog for confirming the deletion of a ball value preset.
 *
 * @param presetName The name of the preset to delete.
 * @param onConfirm Callback when the delete button is clicked.
 * @param onDismiss Callback when the dialog is dismissed.
 */
@Composable
fun DeletePresetConfirmDialog(
    presetName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Delete Preset?", fontWeight = FontWeight.Bold) },
        text = {
            Text("Are you sure you want to delete \"$presetName\"? This cannot be undone.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
