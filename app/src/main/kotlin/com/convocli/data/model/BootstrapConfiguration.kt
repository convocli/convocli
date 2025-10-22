package com.convocli.data.model

import kotlinx.serialization.Serializable

/**
 * Configuration for bootstrap installation.
 *
 * Persisted to DataStore and loaded on app launch.
 */
@Serializable
data class BootstrapConfiguration(
    /**
     * Base URL for bootstrap downloads
     * Example: "https://github.com/termux/termux-packages/releases/download"
     */
    val baseUrl: String,

    /**
     * Bootstrap version to install
     * Example: "bootstrap-2025.09.21-r1+apt-android-7"
     */
    val version: String,

    /**
     * Timeout for download in milliseconds
     */
    val downloadTimeoutMs: Long = 300_000, // 5 minutes

    /**
     * Timeout for extraction in milliseconds
     */
    val extractionTimeoutMs: Long = 600_000, // 10 minutes

    /**
     * Enable download resume
     */
    val enableDownloadResume: Boolean = true,

    /**
     * Maximum download retry attempts
     */
    val maxRetryAttempts: Int = 3,

    /**
     * Retry backoff delay in milliseconds
     */
    val retryBackoffMs: Long = 5_000 // 5 seconds
)
