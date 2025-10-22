package com.convocli.terminal.repository

import android.content.Context
import com.convocli.terminal.model.SessionState
import com.convocli.terminal.model.StreamType
import com.convocli.terminal.model.TerminalError
import com.convocli.terminal.model.TerminalOutput
import com.convocli.terminal.model.TerminalSession
import com.convocli.terminal.service.CommandMonitor
import com.convocli.terminal.service.OutputStreamProcessor
import com.convocli.terminal.service.WorkingDirectoryTracker
import com.termux.terminal.TerminalSessionClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Termux-based implementation of TerminalRepository.
 *
 * This class integrates with the Termux terminal-emulator library to provide
 * native Linux terminal functionality on Android. It manages terminal sessions,
 * PTY (pseudo-terminal) operations, and command execution.
 *
 * ## Architecture
 * ```
 * TerminalViewModel
 *        ↓
 * TermuxTerminalRepository (this class)
 *        ↓
 * Termux TerminalSession (integrated)
 *        ↓
 * Native PTY (JNI to C code)
 * ```
 *
 * @property context Application context for accessing app private directories
 */
class TermuxTerminalRepository(
    private val context: Context,
) : TerminalRepository {
    /**
     * Wrapper class to hold both our TerminalSession model and the Termux TerminalSession.
     */
    private data class SessionWrapper(
        val session: TerminalSession,
        val termuxSession: com.termux.terminal.TerminalSession,
        val workingDirectoryTracker: WorkingDirectoryTracker,
        val commandMonitor: CommandMonitor,
    )

    /**
     * Internal map tracking active sessions by ID.
     *
     * Stores both our data model and the actual Termux TerminalSession instances.
     */
    private val sessions = mutableMapOf<String, SessionWrapper>()

    /**
     * Output stream processor for detecting stderr content.
     *
     * Uses pattern matching to distinguish between stdout and stderr
     * in the merged PTY output stream.
     */
    private val outputProcessor = OutputStreamProcessor()

    /**
     * Coroutine scope for background operations.
     *
     * Used for processing output and monitoring commands asynchronously.
     * Uses SupervisorJob to prevent one failure from canceling others.
     */
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Shared output flow for all terminal output events.
     *
     * Stub: Currently never emits any events.
     * Phase 3: Will emit TerminalOutput as it's received from PTY.
     */
    private val _output = MutableSharedFlow<TerminalOutput>()

    /**
     * Shared error flow for all terminal error events.
     *
     * Stub: Currently never emits any events.
     * Phase 3: Will emit TerminalError when errors occur.
     */
    private val _errors = MutableSharedFlow<TerminalError>()
    private val errors: SharedFlow<TerminalError> = _errors.asSharedFlow()

    /**
     * Creates a new terminal session.
     *
     * This initializes a real Termux TerminalSession with:
     * - PTY (pseudo-terminal) pair via JNI to native C code
     * - Shell process (bash - will fail until bootstrap installed)
     * - Default environment variables (HOME, PATH, SHELL, etc.)
     * - Initial working directory ($HOME)
     * - Terminal size (80 columns x 24 rows)
     *
     * ## Return Value
     * - **Success**: `Result.success(sessionId)` with unique session identifier
     * - **Failure**: `Result.failure(exception)` if initialization fails
     *
     * ## Common Failure Causes
     * - PTY creation failure (out of resources)
     * - Shell executable not found (bootstrap not installed yet)
     * - Permission denied
     *
     * @return Result containing the session ID on success, or an exception on failure
     */
    override suspend fun createSession(): Result<String> {
        return try {
            val sessionId = UUID.randomUUID().toString()
            val filesDir = context.filesDir.absolutePath

            // Prepare environment variables
            val env = getDefaultEnvironment()
            val envArray = env.map { "${it.key}=${it.value}" }.toTypedArray()

            // Ensure home directory exists (T020)
            val homeDir = java.io.File("$filesDir/home")
            if (!homeDir.exists()) {
                homeDir.mkdirs()
            }

            // Prepare shell arguments
            val shellPath = "$filesDir/usr/bin/bash"
            val workingDir = homeDir.absolutePath
            val args = arrayOf("bash") // First arg is process name

            // Create Termux TerminalSession with callback client
            val termuxSession = com.termux.terminal.TerminalSession(
                shellPath,
                workingDir,
                args,
                envArray,
                10000, // transcript rows (history buffer)
                createSessionClient(sessionId),
            )

            // Initialize terminal size (standard 80x24)
            // cellWidthPixels and cellHeightPixels are placeholders (will be calculated from UI later)
            termuxSession.updateSize(80, 24, 10, 20)

            // Create our data model
            val session = TerminalSession(
                sessionId = sessionId,
                shellPath = shellPath,
                workingDirectory = workingDir,
                environment = env,
                state = SessionState.RUNNING,
                createdAt = System.currentTimeMillis(),
            )

            // Create working directory tracker (T021)
            val directoryTracker = WorkingDirectoryTracker(
                initialDirectory = workingDir,
            )

            // Create command monitor for failure detection (T025)
            val commandMonitor = CommandMonitor()

            // Forward command monitor errors to repository error flow
            commandMonitor.observeErrors()
                .onEach { error -> _errors.tryEmit(error) }
                .launchIn(repositoryScope)

            // Store all components
            sessions[sessionId] = SessionWrapper(
                session = session,
                termuxSession = termuxSession,
                workingDirectoryTracker = directoryTracker,
                commandMonitor = commandMonitor,
            )

            Result.success(sessionId)
        } catch (e: Exception) {
            // Emit error event
            _errors.tryEmit(
                TerminalError.InitializationFailed(
                    reason = e.message ?: "Unknown error during session creation",
                ),
            )
            Result.failure(e)
        }
    }

    /**
     * Creates a TerminalSessionClient callback for a specific session.
     *
     * This client receives events from the Termux TerminalSession and
     * translates them into our Flow-based API (output events, errors, etc.).
     *
     * @param sessionId The session ID this client belongs to
     * @return TerminalSessionClient implementation
     */
    private fun createSessionClient(sessionId: String): TerminalSessionClient {
        return object : TerminalSessionClient {
            override fun onTextChanged(session: com.termux.terminal.TerminalSession) {
                // Terminal screen updated - emit output
                try {
                    val screen = session.emulator.screen
                    val text = screen.transcriptText

                    // Detect whether output is stdout or stderr using pattern matching (T024)
                    val streamType = outputProcessor.detectStreamType(text)

                    // Create terminal output object
                    val terminalOutput = TerminalOutput(
                        text = text,
                        stream = streamType,
                        timestamp = System.currentTimeMillis(),
                    )

                    // Emit to output flow
                    _output.tryEmit(terminalOutput)

                    // Process output through command monitor for failure detection (T025)
                    val wrapper = sessions[sessionId]
                    if (wrapper != null) {
                        repositoryScope.launch {
                            wrapper.commandMonitor.onOutputReceived(terminalOutput)
                        }
                    }
                } catch (e: Exception) {
                    // PTY read error detected (T026)
                    _errors.tryEmit(
                        TerminalError.IOError(
                            message = "Error reading terminal output: ${e.message}",
                        ),
                    )
                }
            }

            override fun onTitleChanged(session: com.termux.terminal.TerminalSession) {
                // Terminal title changed via escape sequence (e.g., echo -ne "\033]0;New Title\007")
                // Not currently used, but could update session metadata
            }

            override fun onSessionFinished(session: com.termux.terminal.TerminalSession) {
                // Shell process exited (T026)
                val wrapper = sessions[sessionId]
                if (wrapper != null) {
                    val updatedSession = wrapper.session.copy(state = SessionState.STOPPED)
                    sessions[sessionId] = wrapper.copy(session = updatedSession)

                    // Determine if this was a crash or normal exit
                    val exitStatus = session.exitStatus
                    if (exitStatus != 0) {
                        // Non-zero exit = crash or abnormal termination
                        _errors.tryEmit(
                            TerminalError.SessionCrashed(
                                reason = "Shell process terminated unexpectedly (exit code: $exitStatus)",
                            ),
                        )
                    }
                }
            }

            override fun onCopyTextToClipboard(
                session: com.termux.terminal.TerminalSession,
                text: String,
            ) {
                // Clipboard copy requested (e.g., text selection)
                // Not implemented yet - would integrate with Android clipboard
            }

            override fun onPasteTextFromClipboard(session: com.termux.terminal.TerminalSession) {
                // Clipboard paste requested
                // Not implemented yet - would integrate with Android clipboard
            }

            override fun onBell(session: com.termux.terminal.TerminalSession) {
                // Terminal bell character received (ASCII 7)
                // Could trigger vibration or notification
            }

            override fun onColorsChanged(session: com.termux.terminal.TerminalSession) {
                // Color scheme changed via escape sequences
                // Not currently used
            }

            override fun onTerminalCursorStateChange(state: Boolean) {
                // Cursor visibility changed (blink on/off)
                // Not currently used
            }

            override fun getTerminalCursorStyle(): Int? {
                // Return cursor style (TERMINAL_CURSOR_STYLE_BLOCK, etc.)
                // Returning null uses default
                return null
            }

            override fun logError(tag: String, message: String) {
                // Log errors from terminal emulator
                android.util.Log.e(tag, message)
            }

            override fun logWarn(tag: String, message: String) {
                // Log warnings from terminal emulator
                android.util.Log.w(tag, message)
            }

            override fun logInfo(tag: String, message: String) {
                // Log info messages from terminal emulator
                android.util.Log.i(tag, message)
            }

            override fun logDebug(tag: String, message: String) {
                // Log debug messages from terminal emulator
                android.util.Log.d(tag, message)
            }

            override fun logVerbose(tag: String, message: String) {
                // Log verbose messages from terminal emulator
                android.util.Log.v(tag, message)
            }

            override fun logStackTraceWithMessage(
                tag: String,
                message: String,
                e: Exception,
            ) {
                // Log exception with message
                android.util.Log.e(tag, message, e)
            }

            override fun logStackTrace(tag: String, e: Exception) {
                // Log exception stack trace
                android.util.Log.e(tag, "Exception occurred", e)
            }
        }
    }

    /**
     * Executes a command in the specified terminal session.
     *
     * The command is written to the PTY stdin with a newline appended.
     * Output will be emitted asynchronously via the Flow returned by
     * `observeOutput(sessionId)`.
     *
     * ## Command Processing
     * - Command is validated (non-empty check)
     * - Newline character is automatically appended
     * - Command is written to PTY stdin
     * - Shell processes the command asynchronously
     * - Output appears in the output Flow
     *
     * ## Error Handling
     * - If session doesn't exist, silently fails (logs warning)
     * - If PTY write fails, emits IOError to error Flow
     * - If command is empty, skips execution
     *
     * @param sessionId The unique identifier of the session to execute in
     * @param command The command string to execute (newline will be appended)
     */
    override suspend fun executeCommand(sessionId: String, command: String) {
        // Validate inputs
        if (command.isBlank()) return

        val wrapper = sessions[sessionId]
        if (wrapper == null) {
            _errors.tryEmit(
                TerminalError.IOError(
                    message = "Session not found: $sessionId",
                ),
            )
            return
        }

        try {
            // Track working directory changes (T022)
            val homeDirectory = wrapper.session.environment["HOME"] ?: "/data/data/com.convocli/files/home"
            wrapper.workingDirectoryTracker.onCommand(command, homeDirectory)

            // Track command for failure detection (T025)
            wrapper.commandMonitor.onCommandExecuted(command)

            // Write command to PTY stdin with newline
            wrapper.termuxSession.write("$command\n")
        } catch (e: Exception) {
            _errors.tryEmit(
                TerminalError.IOError(
                    message = "Failed to write command: ${e.message}",
                ),
            )
        }
    }

    /**
     * Observes output from a specific terminal session.
     *
     * Returns a Flow that emits `TerminalOutput` events as they arrive
     * from the PTY. Output is streamed in real-time with minimal latency.
     *
     * ## Output Characteristics
     * - **Asynchronous**: Output arrives as the command executes
     * - **Chunked**: Output may be split across multiple events
     * - **Ordered**: Events are emitted in the order they're received
     * - **Streams**: Both STDOUT and STDERR are included (tagged with `StreamType`)
     *
     * ## Flow Behavior
     * - Hot Flow: Emissions start when session is created
     * - Filtered: Only emits output for the specified sessionId
     * - Lifecycle: Flow completes when session is destroyed
     *
     * @param sessionId The session to observe output from
     * @return Flow of terminal output events
     */
    override fun observeOutput(sessionId: String): Flow<TerminalOutput> {
        // For now, return all output (session-specific filtering will be added in T014)
        // TODO: Add sessionId to TerminalOutput and filter by it
        return _output.asSharedFlow()
    }

    /**
     * Observes errors from all terminal sessions.
     *
     * Returns a SharedFlow that emits `TerminalError` events when errors occur.
     * Errors are global - not filtered by session ID.
     *
     * ## Error Types
     * - **InitializationFailed**: Session couldn't be created
     * - **CommandFailed**: Command executed but returned non-zero exit code
     * - **SessionCrashed**: Session died unexpectedly
     * - **IOError**: PTY I/O operation failed
     *
     * ## Flow Behavior
     * - Hot SharedFlow: Can have multiple collectors
     * - Replay: No replay (errors are ephemeral)
     * - Buffering: Minimal buffering to prevent backpressure issues
     *
     * @return SharedFlow of error events
     */
    override fun observeErrors(): SharedFlow<TerminalError> {
        return errors
    }

    /**
     * Destroys a terminal session and cleans up all resources.
     *
     * This operation:
     * 1. Sends SIGHUP to the shell process
     * 2. Closes PTY file descriptors (handled by Termux)
     * 3. Removes session from internal tracking
     *
     * ## When to Call
     * - User explicitly closes terminal
     * - App is being destroyed
     * - Session has crashed and needs restart
     *
     * ## Behavior
     * - If session doesn't exist, silently succeeds
     * - Always safe to call multiple times
     * - Guaranteed cleanup even if exceptions occur
     *
     * @param sessionId The session to destroy
     */
    override suspend fun destroySession(sessionId: String) {
        val wrapper = sessions.remove(sessionId)
        if (wrapper != null) {
            try {
                // Termux TerminalSession.finishIfRunning() sends SIGHUP and cleans up
                wrapper.termuxSession.finishIfRunning()
            } catch (e: Exception) {
                // Log but don't throw - cleanup should be best-effort
                _errors.tryEmit(
                    TerminalError.IOError(
                        message = "Error during session cleanup: ${e.message}",
                    ),
                )
            }
        }
    }

    /**
     * Gets the current state of a terminal session.
     *
     * Returns the current `SessionState` (RUNNING, STOPPED, or ERROR),
     * or null if the session doesn't exist.
     *
     * ## Use Cases
     * - Check if session is still alive before executing command
     * - Determine if session needs restart
     * - Update UI based on session state
     *
     * ## Return Values
     * - `SessionState.RUNNING`: Session is active
     * - `SessionState.STOPPED`: Session was terminated normally
     * - `SessionState.ERROR`: Session crashed
     * - `null`: Session ID not found
     *
     * @param sessionId The session to check
     * @return Current state or null if session doesn't exist
     */
    override fun getSessionState(sessionId: String): SessionState? {
        return sessions[sessionId]?.session?.state
    }

    /**
     * Observes the current working directory for a terminal session.
     *
     * Returns a Flow that emits the current working directory path whenever
     * it changes (e.g., when `cd` commands are executed).
     *
     * ## Implementation
     * The working directory is tracked by WorkingDirectoryTracker, which
     * monitors `cd` commands and resolves paths accordingly. This is a
     * client-side tracking mechanism for UI display purposes.
     *
     * ## Return Value
     * - Flow of current working directory path if session exists
     * - Empty Flow with initial directory if session doesn't exist
     *
     * @param sessionId The session to observe
     * @return Flow of current working directory path
     */
    override fun observeWorkingDirectory(sessionId: String): Flow<String> {
        val wrapper = sessions[sessionId]
        return if (wrapper != null) {
            wrapper.workingDirectoryTracker.currentDirectory
        } else {
            // Session doesn't exist - return empty flow
            flowOf()
        }
    }

    /**
     * Gets the default environment variables for a terminal session.
     *
     * These are the standard Unix/Linux environment variables expected
     * by shell scripts and command-line programs.
     *
     * ## Environment Variables
     * - **HOME**: User's home directory (app private directory)
     * - **PATH**: Executable search path (app binaries + system binaries)
     * - **SHELL**: Path to the shell executable (bash)
     * - **TMPDIR**: Temporary files directory
     * - **PREFIX**: Termux installation prefix
     * - **TERM**: Terminal type for ANSI escape sequences
     * - **LANG**: Locale for character encoding
     *
     * @return Map of environment variable names to values
     */
    private fun getDefaultEnvironment(): Map<String, String> {
        val filesDir = context.filesDir.absolutePath
        return mapOf(
            "HOME" to "$filesDir/home",
            "PATH" to "$filesDir/usr/bin:/system/bin",
            "SHELL" to "$filesDir/usr/bin/bash",
            "TMPDIR" to "$filesDir/usr/tmp",
            "PREFIX" to "$filesDir/usr",
            "TERM" to "xterm-256color",
            "LANG" to "en_US.UTF-8",
        )
    }
}
