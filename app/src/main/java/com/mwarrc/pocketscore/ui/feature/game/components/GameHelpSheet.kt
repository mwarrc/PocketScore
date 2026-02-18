package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * An educational bottom sheet that explains the game's unique features.
 * 
 * Includes sections on pool probabilities, spotlights, scoring tools, 
 * and turn-based security rules.
 * 
 * @param onDismiss Callback to close the guide
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHelpSheet(
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .systemBarsPadding()
                .displayCutoutPadding()
                .navigationBarsPadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.Lightbulb,
                        null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        "Game Guide",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Master the PocketScore scoreboard",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Sections
            Text(
                "MATCH INSIGHTS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HelpItem(
                icon = Icons.Default.Analytics,
                title = "Pool Probabilities",
                description = "Track which balls are on the table. The app automatically calculates remaining points and warns you when a win is mathematically impossible."
            )

            HelpItem(
                icon = Icons.Default.Palette,
                title = "Live Spotlights",
                description = "Winners (Leaders) and Underdogs (Losers) are dynamically highlighted with premium badges and glowing card backgrounds."
            )

            Text(
                "SCORING TOOLS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            HelpItem(
                icon = Icons.Default.Calculate,
                title = "Smart Math Tool",
                description = "Enter complex strings like '1+7+8' directly. The app calculates the sum and lets you apply it to any player with one tap."
            )

            HelpItem(
                icon = Icons.AutoMirrored.Filled.Undo,
                title = "Contextual Undo",
                description = "Mistakes happen. Undo doesn't just revert points; it returns the turn to the right person and updates all probability data."
            )

            Text(
                "RULES & PRIVACY",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            HelpItem(
                icon = Icons.Default.Lock,
                title = "Strict Turn Security",
                description = "Lock scoring to the current player only. Prevents manual tampering and ensures a legitimate competitive session."
            )

            HelpItem(
                icon = Icons.Default.Groups,
                title = "Guest Sessions",
                description = "Perfect for public scoreboards or warmups. Play without cluttering your history with temporary rounds."
            )

            Spacer(Modifier.height(16.dp))

            // Pro Tip Card
            Surface(
                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.Star,
                        null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Pro Tip: Valid Passes",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            "In Strict Mode, you must record a '0' score to officially pass your turn. This ensures the match history reflects the actual flow of play.",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Got it, let's play!", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun HelpItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}
