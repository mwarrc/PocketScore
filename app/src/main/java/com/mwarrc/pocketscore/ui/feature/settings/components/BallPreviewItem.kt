package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.ui.feature.settings.getBallColor

/**
 * Ball type classification for rendering purposes.
 */
private enum class BallType { SOLID, EIGHT_BALL, STRIPE }

/**
 * Determines the [BallType] for a given ball number.
 *
 * @param number The ball number (1–15).
 * @return The corresponding [BallType].
 */
private fun ballTypeOf(number: Int): BallType = when {
    number == 8 -> BallType.EIGHT_BALL
    number < 8  -> BallType.SOLID
    else        -> BallType.STRIPE
}

/**
 * A visually accurate preview of a pool ball.
 *
 * - **Solids (1–7):** Full-colour ball with a specular highlight.
 * - **8-Ball:** Matte black with a white number circle, matching the iconic look.
 * - **Stripes (9–15):** White ball with a horizontal colour band and the number centred on it.
 *
 * Below the ball, a label shows the ball type and a pill displays the current point value.
 *
 * @param number The ball number (1–15).
 * @param value  The point value assigned to this ball.
 * @param modifier Optional [Modifier] for the root layout.
 */
@Composable
fun BallPreviewItem(
    number: Int,
    value: Int,
    modifier: Modifier = Modifier
) {
    val ballSize = 56.dp
    val ballColor = getBallColor(number)
    val type = ballTypeOf(number)

    val typeLabel = when (type) {
        BallType.SOLID      -> "Solid"
        BallType.EIGHT_BALL -> "8-Ball"
        BallType.STRIPE     -> "Stripe"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // ── Ball shell ──────────────────────────────────────────────────
        Surface(
            modifier = Modifier.size(ballSize),
            shape = CircleShape,
            color = when (type) {
                BallType.STRIPE     -> Color.White
                BallType.EIGHT_BALL -> Color(0xFF111111)
                BallType.SOLID      -> ballColor
            },
            border = BorderStroke(
                width = 1.5.dp,
                color = Color.White.copy(alpha = 0.25f)
            ),
            shadowElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                when (type) {
                    // ── Stripe: white base + colour band ────────────────
                    BallType.STRIPE -> {
                        // Slightly off-white base to reduce "aggression"
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFFF5F5F5))
                        )
                        
                        // Colour band across the middle
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ballSize * 0.25f) // Further thinned band
                                .align(Alignment.Center)
                                .background(ballColor)
                        )
                        
                        // Softened specular on white area
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.4f),
                                            Color.Transparent
                                        ),
                                        center = Offset(ballSize.value * 0.28f, ballSize.value * 0.22f)
                                    )
                                )
                        )
                        
                        // Number circle on the band
                        Box(
                            modifier = Modifier
                                .size(20.dp) // Slightly smaller circle
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = number.toString(),
                                fontSize = if (number >= 10) 9.sp else 10.sp,
                                fontWeight = FontWeight.Black,
                                color = ballColor
                            )
                        }
                    }

                    // ── 8-Ball: dark with white number circle ────────────
                    BallType.EIGHT_BALL -> {
                        // Subtle specular on the dark surface
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.18f),
                                            Color.Transparent
                                        ),
                                        center = Offset(ballSize.value * 0.28f, ballSize.value * 0.22f)
                                    )
                                )
                        )
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "8",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF111111)
                            )
                        }
                    }

                    // ── Solid: full colour + specular ────────────────────
                    BallType.SOLID -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.45f),
                                            Color.Transparent
                                        ),
                                        center = Offset(ballSize.value * 0.3f, ballSize.value * 0.3f)
                                    )
                                )
                        )
                        val needsDark = number in listOf(1, 2, 13)
                        Text(
                            text = number.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                shadow = if (!needsDark) Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(2f, 2f),
                                    blurRadius = 4f
                                ) else null
                            ),
                            color = if (needsDark) Color.Black.copy(alpha = 0.8f) else Color.White
                        )
                    }
                }
            }
        }

        // ── Label + value pill ──────────────────────────────────────────
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = typeLabel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Surface(
                color = when (type) {
                    BallType.EIGHT_BALL -> MaterialTheme.colorScheme.secondaryContainer
                    BallType.STRIPE     -> MaterialTheme.colorScheme.tertiaryContainer
                    BallType.SOLID      -> MaterialTheme.colorScheme.primaryContainer
                },
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 2.dp
            ) {
                Text(
                    text = value.toString(),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Black,
                    color = when (type) {
                        BallType.EIGHT_BALL -> MaterialTheme.colorScheme.onSecondaryContainer
                        BallType.STRIPE     -> MaterialTheme.colorScheme.onTertiaryContainer
                        BallType.SOLID      -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                )
            }
        }
    }
}
