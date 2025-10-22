package com.convocli.data.model

/**
 * Represents errors that can occur during bootstrap installation.
 *
 * Sealed class for type-safe error handling with specific error types.
 * Each error type includes user-facing message and optional technical details.
 */
sealed class BootstrapError {
    /**
     * User-facing error message
     */
    abstract val message: String

    /**
     * Technical details for logging/debugging
     * Not shown to user
     */
    abstract val technicalDetails: String?

    /**
     * True if error is recoverable (user can retry)
     */
    abstract val isRecoverable: Boolean

    /**
     * Network-related error during download
     */
    data class NetworkError(
        override val message: String = "Network connection failed",
        override val technicalDetails: String? = null
    ) : BootstrapError() {
        override val isRecoverable: Boolean = true
    }

    /**
     * Insufficient storage space for bootstrap
     */
    data class InsufficientStorageError(
        val requiredBytes: Long,
        val availableBytes: Long,
        override val message: String = "Insufficient storage space",
        override val technicalDetails: String? = null
    ) : BootstrapError() {
        override val isRecoverable: Boolean = false
    }

    /**
     * Checksum verification failed (corrupted download)
     */
    data class ChecksumMismatchError(
        val expected: String,
        val actual: String,
        override val message: String = "Downloaded file is corrupted",
        override val technicalDetails: String? = null
    ) : BootstrapError() {
        override val isRecoverable: Boolean = true // Can retry download
    }

    /**
     * Archive extraction failed
     */
    data class ExtractionError(
        override val message: String = "Failed to extract bootstrap files",
        override val technicalDetails: String? = null
    ) : BootstrapError() {
        override val isRecoverable: Boolean = true
    }

    /**
     * Failed to set execute permissions on binaries
     */
    data class PermissionError(
        val filePath: String,
        override val message: String = "Failed to set file permissions",
        override val technicalDetails: String? = null
    ) : BootstrapError() {
        override val isRecoverable: Boolean = false // System limitation
    }

    /**
     * Bootstrap validation failed (bash not working)
     */
    data class ValidationError(
        override val message: String = "Bootstrap installation is invalid",
        override val technicalDetails: String? = null
    ) : BootstrapError() {
        override val isRecoverable: Boolean = true // Can reinstall
    }

    /**
     * Installation was cancelled by user
     */
    data class CancellationError(
        override val message: String = "Installation cancelled",
        override val technicalDetails: String? = null
    ) : BootstrapError() {
        override val isRecoverable: Boolean = true
    }

    /**
     * Unknown/unexpected error
     */
    data class UnknownError(
        override val message: String = "An unexpected error occurred",
        override val technicalDetails: String? = null
    ) : BootstrapError() {
        override val isRecoverable: Boolean = true
    }
}
