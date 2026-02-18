package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Dialog for adding a new player to the roster.
 * 
 * Features:
 * - Input validation (non-empty, unique name)
 * - Error messaging for duplicate names
 * - Trimmed input handling
 * 
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback with validated player name when confirmed
 * @param existingNames List of existing player names for duplicate checking
 */
@Composable
fun AddPlayerDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    existingNames: List<String>
) {
    var newFriendName by remember { mutableStateOf("") }
    
    val trimmed = newFriendName.trim()
    val alreadyExists = existingNames.any { it.equals(trimmed, ignoreCase = true) }
    val isValid = trimmed.isNotEmpty() && !alreadyExists

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Player") },
        text = {
            Column {
                OutlinedTextField(
                    value = newFriendName,
                    onValueChange = { newValue -> 
                        newFriendName = newValue.filter { it.isLetterOrDigit() }
                    },
                    label = { Text("Display Name") },
                    singleLine = true,
                    isError = alreadyExists,
                    supportingText = if (alreadyExists) {
                        { Text("This name is already in your roster") }
                    } else null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(trimmed)
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(12.dp)
            ) { 
                Text("Add to Roster") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel") 
            }
        }
    )
}

/**
 * Confirmation dialog for removing a player from the roster.
 * 
 * Explains that removal only hides the player from the active roster picker,
 * while preserving their match history.
 * 
 * @param playerName Name of the player to remove
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback when removal is confirmed
 */
@Composable
fun RemovePlayerDialog(
    playerName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Icon(
                Icons.Default.Delete, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            ) 
        },
        title = { Text("Remove from Roster?") },
        text = { 
            Text(
                "Removing $playerName will hide them from the 'Active Roster' picker, " +
                "but their match history will still be safe."
            ) 
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) { 
                Text("Remove") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel") 
            }
        }
    )
}

/**
 * Dialog for renaming a player globally across all records.
 * 
 * Features:
 * - Warning about global rename impact
 * - Duplicate name validation
 * - Prevents renaming to same name
 * 
 * @param currentName Current player name
 * @param onDismiss Callback when dialog is dismissed
 * @param onConfirm Callback with new name when confirmed
 * @param existingNames List of existing player names for duplicate checking
 */
@Composable
fun RenamePlayerDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    existingNames: List<String>
) {
    var renameValue by remember { mutableStateOf(currentName) }
    
    val trimmed = renameValue.trim()
    val isTaken = existingNames.any { 
        it.trim().equals(trimmed, ignoreCase = true) && 
        !it.trim().equals(currentName.trim(), ignoreCase = true) 
    }
    val isValid = !isTaken && trimmed.isNotEmpty() && trimmed != currentName
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { 
            Icon(
                Icons.Default.Warning, 
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary
            ) 
        },
        title = { Text("Rename Player") },
        text = {
            Column {
                // Warning message
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        "Names are unique IDs. Renaming will update all past match " +
                        "records to match this new name.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                
                // Input field
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { newValue ->
                        renameValue = newValue.filter { it.isLetterOrDigit() }
                    },
                    label = { Text("Update Name") },
                    singleLine = true,
                    isError = isTaken,
                    supportingText = if (isTaken) { 
                        { Text("Name already taken") } 
                    } else null,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onConfirm(trimmed)
                    }
                },
                enabled = isValid,
                shape = RoundedCornerShape(12.dp)
            ) { 
                Text("Update Records") 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text("Cancel") 
            }
        }
    )
}
