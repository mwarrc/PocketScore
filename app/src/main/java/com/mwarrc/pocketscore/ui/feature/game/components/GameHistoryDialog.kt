package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mwarrc.pocketscore.R
import com.mwarrc.pocketscore.core.time.formatTimeAgo
import com.mwarrc.pocketscore.domain.model.GameEvent
import com.mwarrc.pocketscore.domain.model.GameEventType
import com.mwarrc.pocketscore.ui.theme.getMaterialPlayerColor
import com.mwarrc.pocketscore.ui.util.BallColors
import com.mwarrc.pocketscore.ui.util.ImmersiveMode

/** The two views available inside the history dialog. */
private enum class HistoryView { TIMELINE, BALLS }

/**
 * A scoring entry during the match, derived from the event log.
 *
 * [ball] is the resolved pool ball, or `null` when the score does not correspond
 * to any ball in the player's setup (e.g. a manual +20 or a foul deduction).
 */
private data class PottedBall(
    val ball: Int?,
    val playerName: String,
    val points: Int,
    val timestamp: Long,
    val eventId: String,
    val previousScore: Int? = null,
    val newScore: Int? = null
)

/**
 * How an event's score should be drawn.
 *
 * [Known] carries the exact ball number. [SharedValue] means the points value is
 * shared by more than one ball in the player's ball-value setup, so it must never
 * be shown as a specific numbered/coloured ball - only the value itself.
 * [NonBall] means the score maps to no ball at all (manual entry or foul), drawn
 * with a distinct dashed marker so it is never mistaken for a pocketed ball.
 */
private sealed interface BallVisual {
    data class Known(val number: Int) : BallVisual
    data class SharedValue(val points: Int, val ballCount: Int) : BallVisual
    data class NonBall(val points: Int) : BallVisual
}

/**
 * An audit dialog showing the chronological log of game events.
 *
 * Two views:
 *  - **Timeline:** every event, filterable by a horizontally scrolling row of
 *    player chips.
 *  - **Pocketed:** the balls in the order they were potted, with who potted them.
 *
 * Score entries render the actual pool ball that was pocketed instead of a bare
 * "+N" badge. Scores that match no ball (manual entries, fouls) use a distinct
 * dashed marker in both views so they are never mistaken for a ball.
 *
 * @param events List of events for the current match
 * @param playerNames Names of active players to build the filter list
 * @param ballValues Current ball-to-points mapping used to resolve a ball from a score
 * @param onDismiss Callback to close the dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHistoryDialog(
    events: List<GameEvent>,
    playerNames: List<String>,
    ballValues: Map<Int, Int> = emptyMap(),
    onDismiss: () -> Unit
) {
    var view by remember { mutableStateOf(HistoryView.TIMELINE) }
    var selectedPlayer by remember { mutableStateOf<String?>(null) }
    val pottedBalls = remember(events, ballValues) { derivePottedBalls(events, ballValues) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .statusBarsPadding()
                .displayCutoutPadding()
        ) {
            ImmersiveMode()
            Column(modifier = Modifier.fillMaxSize()) {
                // --- Header ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 12.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Match History",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Audit every point change",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                // --- Sleek, borderless View switcher (fixes icon/text overlap) ---
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp)
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val tabWidth = maxWidth / 2
                        val indicatorOffset by animateDpAsState(
                            targetValue = if (view == HistoryView.TIMELINE) 0.dp else tabWidth,
                            animationSpec = spring(
                                dampingRatio = 0.85f,
                                stiffness = Spring.StiffnessMediumLow
                            ),
                            label = "history_tab_indicator"
                        )

                        // Smooth sliding indicator pill
                        Box(
                            modifier = Modifier
                                .offset(x = indicatorOffset)
                                .width(tabWidth)
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            HistoryViewTab(
                                selected = view == HistoryView.TIMELINE,
                                onClick = { view = HistoryView.TIMELINE },
                                label = "Timeline",
                                count = events.size,
                                modifier = Modifier.weight(1f),
                                icon = { tint ->
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = tint
                                    )
                                }
                            )

                            HistoryViewTab(
                                selected = view == HistoryView.BALLS,
                                onClick = { view = HistoryView.BALLS },
                                label = "Pocketed",
                                count = pottedBalls.size.takeIf { it > 0 },
                                modifier = Modifier.weight(1f),
                                icon = { tint ->
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_pool_balls),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = tint
                                    )
                                }
                            )
                        }
                    }
                }

                when (view) {
                    HistoryView.TIMELINE -> TimelineView(
                        events = events,
                        playerNames = playerNames,
                        selectedPlayer = selectedPlayer,
                        onSelectPlayer = { selectedPlayer = it },
                        ballValues = ballValues
                    )

                    HistoryView.BALLS -> PocketedBallsView(
                        events = events,
                        ballValues = ballValues,
                        pottedBalls = pottedBalls
                    )
                }
            }
        }
    }
}

/**
 * Tab button for the Timeline / Pocketed switcher.
 *
 * Deliberately built on [Modifier.selectable] with `indication = null` instead of
 * a clickable [Surface]. The stock M3 press indication painted a dark, boxy
 * ripple inside the pill on tap, which read as a glitch. A short animated press
 * tint gives feedback without that rectangle, and the selected state still
 * animates smoothly (background, content colour, and elevation).
 */
