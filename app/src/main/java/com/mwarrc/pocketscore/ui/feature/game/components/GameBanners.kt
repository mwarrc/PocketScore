package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.Player

/**
 * Persistent banner stating that the game is running in strict turn mode.
 *
 * Strict mode is a rule, not an event, so it is stated once and stays on for
 * as long as it is in force — no toggle, no timeout, no free-mode variant.
 *
 * It used to be a setting ("In-Match Mode Alerts") that switched a banner on
 * and off, and the per-card "Turn Locked" label tried to say the same thing
 * on every card that wasn't the turn holder — inconsistently, because the
 * label was suppressed for leaders and losers. One banner at the top says it
 * once, for everybody, and the cards can stay quiet.
 *
 * @param settings Current application settings
 */
@Composable
fun StrictModeBanner(
    settings: AppSettings
) {
    AnimatedVisibility(
        visible = settings.strictTurnMode,
        enter = fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.95f),
        exit = fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.95f)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier.padding(12.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Lock,
                    null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (settings.enforceStrictMode) {
                        "Strict Mode Locked: only the player on turn can score"
                    } else {
                        "Strict Mode: only the player on turn can score"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}


