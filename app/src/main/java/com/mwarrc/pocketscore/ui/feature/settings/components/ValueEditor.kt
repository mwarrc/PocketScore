package com.mwarrc.pocketscore.ui.feature.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mwarrc.pocketscore.ui.feature.settings.getBallColor

/**
 * An editor card for a single pool ball's point value.
 *
 * Replaces the raw text-field approach with a stepper UI:
 * - A **−** button decrements the value (clamped at 0).
 * - A **+** button increments the value.
 * - The current value is displayed prominently in the centre.
 * - The ball's colour dot and number are shown at the top for quick identification.
 *
 * The card border turns **error-red** when the value exceeds 100 as a visual warning.
 *
 * @param number        The ball number (1–15).
 * @param value         The current point value as a string (may be empty during editing).
 * @param onValueChange Callback invoked with the new string value whenever it changes.
 * @param modifier      Optional [Modifier] for the root [Surface].
 */
@Composable
fun ValueEditor(
    number: Int,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val intValue = value.toIntOrNull() ?: 0
    val isHigh = intValue > 100
    val ballColor = getBallColor(number)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(
            width = if (isHigh) 2.dp else 1.dp,
            color = if (isHigh)
                MaterialTheme.colorScheme.error
            else
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Ball indicator ───────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(ballColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = number.toString(),
                    fontSize = if (number >= 10) 9.sp else 11.sp,
                    fontWeight = FontWeight.Black,
                    color = if (number in listOf(1, 2, 13)) Color.Black else Color.White
                )
            }

            // ── Stepper row ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Decrement
                FilledIconButton(
                    onClick = {
                        val next = (intValue - 1).coerceAtLeast(0)
                        onValueChange(next.toString())
                    },
                    modifier = Modifier.size(30.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Icon(
                        Icons.Default.Remove,
                        contentDescription = "Decrease value for ball $number",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Value display
                Text(
                    text = if (value.isEmpty()) "0" else value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        color = if (isHigh)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )

                // Increment
                FilledIconButton(
                    onClick = {
                        val next = intValue + 1
                        onValueChange(next.toString())
                    },
                    modifier = Modifier.size(30.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Increase value for ball $number",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // ── "pts" label ──────────────────────────────────────────────
            Text(
                text = "pts",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
