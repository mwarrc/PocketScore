package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.GameState
import com.mwarrc.pocketscore.domain.model.Player

/**
 * Expanded content section of a game history card.
 * 
 * Displays:
 * - Next turn player (if game is resumable)
 * - Player scores in descending order
 * - Resume button (if not finalized) or completion status
 * - View details button
 * - Share button
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
        modifier = Modifier.padding(
            start = 72.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 16.dp
        )
    ) {
        // Next turn indicator for resumable games
        if (nextTurnPlayer != null && isResumable) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Next Turn: ${nextTurnPlayer.name}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )

        // Player scores list
        game.players.sortedByDescending { it.score }.forEach { player ->
            PlayerScoreRow(
                player = player,
                isWinner = winners.any { it.id == player.id }
            )
        }

        Spacer(Modifier.height(16.dp))

        // Resume or completion status
        if (!game.isFinalized) {
            ResumeGameButton(onClick = onResume)
        } else {
            MatchCompletedIndicator()
        }

        // Action buttons
        ViewDetailsButton(onClick = onViewDetails)
        
        Spacer(Modifier.height(8.dp))
        
        ShareMatchButton(onClick = onShare)
    }
}

/**
 * Displays a single player's score in the expanded card view.
 */
@Composable
private fun PlayerScoreRow(
    player: Player,
    isWinner: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            player.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            "${player.score}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (player.score < 0) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal
        )
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
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            contentColor = MaterialTheme.colorScheme.primary
        ),
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
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
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
private fun ViewDetailsButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    ) {
        Icon(
            Icons.Default.Analytics,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("View Detailed Records", fontWeight = FontWeight.Bold)
    }
}

/**
 * Button to share the match record.
 */
@Composable
private fun ShareMatchButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(
            Icons.Default.IosShare,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text("Share Match Record", fontWeight = FontWeight.Bold)
    }
}