@Composable
private fun HistoryViewTab(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    icon: @Composable (Color) -> Unit,
    modifier: Modifier = Modifier,
    count: Int? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tab_scale"
    )

    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 150),
        label = "history_tab_fg"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 10.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon(contentColor)
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
            if (count != null) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    shape = CircleShape,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ) {
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineView(
    events: List<GameEvent>,
    playerNames: List<String>,
    selectedPlayer: String?,
    onSelectPlayer: (String?) -> Unit,
    ballValues: Map<Int, Int>
) {
    val filteredEvents = remember(selectedPlayer, events) {
        val source = if (selectedPlayer == null) events else events.filter { it.playerName == selectedPlayer }
        source.reversed()
    }

    val ballVisuals = remember(events, ballValues) { resolveBallVisuals(events, ballValues) }

    Column(modifier = Modifier.fillMaxSize()) {
        // --- Player filter (horizontally scrollable chips: every player is
        // reachable with a single swipe, no dropdown needed) ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item(key = "all_players") {
                PlayerFilterChip(
                    label = "All players",
                    dotColor = MaterialTheme.colorScheme.primary,
                    selected = selectedPlayer == null,
                    onClick = { onSelectPlayer(null) }
                )
            }
            itemsIndexed(playerNames) { _, name ->
                PlayerFilterChip(
                    label = name,
                    dotColor = getMaterialPlayerColor(name),
                    selected = selectedPlayer == name,
                    onClick = { onSelectPlayer(name) }
                )
            }
        }

        Text(
            text = "${filteredEvents.size} ${if (filteredEvents.size == 1) "event" else "events"}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.End,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 4.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            if (filteredEvents.isEmpty()) {
                EmptyHistoryState(
                    message = if (selectedPlayer == null) "No events recorded yet." else "$selectedPlayer hasn't scored yet."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEvents, key = { it.id }) { event ->
                        ImprovedEventItem(
                            event = event,
                            ballValues = ballValues,
                            visual = ballVisuals[event.id]
                        )
                    }
                }
            }
        }
    }
}

/** Filter chip used in the horizontally-scrollable player filter row. */
@Composable
private fun PlayerFilterChip(
    label: String,
    dotColor: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        border = null,
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurface,
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    )
}

@Composable
private fun PocketedBallsView(
    events: List<GameEvent>,
    ballValues: Map<Int, Int>,
    pottedBalls: List<PottedBall> = remember(events, ballValues) { derivePottedBalls(events, ballValues) }
) {
    val ballVisuals = remember(events, ballValues) { resolveBallVisuals(events, ballValues) }

    if (pottedBalls.isEmpty()) {
        EmptyHistoryState(
            message = "No scores yet.\nPotted balls and manual scores appear here."
        )
        return
    }

    val ballCount = pottedBalls.count { it.ball != null }
    val otherCount = pottedBalls.size - ballCount

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Scoring order",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                buildString {
                    append("$ballCount ${if (ballCount == 1) "ball" else "balls"}")
                    if (otherCount > 0) append(" · $otherCount other")
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(pottedBalls, key = { _, ball -> ball.eventId }) { index, potted ->
                PottedBallRow(order = index + 1, potted = potted, ballVisuals = ballVisuals)
            }
        }
    }
}

