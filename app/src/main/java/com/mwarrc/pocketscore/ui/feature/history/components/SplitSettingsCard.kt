package com.mwarrc.pocketscore.ui.feature.history.components
import androidx.compose.foundation.layout.ExperimentalLayoutApi

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
@OptIn(ExperimentalLayoutApi::class)
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
                decimals = settings.settlementRoundingDecimals,
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
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rounding Decimals:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        RoundingDecimalsStepper(
                            decimals = settings.settlementRoundingDecimals,
                            onUpdate = { newDecimals ->
                                onUpdateSettings { it.copy(settlementRoundingDecimals = newDecimals) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SplitSummaryHeader(
    totalAmount: Double,
    currencySymbol: String,
    decimals: Int,
    matchCost: Double,
    settlementMethod: com.mwarrc.pocketscore.domain.model.SettlementMethod,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val methodText = when (settlementMethod) {
        com.mwarrc.pocketscore.domain.model.SettlementMethod.LOSERS_PAY -> "Losers Pay"
        com.mwarrc.pocketscore.domain.model.SettlementMethod.ALL_SPLIT -> "All Split"
        com.mwarrc.pocketscore.domain.model.SettlementMethod.LAST_N_PAY -> "Bottom X Pay"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "TOTAL TO SETTLE", 
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = (if (totalAmount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant).copy(alpha = 0.8f)
            )
            
            val formatString = if (decimals == 0) "%.0f" else "%.${decimals}f"
            Text(
                text = "$currencySymbol ${String.format(formatString, totalAmount)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = if (totalAmount > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )

            AnimatedVisibility(visible = !isExpanded) {
                Text(
                    text = "$methodText   •   $currencySymbol${matchCost.toString().removeSuffix(".0")} / match",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = CircleShape,
            color = if (totalAmount > 0) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.Tune,
                    contentDescription = if (isExpanded) "Collapse Settings" else "Show Settings",
                    tint = if (totalAmount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MatchCostEditor(
    matchCost: Double,
    currencySymbol: String,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit
) {
    // Which field is active: null=none, "cost", "symbol"
    var activeField by remember { mutableStateOf<String?>(null) }

    var costText by remember(matchCost) {
        mutableStateOf(if (matchCost % 1.0 == 0.0) matchCost.toInt().toString() else matchCost.toString())
    }
    var symbolText by remember(currencySymbol) { mutableStateOf(currencySymbol) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // ── Display Row ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cost display chip
            Surface(
                onClick = { activeField = if (activeField == "cost") null else "cost" },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = if (activeField == "cost")
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (activeField == "cost") MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = symbolText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = costText.ifEmpty { "0" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (activeField == "cost")
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "/ match",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Currency symbol chips
            val symbols = listOf("$", "€", "£", "Ksh", "¥")
            // Show current symbol + presets in a compact row
            Surface(
                onClick = { activeField = if (activeField == "symbol") null else "symbol" },
                shape = RoundedCornerShape(12.dp),
                color = if (activeField == "symbol")
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (activeField == "symbol") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    text = symbolText.ifEmpty { "¤" },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (activeField == "symbol")
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // ── Inline numpad for "cost" ──────────────────────────────────────────
        AnimatedVisibility(visible = activeField == "cost") {
            InlineNumpad(
                value = costText,
                showDecimal = true,
                onKey = { key ->
                    val next = when (key) {
                        "⌫" -> if (costText.length > 1) costText.dropLast(1) else "0"
                        "C" -> "0"
                        "." -> if ("." !in costText) "$costText." else costText
                        else -> if (costText == "0") key else costText + key
                    }
                    costText = next
                    next.toDoubleOrNull()?.let { v -> onUpdate { s -> s.copy(matchCost = v) } }
                }
            )
        }

        // ── Quick-select symbol chips when "symbol" active ───────────────────
        AnimatedVisibility(visible = activeField == "symbol") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val presets = listOf("$", "€", "£", "Ksh", "¥", "₹", "₩", "₺", "CHF")
                Text(
                    "Quick select or type a symbol below",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presets.forEach { sym ->
                        FilterChip(
                            selected = symbolText == sym,
                            onClick = {
                                symbolText = sym
                                onUpdate { s -> s.copy(currencySymbol = sym) }
                            },
                            label = { Text(sym, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
                // Symbol inline letter pad (A-Z style but compact)
                InlineSymbolPad(
                    value = symbolText,
                    onKey = { key ->
                        val next = when (key) {
                            "⌫" -> if (symbolText.length > 1) symbolText.dropLast(1) else ""
                            "C" -> ""
                            else -> if (symbolText.length < 4) symbolText + key else symbolText
                        }
                        symbolText = next
                        onUpdate { s -> s.copy(currencySymbol = next) }
                    }
                )
            }
        }
    }
}

// ── Lightweight inline numpad (no system keyboard) ───────────────────────────

@Composable
private fun InlineNumpad(
    value: String,
    showDecimal: Boolean,
    onKey: (String) -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(if (showDecimal) "." else "C", "0", "⌫")
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { key ->
                    InlineKey(
                        label = key,
                        isAction = key == "⌫" || key == "C",
                        modifier = Modifier.weight(1f),
                        onClick = { onKey(key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineSymbolPad(
    value: String,
    onKey: (String) -> Unit
) {
    val rows = listOf(
        listOf("K", "s", "h", "€", "£"),
        listOf("₹", "₩", "₺", "¥", "⌫")
    )
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row.forEach { key ->
                    InlineKey(
                        label = key,
                        isAction = key == "⌫",
                        modifier = Modifier.weight(1f),
                        onClick = { onKey(key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InlineKey(
    label: String,
    isAction: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(8.dp),
        color = if (isAction)
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = if (isAction)
            MaterialTheme.colorScheme.error
        else
            MaterialTheme.colorScheme.onSurface
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
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

@Composable
private fun RoundingDecimalsStepper(
    decimals: Int,
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
            onClick = { if (decimals > 0) onUpdate(decimals - 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Remove, null, modifier = Modifier.size(16.dp))
        }
        
        Text(
            text = ".$decimals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        
        IconButton(
            onClick = { if (decimals < 4) onUpdate(decimals + 1) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
        }
    }
}
