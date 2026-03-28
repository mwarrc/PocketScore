package com.mwarrc.pocketscore.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.ui.util.ImmersiveMode

/**
 * Immersive loading overlay — billiard rack formation.
 *
 * Balls light up in a staggered wave that ripples across the triangle,
 * then fades back — giving the sense of a rack being broken.
 *
 * Optimisations vs previous version:
 *  - Removed Brush gradients and blur modifiers (expensive on every frame).
 *  - Single InfiniteTransition drives all animations (fewer coroutines).
 *  - Ball colours pulled directly from M3 colour scheme — no hardcoded hex.
 *  - No trigonometry at runtime; rack positions are static offsets computed once.
 *  - drawBehind used for the rack triangle outline — zero extra composable nodes.
 *  - AnimatedVisibility enter/exit kept intentionally snappy (300/250 ms).
 */
@Composable
fun AppLoadingOverlay(
    visible: Boolean,
    message: String? = null,
    subMessage: String? = null,
) {
    var showState by remember { mutableStateOf(false) }
    var showStartTime by remember { mutableStateOf(0L) }

    LaunchedEffect(visible) {
        if (visible) {
            if (!showState) {
                showStartTime = System.currentTimeMillis()
                showState = true
            }
        } else {
            if (showState) {
                val elapsed = System.currentTimeMillis() - showStartTime
                if (elapsed < 2500L) {
                    kotlinx.coroutines.delay(2500L - elapsed)
                }
                showState = false
            }
        }
    }

    AnimatedVisibility(
        visible = showState,
        enter = fadeIn(tween(300, easing = EaseOutCubic)),
        exit  = fadeOut(tween(250, easing = EaseInCubic)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
            contentAlignment = Alignment.Center,
        ) {
            ImmersiveMode()

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            ) {
                BilliardRackLoader()

                Spacer(Modifier.height(44.dp))

                Text(
                    text = message ?: "Processing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 0.3.sp,
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = subMessage ?: "Hang tight…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Creative Loading Animation
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Levitating 8-Ball Loader
 * Features a sleek floating 8-ball with a glassy 3D reflection, bouncing up and down
 * over a dynamically scaling ambient shadow to provide premium engagement UI.
 */
@Composable
private fun BilliardRackLoader() {
    val transition = rememberInfiniteTransition(label = "8ball_anim")
    
    // Core Bobbing Animation (the ball floating up and down)
    val offsetY by transition.animateFloat(
        initialValue = -16f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bobbing_y"
    )
    
    // Shadow scaling (shrinks as the ball goes up)
    val shadowScale by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing, delayMillis = 0),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadow_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // The 8-Ball Wrapper
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(y = offsetY.dp)
                .size(76.dp)
        ) {
            // Ball Base (3D Sphere Gradient)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(Color(0xFF444444), Color.Black, Color.Black),
                            center = Offset(50f, 50f), // Offset highlight to top-left
                            radius = 200f
                        )
                    )
            ) {
                // White 8-Ball Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .offset(y = (-2).dp) // Slightly raised to maintain spherical perspective
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0))
                ) {
                    Text(
                        text = "8",
                        fontWeight = FontWeight.Black,
                        color = Color.Black,
                        fontSize = 22.sp
                    )
                }
                
                // Glassy Shimmer Highlight Over the Sphere
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.35f), 
                                    Color.White.copy(alpha = 0.05f), 
                                    Color.Transparent
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(180f, 180f)
                            ),
                            shape = CircleShape
                        )
                )

                // Bottom Ambient Reflection (bounce light)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), 
                                    Color.Transparent
                                ),
                                center = Offset(120f, 180f),
                                radius = 100f
                            ),
                            shape = CircleShape
                        )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Ambient Shadow underneath the levitating ball
        Box(
            modifier = Modifier
                .graphicsLayer { 
                    scaleX = shadowScale
                    scaleY = shadowScale
                    alpha = 1.3f - shadowScale // Fades out as the ball rises (scale shrinks)
                }
                .width(54.dp)
                .height(10.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent),
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(50) 
                )
        )
    }
}