package com.mwarrc.pocketscore.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun ElegantConfetti(
    modifier: Modifier = Modifier,
    trigger: Boolean,
    onAnimationFinished: () -> Unit
) {
    if (!trigger) return

    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    // Elegant palette: Gold, Silver, and Theme Colors
    val colors = remember(primaryColor, secondaryColor) {
        listOf(
            primaryColor,
            secondaryColor,
            tertiaryColor,
            Color(0xFFFFD700), // Gold
            Color(0xFFC0C0C0), // Silver
            Color(0xFFE5E4E2)  // Platinum
        )
    }

    val particles = remember {
        List(60) { // Not too many, keep it elegant
            generateParticle(colors)
        }
    }

    var time by remember { mutableLongStateOf(0L) }

    LaunchedEffect(trigger) {
        val startTime = System.nanoTime()
        while (isActive) {
            val currentTime = System.nanoTime()
            time = (currentTime - startTime) / 1_000_000 // ms
            
            if (time > 2000) { // 2 seconds duration
                break
            }
            withFrameNanos { }
        }
        delay(300) // Small buffer
        onAnimationFinished()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        
        // Time in seconds for physics
        val t = time / 1000f 
        
        particles.forEach { particle ->
            // Physics: Projectile motion with drag
            // x = x0 + vx * t
            // y = y0 + vy * t + 0.5 * g * t^2
            
            val gravity = 1000f * density.density // Gravity
            val drag = 0.95f // Air resistance simulation factor (simplistic)
            
            // Apply simplistic drag by reducing velocity effect over time
            val currentX = centerX + (particle.vx * t * 1500f) // Scale for screen
            val currentY = centerY + (particle.vy * t * 1500f) + (0.5f * gravity * t * t)
            
            val rotation = particle.initialRotation + (particle.rotationSpeed * t)
            val fade = (1f - (t / 2f)).coerceIn(0f, 1f) // Fade out near end

            withTransform({
                translate(currentX, currentY)
                rotate(rotation)
                scale(scaleX = 1f, scaleY = cos(rotation * 0.1f).coerceAtLeast(0.1f)) // 3D flutter effect
            }) {
                drawRoundRect(
                    color = particle.color.copy(alpha = fade),
                    topLeft = Offset(-particle.size / 2, -particle.size / 2),
                    size = Size(particle.size, particle.size * 1.5f), // Rectangular confetti
                    cornerRadius = CornerRadius(particle.size / 4)
                )
            }
        }
    }
}

private data class ParticleConfig(
    val vx: Float,
    val vy: Float,
    val initialRotation: Float,
    val rotationSpeed: Float,
    val color: Color,
    val size: Float
)

private fun generateParticle(colors: List<Color>): ParticleConfig {
    val angle = Random.nextFloat() * 2 * PI.toFloat()
    val speed = Random.nextFloat() * 0.5f + 0.5f // Velocity magnitude
    
    // Explosion pattern: upward bias
    // We want them to shoot up and out, like a fountain
    val fountainAngle = -PI.toFloat() / 2 + (Random.nextFloat() - 0.5f) * 2 // Mainly Up
    
    return ParticleConfig(
        vx = cos(fountainAngle) * speed * (Random.nextFloat() + 0.5f),
        vy = sin(fountainAngle) * speed * (Random.nextFloat() + 0.8f), // Stronger upward force
        initialRotation = Random.nextFloat() * 360f,
        rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
        color = colors.random(),
        size = Random.nextFloat() * 15f + 10f // Size
    )
}
