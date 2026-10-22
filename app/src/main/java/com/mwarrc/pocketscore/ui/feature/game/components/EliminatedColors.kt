package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.luminance

/**
 * Eliminated card colors, derived from M3 roles.
 * Matches spec intent: red-tinted container, muted ink, accent red.
 * Uses compositeOver (no hardcoded hex). No harmonize (not available in deps).
 */
@Immutable
data class EliminatedColors(
    val container: Color,
    val border: Color,
    val nameInk: Color,
    val scoreInk: Color,
    val accent: Color,
)

@Composable
fun rememberEliminatedColors(baseContainer: Color): EliminatedColors {
    val scheme = MaterialTheme.colorScheme
    val isDark = scheme.surface.luminance() < 0.5f
    // errorContainer over surfaceContainer: 45% light / 20% dark (per spec)
    val tintAlpha = if (isDark) 0.20f else 0.45f
    val container = scheme.errorContainer.copy(alpha = tintAlpha).compositeOver(baseContainer)
    return EliminatedColors(
        container = container,
        border = scheme.error.copy(alpha = 0.3f),
        nameInk = scheme.onSurfaceVariant,
        scoreInk = scheme.onSurfaceVariant.copy(alpha = 0.70f),
        accent = scheme.error,
    )
}
