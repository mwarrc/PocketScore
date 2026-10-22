package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun PlayerTile(
    name: String,
    isSelected: Boolean,
    selectionOrder: Int? = null,
    isNew: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Scale spring on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tile_scale",
    )

    // Three clearly distinct states:
    //  selected → solid primary (highest contrast)
    //  new      → tertiary accent (unmistakably different from selected)
    //  idle     → neutral surface
    val containerColor = when {
        isSelected -> colorScheme.primary
        isNew -> colorScheme.tertiaryContainer
        else -> colorScheme.surface
    }
    val outlineColor = when {
        isSelected -> colorScheme.primary
        isNew -> colorScheme.tertiary.copy(alpha = 0.5f)
        else -> colorScheme.outlineVariant.copy(alpha = 0.5f)
    }

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        tonalElevation = if (isSelected || isNew) 1.dp else 0.dp,
        border = BorderStroke(1.dp, outlineColor),
        modifier = modifier
            .height(40.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PlayerAvatar(
                name = name,
                isSelected = isSelected,
                selectionOrder = selectionOrder,
            )

            Text(
                text = name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = when {
                    isSelected -> colorScheme.onPrimary
                    isNew -> colorScheme.onTertiaryContainer
                    else -> colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * The tile's leading identity slot.
 *
 * Always the same fixed size so every tile lines up regardless of state:
 * - unselected → the player's initial (consistent identity cue)
 * - selected   → the pick order number, or a check when order is unknown
 */
@Composable
private fun PlayerAvatar(
    name: String,
    isSelected: Boolean,
    selectionOrder: Int?,
) {
    val colorScheme = MaterialTheme.colorScheme
    // Compact enough that three columns still fit at the largest UI scale.
    val slot = 24.dp

    if (isSelected) {
        Box(
            modifier = Modifier
                .size(slot)
                .background(colorScheme.onPrimary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selectionOrder != null) {
                Text(
                    text = selectionOrder.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = colorScheme.primary,
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colorScheme.primary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    } else {
        Box(
            modifier = Modifier
                .size(slot)
                .background(colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.trim().firstOrNull()?.uppercase() ?: "?",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurfaceVariant,
            )
        }
    }
}
