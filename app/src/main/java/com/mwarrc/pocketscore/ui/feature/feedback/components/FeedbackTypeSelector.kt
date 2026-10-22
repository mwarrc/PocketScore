package com.mwarrc.pocketscore.ui.feature.feedback.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.util.FeedbackType

/**
 * A segmented button row for selecting the type of feedback.
 * 
 * @param selectedType The currently selected [FeedbackType].
 * @param onTypeSelected Callback when a new type is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackTypeSelector(
    selectedType: FeedbackType,
    onTypeSelected: (FeedbackType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Category",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            FeedbackType.entries.filter { it != FeedbackType.OTHER }.forEachIndexed { index, type ->
                val label = when (type) {
                    FeedbackType.BUG_REPORT -> "Bug"
                    FeedbackType.FEATURE_REQUEST -> "Idea"
                    FeedbackType.CONTACT -> "Chat"
                    else -> type.name
                }
                val icon = when (type) {
                    FeedbackType.BUG_REPORT -> Icons.Default.BugReport
                    FeedbackType.FEATURE_REQUEST -> Icons.Default.Lightbulb
                    FeedbackType.CONTACT -> Icons.AutoMirrored.Filled.Chat
                    else -> Icons.AutoMirrored.Filled.Chat
                }
                
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 3),
                    onClick = { onTypeSelected(type) },
                    selected = selectedType == type,
                    label = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                )
            }
        }
    }
}
