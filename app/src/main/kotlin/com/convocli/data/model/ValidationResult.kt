package com.convocli.data.model

/**
 * Result of bootstrap validation.
 *
 * Returned from BootstrapValidator after testing the installation.
 */
sealed class ValidationResult {
    /**
     * Validation succeeded - bootstrap is functional
     */
    data class Success(
        /**
         * Bootstrap version detected
         */
        val version: String,

        /**
         * Bash path
         */
        val bashPath: String,

        /**
         * Core utilities validated
         */
        val validatedUtilities: List<String>
    ) : ValidationResult()

    /**
     * Validation failed - bootstrap is not functional
     */
    data class Failure(
        /**
         * Reason for validation failure
         */
        val reason: String,

        /**
         * Failed command (if applicable)
         */
        val failedCommand: String? = null,

        /**
         * Technical details for debugging
         */
        val technicalDetails: String? = null
    ) : ValidationResult()
}
