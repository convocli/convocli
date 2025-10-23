package com.convocli.data.model

/**
 * Extraction-specific progress information.
 *
 * Emitted from BootstrapExtractor during archive extraction.
 */
data class ExtractionProgress(
    /**
     * Files extracted so far
     */
    val filesExtracted: Int,

    /**
     * Total files to extract
     * May be approximate or unknown initially
     */
    val totalFiles: Int,

    /**
     * Bytes extracted so far
     */
    val bytesExtracted: Long = 0,

    /**
     * Total bytes to extract
     * May be approximate
     */
    val totalBytes: Long = 0,

    /**
     * Path of file currently being extracted
     * Null if between files
     */
    val currentFile: String? = null
) {
    /**
     * Extraction progress percentage (0-100)
     * Based on file count if available, otherwise byte count
     */
    val progressPercentage: Int
        get() = when {
            totalFiles > 0 -> ((filesExtracted * 100) / totalFiles).coerceIn(0, 100)
            totalBytes > 0 -> ((bytesExtracted * 100) / totalBytes).toInt().coerceIn(0, 100)
            else -> 0
        }

    /**
     * True if extraction is complete
     */
    val isComplete: Boolean
        get() = filesExtracted >= totalFiles && totalFiles > 0
}
