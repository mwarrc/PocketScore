package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.mwarrc.pocketscore.domain.model.Player

import androidx.compose.foundation.layout.ExperimentalLayoutApi

/**
 * Match Insight & Elimination Tracker overlay.
 * 
 * This bottom sheet allows users to track which pool balls are still on the table.
 * It automatically calculates if a player is mathematically eliminated based on 
 * the current leader's score and the points remaining on the table.
 * 
 * @param players List of players in the game
 * @param ballsOnTable Set of ball numbers (1-15) currently remaining
 * @param ballValues Map of ball numbers to their respective point values
 * @param onBallsOnTableChange Callback when the table state is updated
 * @param onDismiss Callback to close the sheet
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PoolProbabilitySheet(
    players: List<Player>,
    ballsOnTable: Set<Int>,
    ballValues: Map<Int, Int>,
    onBallsOnTableChange: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val tableSum = remember(ballsOnTable, ballValues) {
        ballsOnTable.sumOf { ballValues[it] ?: 0 }
    }

    val activePlayers = remember(players) { players.filter { it.isActive } }
    val leaderScore = remember(activePlayers) { activePlayers.maxOfOrNull { it.score } ?: 0 }
    val hasLeader = remember(activePlayers) { activePlayers.any { it.score != 0 } }

    // KEY FIX: Allow partial expansion for better gesture control
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { 
            // M3 Expressive drag handle
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .width(32.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding() // Keep content below punch hole
                .systemBarsPadding()
                .displayCutoutPadding()
                .navigationBarsPadding()
        ) {
            var showRules by remember { mutableStateOf(false) }

            // Compact Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // Title Row - More compact
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Pool Probability",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Match insights & elimination tracking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    // Points Badge - M3 Expressive
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "$tableSum",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "pts left",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Compact Ball Selection - M3 Expressive
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Table State",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                onClick = { showRules = !showRules },
                                color = if (showRules) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (showRules) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                tonalElevation = if (showRules) 2.dp else 0.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        if (showRules) Icons.Default.Info else Icons.Default.ArrowDropDown, 
                                        null, 
                                        modifier = Modifier.size(16.dp),
                                        tint = if (showRules) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        "Ball Values",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (showRules) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            
                            Spacer(Modifier.weight(1f))
                            
                            // Compact action buttons
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilledTonalButton(
                                    onClick = { onBallsOnTableChange((1..15).toSet()) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Refresh, 
                                        null, 
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reset", style = MaterialTheme.typography.labelSmall)
                                }
                                
                                OutlinedButton(
                                    onClick = { onBallsOnTableChange(emptySet()) },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    ),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        Icons.Default.ClearAll, 
                                        null, 
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text("Clear", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                        
                        // New Collapsible Scoring Guide
                        androidx.compose.animation.AnimatedVisibility(
                            visible = showRules,
                            enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                            exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(top = 12.dp)
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    "Scoring Reference",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.height(8.dp))
                                
                                // Clean, wrap-based list of values
                                androidx.compose.foundation.layout.FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    (1..15).forEach { num ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(com.mwarrc.pocketscore.ui.feature.settings.getBallColor(num))
                                            )
                                            Text(
                                                "$num = ${ballValues[num]}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        
                        // Compact ball grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(5),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items((1..15).toList()) { ball ->
                                val isOnTable = ballsOnTable.contains(ball)
                                val value = ballValues[ball] ?: 0
                                
                                BallItem(
                                    number = ball,
                                    value = value,
                                    isOnTable = isOnTable,
                                    onClick = {
                                        onBallsOnTableChange(
                                            if (isOnTable) ballsOnTable - ball else ballsOnTable + ball
                                        )
                                    },
                                    size = 46.dp
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // Player list header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Win Probabilities",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            "${activePlayers.size} active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
            }

            // CRITICAL FIX: Scrollable player list with gesture isolation
            // This prevents the sheet from closing when scrolling
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activePlayers.sortedByDescending { it.score }) { player ->
                    CompactPlayerStatusCard(
                        player = player,
                        isLeader = hasLeader && player.score == leaderScore,
                        leaderScore = leaderScore,
                        tableSum = tableSum
                    )
                }
            }

            // Bottom action bar - M3 Expressive
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Done", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Interactive pool ball icon.
 * 
 * Renders as a colored sphere with 3D-like shading when on table, 
 * or a desaturated/crossed-out icon when pocketed.
 * 
 * @param number The ball number (1-15)
 * @param value The point value assigned to this ball
 * @param isOnTable Whether the ball is still in play
 * @param onClick Toggle callback
 * @param size Display size
 */
