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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.domain.model.Player
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val x: Float, val y: Float,
    val vx: Float, val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotationSpeed: Float
)

/**
 * Full-screen winner celebration overlay with canvas confetti.
 * Displays when pool table is cleared and a winner (or winners in a tie) is determined.
 *
 * @param winners List of winning players
 * @param onDismiss Called when the user taps Dismiss
 * @param onRestart Called when the user taps Restart Match
 */
@Composable
fun WinnerCelebrationOverlay(
    winners: List<Player>,
    onDismiss: () -> Unit,
    onRestart: () -> Unit,
    onArchive: () -> Unit
) {
    val confettiColors = listOf(
        Color(0xFFFFC107), Color(0xFFE91E63), Color(0xFF00BCD4),
        Color(0xFF8BC34A), Color(0xFF9C27B0), Color(0xFFFF5722)
    )

    val particles = remember {
        (0..100).map {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat() * -1.5f, // start scattered high above the screen
                vx = (Random.nextFloat() - 0.5f) * 0.05f, // gentle sway
                vy = Random.nextFloat() * 0.4f + 0.3f, // steady downward velocity
                color = confettiColors[Random.nextInt(confettiColors.size)],
                size = Random.nextFloat() * 10f + 5f,
                rotation = Random.nextFloat() * 360f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 2f
            )
        }
    }

    // Continuous smooth looping infinite transition
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1000f,
        animationSpec = infiniteRepeatable(tween(1000000, easing = LinearEasing)),
        label = "time"
    )

    val scale by animateFloatAsState(
        targetValue = 1f, animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessLow),
        label = "scale"
    )

    val isTie = winners.size > 1

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.97f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        // Confetti Canvas (Continuous smooth falling)
        Canvas(modifier = Modifier.fillMaxSize()) {
            particles.forEach { p ->
                // Apply constant downward velocity. When it falls off screen, wrap it back to top.
                val totalY = p.y + (p.vy * time)
                // Wrap smoothly: screen is 0 to 1, we let it fall to 1.2 then wrap to -0.2
                val currentY = ((totalY + 0.2f) % 1.4f) - 0.2f
                
                // Keep smooth x drift with bounds wrapping
                val currentX = (p.x + (p.vx * time) + sin(time * 0.5f + p.x * 10f) * 0.04f) % 1f
                val rot = p.rotation + time * p.rotationSpeed * 50f

                drawRect(
                    color = p.color.copy(alpha = 0.85f),
                    topLeft = Offset(currentX * size.width, currentY * size.height),
                    size = androidx.compose.ui.geometry.Size(p.size, p.size * 0.55f),
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(32.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            // Trophy icon with ambient glow
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Game over label
            Text(
                text = if (isTie) "IT'S A TIE!" else "GAME OVER",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            // Winner name(s)
            if (isTie) {
                Text(
                    text = "🏆 Joint Winners",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                winners.forEach { winner ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = winner.name.firstOrNull()?.uppercase() ?: "?",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = winner.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${winner.score} pts",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                val winner = winners.first()
                Text(
                    text = winner.name,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "🏆 Winner with ${winner.score} points",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            // Actions
            Button(
                onClick = onArchive,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Archive, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save & Archive", fontWeight = FontWeight.Bold)
            }
            
            Button(
                onClick = onRestart,
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                contentPadding = PaddingValues(vertical = 14.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save & Play Again", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onDismiss) {
                Text("View Scores", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        // Close Button (Top Right)
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .systemBarsPadding()
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Icon(
                    Icons.Default.Close, 
                    contentDescription = "Close",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

