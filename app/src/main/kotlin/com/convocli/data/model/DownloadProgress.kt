package com.convocli.data.model

/**
 * Download-specific progress information.
 *
 * Emitted from BootstrapDownloader during active downloads.
 */
data class DownloadProgress(
    /**
     * Bytes downloaded so far
     */
    val bytesDownloaded: Long,

    /**
     * Total bytes to download
     */
    val totalBytes: Long,

    /**
     * Download speed in bytes per second
     * Null if cannot be calculated yet
     */
    val speedBytesPerSecond: Long? = null,

    /**
     * Estimated time remaining in milliseconds
     * Null if cannot be estimated
     */
    val estimatedTimeRemaining: Long? = null
) {
    /**
     * Download progress percentage (0-100)
     */
    val progressPercentage: Int
        get() = if (totalBytes > 0) {
            ((bytesDownloaded * 100) / totalBytes).toInt()
        } else {
            0
        }

    /**
     * True if download is complete
     */
    val isComplete: Boolean
        get() = bytesDownloaded >= totalBytes && totalBytes > 0
}
