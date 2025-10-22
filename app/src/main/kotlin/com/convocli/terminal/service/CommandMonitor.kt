package com.convocli.terminal.service

import com.convocli.terminal.model.StreamType
import com.convocli.terminal.model.TerminalError
import com.convocli.terminal.model.TerminalOutput
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Monitors command execution and detects failures.
 *
 * Tracks commands sent to the terminal and correlates them with output
 * to detect command failures. Since PTY doesn't expose exit codes directly,
 * this service uses heuristic-based detection:
 * - Stderr output indicates potential failure
 * - Specific error patterns indicate definite failure
 * - Command completion is detected by output patterns
 *
 * ## Limitations
 * - Cannot get actual exit codes from PTY (shell continues running)
 * - Exit code is inferred as 1 for all detected failures
 * - Commands that succeed but output to stderr will be flagged as failed
 * - Silent failures (no stderr) won't be detected
 *
 * ## Future Enhancement
 * Could execute `echo $?` after each command to get real exit code,
 * but that would add latency and complexity.
 *
 * ## Usage
 * ```kotlin
 * val monitor = CommandMonitor()
 *
 * // Track command execution
 * monitor.onCommandExecuted("ls /nonexistent")
 *
 * // Process output
 * monitor.onOutputReceived(terminalOutput)
 *
 * // Observe errors
 * monitor.observeErrors().collect { error ->
 *     when (error) {
 *         is TerminalError.CommandFailed -> handleFailure(error)
 *         else -> {}
 *     }
 * }
 * ```
 */
class CommandMonitor {
    /**
     * The last command that was executed.
     *
     * Used to correlate stderr output with the command that produced it.
     * Null if no command has been executed yet.
     */
    private var lastCommand: String? = null

    /**
     * Buffer for stderr output from the current command.
     *
     * Accumulates stderr lines until command completion is detected,
     * then emits CommandFailed error if stderr is present.
     */
    private val stderrBuffer = StringBuilder()

    /**
     * Shared flow for command failure errors.
     *
     * Emits `TerminalError.CommandFailed` when a command failure is detected.
     */
    private val _errors = MutableSharedFlow<TerminalError>()
    val errors: SharedFlow<TerminalError> = _errors.asSharedFlow()

    /**
     * Output stream processor for detecting stderr.
     */
    private val outputProcessor = OutputStreamProcessor()

    /**
     * Notifies the monitor that a command has been executed.
     *
     * This should be called immediately after writing a command to the PTY.
     * The command string is stored to correlate with subsequent stderr output.
     *
     * @param command The command that was executed (e.g., "ls /nonexistent")
     */
    fun onCommandExecuted(command: String) {
        // Store command for error correlation
        lastCommand = command

        // Clear stderr buffer for new command
        stderrBuffer.clear()
    }

    /**
     * Processes terminal output to detect command failures.
     *
     * Analyzes output to determine if it represents stderr (error) output.
     * If stderr is detected, it's accumulated in the buffer. When command
     * completion is detected (prompt return), CommandFailed error is emitted
     * if stderr was present.
     *
     * ## Detection Logic
     * 1. Check if output is stderr (using OutputStreamProcessor)
     * 2. If stderr: accumulate in buffer
     * 3. Check if output indicates command completion (prompt pattern)
     * 4. If complete and stderr present: emit CommandFailed
     * 5. If complete and no stderr: clear buffer and continue
     *
     * @param output The terminal output to process
     */
    suspend fun onOutputReceived(output: TerminalOutput) {
        // Accumulate stderr output
        if (output.stream == StreamType.STDERR) {
            stderrBuffer.append(output.text)
        }

        // Check for command completion
        if (isCommandComplete(output.text)) {
            // Command has completed
            if (stderrBuffer.isNotEmpty()) {
                // Command failed (stderr present)
                emitCommandFailed()
            }

            // Reset for next command
            stderrBuffer.clear()
        }
    }

    /**
     * Checks if the output indicates command completion.
     *
     * Command completion is detected by looking for shell prompt patterns.
     * Common prompts: `$ `, `# `, `> `, or custom PS1 patterns.
     *
     * ## Limitations
     * - Custom prompts may not be detected
     * - Multi-line prompts may cause false positives
     * - Commands that output prompt-like text may trigger false completion
     *
     * @param text The output text to check
     * @return true if output appears to be a prompt (command complete), false otherwise
     */
    private fun isCommandComplete(text: String): Boolean {
        // Check last line for prompt pattern
        val lines = text.lines()
        val lastLine = lines.lastOrNull() ?: return false

        // Common shell prompts
        val promptPatterns = listOf(
            Regex(".*\\$\\s*$"),  // Bash user prompt: "$ "
            Regex(".*#\\s*$"),    // Bash root prompt: "# "
            Regex(".*>\\s*$"),    // Generic prompt: "> "
        )

        for (pattern in promptPatterns) {
            if (pattern.matches(lastLine)) {
                return true
            }
        }

        return false
    }

    /**
     * Emits a CommandFailed error for the last executed command.
     *
     * Uses the accumulated stderr buffer and the last command string
     * to create a TerminalError.CommandFailed event.
     *
     * ## Exit Code
     * Since PTY doesn't expose actual exit codes, we use 1 as a generic
     * "command failed" exit code. This matches the convention where:
     * - 0 = success
     * - 1 = general error
     * - 2+ = specific error codes
     */
    private suspend fun emitCommandFailed() {
        val command = lastCommand ?: "unknown command"
        val stderrText = stderrBuffer.toString().trim()

        // Extract simplified error message
        val errorMessage = outputProcessor.extractErrorMessage(stderrText)

        // Emit error
        _errors.emit(
            TerminalError.CommandFailed(
                command = command,
                exitCode = 1, // Generic failure code (actual code not available from PTY)
                stderr = errorMessage,
            ),
        )
    }

    /**
     * Observes command failure errors.
     *
     * Returns a SharedFlow that emits `TerminalError.CommandFailed` events
     * when command failures are detected.
     *
     * @return SharedFlow of command failure errors
     */
    fun observeErrors(): SharedFlow<TerminalError> {
        return errors
    }

    /**
     * Resets the monitor state.
     *
     * Clears the last command and stderr buffer. Useful for testing
     * or when starting a new session.
     */
    fun reset() {
        lastCommand = null
        stderrBuffer.clear()
    }
}
