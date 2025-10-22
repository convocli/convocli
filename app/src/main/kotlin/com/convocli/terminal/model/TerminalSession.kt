package com.convocli.terminal.model

/**
 * Represents an active terminal session with a shell process.
 *
 * This data class encapsulates all metadata for a terminal session including
 * the shell configuration, working directory, environment variables, and current state.
 *
 * @property sessionId Unique identifier for this session (UUID format)
 * @property shellPath Absolute path to the shell executable (e.g., "/data/data/com.convocli/files/usr/bin/bash")
 * @property workingDirectory Current working directory for command execution
 * @property environment Map of environment variables (HOME, PATH, SHELL, etc.)
 * @property state Current state of the session (RUNNING, STOPPED, or ERROR)
 * @property createdAt Unix timestamp in milliseconds when the session was created
 *
 * @see SessionState
 */
data class TerminalSession(
    val sessionId: String,
    val shellPath: String,
    val workingDirectory: String,
    val environment: Map<String, String>,
    val state: SessionState,
    val createdAt: Long,
)

/**
 * Represents the lifecycle state of a terminal session.
 *
 * State Transitions:
 * ```
 * [NEW] → RUNNING → STOPPED
 *           ↓
 *         ERROR
 * ```
 */
enum class SessionState {
    /**
     * Session is active with a running shell process.
     * Commands can be executed and output is being streamed.
     */
    RUNNING,

    /**
     * Session has been terminated normally.
     * Resources have been cleaned up and no further operations are possible.
     */
    STOPPED,

    /**
     * Session has crashed or encountered a fatal error.
     * Session is no longer functional and should be recreated.
     */
    ERROR,
}
