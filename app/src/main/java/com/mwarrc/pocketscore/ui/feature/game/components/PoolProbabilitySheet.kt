package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.ui.util.ImmersiveMode
import com.mwarrc.pocketscore.ui.util.BallColors

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
 * @param settings Application settings including pool config
 * @param onUpdateSettings Callback to update settings
 * @param onBallsOnTableChange Callback when the table state is updated
 * @param onDismiss Callback to close the sheet
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PoolProbabilitySheet(
    players: List<Player>,
    ballsOnTable: Set<Int>,
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onBallsOnTableChange: (Set<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    val ballValues = settings.ballValues
    val tableSum = remember(ballsOnTable, ballValues) {
        ballsOnTable.sumOf { ballValues[it] ?: 0 }
    }

    val activePlayers = remember(players) { players.filter { it.isActive } }
    val leaderScore = remember(activePlayers) { activePlayers.maxOfOrNull { it.score } ?: 0 }
    val hasLeader = remember(activePlayers) { activePlayers.any { it.score != 0 } }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // True screen height via DisplayMetrics — unaffected by immersive mode / inset changes
    val density = LocalDensity.current
    val context = LocalContext.current
    val trueScreenHeightDp = with(density) {
        context.resources.displayMetrics.heightPixels.toDp()
    }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutHeight = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val topSafeInset = maxOf(statusBarHeight, cutoutHeight)
    
    // Limit to just below the status bar with a small extra margin (8dp)
    // This ensures dialogs never overlap with system UI / dynamic island.
    val maxContentHeight = trueScreenHeightDp - topSafeInset - 8.dp

    // displayCutout inset remains valid even in immersive mode (statusBars returns 0)
    val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutTop = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val topInset = maxOf(statusBarTop, cutoutTop)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
                        .width(56.dp)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f))
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.32f)
    ) {
        ImmersiveMode()
        // heightIn(max) on the content Column caps the sheet's expanded anchor.
        // The sheet sizes itself to its content — so this IS what limits sheet height.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxContentHeight)
                .navigationBarsPadding()
        ) {
            var showRules by remember { mutableStateOf(false) }

            // Scroll protection: consume ALL leftover scroll AND fling velocity so
            // neither a slow drag nor a fast fling can accidentally dismiss the sheet.
            val nestedScrollConnection = remember {
                object : androidx.compose.ui.input.nestedscroll.NestedScrollConnection {
                    override fun onPostScroll(
                        consumed: androidx.compose.ui.geometry.Offset,
                        available: androidx.compose.ui.geometry.Offset,
                        source: androidx.compose.ui.input.nestedscroll.NestedScrollSource
                    ): androidx.compose.ui.geometry.Offset = available

                    override suspend fun onPostFling(
                        consumed: Velocity,
                        available: Velocity
                    ): Velocity = available // absorb remaining velocity → no accidental dismiss
                }
            }

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .nestedScroll(nestedScrollConnection),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // ── Header Section ──────────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Pool Probability",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                letterSpacing = (-0.5).sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "Live match analytics",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "$tableSum",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    "PTS LEFT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // ── Settings Quick Toggle ──────────────────────────────────
                item {
                    Surface(
                        modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(24.dp),
                        onClick = { onUpdateSettings { it.copy(autoRemovePoolBalls = !it.autoRemovePoolBalls) } }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (settings.autoRemovePoolBalls) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        if (settings.autoRemovePoolBalls) Icons.Default.CheckCircle else Icons.Default.Block,
                                        null,
                                        modifier = Modifier.size(20.dp),
                                        tint = if (settings.autoRemovePoolBalls) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Automatic removal",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (settings.autoRemovePoolBalls) "Syncs with score input" else "Manual selection only",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = settings.autoRemovePoolBalls,
                                onCheckedChange = { onUpdateSettings { s -> s.copy(autoRemovePoolBalls = it) } },
                                modifier = Modifier.scale(0.85f)
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // ── Points System Selection ─────────────────────────────────
                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth()) {
                        Text(
                            "POINTS SYSTEM",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        androidx.compose.foundation.lazy.LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(settings.ballValuePresets) { preset ->
                                val isSelected = ballValues == preset.values
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { 
                                        onUpdateSettings { it.copy(ballValues = preset.values) } 
                                    },
                                    label = { Text(preset.name, fontWeight = FontWeight.Bold) },
                                    shape = RoundedCornerShape(16.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                // ── Ball Selection Table ─────────────────────────────────────
                item {
                    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "TABLE STATE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { onBallsOnTableChange((1..15).toSet()) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Reset", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                                TextButton(
                                    onClick = { onBallsOnTableChange(emptySet()) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Icon(Icons.Default.ClearAll, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Clear", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(24.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    maxItemsInEachRow = 5
                                ) {
                                    (1..15).forEach { ball ->
                                        BallItem(
                                            number = ball,
                                            value = ballValues[ball] ?: 0,
                                            isOnTable = ballsOnTable.contains(ball),
                                            onClick = {
                                                val isOn = ballsOnTable.contains(ball)
                                                onBallsOnTableChange(if (isOn) ballsOnTable - ball else ballsOnTable + ball)
                                            },
                                            size = 48.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(32.dp))
                }

                // ── Win Probabilities ───────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "PLAYER STANDINGS",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                "${activePlayers.size} ACTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Use single column list for high-density analytics
                items(activePlayers.sortedByDescending { it.score }) { player ->
                    PlayerStatusRow(
                        player = player,
                        isLeader = hasLeader && player.score == leaderScore,
                        leaderScore = leaderScore,
                        tableSum = tableSum
                    )
                }
            }

            // ── Done Button --─
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(24.dp).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Done", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    val ballColor = BallColors.getBallColor(number)

    val needsDarkText = number in listOf(1, 9)

    Box(modifier = Modifier.size(size)) {
        Surface(
            onClick = {
                onClick()
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            },
            modifier = Modifier
                .fillMaxSize()
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
                        fontSize = (size.value * 0.4f).sp,
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

        // Points Value Badge Preview
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(18.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                .border(1.dp, MaterialTheme.colorScheme.surface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * High-density player status row for Pool analytics.
 * Premium M3 list-based design instead of bulky cards.
 */
@Composable
fun PlayerStatusRow(
    player: Player,
    isLeader: Boolean,
    leaderScore: Int,
    tableSum: Int
) {
    val potentialMax = player.score + tableSum
    // When tableSum == 0 the game is over; potentialMax == score, check still holds.
    val isOut = potentialMax < leaderScore

    val statusColor = when {
        isLeader -> MaterialTheme.colorScheme.primary
        isOut -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.secondary
    }

    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 4.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(if (isLeader) statusColor.copy(alpha = 0.08f) else Color.Transparent)
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player Color Indicator
            Box(
                modifier = Modifier
                    .size(width = 4.dp, height = 32.dp)
                    .clip(CircleShape)
                    .background(com.mwarrc.pocketscore.ui.theme.getMaterialPlayerColor(player.id))
            )

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    player.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Current Score: ${player.score}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when {
                        isLeader -> statusColor.copy(alpha = 0.15f)
                        isOut -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    }
                ) {
                    Text(
                        text = when {
                            isLeader -> "LEADER"
                            isOut -> "ELIMINATED"
                            else -> "CONTENDER"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (isOut) MaterialTheme.colorScheme.error else statusColor
                    )
                }

                Text(
                    text = when {
                        isLeader -> "Potential: $potentialMax"
                        isOut -> "-${leaderScore - potentialMax} from lead"
                        else -> "Need ${leaderScore - player.score} to tie"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isLeader || isOut) statusColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
        modifier = Modifier
            .statusBarsPadding()
            .displayCutoutPadding(),
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Clean minimalistic layout without instructional text
                
                // Use a fixed height box to ensure grid behaves in dialog (with padding above)
                Box(modifier = Modifier.padding(top = 8.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(30.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        gridItems(
                            items = (1..15).toList(),
                            key = { it }
                        ) { ball ->
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
                                size = 70.dp
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
