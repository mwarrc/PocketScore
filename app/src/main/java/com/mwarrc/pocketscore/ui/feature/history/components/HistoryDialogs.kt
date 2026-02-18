package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameState

/**
 * Dialog for choosing how to resume a historical match.
 * 
 * @param onDismiss Callback to close the dialog.
 * @param onConfirm Callback with a boolean flag:
 *                  - `false`: Resume as a new entry (keep history).
 *                  - `true`: Replace existing entry (overwrite history).
 */
@Composable
fun ResumeGameDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Restore, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("How to Resume?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "Choose how you'd like to rejoin this match session.",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Option 1: Resume as new record
                Button(
                    onClick = { onConfirm(false) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Resume (New Entry)", fontWeight = FontWeight.Bold)
                        Text("Keep old data as a separate record", style = MaterialTheme.typography.labelSmall)
                    }
                }

                // Option 2: Overwrite existing record
                Button(
                    onClick = { onConfirm(true) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Replace Game", fontWeight = FontWeight.Bold)
                        Text("Overwrite and continue old session", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Go Back") }
        }
    )
}

/**
 * Confirmation dialog for permanently deleting a match record.
 * 
 * @param onDismiss Callback to close the dialog.
 * @param onConfirm Callback to proceed with deletion.
 */
@Composable
fun DeleteGameDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text("Delete This Record?") },
        text = { 
            Text("Are you sure you want to permanently delete this game record? This action cannot be undone.") 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Delete") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