@Composable
fun BallItem(
    number: Int,
    value: Int,
    isOnTable: Boolean,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 48.dp
) {
    val haptic = LocalHapticFeedback.current
    val ballColor = when (number) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFE91E63) // Pink
        4 -> Color(0xFF9C27B0) // Purple
        5 -> Color(0xFF673AB7) // Deep Purple
        6 -> Color(0xFF3F51B5) // Indigo
        7 -> Color(0xFF2196F3) // Blue
        8 -> Color(0xFF1A1A1A) // Black
        9 -> Color(0xFF009688) // Teal
        10 -> Color(0xFF4CAF50) // Green
        11 -> Color(0xFF8BC34A) // Light Green
        12 -> Color(0xFFCDDC39) // Lime
        13 -> Color(0xFFFFEB3B) // Yellow
        14 -> Color(0xFFFF9800) // Orange
        15 -> Color(0xFFFF5722) // Deep Orange
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val needsDarkText = number in listOf(1, 2, 13)

    Surface(
        onClick = { 
            onClick() 
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        },
        modifier = Modifier
            .size(size)
            .padding(2.dp),
        shape = CircleShape,
        color = if (isOnTable) ballColor else ballColor.copy(alpha = 0.15f),
        border = BorderStroke(
            width = if (isOnTable) 2.dp else 1.dp,
            color = if (isOnTable) Color.White.copy(alpha = 0.5f) else ballColor.copy(alpha = 0.3f)
        ),
        shadowElevation = if (isOnTable) 6.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Ball Gradient for 3D effect
            if (isOnTable) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.4f),
                                    Color.Transparent
                                ),
                                center = Offset(size.value * 0.3f, size.value * 0.3f)
                            )
                        )
                )
            }

            Text(
                number.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = (size.value * 0.4f).sp, // Scale font with size
                    shadow = if (isOnTable && !needsDarkText) Shadow(
                        color = Color.Black.copy(alpha = 0.5f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    ) else null
                ),
                color = when {
                    !isOnTable -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    needsDarkText -> Color.Black.copy(alpha = 0.8f)
                    else -> Color.White
                }
            )

            if (!isOnTable) {
                Icon(
                    Icons.Default.Block,
                    null,
                    modifier = Modifier.size(size * 0.8f),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Compact status indicator for a player within the Pool overlay.
 * 
 * Shows their current score, leader status, or elimination status/distance.
 */
@Composable
fun CompactPlayerStatusCard(
    player: Player,
    isLeader: Boolean,
    leaderScore: Int,
    tableSum: Int
) {
    val potentialMax = player.score + tableSum
    val isOut = potentialMax < leaderScore
    
    val statusColor = when {
        isLeader -> MaterialTheme.colorScheme.primary
        isOut -> MaterialTheme.colorScheme.error
        else -> Color(0xFF4CAF50)
    }
    
    val containerColor = when {
        isLeader -> MaterialTheme.colorScheme.primaryContainer
        isOut -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.tertiaryContainer
    }

    // M3 Expressive compact card
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        color = containerColor,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = if (isLeader) 3.dp else 1.dp,
        border = if (isOut) BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        player.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Score: ${player.score}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Status badge
                if (isLeader) {
                    Surface(
                        shape = CircleShape,
                        color = statusColor,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.CheckCircle, 
                                null, 
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else if (isOut) {
                    Surface(
                        shape = CircleShape,
                        color = statusColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Block, 
                                null, 
                                tint = statusColor,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Status info
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                when {
                    isOut -> {
                        val dist = leaderScore - potentialMax
                        Text(
                            "ELIMINATED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "Behind by $dist pts",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    isLeader -> {
                        Text(
                            "LEADING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "Max potential: $potentialMax",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    else -> {
                        val needed = leaderScore - player.score
                        Text(
                            "IN CONTENTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = statusColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            "Need $needed to tie",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Simplified dialog version of the pool ball selection.
 * 
 * Used for quick access from the TopBar when a full sheet might be too heavy.
 */
@Composable
fun QuickBallSelectDialog(
    ballsOnTable: Set<Int>,
    ballValues: Map<Int, Int>,
    onBallsOnTableChange: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val totalRemaining = remember(ballsOnTable, ballValues) {
        ballsOnTable.sumOf { ballValues[it] ?: 0 }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Analytics,
                        null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Table State",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "$totalRemaining pts",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Clean minimalistic layout without instructional text
                
                // Use a fixed height box to ensure grid behaves in dialog (with padding above)
                Box(modifier = Modifier.padding(top = 16.dp).height(420.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3), // 3 columns x 5 rows = 15 balls (Perfectly Even)
                        horizontalArrangement = Arrangement.spacedBy(24.dp), // Generous spacing
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items((1..15).toList()) { ball ->
                            val isOnTable = ballsOnTable.contains(ball)
                            BallItem(
                                number = ball,
                                value = ballValues[ball] ?: 0,
                                isOnTable = isOnTable,
                                onClick = {
                                    onBallsOnTableChange(
                                        if (isOnTable) ballsOnTable - ball else ballsOnTable + ball
                                    )
                                },
                                size = 64.dp // Bigger touch target
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}
