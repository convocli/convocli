package com.convocli.terminal

import com.convocli.data.model.OutputChunk
import com.convocli.data.model.TerminalSessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Contract for terminal session management and command execution.
 *
 * This repository abstracts the Termux terminal emulator core and provides
 * a Kotlin-friendly reactive API for command execution and output streaming.
 *
 * Implementation must ensure:
 * - Thread-safe command execution
 * - Output streaming without backpressure
 * - Proper session lifecycle management
 * - Working directory tracking
 */
interface TerminalRepository {

    /**
     * Creates a new terminal session and initializes the Termux environment.
     *
     * The session is created with default environment variables and the
     * application's home directory as working directory.
     *
     * @return Flow emitting session state transitions:
     *   - Ready: Session created successfully
     *   - Error: Session creation failed
     *   - Inactive: Session terminated
     */
    fun createSession(): Flow<TerminalSessionState>

    /**
     * Destroys the current terminal session and cleans up resources.
     *
     * This method should:
     * - Terminate all running processes
     * - Close PTY file descriptors
     * - Release native resources
     * - Clear output buffers
     *
     * After calling this, createSession() must be called to execute new commands.
     */
    suspend fun destroySession()

    /**
     * Executes a command in the terminal session.
     *
     * The command is written to the terminal input stream followed by a newline.
     * Output will be emitted via observeOutput() flow.
     *
     * @param command The command string to execute (e.g., "ls -la")
     * @param blockId The associated command block ID for output routing
     *
     * @throws IllegalStateException if session is not active
     * @throws IllegalArgumentException if command is empty or too long (>4096 chars)
     */
    suspend fun executeCommand(command: String, blockId: String)

    /**
     * Cancels a currently executing command.
     *
     * Sends SIGTERM to the process. If process doesn't terminate within 2 seconds,
     * escalates to SIGKILL.
     *
     * @param blockId The command block ID to cancel
     *
     * @throws IllegalArgumentException if no command with blockId is executing
     */
    suspend fun cancelCommand(blockId: String)

    /**
     * Observes terminal output in real-time.
     *
     * Emits OutputChunk instances as terminal generates output. Chunks are
     * buffered and emitted at max 60fps to prevent UI lag.
     *
     * Output includes:
     * - Raw text with ANSI escape codes preserved
     * - Stream type (stdout/stderr)
     * - Timestamp for chronological interleaving
     * - Associated block ID for routing
     *
     * @return Hot flow of output chunks (SharedFlow)
     */
    fun observeOutput(): Flow<OutputChunk>

    /**
     * Current working directory of the terminal session.
     *
     * Updated automatically when commands change directory (cd).
     * Initial value is the app's home directory.
     *
     * @return StateFlow emitting current absolute directory path
     */
    val workingDirectory: StateFlow<String>

    /**
     * Indicates whether the terminal session is active and ready for commands.
     *
     * False if:
     * - Session hasn't been created yet
     * - Session creation failed
     * - Session was explicitly destroyed
     * - Process crashed
     *
     * @return StateFlow emitting session active state
     */
    val isSessionActive: StateFlow<Boolean>

    /**
     * Retrieves the exit code of the last completed command.
     *
     * @param blockId The command block ID
     * @return Exit code (0 = success, non-zero = failure), or null if command still executing
     */
    suspend fun getExitCode(blockId: String): Int?
}
