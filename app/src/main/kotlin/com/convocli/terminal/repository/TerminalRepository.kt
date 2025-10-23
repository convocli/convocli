package com.convocli.terminal.repository

import com.convocli.terminal.data.datastore.PersistedSessionState
import com.convocli.terminal.model.SessionState
import com.convocli.terminal.model.TerminalError
import com.convocli.terminal.model.TerminalOutput
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Repository interface for terminal operations.
 *
 * This interface provides the contract for managing terminal sessions,
 * executing commands, and observing output and errors. It abstracts away
 * the underlying Termux integration details, making the code more testable
 * and maintainable.
 *
 * ## Architecture Pattern
 * ```
 * ViewModel → TerminalRepository (interface) → TermuxTerminalRepository (implementation)
 *                                                        ↓
 *                                               Termux TerminalSession
 *                                                        ↓
 *                                                  Native PTY (JNI)
 * ```
 *
 * ## Thread Safety
 * All methods are thread-safe. Suspend functions use appropriate dispatchers
 * internally. Flow collections are safe for concurrent access.
 *
 * ## Lifecycle
 * - Repository is provided as a Singleton via Hilt
 * - Sessions are managed internally and survive configuration changes
 * - ViewModel should call `destroySession()` when no longer needed
 *
 * ## Usage Example
 * ```kotlin
 * class TerminalViewModel @Inject constructor(
 *     private val repository: TerminalRepository
 * ) : ViewModel() {
 *
 *     private var sessionId: String? = null
 *
 *     init {
 *         viewModelScope.launch {
 *             repository.createSession()
 *                 .onSuccess { id ->
 *                     sessionId = id
 *                     collectOutput(id)
 *                 }
 *                 .onFailure { error ->
 *                     handleError(error)
 *                 }
 *         }
 *     }
 *
 *     fun executeCommand(command: String) {
 *         viewModelScope.launch {
 *             sessionId?.let { id ->
 *                 repository.executeCommand(id, command)
 *             }
 *         }
 *     }
 * }
 * ```
 */
interface TerminalRepository {
    /**
     * Creates a new terminal session.
     *
     * This initializes a Termux TerminalSession with:
     * - PTY (pseudo-terminal) pair
     * - Shell process (bash)
     * - Default environment variables (HOME, PATH, SHELL, etc.)
     * - Initial working directory ($HOME)
     *
     * ## Return Value
     * - **Success**: `Result.success(sessionId)` with a unique session identifier
     * - **Failure**: `Result.failure(exception)` if initialization fails
     *
     * ## Common Failure Causes
     * - PTY creation failure (out of resources)
     * - Shell executable not found
     * - Permission denied
     *
     * ## Threading
     * This is a suspend function that performs I/O operations.
     * Safe to call from any coroutine context.
     *
     * @return Result containing the session ID on success, or an exception on failure
     */
    suspend fun createSession(): Result<String>

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
     * ## Threading
     * This is a suspend function. Safe to call from any coroutine context.
     *
     * @param sessionId The unique identifier of the session to execute in
     * @param command The command string to execute (newline will be appended)
     */
    suspend fun executeCommand(sessionId: String, command: String)

    /**
     * Observes output from a specific terminal session.
     *
     * Returns a Flow that emits `TerminalOutput` events as they arrive
     * from the PTY. Output is streamed in real-time with minimal latency
     * (typically <100ms).
     *
     * ## Output Characteristics
     * - **Asynchronous**: Output arrives as the command executes
     * - **Chunked**: Output may be split across multiple events
     * - **Ordered**: Events are emitted in the order they're received
     * - **Streams**: Both STDOUT and STDERR are included (tagged with `StreamType`)
     * - **ANSI Sequences**: Output may contain ANSI escape codes for colors/formatting
     *
     * ## Flow Behavior
     * - Hot Flow: Emissions start when session is created
     * - Filtered: Only emits output for the specified sessionId
     * - Backpressure: Supports backpressure handling (buffer strategy)
     * - Lifecycle: Flow completes when session is destroyed
     *
     * ## Collection
     * ```kotlin
     * repository.observeOutput(sessionId).collect { output ->
     *     when (output.stream) {
     *         StreamType.STDOUT -> displayNormal(output.text)
     *         StreamType.STDERR -> displayError(output.text)
     *     }
     * }
     * ```
     *
     * @param sessionId The session to observe output from
     * @return Flow of terminal output events
     */
    fun observeOutput(sessionId: String): Flow<TerminalOutput>

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
     * ## Error Handling
     * ```kotlin
     * repository.observeErrors().collect { error ->
     *     when (error) {
     *         is TerminalError.InitializationFailed -> showFatalError(error.reason)
     *         is TerminalError.CommandFailed -> displayCommandError(error)
     *         is TerminalError.SessionCrashed -> offerRestart(error.reason)
     *         is TerminalError.IOError -> logAndRetry(error.message)
     *     }
     * }
     * ```
     *
     * @return SharedFlow of error events
     */
    fun observeErrors(): SharedFlow<TerminalError>

