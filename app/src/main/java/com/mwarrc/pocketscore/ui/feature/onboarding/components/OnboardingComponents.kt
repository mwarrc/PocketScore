package com.mwarrc.pocketscore.ui.feature.onboarding.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.ui.feature.onboarding.PointsSystemOption
import com.mwarrc.pocketscore.ui.util.BallColors

// ─────────
// HAPTICS - one place to tune the feel of the whole screen
// ─────────

/**
 * Haptic vocabulary for onboarding:
 *  - [Select]  → light tick when the chosen scoring system changes
 *  - [Confirm] → firmer feedback for the final "Let's play" commitment
 *
 * These use constants available on every Compose UI version. On Compose UI 1.7+ you can
 * make them crisper by swapping to `HapticFeedbackType.SegmentTick` and
 * `HapticFeedbackType.Confirm`. It's a one-line change here.
 */
object OnboardingHaptics {
    val Select = HapticFeedbackType.TextHandleMove
    val Confirm = HapticFeedbackType.LongPress
}

// ─────────
// FELT BALL STRIP  (lives inside the primary-colored hero)
// ─────────

/**
 * Shows the eight balls with their point values for [option], plus its description.
 *
 * The balls themselves never move. When the selection changes, only the numbers whose
 * value actually differs roll over, so you can *see* what each system changes.
 * The description sits in a fixed two-line slot so the hero never changes height.
 *
 * The hero behind this strip is a soft primary wash rather than solid `primary`, so the
 * strip tints with `primary` and its text uses the normal surface content colors.
 */
@Composable
fun FeltBallStrip(
    option: PointsSystemOption,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colorScheme.primary.copy(alpha = 0.08f))
            .padding(horizontal = 8.dp, vertical = 14.dp)
    ) {
        AnimatedContent(
            targetState = option.name,
            transitionSpec = { fadeIn(tween(220, delayMillis = 60)) togetherWith fadeOut(tween(120)) },
            modifier = Modifier.padding(horizontal = 8.dp),
            label = "strip_title"
        ) { name ->
            Text(
                text = "$name · points per ball",
                style = MaterialTheme.typography.labelLarge,
                color = colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            option.keyValues.entries.forEach { (ball, pts) ->
                PoolBallCell(ball = ball, pts = pts, modifier = Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(12.dp))

        AnimatedContent(
            targetState = option.description,
            transitionSpec = { fadeIn(tween(220, delayMillis = 60)) togetherWith fadeOut(tween(120)) },
            modifier = Modifier.padding(horizontal = 8.dp),
            label = "strip_description"
        ) { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant,
                minLines = 2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * One ball + its point value. The ball color is domain data from [BallColors] (a pool
 * ball is always the same color regardless of theme); everything else is themed.
 */
@Composable
private fun PoolBallCell(
    ball: Int,
    pts: Int,
    modifier: Modifier = Modifier
) {
    val ballColor = BallColors.getBallColor(ball)

    // Yellow (1,9) and orange (5,13) need dark text; every other ball uses white.
    val labelOnBall = when (ball) {
        1, 5, 9, 13 -> Color(0xFF1C1B1F)
        else -> Color.White
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(ballColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$ball",
                color = labelOnBall,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                lineHeight = 13.sp
            )
        }

        Spacer(Modifier.height(4.dp))

        // Fixed-height slot so the roll animation never changes layout.
        Box(
            modifier = Modifier.height(18.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = pts,
                transitionSpec = {
                    (slideInVertically(tween(260)) { it / 2 } + fadeIn(tween(260, delayMillis = 40))) togetherWith
                        (slideOutVertically(tween(160)) { -it / 2 } + fadeOut(tween(120)))
                },
                label = "pts_$ball"
            ) { value ->
                Text(
                    text = "$value",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    lineHeight = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ─────────
// SELECTABLE ROW  (all rows are exactly the same size)
// ─────────

/** Every row uses this height, selected or not, so the list never reflows. */
private val RowHeight = 72.dp

/**
 * A radio-style row for one scoring system: name, tagline, optional "Default" chip.
 *
 * Selected vs. unselected only changes *color* (container, text, radio dot), never size,
 * so the three rows always read as one equal, coherent set. The description lives in the
 * hero strip instead of expanding the row.
 *
 * Press feedback is a small spring scale + ripple. The haptic itself is fired by the
 * caller, so it only happens when the selection actually changes.
 */
@Composable
fun PointsSystemRow(
    option: PointsSystemOption,
    selected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "row_press_${option.name}"
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) colorScheme.primaryContainer else colorScheme.surfaceContainerLow,
        animationSpec = tween(240),
        label = "row_container_${option.name}"
    )
    val titleColor by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimaryContainer else colorScheme.onSurface,
        animationSpec = tween(240),
        label = "row_title_${option.name}"
    )
    val taglineColor by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimaryContainer.copy(alpha = 0.75f) else colorScheme.onSurfaceVariant,
        animationSpec = tween(240),
        label = "row_tagline_${option.name}"
    )
    val chipContainer by animateColorAsState(
        targetValue = if (selected) colorScheme.primary else colorScheme.secondaryContainer,
        animationSpec = tween(240),
        label = "row_chip_bg_${option.name}"
    )
    val chipContent by animateColorAsState(
        targetValue = if (selected) colorScheme.onPrimary else colorScheme.onSecondaryContainer,
        animationSpec = tween(240),
        label = "row_chip_fg_${option.name}"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = RowHeight)
            .scale(pressScale)
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton,
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SelectionDot(selected = selected)

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    maxLines = 1
                )
                if (option.isDefault) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = chipContainer
                    ) {
                        Text(
                            text = "Default",
                            style = MaterialTheme.typography.labelSmall,
                            color = chipContent,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = option.tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = taglineColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Radio dot: ring color fades, inner dot springs in/out. */
@Composable
private fun SelectionDot(selected: Boolean) {
    val colorScheme = MaterialTheme.colorScheme

    val ringColor by animateColorAsState(
        targetValue = if (selected) colorScheme.primary else colorScheme.outline,
        animationSpec = tween(240),
        label = "dot_ring"
    )
    val dotScale by animateFloatAsState(
        targetValue = if (selected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "dot_scale"
    )

    Box(
        modifier = Modifier
            .size(22.dp)
            .border(2.dp, ringColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(dotScale.coerceAtLeast(0f))
                .background(colorScheme.primary, CircleShape)
        )
    }
}

// ─────────
// CTA
// ─────────

/**
 * Filled M3 button with a spring press-scale, an arrow that nudges forward while
 * pressed, and a firm confirm haptic. Colors come from [Button]'s theme defaults.
 */
@Composable
fun GetStartedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cta_press"
    )
    val arrowOffset by animateDpAsState(
        targetValue = if (pressed) 5.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "cta_arrow"
    )

    Button(
        onClick = {
            haptic.performHapticFeedback(OnboardingHaptics.Confirm)
            onClick()
        },
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(pressScale),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "Let's play",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.width(10.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            modifier = Modifier
                .size(18.dp)
                .offset(x = arrowOffset)
        )
    }
}