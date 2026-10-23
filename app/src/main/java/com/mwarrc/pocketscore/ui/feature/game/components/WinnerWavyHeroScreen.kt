package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.Player
import kotlin.math.PI
import kotlin.math.sin

/**
 * Wavy-hero variant of the end-of-match winner screen.
 *
 * Layout (top → bottom):
 *   1. Tinted hero - eyebrow, a large count-up of the winning score, the winner's
 *      name, and a margin or tie note. The hero's bottom edge is an animated
 *      two-layer wave that drifts horizontally against the page below.
 *   2. Final standings - every player, ranked, with a "Last place" chip.
 *   3. Actions - Save & Archive / Play Again.
 *
 * All colors come from [MaterialTheme.colorScheme], so the screen tracks the app
 * theme, dynamic color, dark mode and the user's chosen app font automatically.
 *
 * @param standings     All players in the match, in any order; ranked here.
 * @param winners       Winning [Player] list - 1 item = sole winner, 2+ = tie
 * @param onDismiss     Tap the close button
 * @param onRestart     Tap "Play Again"
 * @param onArchive     Tap "Save & Archive"
 */
@Composable
fun WinnerWavyHeroScreen(
    standings: List<Player>,
    winners: List<Player>,
    onDismiss: () -> Unit,
    onRestart: () -> Unit,
    onArchive: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val isTie = winners.size > 1

    // Rank every player: highest score first. Ties share a rank, and the rank is
    // "how many players finished ahead of you, plus one" - same rule the game
    // screen uses for leader/loser badges.
    val ranked = remember(standings) {
        standings
            .sortedByDescending { it.score }
            .map { player ->
                val rank = 1 + standings.count { it.score > player.score }
                player to rank
            }
    }

    val topScore = standings.maxOfOrNull { it.score } ?: 0
    val lowestScore = standings.minOfOrNull { it.score } ?: 0
    // Margin only makes sense when there is a distinct last place to win against.
    val margin = if (standings.size > 1 && lowestScore < topScore) topScore - lowestScore else 0
    val lastPlaceScore = lowestScore

    val winnerNames = winners.joinToString(" & ") { it.name }

    // ── Entrance ───────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val contentAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(320, easing = EaseOut),
        label = "contentAlpha"
    )
    val contentScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.94f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow),
        label = "contentScale"
    )

    // ── Hero score count-up ────
    // Starts at 0 and eases up to the final score, mirroring the mock's
    // cubic ease-out counter.
    var countUpTarget by remember { mutableStateOf(0) }
    LaunchedEffect(topScore) {
        // One frame at 0 first, so the animation actually runs from zero.
        countUpTarget = topScore
    }
    val animatedScore by animateIntAsState(
        targetValue = countUpTarget,
        animationSpec = tween(durationMillis = 900, easing = EaseOutCubic),
        label = "winnerScore"
    )

    // ── Wave drift phase ───────
    val infiniteTransition = rememberInfiniteTransition(label = "hero_waves")
    val waveDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveDrift"
    )

    // The hero tint: a soft primary wash rather than full-saturation primary, so
    // the large top area stays comfortable under Material You (same approach as
    // the onboarding hero).
    val heroColor = lerp(colorScheme.primaryContainer, colorScheme.surface, 0.25f)
    val heroContent = colorScheme.onPrimaryContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── HERO ───────────
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(heroColor)
                        .systemBarsPadding()
                        .padding(horizontal = 20.dp)
                        // Extra bottom room so the wave edge overlaps empty space,
                        // not the text.
                        .padding(top = 12.dp, bottom = 56.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close button, flush right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDismiss) {
                            Surface(
                                shape = CircleShape,
                                color = heroContent.copy(alpha = 0.10f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Close",
                                        modifier = Modifier.size(18.dp),
                                        tint = heroContent
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(
                            alpha = contentAlpha,
                            scaleX = contentScale,
                            scaleY = contentScale
                        )
                    ) {
                        Text(
                            text = if (isTie) "MATCH ENDED" else "WINNER",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 3.sp,
                            color = heroContent.copy(alpha = 0.75f)
                        )

                        Spacer(Modifier.height(2.dp))

                        // The big score
                        Text(
                            text = "$animatedScore",
                            fontSize = 88.sp,
                            lineHeight = 92.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-3).sp,
                            color = colorScheme.primary
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = if (isTie) "Honours Even" else winners.firstOrNull()?.name.orEmpty(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = heroContent
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = when {
                                isTie -> "$winnerNames · ${topScore} points each"
                                margin > 0 -> "won by $margin points"
                                else -> "${topScore} points"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = heroContent.copy(alpha = 0.8f)
                        )
                    }
                }

                // Wave edge - pinned to the hero's bottom, drawn over it so the
                // page colour appears to cut into the hero as a wave.
                Canvas(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    drawHeroWaves(
                        phase = waveDrift,
                        pageColor = colorScheme.background,
                        width = size.width,
                        height = size.height
                    )
                }
            }

            // ── STANDINGS ──────
            Text(
                text = "Final standings",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, top = 6.dp, bottom = 6.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(
                    items = ranked,
                    key = { _, (player, _) -> player.id }
                ) { _, (player, rank) ->
                    StandingRow(
                        player = player,
                        rank = rank,
                        isWinner = player.score == topScore,
                        isTie = isTie,
                        isLastPlace = standings.size > 1 && player.score == lastPlaceScore,
                        marginToWinner = topScore - player.score
                    )
                }
            }

            // ── ACTIONS ────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp, bottom = 20.dp)
                    .systemBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onArchive,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Archive,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Save & Archive", fontWeight = FontWeight.SemiBold)
                }

                FilledTonalButton(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("Play Again", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/**
 * One row of the final standings list: rank, avatar initial, name (with a
 * "Last place" chip where relevant) and score.
 */
@Composable
private fun StandingRow(
    player: Player,
    rank: Int,
    isWinner: Boolean,
    isTie: Boolean,
    isLastPlace: Boolean,
    marginToWinner: Int
) {
    val colorScheme = MaterialTheme.colorScheme

    // Winners lead with the primary container. Last place deliberately *recedes* -
    // it is the least important row, so instead of an accent tone (tertiaryContainer,
    // which is saturated and made the loser the loudest thing on screen) it gets a
    // washed-out surface with muted text. The "Last place" chip carries the meaning.
    val container = when {
        isWinner -> colorScheme.primaryContainer
        isLastPlace -> colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
        else -> colorScheme.surfaceContainerLow
    }
    val content = when {
        isWinner -> colorScheme.onPrimaryContainer
        isLastPlace -> colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
        else -> colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = container,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = content.copy(alpha = 0.7f),
                modifier = Modifier.width(22.dp)
            )

            Spacer(Modifier.width(12.dp))

            Surface(
                shape = CircleShape,
                color = content.copy(alpha = 0.14f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = player.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = content
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = player.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = content,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isLastPlace) {
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "Last place",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${player.score}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = content
                )
                // Gap to the winner is noise for the winner themselves, and for
                // a tie everyone is level so it would read "−0" on every row.
                if (!isWinner && !isTie && marginToWinner > 0) {
                    Text(
                        text = "−$marginToWinner",
                        style = MaterialTheme.typography.labelSmall,
                        color = content.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// ────────
// Wave edge
// ────────

/**
 * Draws the hero's wave edge: two horizontal sine layers of the page colour,
 * phase-shifted and drifting at different rates, filling from the bottom so the
 * page appears to cut up into the hero.
 *
 * The two layers use a single seamless wavelength across the full width, so the
 * crest meets the right edge at the same height it left the left edge - the
 * shape tiles without a visible seam as it drifts.
 *
 * @param phase     Drift phase in radians (0 → 2π, looping)
 * @param pageColor Colour of the page beneath the hero
 * @param width     Canvas width in px
 * @param height    Canvas height in px
 */
private fun DrawScope.drawHeroWaves(
    phase: Float,
    pageColor: Color,
    width: Float,
    height: Float
) {
    // Two full wavelengths across the width keeps the curve gentle and tileable.
    val wavelengths = 2f
    val steps = 120

    data class Layer(
        val amplitudeFraction: Float,
        val phaseOffset: Float,
        val alpha: Float
    )

    val layers = listOf(
        Layer(amplitudeFraction = 0.45f, phaseOffset = 0f, alpha = 0.45f),
        Layer(amplitudeFraction = 0.30f, phaseOffset = (PI / 2.0).toFloat(), alpha = 1f)
    )

    layers.forEach { layer ->
        val amplitude = layer.amplitudeFraction * height
        val midY = height * 0.52f

        val path = Path().apply {
            moveTo(0f, height)
            for (i in 0..steps) {
                val fraction = i.toFloat() / steps
                val x = fraction * width
                val radians = fraction * (2.0 * PI * wavelengths).toFloat()
                val y = midY + amplitude * sin(radians + phase + layer.phaseOffset)
                lineTo(x, y)
            }
            lineTo(width, height)
            close()
        }

        drawPath(path = path, color = pageColor.copy(alpha = layer.alpha))
    }
}
