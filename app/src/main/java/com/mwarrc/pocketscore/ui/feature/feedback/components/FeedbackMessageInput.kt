package com.mwarrc.pocketscore.ui.feature.feedback.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Large text input field for the feedback message.
 * 
 * @param value The current text value.
 * @param onValueChange Callback when text changes.
 * @param maxChars Maximum character limit.
 */
@Composable
fun FeedbackMessageInput(
    value: String,
    onValueChange: (String) -> Unit,
    maxChars: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Description",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = value,
                onValueChange = { 
                    if (it.length <= maxChars) {
                        onValueChange(it)
                    }
                },
                placeholder = { Text("What's on your mind? Be as detailed as you can. We love stories!") },
                modifier = Modifier.fillMaxWidth().height(240.dp),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
            Text(
                text = "${value.length} / $maxChars",
                style = MaterialTheme.typography.labelSmall,
                color = if (value.length > maxChars * 0.9) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 12.dp, end = 16.dp)
            )
        }
    }
}
