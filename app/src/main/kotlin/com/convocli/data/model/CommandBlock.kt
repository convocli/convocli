package com.convocli.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Represents a single command execution in the UI with its associated output and state.
 *
 * This is a Room entity that persists command blocks to the database for history
 * and session restoration across app restarts.
 *
 * Each CommandBlock captures:
 * - The command text entered by the user
 * - The combined stdout/stderr output
 * - Execution status (pending, executing, success, failure, canceled)
 * - Timing information (start time, end time)
 * - Exit code for completed commands
 * - Working directory context
 * - UI state (expanded/collapsed)
 *
 * @property id Unique identifier (UUID format)
 * @property command The command text entered by user
 * @property output Combined stdout/stderr output (interleaved)
 * @property status Current execution state
 * @property exitCode Process exit code, null until command completes
 * @property startTime Unix timestamp in milliseconds when command was submitted
 * @property endTime Unix timestamp in milliseconds when command completed, null if not complete
 * @property workingDirectory Directory where command was executed
 * @property isExpanded Whether the output is expanded (true) or collapsed (false) in the UI
 */
@Entity(tableName = "command_blocks")
data class CommandBlock(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "command")
    val command: String,

    @ColumnInfo(name = "output")
    val output: String = "",

    @ColumnInfo(name = "status")
    val status: CommandStatus = CommandStatus.PENDING,

    @ColumnInfo(name = "exit_code")
    val exitCode: Int? = null,

    @ColumnInfo(name = "start_time")
    val startTime: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "end_time")
    val endTime: Long? = null,

    @ColumnInfo(name = "working_directory")
    val workingDirectory: String = "/",

    @ColumnInfo(name = "is_expanded")
    val isExpanded: Boolean = false
) {
    /**
     * Formats the timestamp as HH:mm:ss.
     * Example: "14:23:15"
     */
    fun formattedTimestamp(): String {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(startTime))
    }

    /**
     * Formats the execution duration as a human-readable string.
     * Examples: "125ms", "1.2s", "2m 15s"
     * Returns null if command is not complete.
     */
    fun formattedDuration(): String? {
        return endTime?.let { end ->
            val durationMs = end - startTime
            when {
                durationMs < 1000 -> "${durationMs}ms"
                durationMs < 60_000 -> String.format("%.1fs", durationMs / 1000.0)
                else -> String.format("%dm %ds", durationMs / 60_000, (durationMs % 60_000) / 1000)
            }
        }
    }

    /**
     * Checks if the command has completed (success, failure, or canceled).
     */
    fun isComplete(): Boolean = status in listOf(
        CommandStatus.SUCCESS,
        CommandStatus.FAILURE,
        CommandStatus.CANCELED
    )
}

/**
 * Execution states for a command block.
 *
 * State transitions:
 * PENDING → EXECUTING → (SUCCESS | FAILURE | CANCELED)
 *
 * Final states (cannot transition): SUCCESS, FAILURE, CANCELED
 */
enum class CommandStatus {
    /** Command created but not yet sent to terminal */
    PENDING,

    /** Command sent to terminal, output streaming */
    EXECUTING,

    /** Command completed successfully (exit code 0) */
    SUCCESS,

    /** Command completed with error (exit code ≠ 0) */
    FAILURE,

    /** Command was canceled by user or system */
    CANCELED
}
