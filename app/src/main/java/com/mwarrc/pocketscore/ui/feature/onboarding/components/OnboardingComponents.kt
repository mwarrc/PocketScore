package com.mwarrc.pocketscore.ui.feature.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.ui.feature.onboarding.PointsSystemOption
import com.mwarrc.pocketscore.ui.util.BallColors

// ─────────────────────────────────────────────────────────────────────────────
// POINTS SYSTEM CARD  (M3 — no gradients, no glows)
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A selectable M3 card representing one ball-points scoring system.
 *
 * Selection state is communicated purely via:
 *  - Border: primary color when selected, outline-variant when not
 *  - Background: surfaceContainerHighest when selected, surfaceContainerHigh when not
 *  - Tonal elevation
 *
 * Ball values are shown as real colored pool-ball circles (balls 1–8) with the
 * point value and "pts" label rendered BELOW the ball in a plain Column —
 * no clipping, no scale transforms that shrink layout bounds.
 */
@Composable
fun PointsSystemCard(
    option: PointsSystemOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(220),
        label = "border_${option.name}"
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        animationSpec = tween(220),
        label = "bg_${option.name}"
    )

    Surface(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        tonalElevation = if (isSelected) 3.dp else 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            // ── TOP ROW: name + badge + checkmark ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = option.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )

                    if (option.isDefault) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "DEFAULT",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Checkmark circle
                SelectionIndicator(isSelected = isSelected)
            }

            Spacer(Modifier.height(2.dp))

            // ── TAGLINE ─────────────────────────────────────────────────────
            Text(
                text = option.tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.height(16.dp))

            // ── BALL GRID: 2 rows of 4 ──────────────────────────────────────
            // Use a fixed-structure Column → Row layout.
            // No scale() wrappers → layout bounds are never compressed → text always visible.
            val allBalls = option.keyValues.entries.toList()
            val topRow = allBalls.take(4)
            val bottomRow = allBalls.drop(4).take(4)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Row 1: balls 1–4
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    topRow.forEach { (ball, pts) ->
                        PoolBallCell(ball = ball, pts = pts, isSelected = isSelected)
                    }
                }
                // Row 2: balls 5–8
                if (bottomRow.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        bottomRow.forEach { (ball, pts) ->
                            PoolBallCell(ball = ball, pts = pts, isSelected = isSelected)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Spacer(Modifier.height(10.dp))

            // ── DESCRIPTION ─────────────────────────────────────────────────
            Text(
                text = option.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// POOL BALL CELL
// ─────────────────────────────────────────────────────────────────────────────

/**
 * A single pool ball shown as a colored circle with number inside,
 * then the point value and "pts" label BELOW in a Column.
 *
 * Layout is entirely in a Column — no Box layering that could cause clipping.
 * The ball number text is on TOP of the ball color via contentAlignment.
 */
@Composable
private fun PoolBallCell(
    ball: Int,
    pts: Int,
    isSelected: Boolean
) {
    val ballColor = BallColors.getBallColor(ball)

    // Yellow (1,9) and Orange (5,13) need dark text; everything else uses white
    val labelOnBall = when (ball) {
        1, 5, 9, 13 -> Color(0xFF1C1B1F)
        else -> Color.White
    }

    val valueColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(200),
        label = "val_$ball"
    )

    // The entire cell: ball circle + value + pts label
    // All in one Column, nothing overlapping → text is always fully visible
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(0.dp),
        modifier = Modifier.width(52.dp)          // fixed width keeps SpaceEvenly predictable
    ) {

        // ── Colored ball circle ────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ballColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$ball",
                color = labelOnBall,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                lineHeight = 15.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        // ── Point value ────────────────────────────────────────────────
        Text(
            text = "$pts",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 14.sp,
            lineHeight = 14.sp,
            color = valueColor
        )

        // ── "pts" micro label ──────────────────────────────────────────
        Text(
            text = "pts",
            fontSize = 9.sp,
            lineHeight = 9.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SELECTION INDICATOR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SelectionIndicator(isSelected: Boolean) {
    val bgColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceContainerHighest,
        animationSpec = tween(220),
        label = "sel_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outline,
        animationSpec = tween(220),
        label = "sel_border"
    )

    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        if (!isSelected) {
            // Unselected: show hairline border ring by layering a smaller clip
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GET STARTED BUTTON  (clean M3 FilledButton)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PulsingGetStartedButton(
    enabled: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Let's Play",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(10.dp))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
