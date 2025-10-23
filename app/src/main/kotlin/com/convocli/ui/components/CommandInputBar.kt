package com.convocli.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Fixed bottom input bar for entering commands.
 *
 * Features:
 * - Multi-line text input
 * - Monospace font (terminal-style)
 * - Send button
 * - Enter key submits command
 * - Auto-clear after submit
 * - No autocorrect/autocapitalization
 */
@Composable
fun CommandInputBar(
    onCommandSubmit: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialCommand: String? = null
) {
    var commandText by remember(initialCommand) { mutableStateOf(initialCommand ?: "") }
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Bottom
        ) {
            // Multi-line text field
            TextField(
                value = commandText,
                onValueChange = { commandText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("command_input"),
                placeholder = {
                    Text(
                        text = "Enter command...",
                        fontFamily = FontFamily.Monospace
                    )
                },
                textStyle = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp
                ),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send,
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (commandText.isNotBlank()) {
                            onCommandSubmit(commandText)
                            commandText = ""
                            focusManager.clearFocus()
                        }
                    }
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Send button
            FilledIconButton(
                onClick = {
                    if (commandText.isNotBlank()) {
                        onCommandSubmit(commandText)
                        commandText = ""
                        focusManager.clearFocus()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("execute_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Execute command"
                )
            }
        }
    }
}
