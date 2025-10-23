package com.convocli.data.model

/**
 * Represents a fragment of terminal output as it streams in real-time.
 *
 * OutputChunk is ephemeral (not persisted to database) and used for streaming only.
 * Chunks are concatenated into CommandBlock.output.
 */
data class OutputChunk(
    /** Associated command block ID */
    val blockId: String,

    /** Raw output text with ANSI codes preserved */
    val data: String,

    /** Source stream (stdout or stderr) */
    val streamType: StreamType,

    /** Unix timestamp (ms) when generated */
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Distinguishes between standard output and error streams.
 */
enum class StreamType {
    /** Standard output stream */
    STDOUT,

    /** Standard error stream (displayed in red) */
    STDERR
}
