package com.mwarrc.pocketscore.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


// NOTE - this component is only to used in the v1 game screen - as many game support are added it should have a better sharable dynamic help comp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpDialog(
    onDismiss: () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Help & Guide",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Quick Start
                HelpSection(
                    icon = Icons.Default.PlayArrow,
                    title = "Quick Start",
                    content = "PocketScore helps you track scores for pool, snooker, or any turn-based game. Add players, enter scores, and let the app handle the rest!"
                )

                // Scoring
                HelpSection(
                    icon = Icons.Default.Add,
                    title = "How to Score",
                    content = "• Type the points in the input field\n• Tap '+' to add points or '-' to subtract\n• Press 0 then '+' to record a zero score\n• Turns advance automatically after scoring"
                )

                // Undo Feature
                HelpSection(
                    icon = Icons.AutoMirrored.Filled.Undo,
                    title = "Global Undo",
                    content = "Made a mistake? Use the Undo button (top right) to revert the last score entry. This will:\n• Remove the last points added\n• Return the turn to the previous player\n• Update the game history\n\nNote: You can only undo the very last action."
                )

                // Calculator
                HelpSection(
                    icon = Icons.Default.Calculate,
                    title = "Quick Calculator",
                    content = "Need to add up multiple balls? Use the Math tool:\n• Type expressions like: 12 + 45 - 34\n• Use the + and - buttons for quick input\n• See results in real-time\n• Perfect for complex pool scoring!"
                )

                // Manual Corrections
                HelpSection(
                    icon = Icons.Default.Edit,
                    title = "Manual Score Fixes",
                    content = "If you need to fix a score from several turns ago:\n1. Go through each player entering 0 points\n2. When you reach the player who needs fixing:\n   • Use the Calculator to compute the correct total\n   • Enter the corrected score\n3. Continue the game normally"
                )

                // Strict Mode
                HelpSection(
                    icon = Icons.Default.Lock,
                    title = "Strict Turn Mode",
                    content = "When enabled:\n• Only the current player can enter scores\n• Other players' cards are locked\n• Prevents accidental score changes\n• Great for competitive games!\n\nDisable in Settings for casual play where anyone can score."
                )

                // History & Audit
                HelpSection(
                    icon = Icons.Default.History,
                    title = "Game History",
                    content = "Track every move:\n• View all score changes\n• See who scored what and when\n• Identify zero inputs\n• Review undo actions\n• Perfect for settling disputes!"
                )

                // Player Management
                HelpSection(
                    icon = Icons.Default.Group,
                    title = "Managing Players",
                    content = "• Toggle players active/inactive mid-game\n• Inactive players won't appear on the board\n• Turn automatically jumps if current player is disabled\n• You must keep at least one player active"
                )

                // Layout Modes
                HelpSection(
                    icon = Icons.Default.GridView,
                    title = "View Modes",
                    content = "Grid Mode: See all players at once with a featured active player card at the top.\n\nList Mode: Vertical scrolling view with larger cards, perfect for many players."
                )

                // Tips
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Pro Tips",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "• Use the Calculator for complex calculations\n• Enable Strict Mode for tournament play\n• Check History to verify disputed scores\n• The leader gets a trophy icon 🏆",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Got It!")
                }
            }
        }
    }
}

@Composable
private fun HelpSection(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                icon,
                null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 36.dp, bottom = 16.dp)
        )
    }
}
