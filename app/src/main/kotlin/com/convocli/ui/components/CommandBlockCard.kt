package com.convocli.ui.components

import com.convocli.R
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.convocli.data.model.CommandBlock
import com.convocli.data.model.CommandStatus

/**
 * Material 3 Card displaying a single command block.
 *
 * Shows:
 * - Command text (monospace)
 * - Timestamp and duration
 * - Status indicator (icon + color)
 * - Output (monospace, collapsible for long output)
 * - Action buttons (copy, re-run, cancel)
 */
@Composable
fun CommandBlockCard(
    block: CommandBlock,
    onCopyCommand: () -> Unit,
    onCopyOutput: () -> Unit,
    onRerun: () -> Unit,
    onCancel: () -> Unit,
    onToggleExpansion: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Command + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Command text
                Text(
                    text = block.command,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Status indicator
                StatusIndicator(status = block.status)

                // Cancel button (only for executing blocks)
                if (block.status == CommandStatus.EXECUTING) {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Cancel,
                            contentDescription = "Cancel command",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Timestamp and duration
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = block.formattedTimestamp(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                block.formattedDuration()?.let { duration ->
                    Text(
                        text = "• $duration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Output section
            if (block.output.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                val lines = block.output.lines()
                val shouldCollapse = lines.size > 20

                if (shouldCollapse && !block.isExpanded) {
                    // Collapsed view
                    val collapsedText = lines.take(10).joinToString("\n") +
                            "\n...\n" +
                            lines.takeLast(5).joinToString("\n")

                    Text(
                        text = collapsedText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    TextButton(onClick = onToggleExpansion) {
                        Text(stringResource(R.string.show_lines, lines.size))
                    }
                } else {
                    // Expanded view
                    Text(
                        text = block.output,
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (shouldCollapse) {
                        TextButton(onClick = onToggleExpansion) {
                            Text(stringResource(R.string.show_less))
                        }
                    }
                }
            } else if (block.status == CommandStatus.EXECUTING) {
                // Show loading for executing commands with no output yet
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp).padding(top = 8.dp),
                    strokeWidth = 2.dp
                )
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onCopyCommand) {
                    Icon(Icons.Default.ContentCopy, stringResource(R.string.copy_command))
                }
                IconButton(onClick = onCopyOutput) {
                    Icon(Icons.AutoMirrored.Filled.Assignment, stringResource(R.string.copy_output))
                }
                if (block.status != CommandStatus.EXECUTING) {
                    IconButton(onClick = onRerun) {
                        Icon(Icons.Default.Refresh, "Re-run")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusIndicator(status: CommandStatus) {
    val (icon, color) = when (status) {
        CommandStatus.PENDING -> Icons.Default.Schedule to MaterialTheme.colorScheme.onSurfaceVariant
        CommandStatus.EXECUTING -> Icons.Default.HourglassEmpty to MaterialTheme.colorScheme.primary
        CommandStatus.SUCCESS -> Icons.Default.CheckCircle to Color(0xFF4CAF50)
        CommandStatus.FAILURE -> Icons.Default.Error to MaterialTheme.colorScheme.error
    }

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = color,
        modifier = Modifier.size(20.dp)
    )
}
