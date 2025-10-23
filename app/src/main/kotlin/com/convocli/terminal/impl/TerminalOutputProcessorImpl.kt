package com.convocli.terminal.impl

import com.convocli.data.model.OutputChunk
import com.convocli.data.model.StreamType
import com.convocli.terminal.TerminalOutputProcessor
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TerminalOutputProcessor with buffering and binary detection.
 *
 * Features:
 * - Chunks large output into 4KB pieces for efficient processing
 * - Buffers chunks for 60fps UI updates (prevents lag)
 * - Detects binary output and creates placeholder messages
 * - Trims very long output to prevent memory issues
 */
@Singleton
class TerminalOutputProcessorImpl @Inject constructor() : TerminalOutputProcessor {

    private val outputBuffer = mutableListOf<OutputChunk>()
    private var isBuffering = false

    companion object {
        const val MAX_CHUNK_SIZE = 4096  // 4KB per chunk
        const val MAX_OUTPUT_LINES = 10_000
    }

    override suspend fun processOutput(
        rawOutput: String,
        streamType: StreamType,
        blockId: String
    ): List<OutputChunk> {
        // Handle binary output
        if (isBinaryOutput(rawOutput)) {
            return listOf(
                OutputChunk(
                    blockId = blockId,
                    data = createBinaryPlaceholder(rawOutput.length),
                    streamType = streamType
                )
            )
        }

        // Trim output if too long
        val trimmedOutput = trimOutput(rawOutput)

        // Chunk large output
        val chunks = mutableListOf<OutputChunk>()
        var offset = 0

        while (offset < trimmedOutput.length) {
            val end = minOf(offset + MAX_CHUNK_SIZE, trimmedOutput.length)
            val chunkData = trimmedOutput.substring(offset, end)

            chunks.add(
                OutputChunk(
                    blockId = blockId,
                    data = chunkData,
                    streamType = streamType
                )
            )

            offset = end
        }

        // If buffering, accumulate chunks instead of returning
        if (isBuffering) {
            outputBuffer.addAll(chunks)
            return emptyList()
        }

        return chunks
    }

    override fun startBuffering() {
        isBuffering = true
        outputBuffer.clear()
    }

    override fun flushBuffer(): List<OutputChunk> {
        val flushed = outputBuffer.toList()
        outputBuffer.clear()
        return flushed
    }

    override fun stopBuffering() {
        isBuffering = false
        outputBuffer.clear()
    }

    override fun isBinaryOutput(text: String): Boolean {
        if (text.isEmpty()) return false

        // Count non-printable characters
        // Exclude common control characters (newline, tab, carriage return, backspace)
        val nonPrintableCount = text.count { char ->
            char.code < 32 && char !in listOf('\n', '\r', '\t', '\b')
        }

        // If >10% of characters are non-printable, consider it binary
        return nonPrintableCount > text.length * 0.1
    }

    override fun createBinaryPlaceholder(sizeBytes: Int): String {
        val kb = sizeBytes / 1024
        return if (kb > 0) {
            "[Binary output - ${kb}KB]"
        } else {
            "[Binary output - ${sizeBytes} bytes]"
        }
    }

    /**
     * Trims output to prevent memory issues with very long output.
     *
     * If output exceeds MAX_OUTPUT_LINES, keeps first 5000 and last 5000 lines
     * with a truncation message in between.
     */
    private fun trimOutput(output: String): String {
        val lines = output.lines()
        return if (lines.size > MAX_OUTPUT_LINES) {
            val header = lines.take(5000).joinToString("\n")
            val footer = lines.takeLast(5000).joinToString("\n")
            "$header\n\n[... ${lines.size - MAX_OUTPUT_LINES} lines truncated ...]\n\n$footer"
        } else {
            output
        }
    }
}
