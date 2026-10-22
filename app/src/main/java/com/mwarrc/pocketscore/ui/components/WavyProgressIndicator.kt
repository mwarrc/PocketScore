package com.mwarrc.pocketscore.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun WavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    wavelength: Dp = 24.dp,
    amplitude: Float = 1f, // 0.0 to 1.0 scale
    strokeWidth: Dp = 4.dp,
    animationDuration: Int = 2000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wavy_progress")

    // Smooth phase shift for the wave motion
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val maxAmplitudePx = 4.dp // Base amplitude in DP

    Canvas(modifier = modifier
        .fillMaxWidth()
        .height(16.dp) // Increased height for better Material 3 presence
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val strokeWidthPx = strokeWidth.toPx()
        val wavelengthPx = wavelength.toPx()
        val amplitudePx = maxAmplitudePx.toPx() * amplitude

        // 1. Draw the Background Track with Material 3 rounded ends
        val trackPath = Path().apply {
            moveTo(0f, centerY)
            lineTo(width, centerY)
        }

        drawPath(
            path = trackPath,
            color = trackColor,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round
            )
        )

        // 2. Draw the Wavy Active Indicator with smooth Material 3 curves
        val wavePath = Path()
        val points = (width * 2).toInt().coerceAtLeast(200) // Higher quality for smoother curves

        for (i in 0..points) {
            val x = (i.toFloat() / points) * width
            // Smooth sinusoidal wave equation
            val normalizedX = x / wavelengthPx
            val angle = 2.0 * PI * (normalizedX - phaseShift)
            val y = centerY + (sin(angle) * amplitudePx).toFloat()

            if (i == 0) {
                wavePath.moveTo(x, y)
            } else {
                wavePath.lineTo(x, y)
            }
        }

        // Draw the wave with Material 3 rounded stroke
        drawPath(
            path = wavePath,
            color = color,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = androidx.compose.ui.graphics.StrokeJoin.Round
            )
        )
    }
}