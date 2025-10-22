package com.convocli.terminal.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.convocli.terminal.model.SessionState
import com.convocli.terminal.model.TerminalError
import com.convocli.terminal.repository.TerminalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for terminal functionality.
 *
 * This ViewModel manages terminal sessions, command execution, and output display.
 * It serves as the intermediary between the UI layer and the TerminalRepository,
 * exposing reactive state flows for the UI to observe.
 *
 * ## Architecture
 * ```
 * Compose UI (screens/components)
 *        ↓
 * TerminalViewModel (this class)
 *        ↓
 * TerminalRepository (interface)
 *        ↓
 * TermuxTerminalRepository (implementation)
 *        ↓
 * Termux TerminalSession (native PTY)
 * ```
 *
 * ## State Management
 * - Uses MVI (Model-View-Intent) pattern
 * - Exposes StateFlow for reactive UI updates
 * - All state changes happen via ViewModel methods
 * - UI observes state and renders accordingly
 *
 * ## Lifecycle
 * - Created with @HiltViewModel annotation (injected by Hilt)
 * - Scoped to ViewModel lifecycle (survives configuration changes)
 * - Session created in init block
 * - Cleaned up automatically when ViewModel is cleared
 *
 * ## Usage Example
 * ```kotlin
 * @Composable
 * fun TerminalScreen(viewModel: TerminalViewModel = hiltViewModel()) {
 *     val output by viewModel.output.collectAsState()
 *     val isReady by viewModel.isSessionReady.collectAsState()
 *
 *     Column {
 *         Text(output)
 *         if (isReady) {
 *             CommandInput(onExecute = { viewModel.executeCommand(it) })
 *         }
 *     }
 * }
 * ```
 *
 * @property repository The terminal repository for session management and command execution
 */
