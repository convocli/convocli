package com.convocli.data.model

/**
 * Result of prompt detection analysis.
 *
 * Used by PromptDetector to indicate whether a shell prompt was found
 * and how it was detected (pattern match vs timeout).
 */
data class PromptDetectionResult(
    /** True if prompt was detected */
    val promptDetected: Boolean,

    /** Regex pattern that matched (null if timeout) */
    val matchedPattern: String? = null,

    /** True if detection via 2s timeout fallback */
    val timeoutTriggered: Boolean = false
)
