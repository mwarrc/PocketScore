package com.mwarrc.pocketscore.ui.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.unit.sp

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
    val playerColor = colorScheme.primary

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Scale spring on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "tile_scale",
    )

    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.CircleShape,
        color = when {
            isSelected -> colorScheme.primary
            isNew -> colorScheme.primaryContainer.copy(alpha = 0.6f)
            else -> colorScheme.surface
        },
        tonalElevation = if (isSelected || isNew) 1.dp else 0.dp,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isSelected -> colorScheme.primary
                isNew -> colorScheme.primary.copy(alpha = 0.2f)
                else -> colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        ),
        modifier = modifier
            .height(40.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Avatar / order badge
            PlayerAvatar(
                name = name,
                isSelected = isSelected,
                selectionOrder = selectionOrder,
                playerColor = playerColor,
                colorScheme = colorScheme,
            )

            // Name
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 13.sp
                ),
                color = if (isSelected) colorScheme.onPrimary
                else colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlayerAvatar(
    name: String,
    isSelected: Boolean,
    selectionOrder: Int?,
    playerColor: Color,
    colorScheme: ColorScheme,
) {
    Box(
        modifier = Modifier.size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            if (selectionOrder != null) {
                // Filled order badge
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(colorScheme.onPrimary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = selectionOrder.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = playerColor,
                    )
                }
            } else {
                // Check state
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(18.dp),
                )
            }
        } else {
            // Dimmed identity indicator
            Surface(
                modifier = Modifier.size(10.dp),
                shape = CircleShape,
                color = playerColor.copy(alpha = 0.65f)
            ) { Box {} }
        }
    }
}

// --───────────────────────────────
// Add Player Tile — ghost dashed style
// --───────────────────────────────

@Composable
fun AddPlayerTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "add_scale",
    )

    Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.CircleShape,
        color = colorScheme.primaryContainer.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = colorScheme.primary.copy(alpha = 0.2f)
        ),
        modifier = modifier
            .height(40.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Add More",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = colorScheme.primary,
            )
        }
    }
}

// --───────────────────────────────
// Utility — clickable with custom interactionSource, no ripple
// --───────────────────────────────

private fun Modifier.clickableNoRipple(
    interactionSource: MutableInteractionSource,
    onClick: () -> Unit,
): Modifier = this.then(
    Modifier.clickable(
        interactionSource = interactionSource,
        indication = null,
        onClick = onClick,
    )
)