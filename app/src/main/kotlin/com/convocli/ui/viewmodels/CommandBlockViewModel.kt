package com.convocli.ui.viewmodels

import com.convocli.R
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.convocli.data.model.CommandBlock
import com.convocli.terminal.repository.TerminalRepository
import com.convocli.terminal.service.CommandBlockManager
import com.convocli.terminal.util.AnsiColorParser
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Command Blocks UI.
 *
 * Manages state and user interactions for the command blocks interface:
 * - Observable list of command blocks
 * - Command execution
 * - Block actions (copy, re-run, cancel, expand)
 * - Integration with TerminalRepository for actual command execution
 *
 * Follows MVI architecture pattern with unidirectional data flow.
 */
@HiltViewModel
class CommandBlockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commandBlockManager: CommandBlockManager,
    private val terminalRepository: TerminalRepository,
    private val ansiColorParser: AnsiColorParser
) : ViewModel() {

    private val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    /**
     * The current terminal session ID.
     * Null until session is created in init block.
     */
    private var sessionId: String? = null

    private val _uiState = MutableStateFlow(CommandBlocksUiState())
    val uiState: StateFlow<CommandBlocksUiState> = _uiState.asStateFlow()

    init {
        // Create terminal session
        viewModelScope.launch {
            terminalRepository.createSession()
                .onSuccess { id ->
                    sessionId = id

                    // Start observing working directory
                    launch {
                        terminalRepository.observeWorkingDirectory(id).collect { directory ->
                            _uiState.update { it.copy(currentDirectory = directory) }
                        }
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = "Failed to create terminal session: ${error.message}")
                    }
                }
        }

        // Observe command blocks from manager
        viewModelScope.launch {
            commandBlockManager.observeBlocks().collect { blocks ->
                _uiState.update { it.copy(blocks = blocks) }
            }
        }
    }

    /**
     * Executes a command in the terminal and creates a command block.
     */
    fun executeCommand(command: String) {
        if (command.isBlank()) return

        viewModelScope.launch {
            // Create command block
            val blockId = commandBlockManager.createBlock(
                command = command.trim(),
                workingDirectory = _uiState.value.currentDirectory
            )

            // Mark as executing
            commandBlockManager.markExecuting(blockId)

            // Execute command in terminal
            // Note: Actual output capture will be implemented when we integrate
            // with TermuxTerminalRepository's output stream
            sessionId?.let { id ->
                terminalRepository.executeCommand(id, command.trim())
            }

            // For MVP, simulate completion after command execution
            // TODO: Integrate with real terminal output stream to capture actual output
            simulateCommandCompletion(blockId, command)
        }
    }

    /**
     * Temporary simulation for MVP.
     * TODO: Replace with real terminal output integration.
     */
    private fun simulateCommandCompletion(blockId: String, command: String) {
        viewModelScope.launch {
            // Simulate some output
            kotlinx.coroutines.delay(100)
            commandBlockManager.appendOutput(blockId, "Executing: $command\n")

            // Simulate completion
            kotlinx.coroutines.delay(500)
            commandBlockManager.completeBlock(blockId, exitCode = 0, duration = 600)
        }
    }

    /**
     * Copies command text to clipboard.
     */
    fun copyCommand(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlock(blockId) ?: return@launch
            val clip = ClipData.newPlainText(
                context.getString(R.string.clipboard_label_command),
                block.command
            )
            clipboardManager.setPrimaryClip(clip)
            // TODO: Show toast confirmation
        }
    }

    /**
     * Copies output text to clipboard (stripped of ANSI codes).
     */
    fun copyOutput(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlock(blockId) ?: return@launch
            // Strip ANSI codes for clean plain text
            val plainOutput = ansiColorParser.stripAnsiCodes(block.output)
            val clip = ClipData.newPlainText(
                context.getString(R.string.clipboard_label_output),
                plainOutput
            )
            clipboardManager.setPrimaryClip(clip)
            // TODO: Show toast confirmation
        }
    }

    /**
     * Re-runs the same command.
     */
    fun rerunCommand(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlock(blockId) ?: return@launch
            executeCommand(block.command)
        }
    }

    /**
     * Populates input with command for editing.
     * Sets the editingCommand state which the UI can observe.
     */
    fun editAndRun(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlock(blockId) ?: return@launch
            _uiState.update { it.copy(editingCommand = block.command) }
        }
    }

    /**
     * Clears the editing command state.
     */
    fun clearEditingCommand() {
        _uiState.update { it.copy(editingCommand = null) }
    }

    /**
     * Cancels a running command.
     *
     * Sends SIGINT to the terminal session and marks the block as cancelled.
     */
    fun cancelCommand(blockId: String) {
        viewModelScope.launch {
            // Send SIGINT to interrupt the running command
            sessionId?.let { id ->
                terminalRepository.sendSignal(id, signal = 2) // SIGINT
            }

            // Mark block as cancelled
            commandBlockManager.cancelBlock(blockId)
        }
    }

    /**
     * Toggles expansion state of a block.
     */
    fun toggleExpansion(blockId: String) {
        viewModelScope.launch {
            commandBlockManager.toggleExpansion(blockId)
        }
    }

    /**
     * Clears all command blocks.
     */
    fun clearHistory() {
        viewModelScope.launch {
            commandBlockManager.clearBlocks()
        }
    }
}

/**
 * UI state for Command Blocks screen.
 */
data class CommandBlocksUiState(
    val blocks: List<CommandBlock> = emptyList(),
    val currentDirectory: String = "/data/data/com.convocli/files/home",
    val error: String? = null,
    val editingCommand: String? = null  // Command being edited
)
