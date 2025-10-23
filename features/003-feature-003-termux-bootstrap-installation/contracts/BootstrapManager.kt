package com.convocli.bootstrap

import com.convocli.data.model.BootstrapInstallation
import com.convocli.data.model.InstallationProgress
import com.convocli.data.model.ValidationResult
import kotlinx.coroutines.flow.Flow

/**
 * Bootstrap Manager Interface
 *
 * Orchestrates the complete bootstrap installation lifecycle including
 * detection, download, extraction, configuration, and validation.
 *
 * **Feature**: 003 - Termux Bootstrap Installation
 * **Created**: 2025-10-22
 * **Pattern**: Repository pattern with Flow-based progress tracking
 *
 * ## Responsibilities
 * - Check if bootstrap is installed
 * - Orchestrate installation process across multiple services
 * - Emit real-time progress updates via Flow
 * - Handle cancellation and cleanup
 * - Validate bootstrap installation integrity
 * - Manage bootstrap lifecycle (install, validate, delete)
 *
 * ## Usage Example
 * ```kotlin
 * // Check installation status
 * val isInstalled = bootstrapManager.isInstalled()
 *
 * // Install with progress tracking
 * bootstrapManager.install()
 *     .collect { progress ->
 *         when (progress.phase) {
 *             InstallationPhase.DOWNLOADING -> updateDownloadUI(progress)
 *             InstallationPhase.EXTRACTING -> updateExtractionUI(progress)
 *             InstallationPhase.COMPLETE -> showSuccess()
 *         }
 *     }
 *
 * // Cancel installation
 * bootstrapManager.cancelInstallation()
 * ```
 *
 * ## Implementation Notes
 * - Thread-safe: All methods can be called from any coroutine context
 * - Cancellation: Installation can be cancelled via job cancellation or explicit cancelInstallation()
 * - State persistence: Installation status persisted to DataStore
 * - Error handling: Errors emitted via InstallationProgress.error field
 *
 * @see BootstrapDownloader for download implementation
 * @see BootstrapExtractor for extraction implementation
 * @see BootstrapValidator for validation implementation
 */
interface BootstrapManager {

    /**
     * Check if bootstrap is currently installed and valid.
     *
     * Performs a quick validation check by verifying:
     * - Bootstrap directory exists
     * - Bash executable exists
     * - Installation status in DataStore is INSTALLED
     *
     * **Performance**: Fast check (~10-50ms), does not execute commands
     *
     * @return true if bootstrap is installed and appears valid, false otherwise
     *
     * @throws None - returns false on any validation failure
     */
    suspend fun isInstalled(): Boolean

    /**
     * Get current installation status.
     *
     * Returns the persisted installation state from DataStore.
     * Contains metadata about the installation including version, path,
     * and last validation timestamp.
     *
     * **Performance**: Fast (~5ms), reads from DataStore cache
     *
     * @return Flow of BootstrapInstallation updates
     *         Emits immediately with current state, then on any state changes
     *
     * @see BootstrapInstallation for status details
     */
    fun getInstallationStatus(): Flow<BootstrapInstallation>

    /**
     * Install bootstrap with real-time progress tracking.
     *
     * Orchestrates the complete installation process:
     * 1. DETECTING: Check architecture and existing installation
     * 2. DOWNLOADING: Download bootstrap archive (~70MB)
     * 3. EXTRACTING: Extract tar.xz to app files directory
     * 4. CONFIGURING_PERMISSIONS: Set execute permissions on binaries
     * 5. CONFIGURING_ENVIRONMENT: Set PREFIX, PATH, HOME, etc.
     * 6. VERIFYING: Test bash execution
     * 7. COMPLETE: Persist installation status
     *
     * **Cancellation**:
     * - Can be cancelled via job cancellation or cancelInstallation()
     * - Partial downloads/extractions are cleaned up automatically
     * - State reset to NOT_INSTALLED after cancellation
     *
     * **Error Handling**:
     * - Errors emitted via InstallationProgress.error field
     * - Flow does not throw exceptions, always completes normally
     * - Caller should check progress.error for failures
     *
     * **Idempotency**:
     * - If already installed, validates and returns success immediately
     * - Safe to call multiple times
     *
     * **Storage Requirements**:
     * - Minimum 200MB free space required
     * - Checks available space before starting
     * - Emits InsufficientStorageError if not enough space
     *
     * @return Flow<InstallationProgress> that emits progress updates
     *         Flow completes when installation finishes (success or failure)
     *
     * @throws IllegalStateException if installation already in progress
     *
     * @see InstallationProgress for progress details
     * @see BootstrapError for possible error types
     */
    fun install(): Flow<InstallationProgress>

