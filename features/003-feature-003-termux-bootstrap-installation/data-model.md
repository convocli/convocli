# Data Model: Termux Bootstrap Installation

**Feature ID**: 003
**Feature**: Termux Bootstrap Installation
**Created**: 2025-10-22
**Status**: Design Phase
**Purpose**: Define all data entities used in bootstrap installation feature

---

## Overview

This document defines the data model for Feature 003 (Termux Bootstrap Installation). All entities follow Kotlin conventions and are designed for immutability and type safety.

**Design Principles**:
- Immutable data classes (val properties)
- Sealed classes for type-safe state machines
- Non-nullable types where possible
- Clear naming conventions
- Comprehensive KDoc documentation

---

## Core Entities

### BootstrapInstallation

**Purpose**: Represents the current state of bootstrap installation on the device

**Location**: `app/src/main/kotlin/com/convocli/data/model/BootstrapInstallation.kt`

```kotlin
/**
 * Represents the bootstrap installation state and metadata.
 *
 * This entity is persisted to DataStore to track installation status
 * across app launches and device reboots.
 */
data class BootstrapInstallation(
    /**
     * Current installation status
     */
    val status: InstallationStatus,

    /**
     * Bootstrap version installed (e.g., "2024.03.01")
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
```

**Serialization**: Kotlinx Serialization for DataStore persistence

---

### InstallationStatus (Enum)

**Purpose**: Represents the high-level status of bootstrap installation

**Location**: `app/src/main/kotlin/com/convocli/data/model/InstallationStatus.kt`

```kotlin
/**
 * High-level installation status.
 *
 * Used for simple status checks and UI display.
 */
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
```

---

### InstallationProgress

**Purpose**: Detailed progress information for active installation

**Location**: `app/src/main/kotlin/com/convocli/data/model/InstallationProgress.kt`

```kotlin
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
```

---

### InstallationPhase (Enum)

**Purpose**: Detailed phase breakdown for installation process

**Location**: `app/src/main/kotlin/com/convocli/data/model/InstallationPhase.kt`

```kotlin
/**
 * Detailed installation phase.
 *
 * Represents the specific step currently being executed during installation.
 * Used for detailed progress tracking and phase-specific UI updates.
 */
enum class InstallationPhase {
    /**
     * Detecting device architecture and checking if already installed
     */
    DETECTING,

    /**
     * Downloading bootstrap archive from Termux repository
     */
    DOWNLOADING,

    /**
     * Extracting tar.xz archive to filesystem
     */
    EXTRACTING,

    /**
     * Setting execute permissions on bash and core utilities
     */
    CONFIGURING_PERMISSIONS,

    /**
     * Configuring environment variables (PREFIX, PATH, HOME, etc.)
     */
    CONFIGURING_ENVIRONMENT,

    /**
     * Verifying installation by testing bash execution
     */
    VERIFYING,

    /**
     * Installation completed successfully
     */
    COMPLETE
}
```

---

### BootstrapError (Sealed Class)

**Purpose**: Type-safe error representation with specific error types

**Location**: `app/src/main/kotlin/com/convocli/data/model/BootstrapError.kt`

```kotlin
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
```

---

### DownloadProgress

**Purpose**: Progress information specific to download phase

**Location**: `app/src/main/kotlin/com/convocli/data/model/DownloadProgress.kt`

```kotlin
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
```

---

### ExtractionProgress

**Purpose**: Progress information specific to extraction phase

**Location**: `app/src/main/kotlin/com/convocli/data/model/ExtractionProgress.kt`

```kotlin
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
```

---

### ValidationResult

**Purpose**: Result of bootstrap installation validation

**Location**: `app/src/main/kotlin/com/convocli/data/model/ValidationResult.kt`

```kotlin
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
```

---

## Repository Entities

### BootstrapConfiguration

**Purpose**: Configuration parameters for bootstrap installation

**Location**: `app/src/main/kotlin/com/convocli/data/model/BootstrapConfiguration.kt`

```kotlin
/**
 * Configuration for bootstrap installation.
 *
 * Persisted to DataStore and loaded on app launch.
 */
data class BootstrapConfiguration(
    /**
     * Base URL for bootstrap downloads
     * Example: "https://github.com/termux/termux-packages/releases/download"
     */
    val baseUrl: String,

    /**
     * Bootstrap version to install
     * Example: "2024.03.01"
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
```

---

## UI State Entities

### InstallationUiState

**Purpose**: UI-specific state for installation screen

**Location**: `app/src/main/kotlin/com/convocli/ui/viewmodels/InstallationUiState.kt`

```kotlin
/**
 * UI state for installation screen.
 *
 * Derived from InstallationProgress with UI-specific properties.
 */
data class InstallationUiState(
    /**
     * Current installation progress
     */
    val progress: InstallationProgress,

    /**
     * True if installation is in progress (can be cancelled)
     */
    val isInstalling: Boolean,

    /**
     * True if cancel button should be enabled
     */
    val canCancel: Boolean,

    /**
     * True if retry button should be shown
     */
    val canRetry: Boolean,

    /**
     * User-facing status message
     */
    val statusMessage: String,

    /**
     * Error message to display
     * Null if no error
     */
    val errorMessage: String? = null
) {
    companion object {
        /**
         * Initial state before installation starts
         */
        fun initial() = InstallationUiState(
            progress = InstallationProgress(
                phase = InstallationPhase.DETECTING,
                message = "Initializing..."
            ),
            isInstalling = false,
            canCancel = false,
            canRetry = false,
            statusMessage = "Ready to install"
        )
    }
}
```