    /**
     * Destroys a terminal session and cleans up all resources.
     *
     * This operation:
     * 1. Stops output streaming
     * 2. Sends SIGHUP to the shell process
     * 3. Closes PTY file descriptors
     * 4. Cancels all coroutines
     * 5. Removes session from internal tracking
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
     * ## Threading
     * This is a suspend function. Safe to call from any coroutine context.
     *
     * @param sessionId The session to destroy
     */
    suspend fun destroySession(sessionId: String)

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
     * ## Threading
     * Non-blocking function. Safe to call from any thread.
     *
     * @param sessionId The session to check
     * @return Current state or null if session doesn't exist
     */
    fun getSessionState(sessionId: String): SessionState?

    /**
     * Observes the current working directory for a terminal session.
     *
     * Returns a Flow that emits the current working directory whenever it changes.
     * The directory is tracked by monitoring `cd` commands and resolving paths
     * appropriately.
     *
     * ## Directory Tracking
     * - Initial value is the session's starting directory ($HOME)
     * - Updates when `cd` commands are executed
     * - Handles relative paths, absolute paths, `cd ~`, `cd ..`, etc.
     *
     * ## Use Cases
     * - Display current directory in UI (prompt, status bar)
     * - Show breadcrumbs navigation
     * - Provide context for file operations
     *
     * ## Note
     * This is a client-side tracking mechanism for UI purposes. The actual
     * working directory of the shell process is managed by bash itself.
     *
     * @param sessionId The session to observe the working directory for
     * @return Flow of current working directory path
     */
    fun observeWorkingDirectory(sessionId: String): Flow<String>

    /**
     * Sends a signal to the running process in the specified session.
     *
     * Sends a signal (typically SIGINT) to interrupt the currently running command.
     * This is equivalent to pressing Ctrl+C in the terminal.
     *
     * ## Use Cases
     * - Cancel long-running commands (e.g., `sleep 100`)
     * - Interrupt stuck processes
     * - Stop infinite loops
     *
     * ## Behavior
     * - Sends the specified signal to the foreground process
     * - Most commonly used with SIGINT (signal 2)
     * - Process may catch signal and handle gracefully
     * - Most processes will exit with code 130 for SIGINT
     * - If session doesn't exist, emits error event
     *
     * ## Threading
     * This is a suspend function. Safe to call from any coroutine context.
     *
     * @param sessionId The session to send the signal to
     * @param signal The signal number (e.g., 2 for SIGINT)
     */
    suspend fun sendSignal(sessionId: String, signal: Int = 2)

    /**
     * Gets the saved session state if one exists (T030).
     *
     * Checks DataStore for previously saved session state.
     * Used by ViewModel to determine if a session should be restored on app start.
     *
     * @return Flow emitting saved state or null if no saved state exists
     */
    fun getSavedSessionState(): Flow<PersistedSessionState?>

    /**
     * Restores a terminal session from saved state (T030).
     *
     * Recreates a terminal session using previously persisted state.
     * This is used to restore sessions after app restart or configuration changes.
     *
     * ## Process
     * 1. Creates new PTY and shell process
     * 2. Applies saved environment variables
     * 3. Restores working directory (via `cd` command)
     * 4. Returns new session ID
     *
     * ## Return Value
     * - **Success**: `Result.success(sessionId)` with restored session ID
     * - **Failure**: `Result.failure(exception)` if restoration fails
     *
     * ## Note
     * The session ID in the result may differ from the saved session ID
     * because a new session is created. However, the working directory,
     * environment, and other state will match the saved state.
     *
     * @param savedState The persisted session state to restore
     * @return Result containing the new session ID on success, or exception on failure
     */
    suspend fun restoreSession(savedState: PersistedSessionState): Result<String>
}