    /**
     * Cancel ongoing installation.
     *
     * Stops the current installation process and cleans up partial files.
     * Safe to call even if no installation is in progress.
     *
     * **Cleanup Actions**:
     * - Cancels download job
     * - Cancels extraction job
     * - Deletes partial download file
     * - Deletes partial extraction directory
     * - Resets installation status to NOT_INSTALLED
     *
     * **Timing**:
     * - Cancellation is cooperative (not immediate)
     * - May take up to 2 seconds for cancellation to complete
     * - Current operation finishes before cleanup begins
     *
     * **Thread Safety**:
     * - Safe to call from any thread
     * - Safe to call multiple times
     *
     * @throws None - safe operation, never throws
     */
    suspend fun cancelInstallation()

    /**
     * Validate existing bootstrap installation.
     *
     * Performs comprehensive validation by:
     * - Checking bash executable exists and has execute permission
     * - Executing `bash --version` to verify functionality
     * - Testing core utilities (ls, pwd, echo, cat, grep)
     * - Verifying directory structure
     *
     * **Performance**: Slow (~500-1000ms), executes multiple commands
     *
     * **Use Cases**:
     * - Verify installation integrity after app update
     * - Troubleshoot user-reported issues
     * - Validate installation before executing commands
     *
     * @return ValidationResult.Success if bootstrap is functional,
     *         ValidationResult.Failure with reason if not functional
     *
     * @throws None - returns ValidationResult.Failure on exceptions
     *
     * @see ValidationResult for result details
     */
    suspend fun validateInstallation(): ValidationResult

    /**
     * Delete bootstrap installation completely.
     *
     * Removes all bootstrap files and resets installation status.
     * Useful for troubleshooting or freeing storage space.
     *
     * **Deletion Actions**:
     * - Deletes entire bootstrap directory (~150MB)
     * - Clears installation status from DataStore
     * - Removes any cached metadata
     *
     * **Safety**:
     * - Safe to call even if not installed
     * - Does not affect user data or app settings
     * - User can reinstall at any time
     *
     * **Performance**: Slow (~5-10 seconds), deletes many files
     *
     * @throws IOException if deletion fails (rare, usually succeeds)
     */
    suspend fun deleteInstallation()

    /**
     * Get device architecture variant for bootstrap downloads.
     *
     * Maps Android's Build.SUPPORTED_ABIS to Termux architecture names:
     * - arm64-v8a → aarch64
     * - armeabi-v7a → arm
     * - x86_64 → x86_64
     * - x86 → i686
     *
     * **Performance**: Instant (~1ms), reads system property
     *
     * @return Architecture string (e.g., "aarch64", "arm", "x86_64", "i686")
     *
     * @throws IllegalStateException if device architecture is unsupported
     */
    fun getDeviceArchitecture(): String

    /**
     * Get bootstrap installation path.
     *
     * Returns the absolute path where bootstrap is installed or will be installed.
     * Typically: `/data/data/com.convocli/files/usr/`
     *
     * **Performance**: Instant (~1ms)
     *
     * @return Absolute path to bootstrap directory
     */
    fun getBootstrapPath(): String
}
