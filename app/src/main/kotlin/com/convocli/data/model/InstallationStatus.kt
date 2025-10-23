package com.convocli.data.model

import kotlinx.serialization.Serializable

/**
 * High-level installation status.
 *
 * Used for simple status checks and UI display.
 */
@Serializable
enum class InstallationStatus {
    /**
     * Bootstrap is not installed on this device
     */
    NOT_INSTALLED,

    /**
     * Bootstrap is currently being downloaded
     */
    DOWNLOADING,

    /**
     * Bootstrap archive is being extracted
     */
    EXTRACTING,

    /**
     * File permissions and environment variables are being configured
     */
    CONFIGURING,

    /**
     * Installation is being verified (testing bash execution)
     */
    VERIFYING,

    /**
     * Bootstrap is installed and validated
     */
    INSTALLED,

    /**
     * Installation failed (see error details)
     */
    FAILED,

    /**
     * Installation was cancelled by user
     */
    CANCELLED
}
