package com.mwarrc.pocketscore.ui.feature.onboarding.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.R
import com.mwarrc.pocketscore.ui.feature.onboarding.OnboardingPage
import com.mwarrc.pocketscore.ui.feature.onboarding.Particle
import kotlin.random.Random

// --- COMPONENTS CONFIGURATION ---

private object ComponentConstants {
    const val BREATH_DURATION = 2500
    const val ROTATION_DURATION = 3000
    const val CONTENT_ALPHA_DURATION = 400
    const val ICON_TRANSITION_ENTER = 400
    const val ICON_TRANSITION_EXIT = 200
    const val PULSE_DURATION = 1200
    const val PARTICLE_CYCLE_DURATION = 30000
    const val PARALLAX_FACTOR = 50f
}

/**
 * Renders the content for a single onboarding page.
 * Includes parallax effects, breathing animations, and dynamic icon transitions.
 *
 * @param page The [OnboardingPage] data to display.
 * @param pageIndex The index of the page in the pager.
 * @param pagerState Current [PagerState] for calculating scroll offsets.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPageContent(
    page: OnboardingPage,
    pageIndex: Int,
    pagerState: PagerState
) {
    val haptic = LocalHapticFeedback.current
    
    // Calculate parallax effect based on page scroll
    val pageOffset = (pagerState.currentPage - pageIndex) + pagerState.currentPageOffsetFraction
    val parallaxOffset = (pageOffset * ComponentConstants.PARALLAX_FACTOR).dp
    
    // Breathing animation for icon
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(ComponentConstants.BREATH_DURATION, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )
    
    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(ComponentConstants.ROTATION_DURATION, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Scale and Alpha based on active page
    val contentScale by animateFloatAsState(
        targetValue = if (pageIndex == pagerState.currentPage) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "content_scale"
    )
    
    val contentAlpha by animateFloatAsState(
        targetValue = if (pageIndex == pagerState.currentPage) 1f else 0.3f,
        animationSpec = tween(ComponentConstants.CONTENT_ALPHA_DURATION),
        label = "content_alpha"
    )

    var iconPressed by remember { mutableStateOf(false) }
    val pressScale by animateFloatAsState(
        targetValue = if (iconPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .offset(y = parallaxOffset)
            .scale(contentScale)
            .alpha(contentAlpha)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(Modifier.weight(0.3f))

        // Enhanced icon container with glassmorphism feel
        Surface(
            modifier = Modifier
                .size(140.dp)
                .scale(breathScale * pressScale)
                .rotate(rotation)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            iconPressed = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            tryAwaitRelease()
                            iconPressed = false
                        }
                    )
                },
            shape = RoundedCornerShape(40.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp,
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = page,
                    transitionSpec = {
                        fadeIn(tween(ComponentConstants.ICON_TRANSITION_ENTER)) + scaleIn(tween(ComponentConstants.ICON_TRANSITION_ENTER), initialScale = 0.8f) togetherWith
                                fadeOut(tween(ComponentConstants.ICON_TRANSITION_EXIT)) + scaleOut(tween(ComponentConstants.ICON_TRANSITION_EXIT), targetScale = 1.2f)
                    },
                    label = "icon_transition"
                ) { targetPage ->
                    if (targetPage.customIconRes != null) {
                        Icon(
                            painter = painterResource(id = targetPage.customIconRes),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = targetPage.accentColor ?: MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = targetPage.icon ?: Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = targetPage.accentColor ?: MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(56.dp))

        // Subtitle - Label text
        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.labelLarge,
            color = page.accentColor ?: MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp,
            modifier = Modifier.alpha(0.8f)
        )

        Spacer(Modifier.height(12.dp))

        // Title - Headline
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            lineHeight = 38.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(Modifier.height(20.dp))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(Modifier.weight(0.7f))
    }
}

/**
 * A modern page indicator dot that expands when active.
 *
 * @param isActive Whether this indicator represents the current page.
 * @param isPast Whether this indicator represents a page the user has already seen.
 * @param onClick Callback when the indicator is tapped.
 */
@Composable
fun EnhancedPageIndicator(
    isActive: Boolean,
    isPast: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    val width by animateDpAsState(
        targetValue = if (isActive) 32.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicator_width"
    )

    val alpha by animateFloatAsState(
        targetValue = when {
            isActive -> 1f
            isPast -> 0.6f
            else -> 0.3f
        },
        label = "indicator_alpha"
    )

    Box(
        modifier = Modifier
            .width(width)
            .height(8.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(
                if (isActive) MaterialTheme.colorScheme.primary 
                else MaterialTheme.colorScheme.outlineVariant
            )
            .pointerInput(Unit) {
                detectTapGestures {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            }
    )
}

/**
 * A prominent call-to-action button with a pulsing animation and subtle glow.
 *
 * @param onClick Callback when the button is clicked.
 */
@Composable
fun PulsingGetStartedButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(ComponentConstants.PULSE_DURATION, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(ComponentConstants.PULSE_DURATION, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        // Subtle background glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .scale(pulseScale)
                .alpha(glowAlpha)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
        )
        
        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "GET STARTED",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp
                )
                Spacer(Modifier.width(12.dp))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Ambient background animation featuring floating white particles.
 * Motion is subtly linked to the current time and page state.
 *
 * @param pagerState Current [PagerState] to potentially link motion to swipe gestures.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedBackgroundParticles(pagerState: PagerState) {
    val particles = remember {
        List(25) { index ->
            Particle(
                id = index,
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                speed = Random.nextFloat() * 0.3f + 0.2f,
                size = Random.nextFloat() * 4f + 2f,
                alpha = Random.nextFloat() * 0.25f + 0.05f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animatedTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(ComponentConstants.PARTICLE_CYCLE_DURATION, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.3f)
    ) {
        val width = size.width
        val height = size.height

        particles.forEach { particle ->
            val progress = (animatedTime * particle.speed) % 1f
            val x = particle.startX * width + (progress * width * 0.05f)
            val y = (particle.startY + progress) * height % height

            drawCircle(
                color = Color.White,
                radius = particle.size,
                center = Offset(x, y),
                alpha = particle.alpha
            )
        }
    }
}
