package com.convocli.terminal

import com.convocli.data.model.OutputChunk
import com.convocli.data.model.StreamType

/**
 * Contract for processing raw terminal output into structured chunks.
 *
 * Responsibilities:
 * - Buffer output for performance (batch updates at 60fps)
 * - Route stdout/stderr to appropriate streams
 * - Detect and handle binary output
 * - Implement backpressure handling
 * - Interleave stdout/stderr chronologically
 */
interface TerminalOutputProcessor {

    /**
     * Processes raw terminal output into structured chunks.
     *
     * Output is:
     * - Chunked into manageable sizes (max 4KB per chunk)
     * - Tagged with stream type (stdout/stderr)
     * - Timestamped for chronological interleaving
     * - Associated with command block ID for routing
     *
     * Binary output is detected and replaced with placeholder text.
     *
     * @param rawOutput Raw output from terminal (with ANSI codes)
     * @param streamType The source stream (STDOUT or STDERR)
     * @param blockId The associated command block ID
     * @return List of output chunks ready for emission
     */
    suspend fun processOutput(
        rawOutput: String,
        streamType: StreamType,
        blockId: String
    ): List<OutputChunk>

    /**
     * Starts output buffering for performance optimization.
     *
     * Collects output chunks and batches them for UI updates.
     * Flushes buffer at 60fps (every ~16ms) to prevent UI lag.
     *
     * Call this when starting command execution.
     */
    fun startBuffering()

    /**
     * Flushes the output buffer and returns collected chunks.
     *
     * Should be called:
     * - Periodically (every 16ms) for 60fps updates
     * - When command completes (final flush)
     *
     * @return List of buffered output chunks
     */
    fun flushBuffer(): List<OutputChunk>

    /**
     * Stops buffering and clears the buffer.
     *
     * Call this when command execution completes or is canceled.
     */
    fun stopBuffering()

    /**
     * Detects if output contains binary (non-printable) characters.
     *
     * Binary output (e.g., from `cat image.png`) should not be displayed
     * directly as it corrupts the UI.
     *
     * @param text Text to analyze
     * @return True if binary content detected
     */
    fun isBinaryOutput(text: String): Boolean

    /**
     * Creates a placeholder message for binary output.
     *
     * Example: "[Binary output - 15KB]"
     *
     * @param sizeBytes Size of binary output in bytes
     * @return Placeholder message for display
     */
    fun createBinaryPlaceholder(sizeBytes: Int): String
}
