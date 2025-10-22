package com.convocli.terminal.model

/**
 * Represents a chunk of output from terminal command execution.
 *
 * Terminal output is streamed asynchronously as it becomes available from the PTY.
 * Each `TerminalOutput` represents one chunk of text, tagged with its stream type
 * (stdout vs stderr) and the timestamp when it was received.
 *
 * ## Output Streaming
 * Output is emitted via Kotlin Flow as soon as it's available:
 * ```kotlin
 * repository.observeOutput(sessionId).collect { output ->
 *     appendToDisplay(output.text, output.stream)
 * }
 * ```
 *
 * ## ANSI Escape Sequences
 * The `text` field may contain ANSI escape sequences for:
 * - Colors and text formatting (bold, italic, underline)
 * - Cursor positioning
 * - Screen clearing
 *
 * These should be parsed and handled by the terminal emulator or UI layer.
 *
 * @property text The text content of this output chunk (may contain ANSI sequences)
 * @property stream Whether this is standard output (STDOUT) or standard error (STDERR)
 * @property timestamp Unix timestamp in milliseconds when this output was received
 *
 * @see StreamType
 */
data class TerminalOutput(
    val text: String,
    val stream: StreamType,
    val timestamp: Long,
)