@Composable
private fun PottedBallRow(order: Int, potted: PottedBall, ballVisuals: Map<String, BallVisual>) {
    val playerColor = getMaterialPlayerColor(potted.playerName)
    val visual = ballVisuals[potted.eventId]
        ?: potted.ball?.let { BallVisual.Known(it) }
        ?: BallVisual.NonBall(potted.points)
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = playerColor.copy(alpha = if (isDark) 0.14f else 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Order badge ("1", "2", …)
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "$order",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                BallVisual(visual = visual, size = 34.dp)
            }

            Column(modifier = Modifier.weight(1f)) {
                val title = if (potted.previousScore != null && potted.newScore != null) {
                    "${potted.playerName}: ${potted.previousScore} ➝ ${potted.newScore}"
                } else {
                    potted.playerName
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val description = when (visual) {
                    is BallVisual.Known -> "Ball ${visual.number}"
                    is BallVisual.SharedValue -> "${visual.points} pts"
                    is BallVisual.NonBall -> "Not a ball"
                }
                Text(
                    text = "$description · ${formatTimeAgo(potted.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = playerColor.copy(alpha = 0.16f)
            ) {
                Text(
                    text = "+${potted.points}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = playerColor,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }
    }
}

/**
 * Derives the ordered list of scoring entries from the event log.
 *
 * Positive score events add an entry. [PottedBall.ball] is resolved from
 * [GameEvent.autoRemovedBall] when available, otherwise from the ball-value map,
 * and stays `null` for scores that map to no ball (manual +20, etc.) so they can
 * be drawn with the non-ball marker. An undo removes the most recent matching
 * entry again.
 */
private fun derivePottedBalls(events: List<GameEvent>, ballValues: Map<Int, Int>): List<PottedBall> {
    val potted = mutableListOf<PottedBall>()

    events.forEach { event ->
        when (event.type) {
            GameEventType.SCORE -> {
                if (event.points > 0) {
                    val ball = event.autoRemovedBall
                        ?: ballValues.entries.firstOrNull { it.value == event.points }?.key
                    // With no ball setup at all (ball management disabled) keep the
                    // legacy behaviour: the list only ever held resolvable balls.
                    if (ball != null || ballValues.isNotEmpty()) {
                        potted.add(
                            PottedBall(
                                ball = ball,
                                playerName = event.playerName,
                                points = event.points,
                                timestamp = event.timestamp,
                                eventId = event.id,
                                previousScore = event.previousScore,
                                newScore = event.newScore
                            )
                        )
                    }
                }
            }

            GameEventType.UNDO -> {
                val restored = event.autoRemovedBall
                val index = potted.indexOfLast { ball ->
                    ball.playerName == event.playerName && (restored == null || ball.ball == restored)
                }
                if (index >= 0) potted.removeAt(index)
            }

            else -> Unit
        }
    }

    return potted
}

/**
 * Resolves how each event's ball should be drawn, based purely on the player's
 * ball-value setup - not on what is left on the table.
 *
 * If a points value is shared by more than one ball in [ballValues] (for example
 * 3/4/5/6 all worth 6), that value can never be attributed to a specific ball, so
 * it is always drawn as the shared-value marker showing just the value.
 *
 * Resolution order:
 *  1. Negative scores - non-ball marker (fouls aren't balls).
 *  2. Value shared by 2+ balls in the setup - shared-value marker.
 *  3. [GameEvent.autoRemovedBall] - exact ball.
 *  4. Unique value match in the setup - exact ball.
 *  5. No match at all - non-ball marker (manual score).
 *
 * @param events Chronological event log
 * @param ballValues Current ball-to-points mapping
 * @return Map of event ID to its resolved [BallVisual]
 */
private fun resolveBallVisuals(
    events: List<GameEvent>,
    ballValues: Map<Int, Int>
): Map<String, BallVisual> {
    // How many balls in the setup carry each value. Used to decide shared vs unique.
    val ballCountByValue = ballValues.values.groupingBy { it }.eachCount()

    return events.mapNotNull { event ->
        if (event.type != GameEventType.SCORE || event.points == 0) return@mapNotNull null

        // No ball setup (ball management disabled): keep the legacy generic icon
        // rather than flagging every score as "not a ball".
        if (ballValues.isEmpty()) return@mapNotNull null

        // A deduction can never be attributed to a ball on the table.
        if (event.points < 0) return@mapNotNull (event.id to BallVisual.NonBall(event.points))

        val sharedCount = ballCountByValue[event.points] ?: 0
        val visual = when {
            // Value is shared by several balls: always the shared marker, never a ball.
            sharedCount > 1 -> BallVisual.SharedValue(event.points, sharedCount)
            event.autoRemovedBall != null -> BallVisual.Known(event.autoRemovedBall)
            else -> ballValues.entries.firstOrNull { it.value == event.points }?.key
                ?.let { BallVisual.Known(it) }
        }

        event.id to (visual ?: BallVisual.NonBall(event.points))
    }.toMap()
}

private fun ordinal(number: Int): String {
    val suffix = when {
        number % 100 in 11..13 -> "th"
        number % 10 == 1 -> "st"
        number % 10 == 2 -> "nd"
        number % 10 == 3 -> "rd"
        else -> "th"
    }
    return "$number$suffix"
}

@Composable
private fun EmptyHistoryState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.History,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ImprovedEventItem(event: GameEvent, ballValues: Map<Int, Int>, visual: BallVisual?) {
    val playerColor = getMaterialPlayerColor(event.playerName)
    // Follow the app theme (MaterialTheme) instead of the system setting.
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val containerColor by animateColorAsState(
        targetValue = when {
            event.type == GameEventType.UNDO -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            event.type == GameEventType.STATUS_CHANGE -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            else -> playerColor.copy(alpha = if (isDark) 0.14f else 0.08f)
        },
        label = "history_item_container"
    )

    val isScoreLike = event.type == GameEventType.SCORE || event.type == GameEventType.UNDO
    val icon = when {
        event.type == GameEventType.UNDO -> Icons.AutoMirrored.Filled.Undo
        event.type == GameEventType.STATUS_CHANGE -> Icons.Default.Warning
        else -> Icons.Default.History
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Leading visual (34.dp size matching Pocketed list)
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    event.type == GameEventType.SCORE && visual != null -> BallVisual(
                        visual = visual,
                        size = 34.dp,
                        modifier = Modifier
                    )
                    else -> Surface(
                        shape = CircleShape,
                        color = if (event.type == GameEventType.UNDO) MaterialTheme.colorScheme.error else playerColor,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = if (event.type == GameEventType.UNDO) MaterialTheme.colorScheme.onError else Color.White
                            )
                        }
                    }
                }
            }

            // Text Info (2 compact lines matching Pocketed list)
            Column(modifier = Modifier.weight(1f)) {
                val title = when (event.type) {
                    GameEventType.SCORE -> {
                        if (event.previousScore != null && event.newScore != null) {
                            "${event.playerName}: ${event.previousScore} ➝ ${event.newScore}"
                        } else {
                            event.playerName
                        }
                    }
                    GameEventType.UNDO -> "Action Undone"
                    GameEventType.CORRECTION -> "Correction: ${event.playerName}"
                    GameEventType.STATUS_CHANGE -> "Rule Change"
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (event.type == GameEventType.UNDO) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val description = when (event.type) {
                    GameEventType.SCORE -> when (visual) {
                        is BallVisual.Known -> "Pocketed ball ${visual.number}"
                        is BallVisual.SharedValue -> "Shared worth ${visual.points} pts"
                        is BallVisual.NonBall -> if (event.points > 0) {
                            "Added ${event.points} pts · not a ball"
                        } else {
                            "Deducted ${-event.points} pts · not a ball"
                        }
                        null -> if (event.points > 0) "Added ${event.points} pts" else "Score updated"
                    }
                    GameEventType.UNDO -> "Reverted ${event.points} pts for ${event.playerName}"
                    GameEventType.STATUS_CHANGE -> event.message ?: "Game status updated"
                    else -> "Manual adjustment"
                }

                Text(
                    text = "$description · ${formatTimeAgo(event.timestamp)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Points badge (compact styling matching Pocketed list)
            if (isScoreLike) {
                val badgeColor = if (event.type == GameEventType.UNDO) MaterialTheme.colorScheme.error else playerColor
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = badgeColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = "${if (event.points > 0 && event.type != GameEventType.UNDO) "+" else ""}${event.points}",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = badgeColor
                    )
                }
            }
        }
    }
}

