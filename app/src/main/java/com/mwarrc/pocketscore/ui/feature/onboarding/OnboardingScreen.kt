package com.mwarrc.pocketscore.ui.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.SportsScore
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val description: String,
    val useCustomIcon: Boolean = false
)

// Particle data class for animated background
data class Particle(
    val id: Int,
    val startX: Float,
    val startY: Float,
    val speed: Float,
    val size: Float,
    val alpha: Float
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val pages = remember {
        listOf(
            OnboardingPage(
                title = "Track with Purpose",
                subtitle = "THE MISSION",
                description = "Built for those who value precision. Every point matters, every game counts."
            ),
            OnboardingPage(
                title = "Designed to Disappear",
                subtitle = "THE PHILOSOPHY",
                description = "Minimal interface. Maximum focus. Your scoreboard shouldn't distract from the game."
            ),
            OnboardingPage(
                title = "Ready When You Are",
                subtitle = "LET'S BEGIN",
                description = "No accounts. No setup. Just you, your friends, and the game ahead.",
                useCustomIcon = true
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    
    var isAutoScrolling by remember { mutableStateOf(true) }
    var dragOffset by remember { mutableFloatStateOf(0f) }

    // Auto-scroll with smooth stopping
    LaunchedEffect(isAutoScrolling) {
        if (isAutoScrolling) {
            while (pagerState.currentPage < pages.size - 1) {
                delay(4000)
                if (isAutoScrolling) {
                    pagerState.animateScrollToPage(
                        pagerState.currentPage + 1,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }
            isAutoScrolling = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Animated particle background
        AnimatedBackgroundParticles(pagerState)

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { 
                            isAutoScrolling = false
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            dragOffset = dragAmount.x
                        },
                        onDragEnd = { dragOffset = 0f }
                    )
                }
        ) { pageIndex ->
            OnboardingPageContent(
                page = pages[pageIndex],
                pageIndex = pageIndex,
                pagerState = pagerState
            )
        }

        // Bottom controls with enhanced animations
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Enhanced page indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                repeat(pages.size) { index ->
                    EnhancedPageIndicator(
                        isActive = index == pagerState.currentPage,
                        isPast = index < pagerState.currentPage,
                        onClick = {
                            isAutoScrolling = false
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(
                                    index,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy
                                    )
                                )
                            }
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
                }
            }

            // Animated "Get Started" button
            AnimatedVisibility(
                visible = pagerState.currentPage == pages.size - 1,
                enter = fadeIn(tween(400)) + slideInVertically { it / 2 } + scaleIn(tween(400)),
                exit = fadeOut(tween(200)) + slideOutVertically { it / 2 } + scaleOut(tween(200))
            ) {
                PulsingGetStartedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onComplete()
                    }
                )
            }
        }

        // Enhanced Skip button with fade and slide
        AnimatedVisibility(
            visible = pagerState.currentPage < pages.size - 1,
            enter = fadeIn(tween(300)) + slideInVertically { -it },
            exit = fadeOut(tween(200)) + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onComplete()
                },
                modifier = Modifier.padding(top = 48.dp, end = 16.dp)
            ) {
                Text(
                    "SKIP",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )
            }
        }
    }
}

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
    val parallaxOffset = (pageOffset * 50).dp
    
    // Breathing animation for icon
    val infiniteTransition = rememberInfiniteTransition(label = "breath")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_scale"
    )
    
    // Rotation animation
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    // Scale based on active page
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
        animationSpec = tween(400),
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

        // Enhanced icon container with multiple effects
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
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                Brush.sweepGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                )
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Animated icon with content transition
                AnimatedContent(
                    targetState = page.useCustomIcon,
                    transitionSpec = {
                        fadeIn(tween(400)) + scaleIn(tween(400)) togetherWith
                                fadeOut(tween(200)) + scaleOut(tween(200))
                    },
                    label = "icon_transition"
                ) { useCustom ->
                    if (useCustom) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pocket_logo),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = when (pageIndex) {
                                0 -> Icons.Default.SportsScore
                                1 -> Icons.Default.Lightbulb
                                else -> Icons.Default.Check
                            },
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(56.dp))

        // Animated text content
        AnimatedContent(
            targetState = page.subtitle,
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically { it / 2 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically { -it / 2 })
            },
            label = "subtitle"
        ) { subtitle ->
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        AnimatedContent(
            targetState = page.title,
            transitionSpec = {
                (fadeIn(tween(400, delayMillis = 100)) + slideInVertically { it / 2 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically { -it / 2 })
            },
            label = "title"
        ) { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                lineHeight = 40.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(Modifier.height(20.dp))

        AnimatedContent(
            targetState = page.description,
            transitionSpec = {
                (fadeIn(tween(400, delayMillis = 200)) + slideInVertically { it / 2 }) togetherWith
                        (fadeOut(tween(200)) + slideOutVertically { -it / 2 })
            },
            label = "description"
        ) { description ->
            Text(
                text = description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(Modifier.weight(0.7f))
    }
}

@Composable
fun EnhancedPageIndicator(
    isActive: Boolean,
    isPast: Boolean,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    
    val width by animateDpAsState(
        targetValue = if (isActive) 40.dp else 8.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "indicator_width"
    )

    val height by animateDpAsState(
        targetValue = if (isActive) 10.dp else 8.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "indicator_height"
    )

    val alpha by animateFloatAsState(
        targetValue = when {
            isActive -> 1f
            isPast -> 0.6f
            else -> 0.3f
        },
        label = "indicator_alpha"
    )

    Surface(
        modifier = Modifier
            .width(width)
            .height(height)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                }
            },
        shape = RoundedCornerShape(5.dp),
        color = if (isActive) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }
    ) {}
}

@Composable
fun PulsingGetStartedButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.0f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        // Glow effect
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(1.02f)
                .alpha(glowAlpha),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primary
        ) {}
        
        // Main button
        FilledTonalButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .scale(pulseScale),
            shape = RoundedCornerShape(16.dp)
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
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AnimatedBackgroundParticles(pagerState: PagerState) {
    val particles = remember {
        List(30) { index ->
            Particle(
                id = index,
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                speed = Random.nextFloat() * 0.5f + 0.3f,
                size = Random.nextFloat() * 4f + 2f,
                alpha = Random.nextFloat() * 0.3f + 0.1f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val animatedTime by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.4f)
    ) {
        val width = size.width
        val height = size.height

        particles.forEach { particle ->
            val progress = (animatedTime * particle.speed) % 1f
            val x = particle.startX * width + (progress * width * 0.1f)
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