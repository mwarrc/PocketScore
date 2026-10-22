package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Confirmation shown when a score entry is worth more than the highest-value ball
 * in the player's current point system.
 *
 * The threshold is fully dynamic: it is derived from the active ball-value map, so
 * a default setup warns above 17, while a custom system such as `2 = 39` warns above
 * 39. The dialog exists to catch typos (a stray digit turning 6 into 60) without
 * ever blocking a legitimate custom score.
 *
 * @param enteredValue The absolute score the player tried to enter
 * @param maxBallValue The highest points any single ball is worth in the active setup
 * @param hapticEnabled Whether to vibrate when the dialog appears
 * @param onConfirm Proceed with the score as entered
 * @param onCancel Discard the entry
 * @param onConfirmWithSuppression Proceed and never ask again (persisted)
 */
@Composable
fun ExceedsMaxValueDialog(
    enteredValue: Int,
    maxBallValue: Int,
    hapticEnabled: Boolean,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onConfirmWithSuppression: () -> Unit
) {
    var dontShowAgain by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onCancel,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                "Value above your point system",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "You entered $enteredValue, but the highest value in your current " +
                        "point system is $maxBallValue.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "This is usually a typo. Continue only if this score is intentional.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dontShowAgain = !dontShowAgain },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it }
                    )
                    Text(
                        "Don't show this again",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (dontShowAgain) onConfirmWithSuppression() else onConfirm()
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (dontShowAgain) "Continue & don't ask" else "Continue anyway")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Edit entry")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
