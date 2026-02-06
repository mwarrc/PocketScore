package com.mwarrc.pocketscore.ui.feature.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically

import android.content.ClipData
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickCalculatorSheet(
    settings: AppSettings,
    onDismiss: () -> Unit
) {
    var result by remember { mutableStateOf("0") }
    var expression by remember { mutableStateOf(TextFieldValue("")) }
    var showNumpad by remember { mutableStateOf(false) }

    val clipboard = LocalClipboard.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun evaluate(expr: String): String {
        if (expr.isBlank()) return "0"
        return try {
            val cleanExpr = expr.replace(" ", "")
            if (cleanExpr.isEmpty()) return "0"

            val regex = "(?=[+\\-*/])|(?<=[+\\-*/])".toRegex()
            val tokens = cleanExpr.split(regex).filter { it.isNotBlank() }.toMutableList()

            if (tokens.firstOrNull() == "-") {
                val firstNum = tokens.getOrNull(1)
                if (firstNum != null) {
                    tokens[0] = "-$firstNum"
                    tokens.removeAt(1)
                }
            }

            var i = 0
            while (i < tokens.size) {
                if (tokens[i] == "*" || tokens[i] == "/") {
                    val left = tokens[i - 1].toDouble()
                    val right = tokens[i + 1].toDouble()
                    val res = if (tokens[i] == "*") left * right else left / right
                    tokens[i - 1] = res.toString()
                    tokens.removeAt(i)
                    tokens.removeAt(i)
                    i--
                }
                i++
            }

            var total = tokens[0].toDouble()
            i = 1
            while (i < tokens.size) {
                val op = tokens[i]
                val nextVal = tokens[i + 1].toDouble()
                total = if (op == "+") total + nextVal else total - nextVal
                i += 2
            }

            if (total % 1 == 0.0) total.toLong().toString() else "%.2f".format(total)
        } catch (_: Exception) {
            "..."
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Scrollable Content Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
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
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = result,
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            color = if (result == "...") {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
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

                Spacer(Modifier.height(24.dp))

                OutlinedTextField(
                    value = expression,
                    onValueChange = { newValue ->
                        if (newValue.text.all { it.isDigit() || "+-*/. ".contains(it) }) {
                            expression = newValue
                            val evalResult = evaluate(newValue.text)
                            if (evalResult != "...") result = evalResult
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { 
                            if (it.isFocused && settings.useCustomKeyboard) {
                                showNumpad = true
                            }
                        },
                    placeholder = { 
                        Text(
                            "Enter numbers or sum (12 + 4)...", 
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        ) 
                    },
                    singleLine = true,
                    readOnly = settings.useCustomKeyboard,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = {
                        if (expression.text.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    expression = TextFieldValue("")
                                    result = "0"
                                }
                            ) {
                                Icon(Icons.Default.Clear, "Clear expression")
                            }
                        }
                    },
                    textStyle = MaterialTheme.typography.titleLarge
                )

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("+", "-", "*", "/").forEach { op ->
                        FilledTonalButton(
                            onClick = {
                                val text = expression.text
                                val selection = expression.selection
                                val before = text.substring(0, selection.start)
                                val after = text.substring(selection.end)
                                val newText = "$before$op$after"
                                val newCursorPos = selection.start + op.length
                                expression = TextFieldValue(
                                    text = newText,
                                    selection = TextRange(newCursorPos)
                                )
                                val evalResult = evaluate(newText)
                                if (evalResult != "...") result = evalResult
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            when(op) {
                                "+" -> Icon(Icons.Default.Add, contentDescription = "Add")
                                "-" -> Icon(Icons.Default.Remove, contentDescription = "Subtract")
                                "*" -> Text("×", style = MaterialTheme.typography.titleLarge)
                                "/" -> Text("÷", style = MaterialTheme.typography.titleLarge)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            expression = TextFieldValue("")
                            result = "0"
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reset")
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

            Box(modifier = Modifier.fillMaxWidth()) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }

            // Custom Numpad at the bottom of the Column
            AnimatedVisibility(
                visible = showNumpad && settings.useCustomKeyboard,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                ScoreNumpad(
                    onNumberClick = { char ->
                        val text = expression.text
                        val selection = expression.selection
                        val before = text.substring(0, selection.start)
                        val after = text.substring(selection.end)
                        val newText = "$before$char$after"
                        val newCursorPos = selection.start + char.length
                        expression = TextFieldValue(
                            text = newText,
                            selection = TextRange(newCursorPos)
                        )
                        val evalResult = evaluate(newText)
                        if (evalResult != "...") result = evalResult
                    },
                    onBackspaceClick = {
                        val text = expression.text
                        val selection = expression.selection
                        if (selection.start > 0 || selection.end > selection.start) {
                            val before = if (selection.start == selection.end) {
                                text.substring(0, selection.start - 1)
                            } else {
                                text.substring(0, selection.start)
                            }
                            val after = text.substring(selection.end)
                            val newText = "$before$after"
                            val newCursorPos = if (selection.start == selection.end) {
                                selection.start - 1
                            } else {
                                selection.start
                            }
                            expression = TextFieldValue(
                                text = newText,
                                selection = TextRange(newCursorPos)
                            )
                            val evalResult = evaluate(newText)
                            if (evalResult != "...") result = evalResult
                        }
                    },
                    onDismiss = { showNumpad = false },
                    isPinned = false,
                    onTogglePin = {}
                )
            }
        }
    }
}

