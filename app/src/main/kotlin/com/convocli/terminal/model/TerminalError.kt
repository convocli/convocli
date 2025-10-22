package com.convocli.terminal.model

/**
 * Represents errors that can occur during terminal operations.
 *
 * This sealed class hierarchy provides type-safe error handling for all
 * terminal-related failures. Each error type includes context-specific
 * information to help with debugging and user-facing error messages.
 *
 * ## Error Handling Pattern
 * ```kotlin
 * repository.observeErrors().collect { error ->
 *     when (error) {
 *         is TerminalError.InitializationFailed -> handleFatalError(error.reason)
 *         is TerminalError.CommandFailed -> displayCommandError(error)
 *         is TerminalError.SessionCrashed -> offerRestart(error.reason)
 *         is TerminalError.IOError -> logAndRetry(error.message)
 *     }
 * }
 * ```
 *
 * ## Error Categories
 * - **Fatal**: InitializationFailed, SessionCrashed
 * - **Recoverable**: CommandFailed, IOError
 */
sealed class TerminalError {
    /**
     * Terminal session initialization failed.
     *
     * This is a fatal error that prevents the session from starting.
     *
     * ## Common Causes
     * - PTY creation failure (system out of resources)
     * - Shell executable not found
     * - Permission denied to create PTY
     * - Invalid environment configuration
     *
     * ## Recovery
     * - Display error dialog to user
     * - Prevent further command execution
     * - Suggest checking app permissions or reinstalling
     *
     * @property reason Human-readable description of why initialization failed
     */
    data class InitializationFailed(
        val reason: String,
    ) : TerminalError()

    /**
     * A command execution failed.
     *
     * This is a recoverable error - the session continues running,
     * but the specific command did not execute successfully.
     *
     * ## Common Causes
     * - Command not found (typo or not installed)
     * - Permission denied (trying to access restricted files)
     * - File not found (command argument refers to missing file)
     * - Invalid syntax or arguments
     *
     * ## Recovery
     * - Display stderr output to user
     * - Show exit code for debugging
     * - Session continues - user can try another command
     *
     * @property command The command that failed
     * @property exitCode The exit code returned by the command (typically non-zero)
     * @property stderr The error output from the command
     */
    data class CommandFailed(
        val command: String,
        val exitCode: Int,
        val stderr: String,
    ) : TerminalError()

    /**
     * The terminal session crashed unexpectedly.
     *
     * This is a fatal error that terminates the session.
     *
     * ## Common Causes
     * - Shell process died unexpectedly
     * - Broken pipe (PTY connection lost)
     * - Out of memory
     * - Process killed by system
     * - Resource exhaustion
     *
     * ## Recovery
     * - Detect session is no longer functional
     * - Offer user option to restart session
     * - Log detailed crash information for debugging
     *
     * @property reason Human-readable description of why the session crashed
     */
    data class SessionCrashed(
        val reason: String,
    ) : TerminalError()

    /**
     * An I/O error occurred during PTY operations.
     *
     * This may be recoverable depending on the specific error.
     *
     * ## Common Causes
     * - Read timeout (no data available)
     * - Write failure (PTY buffer full)
     * - Connection lost (intermittent)
     * - File descriptor closed unexpectedly
     *
     * ## Recovery
     * - Log the error for debugging
     * - Retry the operation (if transient)
     * - If persistent, treat as SessionCrashed
     *
     * @property message Detailed error message from the I/O exception
     */
    data class IOError(
        val message: String,
    ) : TerminalError()
}
