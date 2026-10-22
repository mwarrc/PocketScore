package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Confines swipe-to-navigate to a specific, bounded area (the header band).
 *
 * The gesture used to sit on the entire scrolling column, which meant a stray
 * horizontal drag anywhere — over the roster, a player tile, or the name field —
 * could fire a navigation. Scoping it to the header keeps the shortcut available
 * while removing the accidental-trigger surface.
 *
 * A directional chevron fades in as the user drags, so the swipe is discoverable
 * and its direction is obvious.
 *
 * @param onSwipeLeft Invoked when the user swipes far enough to the left
 * @param onSwipeRight Invoked when the user swipes far enough to the right
 * @param modifier Modifier for the gesture area
 * @param content The area that receives and displays the gesture
 */
@Composable
fun SwipeNavigationArea(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // Distance the user must travel before the swipe counts.
    val threshold = 180f
    var offset by remember { mutableFloatStateOf(0f) }

    val hintAlpha by animateFloatAsState(
        targetValue = (abs(offset) / threshold).coerceIn(0f, 1f) * 0.6f,
        label = "swipe_hint"
    )

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (offset > threshold) onSwipeRight()
                            else if (offset < -threshold) onSwipeLeft()
                            offset = 0f
                        },
                        onDragCancel = { offset = 0f },
                        onHorizontalDrag = { _, dragAmount -> offset += dragAmount }
                    )
                }
        ) {
            content()
        }

        // Left hint (swipe left → navigate left)
        if (offset < -20f) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = hintAlpha),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 8.dp)
                    .size(24.dp)
            )
        }

        // Right hint
        if (offset > 20f) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = hintAlpha),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .size(24.dp)
            )
        }
    }
}
