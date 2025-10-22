package com.convocli.terminal.service

import com.convocli.terminal.model.StreamType

/**
 * Processes terminal output to distinguish between stdout and stderr streams.
 *
 * Since Termux PTY merges stdout and stderr at the terminal level (standard
 * terminal behavior), this processor uses pattern matching to identify error
 * output based on common shell error patterns.
 *
 * ## Detection Strategy
 * Stderr is identified by matching against common error patterns:
 * - Command not found errors
 * - File/directory not found errors
 * - Permission denied errors
 * - Syntax errors
 * - Other common shell error messages
 *
 * ## Limitations
 * - This is heuristic-based, not perfect
 * - Custom error messages may not be detected
 * - Non-English locales may have different error messages
 * - Some stderr output might be classified as stdout
 *
 * ## Usage
 * ```kotlin
 * val processor = OutputStreamProcessor()
 * val streamType = processor.detectStreamType(outputText)
 * ```
 */
class OutputStreamProcessor {
    /**
     * Common error patterns that indicate stderr content.
     *
     * These patterns match typical shell error messages from bash, coreutils,
     * and common Linux commands.
     */
    private val stderrPatterns = listOf(
        // Command not found
        Regex(".*: command not found", RegexOption.IGNORE_CASE),
        Regex("bash: .*: command not found", RegexOption.IGNORE_CASE),
        Regex("sh: .*: not found", RegexOption.IGNORE_CASE),

        // File/directory errors
        Regex(".*: No such file or directory", RegexOption.IGNORE_CASE),
        Regex(".*: cannot access", RegexOption.IGNORE_CASE),
        Regex(".*: cannot open", RegexOption.IGNORE_CASE),
        Regex(".*: is a directory", RegexOption.IGNORE_CASE),
        Regex(".*: not a directory", RegexOption.IGNORE_CASE),

        // Permission errors
        Regex(".*: Permission denied", RegexOption.IGNORE_CASE),
        Regex(".*: permission denied", RegexOption.IGNORE_CASE),
        Regex(".*: access denied", RegexOption.IGNORE_CASE),

        // Syntax and usage errors
        Regex(".*: invalid option.*", RegexOption.IGNORE_CASE),
        Regex(".*: illegal option.*", RegexOption.IGNORE_CASE),
        Regex(".*: unrecognized option.*", RegexOption.IGNORE_CASE),
        Regex(".*: syntax error.*", RegexOption.IGNORE_CASE),
        Regex("usage: .*", RegexOption.IGNORE_CASE),

        // Common error prefixes
        Regex("error: .*", RegexOption.IGNORE_CASE),
        Regex("fatal: .*", RegexOption.IGNORE_CASE),
        Regex("warning: .*", RegexOption.IGNORE_CASE),
        Regex(".*: error: .*", RegexOption.IGNORE_CASE),

        // Git errors (common in terminal use)
        Regex("fatal: not a git repository", RegexOption.IGNORE_CASE),
        Regex("git: .*", RegexOption.IGNORE_CASE),

        // Package manager errors
        Regex("E: .*", RegexOption.IGNORE_CASE), // apt/dpkg errors
        Regex(".*: package not found", RegexOption.IGNORE_CASE),

        // Python errors
        Regex(".*Error: .*", RegexOption.IGNORE_CASE),
        Regex("Traceback \\(most recent call last\\):", RegexOption.IGNORE_CASE),

        // General failure indicators
        Regex("failed to .*", RegexOption.IGNORE_CASE),
        Regex("could not .*", RegexOption.IGNORE_CASE),
        Regex("unable to .*", RegexOption.IGNORE_CASE),
    )

    /**
     * Detects the stream type (stdout or stderr) for terminal output text.
     *
     * Analyzes the output text to determine if it represents error output (stderr)
     * or normal output (stdout) based on pattern matching.
     *
     * ## Detection Logic
     * 1. Split text into lines
     * 2. Check each line against stderr patterns
     * 3. If any line matches, classify entire chunk as stderr
     * 4. Otherwise, classify as stdout
     *
     * ## Edge Cases
     * - Empty text is classified as stdout
     * - Text containing only whitespace is classified as stdout
     * - Mixed stdout/stderr content is classified based on presence of error patterns
     *
     * @param text The terminal output text to analyze
     * @return StreamType.STDERR if error patterns detected, StreamType.STDOUT otherwise
     */
    fun detectStreamType(text: String): StreamType {
        // Empty or whitespace-only text is stdout
        if (text.isBlank()) {
            return StreamType.STDOUT
        }

        // Check each line for error patterns
        val lines = text.lines()
        for (line in lines) {
            if (isStderrLine(line)) {
                return StreamType.STDERR
            }
        }

        // No error patterns found - classify as stdout
        return StreamType.STDOUT
    }

    /**
     * Checks if a single line matches any stderr pattern.
     *
     * @param line The line to check
     * @return true if line matches an error pattern, false otherwise
     */
    private fun isStderrLine(line: String): Boolean {
        // Skip empty lines
        if (line.isBlank()) {
            return false
        }

        // Check against all stderr patterns
        for (pattern in stderrPatterns) {
            if (pattern.matches(line)) {
                return true
            }
        }

        return false
    }

    /**
     * Extracts error message from stderr output.
     *
     * Attempts to extract the core error message from stderr output by
     * removing common prefixes and command names.
     *
     * ## Examples
     * - "bash: foo: command not found" → "foo: command not found"
     * - "ls: /nonexistent: No such file or directory" → "No such file or directory"
     * - "error: failed to open file" → "failed to open file"
     *
     * @param stderrText The stderr output text
     * @return Simplified error message, or original text if no simplification possible
     */
    fun extractErrorMessage(stderrText: String): String {
        val trimmed = stderrText.trim()

        // Remove bash prefix
        val bashPrefix = Regex("^bash: (.+)$")
        bashPrefix.matchEntire(trimmed)?.let {
            return it.groupValues[1]
        }

        // Remove sh prefix
        val shPrefix = Regex("^sh: (.+)$")
        shPrefix.matchEntire(trimmed)?.let {
            return it.groupValues[1]
        }

        // Remove error prefix
        val errorPrefix = Regex("^error: (.+)$", RegexOption.IGNORE_CASE)
        errorPrefix.matchEntire(trimmed)?.let {
            return it.groupValues[1]
        }

        // Remove fatal prefix
        val fatalPrefix = Regex("^fatal: (.+)$", RegexOption.IGNORE_CASE)
        fatalPrefix.matchEntire(trimmed)?.let {
            return it.groupValues[1]
        }

        // Return original if no simplification found
        return trimmed
    }
}