/**
 * Renders a resolved ball: either the exact numbered ball, or the shared-value
 * marker when the points value belongs to several balls in the player's setup.
 */
@Composable
private fun BallVisual(
    visual: BallVisual,
    size: Dp,
    modifier: Modifier = Modifier
) {
    when (visual) {
        is BallVisual.Known -> HistoryBall(number = visual.number, size = size, modifier = modifier)
        is BallVisual.SharedValue -> SharedValueMarker(points = visual.points, size = size, modifier = modifier)
        is BallVisual.NonBall -> NonBallMarker(points = visual.points, size = size, modifier = modifier)
    }
}

/** Unique, non-standard colour reserved for the shared-value marker. */
private val SharedValueMarkerColor = Color(0xFF6C5CE7)

/**
 * Accent for score entries that map to no ball at all, adapted to the theme.
 *
 * On light surfaces the dark-mode amber washed out badly, so light mode uses a
 * deep burnt amber that keeps its contrast against white. Dark mode keeps the
 * brighter amber. Both stay clear of the shared-value purple so the two markers
 * never read as the same thing.
 */
@Composable
private fun nonBallMarkerColor(): Color {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return if (isDark) Color(0xFFFFB74D) else Color(0xFFB45309)
}

/**
 * Marker for a score that does not correspond to any ball (manual entry or
 * foul). A dashed ring with the signed value inside - deliberately not a solid
 * ball shape, so it can never be mistaken for a pocketed ball.
 */
