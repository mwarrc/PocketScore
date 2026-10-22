package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.domain.model.Player

/**
 * Expanded content section of a game history card.
 * 
 * Displays:
 * - Next turn player banner (if game is resumable)
 * - Ranked player score list with position badges
 * - Resume button (if not finalized) or match completion badge
 * - Quick stats and share action buttons
 * 
 * @param game The game state to display
 * @param winners List of winning players
 * @param nextTurnPlayer The player whose turn is next (if resumable)
 * @param isResumable Whether the game can be resumed
 * @param onResume Callback to resume the game
 * @param onViewDetails Callback to view detailed match analysis
 * @param onShare Callback to share the match record
 */
@Composable
fun GameHistoryCardExpandedContent(
    game: GameState,
    winners: List<Player>,
    nextTurnPlayer: Player?,
    isResumable: Boolean,
    onResume: () -> Unit,
    onViewDetails: () -> Unit,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Next turn indicator for resumable games
        if (nextTurnPlayer != null && isResumable) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSecondary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Next Turn: ${nextTurnPlayer.name}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
            modifier = Modifier.padding(vertical = 2.dp)
        )

        // Player scores leaderboard
        val sortedPlayers = game.players.sortedByDescending { it.score }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sortedPlayers.forEachIndexed { index, player ->
                val isWinner = winners.any { it.id == player.id }
                PlayerScoreRow(
                    rank = index + 1,
                    player = player,
                    isWinner = isWinner
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Resume or completion status
        if (!game.isFinalized) {
            ResumeGameButton(onClick = onResume)
        } else {
            MatchCompletedIndicator()
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ViewDetailsButton(
                onClick = onViewDetails,
                modifier = Modifier.weight(1f)
            )
            ShareMatchButton(
                onClick = onShare,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Displays a single player's score with rank badge in the expanded card view.
 */
@Composable
private fun PlayerScoreRow(
    rank: Int,
    player: Player,
    isWinner: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = if (isWinner) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Rank badge
                Surface(
                    shape = CircleShape,
                    color = if (isWinner) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "$rank",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isWinner) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }

                Text(
                    text = player.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${player.score} pts",
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    player.score < 0 -> MaterialTheme.colorScheme.error
                    isWinner -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isWinner) FontWeight.Bold else FontWeight.SemiBold
            )
        }
    }
}

/**
 * Button to resume an unfinished game.
 */
@Composable
private fun ResumeGameButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            Icons.Default.Restore,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Resume Game", fontWeight = FontWeight.Bold)
    }
}

/**
 * Indicator showing that a match has been completed and locked.
 */
@Composable
private fun MatchCompletedIndicator() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Match Completed & Locked",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Button to view detailed match analysis and timeline.
 */
@Composable
private fun ViewDetailsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            Icons.Default.Analytics,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Stats", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Button to share the match record.
 */
@Composable
private fun ShareMatchButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Icon(
            Icons.Default.Share,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Share", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
    }
}
