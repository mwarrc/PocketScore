package com.mwarrc.pocketscore.ui.feature.history.import_.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Sticky bottom action bar for the import screen.
 *
 * Displays:
 * - A contextual hint strip when duplicates are present (e.g. "✓ 3 new · 2 skipped").
 * - A Cancel outlined button.
 * - An Import primary button whose label updates with the new match count.
 * - A loading state with a spinner while the import is processing.
 *
 * @param onCancel       Called when the user taps Cancel.
 * @param onConfirm      Called when the user taps Import.
 * @param isProcessing   Whether an import is currently in progress.
 * @param newGamesCount  Number of new (non-duplicate) matches to be imported.
 * @param duplicateCount Number of matches that will be skipped.
 */
@Composable
fun ImportBottomBar(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    isProcessing: Boolean,
    newGamesCount: Int,
    duplicateCount: Int
) {
    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // Contextual hint strip
            if (duplicateCount > 0 && !isProcessing) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Text(
                        text = "✓ $newGamesCount new matches will be added · $duplicateCount skipped",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isProcessing
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier.weight(2f),
                    contentPadding = PaddingValues(vertical = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Importing…")
                    } else {
                        Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (newGamesCount > 0)
                                "Import $newGamesCount ${if (newGamesCount == 1) "Match" else "Matches"}"
                            else
                                "Import & Merge"
                        )
                    }
                }
            }
        }
    }
}