@Composable
private fun NonBallMarker(
    points: Int,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val marker = nonBallMarkerColor()
    val strokeWidth = (size.value * 0.065f).dp

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = this.size.minDimension / 2f - strokeWidth.toPx() / 2f
            drawCircle(color = marker.copy(alpha = 0.12f), radius = radius)
            drawCircle(
                color = marker,
                radius = radius,
                style = Stroke(
                    width = strokeWidth.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(radius * 0.32f, radius * 0.2f)
                    )
                )
            )
        }
        // Big enough to read at the 34dp size used in the Pocketed list, where
        // the old 0.26f scale came out under 9sp.
        Text(
            text = "${if (points > 0) "+" else "−"}${kotlin.math.abs(points)}",
            fontSize = (size.value * 0.34f).sp,
            lineHeight = (size.value * 0.34f).sp,
            fontWeight = FontWeight.Black,
            color = marker,
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Marker for a points value shared by several balls (e.g. 3/4/5/6 all worth 6).
 *
 * Deliberately not a pool ball and never in a ball colour: three overlapping
 * circles in a colour that appears nowhere in the ball palette, carrying only the
 * points value. It reads as one interchangeable unit rather than a specific ball.
 */
@Composable
private fun SharedValueMarker(
    points: Int,
    size: Dp,
    modifier: Modifier = Modifier
) {
    val marker = SharedValueMarkerColor
    val bubble = size * 0.62f

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = marker.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, marker.copy(alpha = 0.45f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Two offset bubbles behind, the value bubble in front.
            Box(
                modifier = Modifier
                    .size(bubble)
                    .offset(x = -size * 0.2f, y = -size * 0.1f)
                    .clip(CircleShape)
                    .background(marker.copy(alpha = 0.4f))
            )
            Box(
                modifier = Modifier
                    .size(bubble)
                    .offset(x = size * 0.2f, y = -size * 0.1f)
                    .clip(CircleShape)
                    .background(marker.copy(alpha = 0.6f))
            )
            Box(
                modifier = Modifier
                    .size(bubble)
                    .offset(y = size * 0.16f)
                    .clip(CircleShape)
                    .background(marker),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(
                                    bubble.value * 0.6f,
                                    bubble.value * 0.6f
                                )
                            )
                        )
                )
                Text(
                    text = points.toString(),
                    fontSize = (size.value * 0.3f).sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Small, non-interactive pool ball used inside the history list.
 *
 * Every ball is colour-dominant so the number always has the same weight and
 * legibility as a solid. Stripes (9–15) are signalled by two narrow white caps
 * at the poles instead of a mostly-white ball with a thin colour band - the old
 * look left the number tiny and the ball washed out. The 8-ball keeps the classic
 * black-and-white look, with a larger number than before.
 */
@Composable
private fun HistoryBall(
    number: Int,
    size: Dp = 34.dp,
    modifier: Modifier = Modifier
) {
    val ballColor = BallColors.getBallColor(number)
    val isStripe = number in 9..15
    val isEight = number == 8
    val needsDarkText = number == 1 || number == 9

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = if (isEight) Color(0xFF111111) else ballColor,
        border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.15f)),
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isStripe) {
                // Narrow white caps at the poles. Enough to read as a stripe at a
                // glance, while the colour still fills ~70% of the ball.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(size * 0.16f)
                        .align(Alignment.TopCenter)
                        .background(Color.White)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(size * 0.16f)
                        .align(Alignment.BottomCenter)
                        .background(Color.White)
                )
            }

            // Specular highlight for a subtle 3D feel
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                            center = androidx.compose.ui.geometry.Offset(size.value * 0.3f, size.value * 0.3f)
                        )
                    )
            )

            if (isEight) {
                Box(
                    modifier = Modifier
                        .size(size * 0.64f)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        fontSize = (size.value * 0.34f).sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF111111)
                    )
                }
            } else {
                // Same size for solids and stripes, so a pocketed 9 is as easy to
                // read as a pocketed 2.
                Text(
                    text = number.toString(),
                    fontSize = (size.value * 0.4f).sp,
                    fontWeight = FontWeight.Black,
                    color = if (needsDarkText) Color.Black.copy(alpha = 0.8f) else Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        shadow = if (!needsDarkText) Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = androidx.compose.ui.geometry.Offset(1.5f, 1.5f),
                            blurRadius = 3f
                        ) else null
                    )
                )
            }
        }
    }
}
