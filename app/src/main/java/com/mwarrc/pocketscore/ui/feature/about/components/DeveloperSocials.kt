package com.mwarrc.pocketscore.ui.feature.about.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.R

/**
 * Anchored bottom section for the About screen containing social links.
 * 
 * Features a refined layout with a call-to-action message, 
 * social icon row with animations, and developer credit.
 */
@Composable
fun DeveloperSocials() {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Call to Action
        Text(
            text = "Chat on IG or simply leave a follow to show your support.",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 48.dp)
                .padding(bottom = 20.dp)
        )

        // Social Icons Row
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InstagramButton(
                onClick = { uriHandler.openUri("https://instagram.com/mwarrc") }
            )
            SocialButton(
                iconRes = R.drawable.ic_brand_github,
                contentDescription = "GitHub",
                onClick = { uriHandler.openUri("https://github.com/mwarrc/PocketScore") }
            )
            SocialButton(
                iconRes = R.drawable.ic_brand_x,
                contentDescription = "X (Twitter)",
                onClick = { uriHandler.openUri("https://twitter.com/mwarrc") }
            )
            SocialButton(
                iconVector = Icons.Default.Email,
                contentDescription = "Email",
                onClick = { uriHandler.openUri("mailto:mwarrc.dev@gmail.com") }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Developer Credit
        Text(
            text = "Mwariri Clinton",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            letterSpacing = 0.5.sp
        )

        Spacer(Modifier.height(4.dp))

        // "Built with love" Attribution
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

/**
 * Refined Instagram button with pulse animation and gradient icon.
 */
@Composable
private fun InstagramButton(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "ig_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val igGradient = remember {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFF833AB4), Color(0xFFC13584), Color(0xFFE1306C),
                Color(0xFFFD1D1D), Color(0xFFF56040), Color(0xFFF77737)
            )
        )
    }

    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .scale(scale),
        shape = CircleShape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_brand_instagram),
            contentDescription = "Instagram",
            modifier = Modifier
                .size(22.dp)
                .graphicsLayer(alpha = 0.99f)
                .drawWithCache {
                    onDrawWithContent {
                        drawContent()
                        drawRect(igGradient, blendMode = BlendMode.SrcIn)
                    }
                }
        )
    }
}

/**
 * generic social icon button with uniform styling.
 */
@Composable
private fun SocialButton(
    iconRes: Int? = null,
    iconVector: ImageVector? = null,
    contentDescription: String,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        if (iconRes != null) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else if (iconVector != null) {
            Icon(
                imageVector = iconVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
