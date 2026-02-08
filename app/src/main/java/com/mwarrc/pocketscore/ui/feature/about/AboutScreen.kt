package com.mwarrc.pocketscore.ui.feature.about

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.R
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRoadmap: () -> Unit,
    showComingSoonFeatures: Boolean = true,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var logoClickCount by remember { mutableIntStateOf(0) }
    val uriHandler = LocalUriHandler.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "PocketScore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(top = 32.dp),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (showComingSoonFeatures) return@clickable // Already enabled

                                logoClickCount++
                                if (logoClickCount >= 8) {
                                    onUpdateSettings { it.copy(showComingSoonFeatures = true) }
                                    logoClickCount = 0
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Developer features unlocked! ")
                                    }
                                } else {
                                    // Optional: Subtle haptic or feedback?
                                }
                            },
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        shadowElevation = 4.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_pocket_logo),
                                contentDescription = null,
                                modifier = Modifier.size(44.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "v0.1.2 Expressive",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(0.6f)
                    )

                    if (showComingSoonFeatures) {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Dev Mode",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(8.dp))
                            Switch(
                                checked = true,
                                onCheckedChange = { 
                                    onUpdateSettings { it.copy(showComingSoonFeatures = false) }
                                },
                                modifier = Modifier.scale(0.7f),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    AboutInfoCard(
                        icon = Icons.Default.AutoAwesome,
                        title = "The Experience",
                        description = "Crafted for focus. Featuring Material You dynamic coloring, tactile haptic feedback, and a seamless interface that adapts to your device's aesthetic."
                    )
                    
                    if (showComingSoonFeatures) {
                        Spacer(Modifier.height(12.dp))
                        AboutInfoCard(
                            icon = Icons.Default.Timeline,
                            title = "Future Roadmap",
                            description = "Next milestones: Real-time multiplayer sync, advanced game statistics, and personalized player profiles.",
                            onClick = onNavigateToRoadmap,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    AboutInfoCard(
                        icon = Icons.Default.Terminal,
                        title = "Open Source & Tech",
                        description = "100% Kotlin & Jetpack Compose. PocketScore is open-source, built with Material 3 principles and type-safe DataStore for a modern, high-performance architecture."
                    )

                    Spacer(Modifier.height(12.dp))

                    AboutInfoCard(
                        icon = Icons.Filled.Lock,
                        title = "Privacy Focused",
                        description = "Zero tracking, zero ads. All your game data stays on your device. Offline-first architecture ensures you can play anywhere, anytime."
                    )
                    
                    Spacer(Modifier.height(120.dp)) // Padding for bottom anchored section
                }
            }

            // Anchored Developer Section at the Bottom
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Chat on IG or simply leave a follow to show your support.",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 48.dp).padding(bottom = 16.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InstagramPromo(
                        onClick = { uriHandler.openUri("https://instagram.com/mwarrc") }
                    )
                    MinimalSocialButton(
                        iconRes = R.drawable.ic_brand_github,
                        contentDescription = "GitHub",
                        onClick = { uriHandler.openUri("https://github.com/mwarrc/PocketScore") }
                    )
                    MinimalSocialButton(
                        iconRes = R.drawable.ic_brand_x,
                        contentDescription = "X (Twitter)",
                        onClick = { uriHandler.openUri("https://twitter.com/mwarrc") }
                    )
                    MinimalSocialButton(
                        iconVector = Icons.Default.Email,
                        contentDescription = "Email",
                        onClick = { uriHandler.openUri("mailto:mwarrc.dev@gmail.com") }
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Mwariri Clinton",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Built with",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(10.dp),
                        tint = Color(0xFFE91E63).copy(alpha = 0.7f)
                    )
                    Text(
                        text = "in Kenya",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }


    }
}

@Composable
fun AboutInfoCard(
    icon: ImageVector,
    title: String,
    description: String,
    containerColor: Color = Color.Transparent,
    iconColor: Color = MaterialTheme.colorScheme.primary,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null, onClick = onClick ?: {})
            .padding(vertical = 12.dp, horizontal = 4.dp), // Added horizontal padding for touch target
        verticalAlignment = Alignment.Top
    ) {
        // Minimal Icon - Direct, no background container
        Icon(
            icon,
            null,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp), // Visual alignment with text cap height
            tint = iconColor
        )

        Spacer(Modifier.width(20.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                
                // Show arrow if clickable
                if (onClick != null) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            Spacer(Modifier.height(4.dp))
            
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun InstagramPromo(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "ig_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val brush = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF833AB4),
                Color(0xFFC13584),
                Color(0xFFE1306C),
                Color(0xFFFD1D1D),
                Color(0xFFF56040),
                Color(0xFFF77737),
                Color(0xFFFCAF45),
                Color(0xFFFFDC80)
            )
        )
    }

    Box(contentAlignment = Alignment.Center) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_brand_instagram),
                contentDescription = "Instagram",
                modifier = Modifier
                    .size(24.dp)
                    .scale(scale)
                    .graphicsLayer(alpha = 0.99f)
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            drawRect(brush, blendMode = BlendMode.SrcIn)
                        }
                    }
            )
        }

        // Thought-style Bubble
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 12.dp, y = (-12).dp)
        ) {
            // Main Cloud
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.scale(0.9f),
                shadowElevation = 3.dp
            ) {
                Text(
                    "Connect",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            
            // Thought trail
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier
                    .size(6.dp)
                    .offset(x = 2.dp, y = 22.dp),
                shadowElevation = 1.dp
            ) {}
            
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier
                    .size(4.dp)
                    .offset(x = (-2).dp, y = 28.dp),
                shadowElevation = 1.dp
            ) {}
        }
    }
}

@Composable
fun MinimalSocialButton(
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp)
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        } else if (iconVector != null) {
            Icon(
                imageVector = iconVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}
