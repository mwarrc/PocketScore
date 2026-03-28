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
import com.mwarrc.pocketscore.ui.util.ImmersiveMode
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext

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
    // --- Dynamic Safe Area Height Calculation ---
    val density = LocalDensity.current
    val context = LocalContext.current
    val trueScreenHeightDp = with(density) {
        context.resources.displayMetrics.heightPixels.toDp()
    }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutHeight = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val topSafeInset = maxOf(statusBarHeight, cutoutHeight)
    
    val maxContentHeight = trueScreenHeightDp - topSafeInset - 8.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        ImmersiveMode()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxContentHeight)
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
                "TABLE & MATCH INTELLIGENCE",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            HelpItem(
                icon = Icons.Default.AutoAwesome,
                title = "Auto-Remove & Probabilities",
                description = "In pool games, scoring perfectly matching points auto-removes the ball from the table. Tap 'Balls Remaining' to recalculate mathematically impossible comebacks."
            )

            HelpItem(
                icon = Icons.Default.EmojiEvents,
                title = "Early Winner Detection",
                description = "The app computes 'Last Man Standing' and early clearances. If players trailing safely can't catch the leader using the remaining balls, the match automatically ends."
            )

            HelpItem(
                icon = Icons.Default.Palette,
                title = "Dynamic Live Spotlights",
                description = "Watch the leaderboards glow! Leaders receive a gold spotlight and badge. Losers or eliminated players receive a red alert. Ties are dynamically tracked in real time."
            )

            Text(
                "ADVANCED SCORING WORKFLOWS",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            HelpItem(
                icon = Icons.Default.Keyboard,
                title = "Rapid Numpad Pinning",
                description = "Instead of opening and closing the keyboard per player, open the custom numpad and tap the 'Pin' icon. The numpad remains open so you can shotgun scores consecutively!"
            )

            HelpItem(
                icon = Icons.Default.Calculate,
                title = "Smart Score Calculation",
                description = "Need to enter a complex score like '7+1+8'? Open the Quick Calculator tool from the bottom bar, sum it up, and tap a player's row to apply it instantly."
            )

            HelpItem(
                icon = Icons.AutoMirrored.Filled.Undo,
                title = "Chronological Quick Undo",
                description = "Made a mistake? Just hit Undo. Every event is mapped, so it won't just subtract points — it physically reverses the turn order and recalculates table balls too."
            )

            Text(
                "SECURITY & PROGRESS PRESERVATION",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            HelpItem(
                icon = Icons.Default.Lock,
                title = "Strict Turn Security",
                description = "Enable Strict Mode in settings. The scoreboard completely disables scoring for inactive players, meaning nobody can accidentally hit the wrong row during intense matches."
            )

            HelpItem(
                icon = Icons.Default.Save,
                title = "Guest Override Capability",
                description = "Playing in Guest Mode to avoid clutter? If an unexpected legendary game happens, don't worry! Toggle 'Override Guest Status' in the final match screen to force-save the history permanently."
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
