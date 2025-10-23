package com.convocli.ui.viewmodels

import android.content.ClipData
import android.content.ClipboardManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.convocli.data.model.CommandBlock
import com.convocli.data.model.CommandStatus
import com.convocli.data.model.TerminalSessionState
import com.convocli.terminal.TerminalRepository
import com.convocli.terminal.CommandBlockManager
import com.convocli.terminal.TerminalOutputProcessor
import com.convocli.terminal.impl.SimplePromptDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
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
    private val clipboardManager: ClipboardManager,
    private val commandBlockManager: CommandBlockManager,
    private val terminalRepository: TerminalRepository,
    private val outputProcessor: TerminalOutputProcessor,
    private val promptDetector: SimplePromptDetector
) : ViewModel() {

    companion object {
        private const val TAG = "CommandBlockViewModel"
    }

    private val _uiState = MutableStateFlow(CommandBlocksUiState())
    val uiState: StateFlow<CommandBlocksUiState> = _uiState.asStateFlow()

    init {
        // Create terminal session asynchronously (won't block UI thread due to flowOn(Dispatchers.IO))
        viewModelScope.launch {
            terminalRepository.createSession()
                .catch { e: Throwable ->
                    // Gracefully handle any exceptions not caught by the Flow itself
                    Log.e(TAG, "Uncaught exception during session creation", e)
                    _uiState.update {
                        it.copy(
                            isSessionReady = false,
                            error = "Failed to create terminal session: ${e.message}"
                        )
                    }
                }
                .collect { sessionState: TerminalSessionState ->
                    when (sessionState) {
                        is TerminalSessionState.Ready -> {
                            Log.d(TAG, "Terminal session ready: ${sessionState.session.sessionId}")

                            // Update UI state: session is now ready for commands
                            _uiState.update { it.copy(isSessionReady = true, error = null) }

                            // Observe working directory changes
                            launch {
                                terminalRepository.workingDirectory.collect { directory ->
                                    _uiState.update { it.copy(currentDirectory = directory) }
                                }
                            }
                        }
                        is TerminalSessionState.Error -> {
                            Log.e(TAG, "Failed to create session: ${sessionState.message}", sessionState.cause)
                            _uiState.update {
                                it.copy(
                                    isSessionReady = false,
                                    error = "Failed to create terminal session: ${sessionState.message}"
                                )
                            }
                        }
                        is TerminalSessionState.Inactive -> {
                            Log.w(TAG, "Session became inactive")
                            _uiState.update { it.copy(isSessionReady = false) }
                        }
                    }
                }
        }

        // Observe command blocks from manager
        viewModelScope.launch {
            commandBlockManager.observeBlocks().collect { blocks ->
                _uiState.update { it.copy(blocks = blocks) }
            }
        }

        // Observe terminal output for streaming updates
        observeTerminalOutput()
    }

    /**
     * Observes terminal output and updates command blocks in real-time.
     * Throttles updates to 60fps (16ms) to prevent UI lag.
     */
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    private fun observeTerminalOutput() {
        viewModelScope.launch {
            terminalRepository.observeOutput()
                .debounce(16) // 60fps throttle
                .collect { chunk ->
                    processOutputChunk(chunk)
                }
        }
    }

    /**
     * Processes a single output chunk and updates the corresponding command block.
     * Also detects command completion via prompt detection.
     *
     * Note: Chunks contain incremental output only (not full transcript), so we append.
     */
    private suspend fun processOutputChunk(chunk: com.convocli.data.model.OutputChunk) {
        val block = commandBlockManager.getBlockById(chunk.blockId) ?: return

        // Append incremental chunk data to existing output
        val newOutput = block.output + chunk.data

        // Update block output
        commandBlockManager.updateBlockOutput(block.id, newOutput)

        // Check for command completion (prompt detected)
        if (block.status == CommandStatus.EXECUTING && promptDetector.detectPrompt(newOutput)) {
            // Get exit code from terminal
            val exitCode = terminalRepository.getExitCode(block.id) ?: 0

            // Update status based on exit code
            val finalStatus = if (exitCode == 0) CommandStatus.SUCCESS else CommandStatus.FAILURE

            commandBlockManager.updateBlockStatus(block.id, finalStatus, exitCode)

            Log.d(TAG, "Command completed: blockId=${block.id}, exitCode=$exitCode, status=$finalStatus")
        }
    }

    /**
     * Executes a command in the terminal and creates a command block.
     */
    fun executeCommand(command: String) {
        if (command.isBlank()) return

        viewModelScope.launch {
            var block: CommandBlock? = null
            try {
                // Create command block
                block = commandBlockManager.createBlock(
                    command = command.trim(),
                    workingDir = _uiState.value.currentDirectory
                )

                // Update state to EXECUTING
                commandBlockManager.updateBlockStatus(
                    id = block.id,
                    status = CommandStatus.EXECUTING
                )

                // Execute command via terminal
                terminalRepository.executeCommand(command.trim(), block.id)

            } catch (e: Exception) {
                Log.e(TAG, "Error executing command: ${e.message}", e)

                // Update block status to FAILURE (don't leave it in EXECUTING state)
                block?.let {
                    commandBlockManager.updateBlockStatus(
                        id = it.id,
                        status = CommandStatus.FAILURE,
                        exitCode = -1
                    )
                }

                // Update UI state with error message
                val errorMessage = when {
                    e.message?.contains("Bootstrap not installed") == true ->
                        "Terminal not ready: Bash shell not installed. Please install bootstrap first."
                    e.message?.contains("No active terminal session") == true ->
                        "Terminal session not ready. Please wait a moment and try again."
                    else ->
                        "Failed to execute command: ${e.message}"
                }

                _uiState.update {
                    it.copy(error = errorMessage)
                }
            }
        }
    }

    /**
     * Copies command text to clipboard.
     */
    fun copyCommand(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlockById(blockId) ?: return@launch
            val clip = ClipData.newPlainText("Command", block.command)
            clipboardManager.setPrimaryClip(clip)
            // TODO: Show toast confirmation
        }
    }

    /**
     * Copies output text to clipboard.
     */
    fun copyOutput(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlockById(blockId) ?: return@launch
            // TODO (T025): Strip ANSI codes when ANSI parser is implemented
            val clip = ClipData.newPlainText("Output", block.output)
            clipboardManager.setPrimaryClip(clip)
            // TODO: Show toast confirmation
        }
    }

    /**
     * Re-runs the same command.
     */
    fun rerunCommand(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlockById(blockId) ?: return@launch
            executeCommand(block.command)
        }
    }

    /**
     * Populates input with command for editing.
     * Sets the editingCommand state which the UI can observe.
     */
    fun editAndRun(blockId: String) {
        viewModelScope.launch {
            val block = commandBlockManager.getBlockById(blockId) ?: return@launch
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
            try {
                // Cancel command in terminal (sends SIGINT)
                terminalRepository.cancelCommand(blockId)

                // Mark block as cancelled
                commandBlockManager.updateBlockStatus(
                    id = blockId,
                    status = CommandStatus.CANCELED
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error cancelling command", e)
            }
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
            commandBlockManager.deleteAllBlocks()
        }
    }
}

/**
 * UI state for Command Blocks screen.
 */
data class CommandBlocksUiState(
    val blocks: List<CommandBlock> = emptyList(),
    val currentDirectory: String = "/data/data/com.convocli/files/home",
    val isSessionReady: Boolean = false,  // Terminal session initialization status
    val error: String? = null,
    val editingCommand: String? = null  // Command being edited
)
