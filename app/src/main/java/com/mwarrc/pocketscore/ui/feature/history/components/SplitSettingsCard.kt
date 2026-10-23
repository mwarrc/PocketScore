package com.mwarrc.pocketscore.ui.feature.history.components

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mwarrc.pocketscore.domain.model.AppSettings
import com.mwarrc.pocketscore.domain.model.SettlementMethod

/**
 * Modern header card for the Match Split / Settle tab.
 * Displays the total amount to settle and provides intuitive quick-access controls
 * for settlement rules, match cost, currency, and rounding.
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
    var isExpanded by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = if (totalAmount > 0)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Header (tap to expand/collapse settings)
            SplitSummaryHeader(
                totalAmount = totalAmount,
                currencySymbol = settings.currencySymbol,
                decimals = settings.settlementRoundingDecimals,
                matchCost = settings.matchCost,
                settlementMethod = settings.settlementMethod,
                isExpanded = isExpanded,
                onClick = { isExpanded = !isExpanded }
            )

            // Animated Settings Section
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn(tween(200)) + expandVertically(tween(250)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(200))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    // 1. Settlement Rule Selector
                    SettlementRuleSection(
                        currentMethod = settings.settlementMethod,
                        lastLosersCount = settings.lastLosersCount,
                        onUpdate = onUpdateSettings
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    // 2. Match Cost Editor with quick presets & steppers
                    MatchCostSection(
                        matchCost = settings.matchCost,
                        currencySymbol = settings.currencySymbol,
                        onUpdate = onUpdateSettings
                    )

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    )

                    // 3. Currency & Rounding row
                    CurrencyAndRoundingRow(
                        currentSymbol = settings.currencySymbol,
                        decimals = settings.settlementRoundingDecimals,
                        onUpdate = onUpdateSettings
                    )
                }
            }
        }
    }
}

/**
 * Top summary banner showing total to settle and active rule.
 */
@Composable
private fun SplitSummaryHeader(
    totalAmount: Double,
    currencySymbol: String,
    decimals: Int,
    matchCost: Double,
    settlementMethod: SettlementMethod,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val methodText = when (settlementMethod) {
        SettlementMethod.SOLE_LOSER_PAYS -> "Loser Pays"
        SettlementMethod.LOSERS_PAY -> "Losers Pay"
        SettlementMethod.ALL_SPLIT -> "Even Split"
        SettlementMethod.LAST_N_PAY -> "Bottom X"
    }

    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = tween(220),
        label = "settle_chevron"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "TOTAL TO SETTLE",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            val formatString = if (decimals == 0) "%.0f" else "%.${decimals}f"
            Text(
                text = "$currencySymbol ${String.format(formatString, totalAmount)}",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = if (totalAmount > 0)
                    MaterialTheme.colorScheme.onSurface
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            Text(
                text = "$methodText   •   $currencySymbol${matchCost.toString().removeSuffix(".0")} / match",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = if (isExpanded) "Hide Settings" else "Show Settings",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationZ = chevronRotation }
                )
            }
        }
    }
}

/**
 * Clean segmented selector for the 4 settlement rules with descriptive explanations.
 */
@Composable
private fun SettlementRuleSection(
    currentMethod: SettlementMethod,
    lastLosersCount: Int,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Settlement Logic",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        val methods = listOf(
            Triple(SettlementMethod.SOLE_LOSER_PAYS, "Loser\nPays", Icons.Default.PersonRemove),
            Triple(SettlementMethod.LOSERS_PAY, "Losers\nPay", Icons.Default.GroupRemove),
            Triple(SettlementMethod.ALL_SPLIT, "Even\nSplit", Icons.Default.Groups),
            Triple(SettlementMethod.LAST_N_PAY, "Bottom\nX", Icons.Default.FormatListNumbered)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            methods.forEach { (method, label, icon) ->
                val isSelected = currentMethod == method
                Surface(
                    onClick = { onUpdate { it.copy(settlementMethod = method) } },
                    modifier = Modifier.weight(1f).height(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }

        // Explanatory hint for the active rule
        val hintText = when (currentMethod) {
            SettlementMethod.SOLE_LOSER_PAYS -> "The single player with the lowest score pays the table cost (ties split)."
            SettlementMethod.LOSERS_PAY -> "All players except the winner share the match cost."
            SettlementMethod.ALL_SPLIT -> "Every player in the match splits the cost equally."
            SettlementMethod.LAST_N_PAY -> "The bottom $lastLosersCount players with the lowest scores split the cost."
        }

        Text(
            text = hintText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Stepper for Bottom X
        if (currentMethod == SettlementMethod.LAST_N_PAY) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Number of bottom players:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalIconButton(
                        onClick = { if (lastLosersCount > 1) onUpdate { it.copy(lastLosersCount = lastLosersCount - 1) } },
                        enabled = lastLosersCount > 1,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = "$lastLosersCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    FilledTonalIconButton(
                        onClick = { onUpdate { it.copy(lastLosersCount = lastLosersCount + 1) } },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * Match cost section with steppers and quick presets.
 */
@Composable
private fun MatchCostSection(
    matchCost: Double,
    currencySymbol: String,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Match Cost",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Current cost badge + steppers
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilledTonalIconButton(
                    onClick = {
                        val next = (matchCost - 5.0).coerceAtLeast(0.0)
                        onUpdate { it.copy(matchCost = next) }
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Decrease cost", modifier = Modifier.size(16.dp))
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$currencySymbol ${matchCost.toString().removeSuffix(".0")}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }

                FilledTonalIconButton(
                    onClick = {
                        val next = matchCost + 5.0
                        onUpdate { it.copy(matchCost = next) }
                    },
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Increase cost", modifier = Modifier.size(16.dp))
                }
            }
        }

        // Quick Preset Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val presets = listOf(5.0, 10.0, 15.0, 20.0, 30.0, 50.0)
            presets.forEach { preset ->
                val isSelected = kotlin.math.abs(matchCost - preset) < 0.1
                Surface(
                    onClick = { onUpdate { it.copy(matchCost = preset) } },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${preset.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Currency symbol and decimal rounding row.
 */
@Composable
private fun CurrencyAndRoundingRow(
    currentSymbol: String,
    decimals: Int,
    onUpdate: ((AppSettings) -> AppSettings) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Currency Symbol",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Currency presets
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val symbols = listOf("Ksh", "$", "€", "£", "¥")
                symbols.forEach { sym ->
                    val isSelected = currentSymbol == sym
                    Surface(
                        onClick = { onUpdate { it.copy(currencySymbol = sym) } },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = sym,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Decimals",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0 to "0 (.0)", 2 to "2 (.00)").forEach { (dec, label) ->
                    val isSelected = decimals == dec
                    Surface(
                        onClick = { onUpdate { it.copy(settlementRoundingDecimals = dec) } },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
