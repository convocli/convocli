package com.convocli.terminal.contracts

import com.convocli.data.model.PromptDetectionResult

/**
 * Contract for detecting command completion via shell prompt recognition.
 *
 * Command completion is detected by:
 * 1. Pattern matching against known shell prompts (bash, zsh, sh)
 * 2. Timeout fallback: 2 seconds of silence indicates completion
 *
 * This detector must prevent false positives from mid-command output
 * that resembles prompts (e.g., "$ " in a string).
 */
interface PromptDetector {

    /**
     * Analyzes output to detect if a shell prompt is present.
     *
     * Checks the last line of output against pre-configured prompt patterns.
     * Does NOT check mid-command lines to prevent false positives.
     *
     * Supported patterns:
     * - Bash: "$ ", "# " (root)
     * - Zsh: "% "
     * - General: "user@host:~$ ", "user@host:~# "
     *
     * @param output Recent terminal output (typically last 1-5 lines)
     * @return PromptDetectionResult with detection status and matched pattern
     */
    fun detectPrompt(output: String): PromptDetectionResult

    /**
     * Updates the timestamp of the last received output.
     *
     * Used for timeout-based completion detection.
     * Call this every time new output is received.
     */
    fun updateLastOutputTime()

    /**
     * Checks if timeout has elapsed since last output.
     *
     * Returns true if 2 seconds have passed with no new output,
     * indicating command likely completed but prompt not recognized.
     *
     * @return True if timeout elapsed, false otherwise
     */
    fun checkTimeout(): Boolean

    /**
     * Resets the detector state for a new command.
     *
     * Clears output history and resets timeout timer.
     * Call this when starting a new command execution.
     */
    fun reset()
}
