package com.convocli.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.convocli.ui.components.CommandBlockCard
import com.convocli.ui.components.CommandInputBar
import com.convocli.ui.viewmodels.CommandBlockViewModel

/**
 * Main screen for Command Blocks UI.
 *
 * Displays a chat-like interface with command blocks in a scrolling list
 * and a fixed input bar at the bottom for entering commands.
 *
 * Features:
 * - LazyColumn for efficient rendering of command blocks
 * - Auto-scroll to newest block when command executed
 * - Empty state when no commands
 * - Material 3 theming
 */
@Composable
fun CommandBlocksScreen(
    viewModel: CommandBlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new block added
    LaunchedEffect(uiState.blocks.size) {
        if (uiState.blocks.isNotEmpty()) {
            listState.animateScrollToItem(uiState.blocks.size - 1)
        }
    }

    Scaffold(
        bottomBar = {
            CommandInputBar(
                onCommandSubmit = { command ->
                    viewModel.executeCommand(command)
                    viewModel.clearEditingCommand()
                },
                initialCommand = uiState.editingCommand
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.blocks.isEmpty()) {
                // Empty state
                EmptyState()
            } else {
                // Command blocks list
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(
                        items = uiState.blocks,
                        key = { it.id }
                    ) { block ->
                        CommandBlockCard(
                            block = block,
                            onCopyCommand = { viewModel.copyCommand(block.id) },
                            onCopyOutput = { viewModel.copyOutput(block.id) },
                            onRerun = { viewModel.rerunCommand(block.id) },
                            onCancel = { viewModel.cancelCommand(block.id) },
                            onToggleExpansion = { viewModel.toggleExpansion(block.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No commands yet",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try: ls, pwd, or echo 'Hello World'",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
