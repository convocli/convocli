package com.convocli.data.model

/**
 * Detailed progress information during bootstrap installation.
 *
 * Emitted as a Flow from BootstrapManager during installation.
 * Contains phase-specific progress metrics and error information.
 */
data class InstallationProgress(
    /**
     * Current installation phase
     */
    val phase: InstallationPhase,

    /**
     * Bytes downloaded so far
     * Only relevant during DOWNLOADING phase
     */
    val bytesDownloaded: Long = 0,

    /**
     * Total bytes to download
     * Only relevant during DOWNLOADING phase
     */
    val totalBytes: Long = 0,

    /**
     * Files extracted so far
     * Only relevant during EXTRACTING phase
     */
    val filesExtracted: Int = 0,

    /**
     * Total files to extract
     * Only relevant during EXTRACTING phase
     */
    val totalFiles: Int = 0,

    /**
     * Estimated time remaining in milliseconds
     * Null if cannot be estimated
     */
    val estimatedTimeRemaining: Long? = null,

    /**
     * Current phase message (user-facing)
     * Example: "Downloading bootstrap...", "Extracting files...", "Setting permissions..."
     */
    val message: String,

    /**
     * Error information if phase failed
     * Null if no error
     */
    val error: BootstrapError? = null
) {
    /**
     * Overall progress percentage (0-100)
     * Calculated based on current phase and phase-specific progress
     */
    val progressPercentage: Int
        get() = when (phase) {
            InstallationPhase.DETECTING -> 5
            InstallationPhase.DOWNLOADING -> {
                if (totalBytes > 0) {
                    5 + ((bytesDownloaded * 50) / totalBytes).toInt()
                } else {
                    5
                }
            }
            InstallationPhase.EXTRACTING -> {
                if (totalFiles > 0) {
                    55 + ((filesExtracted * 30) / totalFiles).toInt()
                } else {
                    55
                }
            }
            InstallationPhase.CONFIGURING_PERMISSIONS -> 85
            InstallationPhase.CONFIGURING_ENVIRONMENT -> 90
            InstallationPhase.VERIFYING -> 95
            InstallationPhase.COMPLETE -> 100
        }

    /**
     * True if this phase represents completion (success or failure)
     */
    val isTerminal: Boolean
        get() = phase == InstallationPhase.COMPLETE || error != null
}
