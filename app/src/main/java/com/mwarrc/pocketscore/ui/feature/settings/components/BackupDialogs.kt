package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Dialog for creating a new backup snapshot.
 *
 * @param snapshotName Current value of the snapshot name input.
 * @param onNameChange Callback when the snapshot name input changes.
 * @param onConfirm Callback when the create button is clicked.
 * @param onDismiss Callback when the dialog is dismissed or cancel is clicked.
 */
@Composable
fun CreateSnapshotDialog(
    snapshotName: String,
    onNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Create Snapshot",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Save a point-in-time version of all your current records.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = snapshotName,
                    onValueChange = onNameChange,
                    label = { Text("Snapshot Name") },
                    placeholder = { Text("e.g. Before merging with Bob") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

/**
 * Dialog for confirming the deletion of a snapshot.
 *
 * @param snapshotName The name of the snapshot to delete.
 * @param onConfirm Callback when the delete button is clicked.
 * @param onDismiss Callback when the dialog is dismissed or cancel is clicked.
 */
@Composable
fun DeleteSnapshotDialog(
    snapshotName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.DeleteForever,
                        null,
                        modifier = Modifier.size(28.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        title = {
            Text(
                "Delete Snapshot?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                "Are you sure you want to permanently delete '$snapshotName'? This action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
