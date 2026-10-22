package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Informational dialog explaining what Strict Mode does and how it affects gameplay.
 *
 * @param onDismissRequest Callback when the dialog is dismissed or acknowledged.
 */
@Composable
fun StrictModeInfoDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        icon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("About Strict Mode", fontWeight = FontWeight.Bold) },
        text = {
            Text(
                "Strict Mode is designed for fair, competitive play. It prevents unauthorized edits by ensuring only the active player's score can be modified and locking the roster during a game.\n\nWhen 'Lock Scoring Rules' is enabled, these protections are forced ON and cannot be disabled until the match is finalized or reset.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Got it", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

