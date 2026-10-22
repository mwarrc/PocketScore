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
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Restore, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = { 
            Text(
                "Resume Match?", 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Text(
                "Restoring this match will return you to the Game Screen. Previous scores and player standings will be preserved exactly where you left off.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Resume Now", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { 
                Text(
                    "Close", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ) 
            }
        },
        properties = androidx.compose.ui.window.DialogProperties(
            usePlatformDefaultWidth = true
        )
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

/**
 * Critical confirmation dialog for bulk deleting multiple matches.
 * 
 * @param count Number of matches to be deleted.
 * @param onDismiss Callback to close the dialog.
 * @param onConfirm Callback to proceed with deletion.
 */
@Composable
fun DeleteMultipleGamesDialog(
    count: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.DeleteForever, 
                        null, 
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        title = { 
            Text(
                "Delete $count Records?", 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            ) 
        },
        text = {
            Text(
                "You are about to delete $count selected match(es) permanently. These records will be erased from history and rankings. This action is irreversible.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete $count Records", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) { 
                Text(
                    "Close", 
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ) 
            }
        }
    )
}
