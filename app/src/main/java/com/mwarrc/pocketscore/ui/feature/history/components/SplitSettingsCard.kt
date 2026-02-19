package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.SettlementMethod

/**
 * Header card for the Match Split tab. 
 * Displays the total amount to settle and provides quick access to settlement settings.
 * 
 * @param totalAmount Total session amount for all selected matches
 * @param settings Current app settings
 * @param onUpdateSettings Callback to update settlement preferences
 */
@Composable
fun SplitSettingsCard(
    totalAmount: Double,
    settings: AppSettings,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit
) {
    var showQuickSettings by remember { mutableStateOf(false) }
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (totalAmount > 0) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp), 
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Summary Header (interactive to expand/collapse)
            SplitSummaryHeader(
                totalAmount = totalAmount,
                currencySymbol = settings.currencySymbol,
                matchCost = settings.matchCost,
                settlementMethod = settings.settlementMethod,
                isExpanded = showQuickSettings,
                onClick = { showQuickSettings = !showQuickSettings }
            )

            // Animated Settings Section
            AnimatedVisibility(visible = showQuickSettings) {
                Column(
                    modifier = Modifier.padding(top = 16.dp), 
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    
                    MatchCostEditor(
                        matchCost = settings.matchCost,
                        currencySymbol = settings.currencySymbol,
                        onUpdate = onUpdateSettings
                    )
                    
                    SettlementRuleSelector(
                        currentMethod = settings.settlementMethod,
                        lastLosersCount = settings.lastLosersCount,
                        onUpdate = onUpdateSettings
                    )
                }
            }
        }
    }
}

@Composable
private fun SplitSummaryHeader(
    totalAmount: Double,
    currencySymbol: String,
    matchCost: Double,
    settlementMethod: com.mwarrc.pocketscore.domain.model.SettlementMethod,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp) // Added padding to prevent cutoff
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "  Total Session to Settle", 
                style = MaterialTheme.typography.labelMedium, 
                color = if (totalAmount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$currencySymbol ${String.format("%.0f", totalAmount)}",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = if (totalAmount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
                Surface(
                    color = (if (totalAmount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = settlementMethod.name.replace("_", " "),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (totalAmount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                if (!isExpanded) {
                    Surface(
                        color = if (totalAmount > 0) 
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp, 
                            (if (totalAmount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "$currencySymbol${matchCost.toString().removeSuffix(".0")}/match",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (totalAmount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Tune,
                    contentDescription = if (isExpanded) "Collapse Settings" else "Show Settings",
                    tint = if (totalAmount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
        }
    }
}

@Composable
private fun MatchCostEditor(
    matchCost: Double,
    currencySymbol: String,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit
) {
    var symbolText by remember { mutableStateOf(currencySymbol) }
    var costText by remember(matchCost) { mutableStateOf(if (matchCost % 1.0 == 0.0) matchCost.toInt().toString() else matchCost.toString()) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = costText,
            onValueChange = { newValue ->
                if (newValue.isEmpty() || newValue.all { it.isDigit() || it == '.' }) {
                    if (newValue.count { it == '.' } <= 1) {
                        costText = newValue
                        val dValue = newValue.toDoubleOrNull()
                        if (dValue != null) {
                            onUpdate { s -> s.copy(matchCost = dValue) }
                        }
                    }
                }
            },
            label = { Text("Cost per Match") },
            modifier = Modifier.weight(1f),
            prefix = { 
                Text(
                    text = symbolText,
                    modifier = Modifier.padding(end = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ) 
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
        
        OutlinedTextField(
            value = symbolText,
            onValueChange = { newSymbol -> 
                symbolText = newSymbol
                onUpdate { s -> s.copy(currencySymbol = newSymbol) } 
            },
            label = { Text("Symbol") },
            modifier = Modifier.width(90.dp),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
            )
        )
    }
}

@Composable
private fun SettlementRuleSelector(
    currentMethod: SettlementMethod,
    lastLosersCount: Int,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Settlement Rule", 
            style = MaterialTheme.typography.labelLarge, 
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                .padding(4.dp)
        ) {
            val methods = listOf(
                Triple(SettlementMethod.LOSERS_PAY, "Losers", Icons.Default.PersonRemove),
                Triple(SettlementMethod.ALL_SPLIT, "All Split", Icons.Default.Groups),
                Triple(SettlementMethod.LAST_N_PAY, "Bottom X", Icons.Default.FormatListNumbered)
            )
            
            methods.forEach { (method, label, icon) ->
                val isSelected = currentMethod == method
                Surface(
                    onClick = { onUpdate { it.copy(settlementMethod = method) } },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(icon, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        AnimatedVisibility(visible = currentMethod == SettlementMethod.LAST_N_PAY) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Number of losers to pay:",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                
                LoserCountStepper(
                    count = lastLosersCount,
                    onUpdate = { newCount -> 
                        onUpdate { it.copy(lastLosersCount = newCount) } 
                    }
                )
            }
        }
    }
}

@Composable
private fun LoserCountStepper(
    count: Int,
    onUpdate: (Int) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(horizontal = 4.dp)
    ) {
        IconButton(
            onClick = { if (count > 1) onUpdate(count - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
        }
        
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        IconButton(
            onClick = { onUpdate(count + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
        }
    }
}