@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val repository: TerminalRepository,
) : ViewModel() {
    /**
     * The current terminal session ID.
     * Null if no session has been created yet.
     */
    private var sessionId: String? = null

    /**
     * Terminal output text.
     *
     * Accumulates all output from the terminal session.
     * UI should display this in a scrollable text view.
     *
     * Initially empty. Updated as commands execute and produce output.
     */
    private val _output = MutableStateFlow("")
    val output: StateFlow<String> = _output.asStateFlow()

    /**
     * Session readiness state.
     *
     * Indicates whether the terminal session is ready to accept commands.
     * - `false`: Session is being created or has failed
     * - `true`: Session is running and ready
     *
     * UI should disable command input when false.
     */
    private val _isSessionReady = MutableStateFlow(false)
    val isSessionReady: StateFlow<Boolean> = _isSessionReady.asStateFlow()

    /**
     * Latest terminal error.
     *
     * Emits errors from the terminal session (initialization failures,
     * command failures, session crashes, etc.).
     *
     * UI should display errors to the user (e.g., in a Snackbar or dialog).
     *
     * Null when no error has occurred.
     */
    private val _error = MutableStateFlow<TerminalError?>(null)
    val error: StateFlow<TerminalError?> = _error.asStateFlow()

    /**
     * Command execution state.
     *
     * Indicates whether a command is currently being executed.
     * - `false`: Idle, ready for next command
     * - `true`: Command is executing
     *
     * UI can show a loading indicator when true.
     */
    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    init {
        // Create terminal session on initialization
        createSession()
    }

    /**
     * Creates a new terminal session.
     *
     * This is called automatically in the init block, but can also be
     * called manually to restart a failed or closed session.
     *
     * ## Process
     * 1. Calls repository.createSession()
     * 2. On success: stores sessionId, sets isSessionReady=true, starts output collection
     * 3. On failure: sets error state, keeps isSessionReady=false
     *
     * ## Error Handling
     * If session creation fails (e.g., bash executable not found),
     * the error will be emitted to the error StateFlow and can be
     * displayed to the user.
     */
    private fun createSession() {
        viewModelScope.launch {
            repository.createSession()
                .onSuccess { id ->
                    sessionId = id
                    _isSessionReady.value = true

                    // Start collecting output from the session
                    collectOutput(id)

                    // Also collect errors globally
                    collectErrors()
                }
                .onFailure { exception ->
                    _isSessionReady.value = false
                    _error.value = TerminalError.InitializationFailed(
                        reason = exception.message ?: "Unknown error",
                    )
                }
        }
    }

    /**
     * Collects terminal output from the session and updates the output StateFlow.
     *
     * This runs in a coroutine for the lifetime of the ViewModel.
     * Output is accumulated (appended) to the existing output.
     *
     * @param sessionId The session to collect output from
     */
    private fun collectOutput(sessionId: String) {
        viewModelScope.launch {
            repository.observeOutput(sessionId)
                .collect { terminalOutput ->
                    // Append new output to existing output
                    // In the future, we may want to limit the buffer size
                    // to prevent memory issues with very long sessions
                    _output.value = terminalOutput.text
                }
        }
    }

    /**
     * Collects terminal errors and updates the error StateFlow.
     *
     * This runs in a coroutine for the lifetime of the ViewModel.
     * Errors are global (not session-specific).
     */
    private fun collectErrors() {
        viewModelScope.launch {
            repository.observeErrors()
                .collect { error ->
                    _error.value = error

                    // If session crashed, mark as not ready
                    when (error) {
                        is TerminalError.SessionCrashed -> {
                            _isSessionReady.value = false
                        }
                        is TerminalError.InitializationFailed -> {
                            _isSessionReady.value = false
                        }
                        else -> {
                            // Other errors don't necessarily mean session is dead
                        }
                    }
                }
        }
    }

    /**
     * Executes a command in the terminal session.
     *
     * The command is sent to the PTY stdin and will be processed by the shell.
     * Output will appear asynchronously in the output StateFlow.
     *
     * ## Validation
     * - Command must not be blank
     * - Session must exist and be ready
     *
     * ## Execution Flow
     * 1. Validates command and session state
     * 2. Sets isExecuting=true
     * 3. Calls repository.executeCommand()
     * 4. Sets isExecuting=false
     * 5. Output appears asynchronously via output Flow
     *
     * ## Error Handling
     * If the command fails to execute (e.g., session doesn't exist),
     * an error will be emitted to the error StateFlow.
     *
     * @param command The command to execute (e.g., "ls -la", "echo hello")
     */
    fun executeCommand(command: String) {
        // Validate command
        if (command.isBlank()) {
            return
        }

        // Check session exists
        val id = sessionId
        if (id == null) {
            _error.value = TerminalError.IOError(
                message = "No terminal session available",
            )
            return
        }

        // Check session is ready
        if (!_isSessionReady.value) {
            _error.value = TerminalError.IOError(
                message = "Terminal session is not ready",
            )
            return
        }

        viewModelScope.launch {
            _isExecuting.value = true
            try {
                repository.executeCommand(id, command)
            } finally {
                _isExecuting.value = false
            }
        }
    }

    /**
     * Restarts the terminal session.
     *
     * This destroys the current session (if any) and creates a new one.
     * Useful for recovering from errors or resetting the terminal state.
     *
     * ## Process
     * 1. Destroys current session
     * 2. Clears output
     * 3. Resets error state
     * 4. Creates new session
     */
    fun restartSession() {
        viewModelScope.launch {
            // Destroy existing session
            sessionId?.let { id ->
                repository.destroySession(id)
            }

            // Reset state
            sessionId = null
            _output.value = ""
            _error.value = null
            _isSessionReady.value = false

            // Create new session
            createSession()
        }
    }

    /**
     * Clears the terminal output buffer.
     *
     * This does NOT affect the actual terminal session or its scrollback buffer.
     * It only clears the UI display.
     */
    fun clearOutput() {
        _output.value = ""
    }

    /**
     * Dismisses the current error.
     *
     * Sets error state to null. UI should call this after displaying
     * an error to the user (e.g., when dismissing an error dialog).
     */
    fun dismissError() {
        _error.value = null
    }

    /**
     * Gets the current session state.
     *
     * @return SessionState (RUNNING, STOPPED, ERROR) or null if no session
     */
    fun getSessionState(): SessionState? {
        val id = sessionId ?: return null
        return repository.getSessionState(id)
    }

    override fun onCleared() {
        super.onCleared()

        // Clean up session when ViewModel is destroyed
        viewModelScope.launch {
            sessionId?.let { id ->
                repository.destroySession(id)
            }
        }
    }
}