---

## Database Entities (Future)

### BootstrapInstallationHistory (Room Entity)

**Purpose**: Historical record of installation attempts

**Note**: Optional - only if we want to track installation history

**Location**: `app/src/main/kotlin/com/convocli/data/db/entity/BootstrapInstallationHistoryEntity.kt`

```kotlin
/**
 * Historical record of bootstrap installation attempts.
 *
 * Stored in Room database for analytics and troubleshooting.
 * OPTIONAL - not required for MVP.
 */
@Entity(tableName = "bootstrap_installation_history")
data class BootstrapInstallationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /**
     * Timestamp of installation attempt
     */
    val timestamp: Long,

    /**
     * Bootstrap version attempted
     */
    val version: String,

    /**
     * Device architecture
     */
    val architecture: String,

    /**
     * Installation result (SUCCESS, FAILURE, CANCELLED)
     */
    val result: String,

    /**
     * Error message if failed
     */
    val errorMessage: String? = null,

    /**
     * Installation duration in milliseconds
     */
    val durationMs: Long
)
```

---

## Utility Extensions

### Extension Functions

**Location**: `app/src/main/kotlin/com/convocli/data/model/Extensions.kt`

```kotlin
/**
 * Convert bytes to human-readable format (KB, MB, GB)
 */
fun Long.toHumanReadableSize(): String {
    val kb = 1024L
    val mb = kb * 1024
    val gb = mb * 1024

    return when {
        this >= gb -> "%.2f GB".format(this.toDouble() / gb)
        this >= mb -> "%.2f MB".format(this.toDouble() / mb)
        this >= kb -> "%.2f KB".format(this.toDouble() / kb)
        else -> "$this bytes"
    }
}

/**
 * Convert milliseconds to human-readable duration (e.g., "2m 30s")
 */
fun Long.toHumanReadableDuration(): String {
    val seconds = this / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60

    return when {
        minutes > 0 -> "${minutes}m ${remainingSeconds}s"
        else -> "${seconds}s"
    }
}

/**
 * Map device architecture to Termux bootstrap variant
 */
fun String.toBootstrapArchitecture(): String = when (this) {
    "arm64-v8a" -> "aarch64"
    "armeabi-v7a" -> "arm"
    "x86_64" -> "x86_64"
    "x86" -> "i686"
    else -> throw IllegalArgumentException("Unsupported architecture: $this")
}
```

---

## Data Flow Summary

```
User Action (Install)
    ↓
InstallationViewModel
    ↓
BootstrapManager.install()
    ↓
Flow<InstallationProgress>
    ↓
    ├─ InstallationPhase.DETECTING → progress emitted
    ├─ InstallationPhase.DOWNLOADING → DownloadProgress mapped to InstallationProgress
    ├─ InstallationPhase.EXTRACTING → ExtractionProgress mapped to InstallationProgress
    ├─ InstallationPhase.CONFIGURING_PERMISSIONS → progress emitted
    ├─ InstallationPhase.CONFIGURING_ENVIRONMENT → progress emitted
    └─ InstallationPhase.VERIFYING → ValidationResult checked
    ↓
InstallationProgress(phase=COMPLETE)
    ↓
BootstrapInstallation persisted to DataStore
    ↓
UI shows success message
```

---

## Type Safety Benefits

**Sealed Classes**:
- `BootstrapError`: Compile-time exhaustive error handling
- `ValidationResult`: Type-safe success/failure result

**Enums**:
- `InstallationStatus`: Finite set of status values
- `InstallationPhase`: Clear phase progression

**Immutable Data Classes**:
- Thread-safe
- Predictable state updates
- Easy to test

**Non-nullable Types**:
- Null safety enforced by compiler
- Fewer runtime null checks

---

## Testing Considerations

**Unit Test Helpers**:
```kotlin
// Fixture factory for testing
object InstallationProgressFixtures {
    fun downloading(bytesDownloaded: Long = 1000, totalBytes: Long = 7000): InstallationProgress {
        return InstallationProgress(
            phase = InstallationPhase.DOWNLOADING,
            bytesDownloaded = bytesDownloaded,
            totalBytes = totalBytes,
            message = "Downloading..."
        )
    }

    fun complete() = InstallationProgress(
        phase = InstallationPhase.COMPLETE,
        message = "Installation complete"
    )

    fun error(error: BootstrapError) = InstallationProgress(
        phase = InstallationPhase.DOWNLOADING,
        message = "Error occurred",
        error = error
    )
}
```

---

## Next Steps

**After Data Model Approval**:

1. ✅ Create contracts/ directory with interface definitions
2. ✅ Implement entity classes in Kotlin
3. ✅ Add Kotlinx Serialization annotations for DataStore
4. ✅ Create unit tests for entity helper methods
5. ✅ Proceed to `/specswarm:tasks` for implementation task generation

---

**Data Model Status**: ✅ COMPLETE - Ready for Contract Definitions
**Created**: 2025-10-22
**Next Phase**: API Contracts (contracts/ directory)

---

*All entities defined in this document will be implemented in Kotlin with full type safety, immutability, and comprehensive KDoc documentation.*
