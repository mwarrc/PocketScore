package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

import android.content.ClipData
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.ui.focus.onFocusChanged
import com.mwarrc.pocketscore.domain.model.AppSettings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.mwarrc.pocketscore.domain.model.Player
import com.mwarrc.pocketscore.ui.util.ImmersiveMode

/**
 * Expression Calculator overlay.
 * 
 * Allows users to calculate complex score additions (e.g., 20 + 15 + 8) 
 * without leaving the game screen. Supports basic arithmetic and parenthesis.
 * Includes a "Quick Ribbon" to insert current player scores into the formula.
 * 
 * @param settings Current application settings
 * @param expression Current formula text as a [TextFieldValue]
 * @param onExpressionChange Callback when formula changes
 * @param players List of active players for the quick ribbon
 * @param onAddScore Callback to apply a score to a player
 * @param onUpdateSettings Callback to toggle internal numpad settings
 * @param onDismiss Callback to close the sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCalculatorSheet(
    settings: AppSettings,
    expression: TextFieldValue,
    onExpressionChange: (TextFieldValue) -> Unit,
    players: List<Player>,
    onAddScore: (String, Int, Int?) -> Unit,
    onUpdateSettings: ((AppSettings) -> AppSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val result = remember(expression) { evaluate(expression.text) }


    val clipboard = LocalClipboard.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }

    // Auto-focus and open keyboard on launch with a small delay to prevent animation jank
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(300)
        focusRequester.requestFocus()
        keyboardController?.show()

    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val activePlayers = remember(players) { players.filter { it.isActive } }
    val scrollState = rememberScrollState()

    // --- Dynamic Safe Area Height Calculation ---
    val density = LocalDensity.current
    val context = LocalContext.current
    val trueScreenHeightDp = with(density) {
        context.resources.displayMetrics.heightPixels.toDp()
    }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val cutoutHeight = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
    val topSafeInset = maxOf(statusBarHeight, cutoutHeight)
    
    // Limit to just below the status bar with a small extra margin (8dp)
    // This ensures dialogs never overlap with system UI / dynamic island.
    val maxContentHeight = trueScreenHeightDp - topSafeInset - 8.dp

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        ImmersiveMode()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxContentHeight) // Capped below status bar
                .navigationBarsPadding() // Keep nav bar clear if visible
        ) {
            // Scrollable Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding() // Adjust for system keyboard
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Expression Calculator",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(24.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (result != "0" && result != "...") {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipEntry(
                                            ClipData.newPlainText("Calculator Result", result)
                                        )
                                    )
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(
                                        message = "Copied $result to clipboard",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        },
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (expression.text.isNotEmpty()) {
                            Text(
                                text = "Preview Result (Tap to copy)",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }

                val calculatorTransformation = remember {
                    VisualTransformation { text ->
                        val transformed = text.text
                            .replace("*", "×")
                            .replace("/", "÷")
                        androidx.compose.ui.text.input.TransformedText(
                            androidx.compose.ui.text.AnnotatedString(transformed),
                            androidx.compose.ui.text.input.OffsetMapping.Identity
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                BasicTextField(
                    value = expression,
                    onValueChange = { newValue ->
                        if (newValue.text.all { it.isDigit() || "+-*/. ()".contains(it) || it == '\n' }) {
                            onExpressionChange(newValue)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    readOnly = false,

                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    visualTransformation = calculatorTransformation,
                    textStyle = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(
                                    1.dp, 
                                    MaterialTheme.colorScheme.primary, 
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (expression.text.isEmpty()) {
                                        Text(
                                            "Enter sum (12 + 4)...",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    } else {
                                        innerTextField()
                                    }
                                }

                                if (expression.text.isNotEmpty()) {
                                    IconButton(
                                        onClick = { onExpressionChange(TextFieldValue("")) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Clear, 
                                            "Clear",
                                            modifier = Modifier.size(18.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("+", "-", "*", "/", "(", ")").forEach { op ->
                        FilledTonalButton(
                            onClick = {
                                val text = expression.text
                                val selection = expression.selection
                                val before = text.substring(0, selection.start)
                                val after = text.substring(selection.end)
                                val newText = "$before$op$after"
                                val newCursorPos = selection.start + op.length
                                onExpressionChange(TextFieldValue(
                                    text = newText,
                                    selection = TextRange(newCursorPos)
                                ))
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            when(op) {
                                "+" -> Icon(Icons.Default.Add, "Add")
                                "-" -> Icon(Icons.Default.Remove, "Subtract")
                                "*" -> Text("×", style = MaterialTheme.typography.titleLarge)
                                "/" -> Text("÷", style = MaterialTheme.typography.titleLarge)
                                else -> Text(op, style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(24.dp))
            }

            // Quick Score Ribbon (Insertion only)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    "Insert Player Score into Formula",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activePlayers.withIndex().toList()) { (index, player) ->
                        val playerColors = listOf(
                            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer,
                            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val (containerColor, contentColor) = playerColors[index % playerColors.size]

                        Surface(
                            onClick = {
                                val currentText = expression.text
                                val toAppend = "(${player.score})"
                                val newText = when {
                                    currentText.isEmpty() -> toAppend
                                    "+-*/(".contains(currentText.last()) -> currentText + toAppend
                                    else -> "$currentText+$toAppend"
                                }
                                onExpressionChange(TextFieldValue(newText, TextRange(newText.length)))
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = containerColor,
                            contentColor = contentColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    player.name,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "(${player.score})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = contentColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onExpressionChange(TextFieldValue("")) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reset All")
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Close, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Close")
                }
            }

        }
    }
}

private fun evaluate(expr: String): String {
    if (expr.isBlank()) return "0"
    return try {
        var processed = expr.replace(" ", "")
        
        // Handle parenthesis recursively
        val parenRegex = "\\(([^()]+)\\)".toRegex()
        while (processed.contains("(")) {
            val match = parenRegex.find(processed) ?: break
            val innerVal = evaluate(match.groupValues[1])
            if (innerVal == "...") return "..."
            processed = processed.replaceRange(match.range, innerVal)
        }

        if (processed.isEmpty()) return "0"

        val regex = "(?=[+\\-*/])|(?<=[+\\-*/])".toRegex()
        val tokens = processed.split(regex).filter { it.isNotBlank() }.toMutableList()

        // Handle leading negative
        if (tokens.firstOrNull() == "-") {
            val next = tokens.getOrNull(1) ?: return "0"
            tokens[0] = "-$next"
            tokens.removeAt(1)
        }

        // MD: Multiplication and Division
        var i = 0
        while (i < tokens.size) {
            if (tokens[i] == "*" || tokens[i] == "/") {
                val left = tokens.getOrNull(i - 1)?.toDoubleOrNull() ?: 0.0
                val right = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
                val res = if (tokens[i] == "*") left * right else left / right
                tokens[i - 1] = res.toString()
                tokens.removeAt(i)
                tokens.removeAt(i)
                i--
            }
            i++
        }

        // AS: Addition and Subtraction
        if (tokens.isEmpty()) return "0"
        var total = tokens[0].toDoubleOrNull() ?: 0.0
        i = 1
        while (i < tokens.size) {
            val op = tokens[i]
            val nextVal = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: 0.0
            total = if (op == "+") total + nextVal else total - nextVal
            i += 2
        }

        if (total % 1 == 0.0) total.toLong().toString() else "%.2f".format(total)
    } catch (_: Exception) {
        "..."
    }
}

