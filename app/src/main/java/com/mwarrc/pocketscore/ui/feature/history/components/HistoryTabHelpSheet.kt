package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.ui.feature.game.components.rememberGameSheetMaxHeight
import com.mwarrc.pocketscore.ui.util.ImmersiveMode

/**
 * Information data class representing a single feature explanation item in the tab guide.
 */
data class TabHelpItem(
    val icon: ImageVector,
    val title: String,
    val description: String
)

/**
 * Comprehensive modal bottom sheet displayed when tapping the Info ("i") icon in the History screen top bar.
 * Provides tailored explanations and instructions for whichever tab is currently active.
 *
 * @param tabTitle The title of the currently active history tab ("Matches", "Settle", "Ranks", "Players").
 * @param onDismiss Callback to dismiss the info sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTabHelpSheet(
    tabTitle: String,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()
    val maxContentHeight = rememberGameSheetMaxHeight()

    val (headerIcon, headerSubtitle, items) = when (tabTitle) {
        "Matches" -> Triple(
            Icons.Default.History,
            "Everything you need to master your match records and archives.",
            listOf(
                TabHelpItem(
                    icon = Icons.Default.Analytics,
                    title = "Deep-Dive Insights",
                    description = "Tap any completed game to inspect per-turn scoring momentum, point timelines, and in-depth player statistics."
                ),
                TabHelpItem(
                    icon = Icons.Default.CheckCircle,
                    title = "Multi-Select & Batch Actions",
                    description = "Long-press any match card to enter selection mode. Select multiple games to batch export (.pscore), archive, or delete together."
                ),
                TabHelpItem(
                    icon = Icons.Default.PlayArrow,
                    title = "Resume In-Progress Games",
                    description = "If a game was paused or left unfinished, tap 'Resume Game' on the match card to jump straight back into the action."
                ),
                TabHelpItem(
                    icon = Icons.Default.Archive,
                    title = "Safe Archiving",
                    description = "Archiving hides matches from your main history list while preserving all leaderboard rankings and player statistics."
                )
            )
        )
        "Settle" -> Triple(
            Icons.Default.Payments,
            "Fairly split table fees, food bills, or tournament stakes.",
            listOf(
                TabHelpItem(
                    icon = Icons.Default.Checklist,
                    title = "Match Selection",
                    description = "Choose which matches to include in the settlement calculation, then enter total costs to divide."
                ),
                TabHelpItem(
                    icon = Icons.Default.Balance,
                    title = "Even vs Weighted Split",
                    description = "Split costs equally among players, or weight the bill based on player points and game results according to your house rules."
                ),
                TabHelpItem(
                    icon = Icons.Default.SwapHoriz,
                    title = "Minimal Debt Transfers",
                    description = "PocketScore computes the optimal payment routes so everyone settles their balances with the fewest transactions."
                )
            )
        )
        "Ranks" -> Triple(
            Icons.Default.Leaderboard,
            "Track competitive standings, win rates, and all-time records.",
            listOf(
                TabHelpItem(
                    icon = Icons.Default.EmojiEvents,
                    title = "Podium Standings",
                    description = "Players are ranked by overall win percentage, average points, and match consistency. The top 3 players earn Gold, Silver, and Bronze badges."
                ),
                TabHelpItem(
                    icon = Icons.Default.FilterList,
                    title = "Activity Threshold",
                    description = "Only players with enough recorded matches qualify for the leaderboard podium to ensure fair representation."
                ),
                TabHelpItem(
                    icon = Icons.Default.VisibilityOff,
                    title = "Hidden Players",
                    description = "Players marked as 'Hidden from Leaderboard' in the Players tab retain their game records but stay off the public board."
                )
            )
        )
        else -> Triple(
            Icons.Default.Group,
            "Manage your player roster, visibility controls, and global records.",
            listOf(
                TabHelpItem(
                    icon = Icons.Default.Visibility,
                    title = "Leaderboard Visibility",
                    description = "Hide a player to keep them out of rankings. All their past matches remain completely safe in History."
                ),
                TabHelpItem(
                    icon = Icons.Default.Home,
                    title = "Home Picker Visibility",
                    description = "Turn off to prevent a player from cluttering the quick-start roster on the Home screen. Great for guests or friends who aren't currently around."
                ),
                TabHelpItem(
                    icon = Icons.Default.Edit,
                    title = "Global Renaming",
                    description = "Player names act as unique identifiers. Renaming a player updates every past match record globally so their history stays connected."
                ),
                TabHelpItem(
                    icon = Icons.Default.Delete,
                    title = "Remove from Roster",
                    description = "Takes someone off your active pickers while permanently preserving their existing match history and stats."
                ),
                TabHelpItem(
                    icon = Icons.Default.Check,
                    title = "Bulk Selection",
                    description = "Tap any player avatar to select multiple players and toggle their settings or remove them together."
                )
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
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
                .padding(bottom = 36.dp)
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
                        imageVector = headerIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$tabTitle Guide",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = headerSubtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Items list
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                items.forEach { item ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = item.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Action button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Text(
                    text = "Got It",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
