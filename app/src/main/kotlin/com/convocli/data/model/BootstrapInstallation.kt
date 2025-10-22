package com.convocli.data.model

import kotlinx.serialization.Serializable

/**
 * Represents the bootstrap installation state and metadata.
 *
 * This entity is persisted to DataStore to track installation status
 * across app launches and device reboots.
 */
@Serializable
data class BootstrapInstallation(
    /**
     * Current installation status
     */
    val status: InstallationStatus,

    /**
     * Bootstrap version installed (e.g., "2025.09.21")
     * Null if not installed
     */
    val version: String? = null,

    /**
     * Absolute path to bootstrap installation directory
     * Example: "/data/data/com.convocli/files/usr"
     * Null if not installed
     */
    val installedPath: String? = null,

    /**
     * Timestamp when installation completed (milliseconds since epoch)
     * Null if not installed
     */
    val installationDate: Long? = null,

    /**
     * Timestamp of last successful validation (milliseconds since epoch)
     * Null if never validated
     */
    val lastValidated: Long? = null,

    /**
     * Device architecture variant (e.g., "aarch64", "arm", "x86_64")
     * Null if not detected yet
     */
    val architecture: String? = null,

    /**
     * Size of installed bootstrap in bytes
     * Null if not installed
     */
    val installedSizeBytes: Long? = null
)
