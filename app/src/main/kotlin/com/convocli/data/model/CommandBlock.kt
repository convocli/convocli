package com.convocli.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a single command execution as a displayable unit in the command blocks UI.
 *
 * Each CommandBlock captures:
 * - The command text entered by the user
 * - The combined stdout/stderr output
 * - Execution status (pending, executing, success, failure)
 * - Timing information (timestamp, duration)
 * - Exit code for completed commands
 * - Working directory context
 * - UI state (expanded/collapsed)
 *
 * This is an immutable data class for predictable state management and testing.
 *
 * @property id Unique identifier (UUID format)
 * @property command The command text entered by user
 * @property output Combined stdout/stderr output
 * @property status Current execution state
 * @property timestamp Unix timestamp in milliseconds when command was submitted
 * @property executionDuration Time taken to complete in milliseconds, null if pending/executing
 * @property exitCode Process exit code, null until command completes
 * @property workingDirectory Directory where command was executed
 * @property isExpanded Whether the output is expanded (true) or collapsed (false) in the UI
 */
@Serializable
data class CommandBlock(
    val id: String,
    val command: String,
    val output: String,
    val status: CommandStatus,
    val timestamp: Long,
    val executionDuration: Long? = null,
    val exitCode: Int? = null,
    val workingDirectory: String,
    val isExpanded: Boolean = true
) {
    /**
     * Formats the timestamp as a relative time string.
     * Examples: "just now", "2 minutes ago", "1 hour ago"
     */
    fun formattedTimestamp(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 5000 -> "just now"
            diff < 60000 -> "${diff / 1000}s ago"
            diff < 3600000 -> "${diff / 60000}m ago"
            diff < 86400000 -> "${diff / 3600000}h ago"
            else -> "${diff / 86400000}d ago"
        }
    }

    /**
     * Formats the execution duration as a human-readable string.
     * Examples: "1.2s", "45s", "2m 15s"
     */
    fun formattedDuration(): String? {
        if (executionDuration == null) return null

        val seconds = executionDuration / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60

        return when {
            minutes > 0 -> "${minutes}m ${remainingSeconds}s"
            seconds >= 1 -> "${executionDuration / 1000.0}s"
            else -> "${executionDuration}ms"
        }
    }

    /**
     * Checks if the command was cancelled by user or signal.
     * Exit codes 130 (SIGINT), 143 (SIGTERM), 137 (SIGKILL) indicate cancellation.
     */
    fun isCancelled(): Boolean {
        return exitCode in listOf(130, 143, 137)
    }

    /**
     * Gets the number of lines in the output.
     */
    fun lineCount(): Int {
        return output.lines().size
    }
}

/**
 * Execution states for a command block.
 *
 * State transitions:
 * PENDING → EXECUTING → (SUCCESS | FAILURE)
 */
@Serializable
enum class CommandStatus {
    /** Command submitted but not yet executing */
    PENDING,

    /** Command currently running */
    EXECUTING,

    /** Command completed with exit code 0 */
    SUCCESS,

    /** Command completed with non-zero exit code or was cancelled */
    FAILURE
}
