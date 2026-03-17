package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.Player
import kotlin.math.PI
import kotlin.math.sin

/**
 * Full-screen winner celebration overlay.
 *
 * Design: Expressive Material 3, dynamic color only.
 *   - Three layered sine waves animated across the bottom of the screen (primary + surfaceVariant)
 *   - Geometric trophy mark drawn entirely in Canvas — no assets
 *   - Spring entrance animation on the card column
 *   - No emojis, no purple gradients, no confetti
 *
 * Labels:
 *   Single winner → eyebrow "CHAMPION",    headline = winner.name
 *   Tie           → eyebrow "MATCH ENDED", headline = "Honours Even"
 *
 * @param winners   Winning [Player] list — 1 item = sole winner, 2+ = tie
 * @param onDismiss Tap "View Scores" or the close button
 * @param onRestart Tap "Play Again"
 * @param onArchive Tap "Save & Archive"
 */
@Composable
fun WinnerCelebrationOverlay(
    winners: List<Player>,
    onDismiss: () -> Unit,
    onRestart: () -> Unit,
    onArchive: () -> Unit
) {
    val isTie = winners.size > 1
    val colorScheme = MaterialTheme.colorScheme

    // ── Entrance spring ───────────────────────────────────────────────────────
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val cardScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.88f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow),
        label = "cardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(320, easing = EaseOut),
        label = "cardAlpha"
    )

    // ── Wave phase — single float drives all sine wave animation ─────────────
    // Runs 0 → 2π continuously. At 4800ms per cycle the waves move at a calm,
    // meditative pace — fast enough to read as alive, slow enough to feel elegant.
    val infiniteTransition = rememberInfiniteTransition(label = "bg")

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    // ── Root ──────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {

        // ── Animated sine wave background ─────────────────────────────────────
        // Three staggered waves drawn as filled paths that flood from the bottom
        // upward. Each wave has a different amplitude, frequency, speed offset,
        // and opacity so they layer into a living, breathing surface.
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawAnimatedWaves(
                phase = wavePhase,
                primaryColor = colorScheme.primary,
                surfaceVariantColor = colorScheme.surfaceVariant,
                width = size.width,
                height = size.height
            )
        }

        // ── Card content ──────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .graphicsLayer(
                    scaleX = cardScale,
                    scaleY = cardScale,
                    alpha = cardAlpha
                )
        ) {

            // Trophy mark — geometric, Canvas-drawn, no assets
            Surface(
                shape = CircleShape,
                color = colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawTrophyMark(
                        color = colorScheme.onPrimaryContainer,
                        center = center,
                        sizePx = size.minDimension * 0.50f
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            // Eyebrow
            Text(
                text = if (isTie) "MATCH ENDED" else "CHAMPION",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 4.sp,
                color = colorScheme.primary
            )

            Spacer(Modifier.height(10.dp))

            // Headline
            if (isTie) {
                Text(
                    text = "Honours Even",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = winners.first().name,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp
                )
            }

            Spacer(Modifier.height(8.dp))

            // Score sub-label
            if (isTie) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    winners.forEachIndexed { index, player ->
                        if (index > 0) {
                            Text(
                                text = "·",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${player.name}  ${player.score} pts",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (winners.isNotEmpty()) {
                Text(
                    text = "${winners.first().score} points",
                    style = MaterialTheme.typography.titleSmall,
                    color = colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(38.dp))

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(0.70f),
                thickness = 1.dp,
                color = colorScheme.outlineVariant.copy(alpha = 0.45f)
            )

            Spacer(Modifier.height(30.dp))

            // Primary action
            Button(
                onClick = onArchive,
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Archive,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Save & Archive",
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            }

            Spacer(Modifier.height(10.dp))

            // Secondary action
            FilledTonalButton(
                onClick = onRestart,
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Play Again",
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.3.sp
                )
            }

            //Spacer(Modifier.height(2.dp))

          /*  TextButton(onClick = onDismiss) {
                Text(
                    "View Scores",
                    style = MaterialTheme.typography.labelLarge,
                    color = colorScheme.onSurfaceVariant,
                    letterSpacing = 0.3.sp
                )
            }*/
        }

        // ── Close button — top-right ──────────────────────────────────────────
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .systemBarsPadding()
        ) {
            Surface(
                shape = CircleShape,
                color = colorScheme.surfaceVariant.copy(alpha = 0.55f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        modifier = Modifier.size(18.dp),
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Animated sine wave background
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Draws three layered sine waves as filled paths that rise from the bottom of
 * the screen. Each layer has a distinct amplitude, frequency, phase offset, and
 * opacity — together they produce a gentle, breathing wave effect.
 *
 * Wave anatomy:
 *   back   → tallest, slowest, most transparent   (surfaceVariant tint)
 *   mid    → medium height, phase-shifted 120°     (primary, low alpha)
 *   front  → shortest, fastest, most opaque        (primary, mid alpha)
 *
 * @param phase              Current animation phase in radians (0 → 2π, looping)
 * @param primaryColor       [ColorScheme.primary] — tints front & mid wave
 * @param surfaceVariantColor [ColorScheme.surfaceVariant] — tints back wave
 * @param width              Canvas width in px
 * @param height             Canvas height in px
 */
private fun DrawScope.drawAnimatedWaves(
    phase: Float,
    primaryColor: Color,
    surfaceVariantColor: Color,
    width: Float,
    height: Float
) {
    data class WaveConfig(
        val amplitudeFraction: Float,  // wave height as fraction of screen height
        val frequency: Float,          // number of full wave cycles across width
        val phaseOffset: Float,        // additional phase shift in radians
        val yBaseFraction: Float,      // vertical center of the wave (0=top, 1=bottom)
        val color: Color
    )

    val waves = listOf(
        // Back wave — tall, slow, very faint surfaceVariant tint
        WaveConfig(
            amplitudeFraction = 0.11f,
            frequency = 1.4f,
            phaseOffset = 0f,
            yBaseFraction = 0.72f,
            color = surfaceVariantColor.copy(alpha = 0.18f)
        ),
        // Mid wave — medium, phase-offset 120°, primary tint
        WaveConfig(
            amplitudeFraction = 0.085f,
            frequency = 1.8f,
            phaseOffset = (2.0 * PI / 3.0).toFloat(),
            yBaseFraction = 0.76f,
            color = primaryColor.copy(alpha = 0.10f)
        ),
        // Front wave — shortest, fastest (phase 240°), strongest primary tint
        WaveConfig(
            amplitudeFraction = 0.065f,
            frequency = 2.2f,
            phaseOffset = (4.0 * PI / 3.0).toFloat(),
            yBaseFraction = 0.80f,
            color = primaryColor.copy(alpha = 0.14f)
        )
    )

    val steps = 180  // horizontal sample count — smooth curve, not expensive

    waves.forEach { wave ->
        val amplitude = wave.amplitudeFraction * height
        val yBase     = wave.yBaseFraction * height

        val path = Path().apply {
            // Start at bottom-left corner so the fill floods downward
            moveTo(0f, height)

            // Walk across the width sampling the sine function
            for (i in 0..steps) {
                val x = (i.toFloat() / steps) * width
                // x position mapped to radians (frequency full cycles across width)
                val radians = (i.toFloat() / steps) * (2.0 * PI * wave.frequency).toFloat()
                val y = yBase + amplitude * sin(radians + phase + wave.phaseOffset)
                if (i == 0) lineTo(x, y) else lineTo(x, y)
            }

            // Close the path along the bottom edge
            lineTo(width, height)
            close()
        }

        drawPath(path = path, color = wave.color)
    }
}



/**
 * Draws a clean geometric trophy silhouette using only [DrawScope] primitives.
 * Scales proportionally with [sizePx] (the diameter of the bounding box).
 *
 * Structure:
 *   cup body   → cubic-bezier path
 *   handles    → left & right cubic arcs, stroked with round caps
 *   stem       → filled rectangle
 *   base       → round-rect bar
 *
 * @param color   Solid fill / stroke color — use [ColorScheme.onPrimaryContainer]
 * @param center  Center of the bounding circle (from Canvas)
 * @param sizePx  Diameter of the trophy's bounding box in pixels
 */
private fun DrawScope.drawTrophyMark(
    color: Color,
    center: Offset,
    sizePx: Float
) {
    val h = sizePx / 2f   // half-size convenience
    val cx = center.x
    val cy = center.y

    // ── Cup body ──────────────────────────────────────────────────────────────
    val cupPath = Path().apply {
        val left       = cx - h * 0.54f
        val right      = cx + h * 0.54f
        val top        = cy - h * 0.70f
        val bowlBottom = cy + h * 0.06f

        moveTo(left, top)
        lineTo(right, top)
        // right wall → smooth curve into stem
        lineTo(right, bowlBottom - h * 0.04f)
        cubicTo(
            right,           bowlBottom + h * 0.20f,
            cx + h * 0.22f, bowlBottom + h * 0.32f,
            cx,              bowlBottom + h * 0.34f
        )
        // mirror left side
        cubicTo(
            cx - h * 0.22f, bowlBottom + h * 0.32f,
            left,            bowlBottom + h * 0.20f,
            left,            bowlBottom - h * 0.04f
        )
        close()
    }
    drawPath(cupPath, color = color)

    // ── Handles ───────────────────────────────────────────────────────────────
    val handleStroke = Stroke(width = sizePx * 0.072f, cap = StrokeCap.Round)
    val handleTop    = cy - h * 0.54f
    val handleBottom = cy - h * 0.06f
    val handleBulge  = h * 0.30f

    // Left
    drawPath(
        path = Path().apply {
            val hx = cx - h * 0.54f
            moveTo(hx, handleTop)
            cubicTo(hx - handleBulge, handleTop, hx - handleBulge, handleBottom, hx, handleBottom)
        },
        color = color,
        style = handleStroke
    )
    // Right
    drawPath(
        path = Path().apply {
            val hx = cx + h * 0.54f
            moveTo(hx, handleTop)
            cubicTo(hx + handleBulge, handleTop, hx + handleBulge, handleBottom, hx, handleBottom)
        },
        color = color,
        style = handleStroke
    )

    // ── Stem ──────────────────────────────────────────────────────────────────
    val stemTop    = cy + h * 0.34f
    val stemBottom = cy + h * 0.60f
    val stemW      = h * 0.13f
    drawRect(
        color = color,
        topLeft = Offset(cx - stemW, stemTop),
        size = Size(stemW * 2f, stemBottom - stemTop)
    )

    // ── Base ──────────────────────────────────────────────────────────────────
    val baseH = h * 0.14f
    val baseW = h * 0.78f
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - baseW, stemBottom),
        size = Size(baseW * 2f, baseH),
        cornerRadius = CornerRadius(baseH / 2f)
    )
}