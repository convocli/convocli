package com.convocli.terminal.model

/**
 * Distinguishes between standard output and standard error streams.
 *
 * Terminal processes have two primary output streams:
 * - **STDOUT**: Normal command output
 * - **STDERR**: Error messages and diagnostics
 *
 * This enum allows the UI to render these streams differently
 * (e.g., errors in red, normal output in white).
 *
 * ## Usage Example
 * ```kotlin
 * when (output.stream) {
 *     StreamType.STDOUT -> displayNormal(output.text)
 *     StreamType.STDERR -> displayError(output.text)
 * }
 * ```
 */
enum class StreamType {
    /**
     * Standard output stream.
     * Contains normal command output, results, and informational messages.
     */
    STDOUT,

    /**
     * Standard error stream.
     * Contains error messages, warnings, and diagnostic information.
     */
    STDERR,
}
