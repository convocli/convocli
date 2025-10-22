package com.convocli.bootstrap

import com.convocli.data.model.DownloadProgress
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Bootstrap Downloader Interface
 *
 * Handles downloading bootstrap archives from Termux repository with
 * progress tracking, resume capability, and integrity verification.
 *
 * **Feature**: 003 - Termux Bootstrap Installation
 * **Created**: 2025-10-22
 * **Pattern**: Flow-based progress tracking with suspend functions
 *
 * ## Responsibilities
 * - Download bootstrap archives from Termux repository
 * - Track download progress (bytes, speed, ETA)
 * - Resume interrupted downloads (if supported)
 * - Verify download integrity via checksums
 * - Handle network errors and retries
 *
 * ## Usage Example
 * ```kotlin
 * val architecture = "aarch64"
 * val destination = File(context.filesDir, "bootstrap.tar.xz")
 *
 * // Download with progress tracking
 * downloader.download(architecture, destination)
 *     .collect { progress ->
 *         println("Downloaded: ${progress.bytesDownloaded} / ${progress.totalBytes}")
 *         println("Progress: ${progress.progressPercentage}%")
 *         println("Speed: ${progress.speedBytesPerSecond} B/s")
 *     }
 *
 * // Verify checksum
 * val isValid = downloader.verifyChecksum(destination, expectedChecksum)
 * if (!isValid) {
 *     println("Download corrupted, retrying...")
 * }
 * ```
 *
 * ## Implementation Notes
 * - HTTP Client: Ktor Client Android
 * - Threading: All operations run on Dispatchers.IO
 * - Cancellation: Download can be cancelled via job cancellation
 * - Retry: Automatic retry with exponential backoff (up to 3 attempts)
 *
 * @see BootstrapManager for orchestration
 * @see DownloadProgress for progress details
 */
interface BootstrapDownloader {

    /**
     * Download bootstrap archive for device architecture.
     *
     * Downloads the bootstrap tar.xz archive (~70MB) from Termux repository
     * to the specified destination file.
     *
     * **Progress Tracking**:
     * - Emits DownloadProgress updates every ~100ms or every 100KB
     * - Includes bytes downloaded, total size, speed, and ETA
     * - Final emit when download completes (bytesDownloaded == totalBytes)
     *
     * **Network Behavior**:
     * - Uses HTTP GET request
     * - Follows redirects automatically
     * - 5-minute timeout (configurable)
     * - Automatic retry on network failure (up to 3 attempts)
     * - Exponential backoff between retries (1s, 2s, 4s)
     *
     * **File Handling**:
     * - Creates destination file if not exists
     * - Overwrites existing file (no resume in this method)
     * - Uses buffered streams for efficiency
     *
     * **Cancellation**:
     * - Cancellable via job cancellation
     * - Partial file deleted on cancellation
     * - Flow completes immediately on cancel
     *
     * **Error Handling**:
     * - NetworkError: Connection failed, timeout, etc.
     * - InsufficientStorageError: Not enough disk space
     * - IOError: File system errors
     * - Flow throws exception on unrecoverable errors
     *
     * @param architecture Device architecture (e.g., "aarch64", "arm", "x86_64", "i686")
     * @param destination Target file for download
     *
     * @return Flow<DownloadProgress> that emits progress updates
     *         Flow completes when download finishes successfully
     *
     * @throws IllegalArgumentException if architecture is unsupported
     * @throws IOException if file system errors occur
     * @throws NetworkException if network errors occur (after retries)
     *
     * @see resumeDownload for resuming interrupted downloads
     */
    fun download(
        architecture: String,
        destination: File
    ): Flow<DownloadProgress>

    /**
     * Resume interrupted download from last known position.
     *
     * Continues download using HTTP range requests (if server supports).
     * If range requests not supported, falls back to full download.
     *
     * **Resume Behavior**:
     * - Checks server support for range requests (Accept-Ranges header)
     * - Validates partial file size matches bytesAlreadyDownloaded
     * - Appends new data to existing file
     * - Falls back to full download if resume fails
     *
     * **Use Cases**:
     * - User's network connection dropped mid-download
     * - App was killed by system during download
     * - Download timeout occurred
     *
     * **Validation**:
     * - Verifies partial file exists
     * - Checks partial file size == bytesAlreadyDownloaded
     * - Deletes corrupted partial file and restarts if mismatch
     *
     * @param architecture Device architecture
     * @param destination Target file (must exist with partial data)
     * @param bytesAlreadyDownloaded Size of partial download in bytes
     *
     * @return Flow<DownloadProgress> starting from bytesAlreadyDownloaded
     *
     * @throws IllegalArgumentException if bytesAlreadyDownloaded > file size
     * @throws FileNotFoundException if destination does not exist
     *
     * @see download for initial download
     */
    suspend fun resumeDownload(
        architecture: String,
        destination: File,
        bytesAlreadyDownloaded: Long
    ): Flow<DownloadProgress>

    /**
     * Verify downloaded archive integrity via checksum.
     *
     * Computes SHA-256 checksum of downloaded file and compares with
     * expected checksum from Termux repository.
     *
     * **Algorithm**: SHA-256 (industry standard)
     *
     * **Performance**:
     * - ~1-2 seconds for 70MB file
     * - Runs on Dispatchers.IO (does not block UI)
     *
     * **Use Cases**:
     * - Verify download completed successfully
     * - Detect corrupted downloads
     * - Ensure security (prevent tampered archives)
     *
     * **Checksum Format**:
     * - Lowercase hex string (64 characters)
     * - Example: "a3f2e1..." (64 hex digits)
     *
     * @param file Downloaded archive file
     * @param expectedChecksum Expected SHA-256 checksum (hex string)
     *
     * @return true if checksum matches, false if mismatch
     *
     * @throws FileNotFoundException if file does not exist
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if expectedChecksum format invalid
     */
    suspend fun verifyChecksum(file: File, expectedChecksum: String): Boolean

    /**
     * Get download URL for device architecture.
     *
     * Constructs the full download URL for the bootstrap archive.
     *
     * **URL Pattern** (example, depends on research findings):
     * ```
     * https://github.com/termux/termux-packages/releases/download/
     *     bootstrap-{version}/bootstrap-{architecture}.tar.xz
     * ```
     *
     * **Architecture Mapping**:
     * - aarch64 → bootstrap-aarch64.tar.xz
     * - arm → bootstrap-arm.tar.xz
     * - x86_64 → bootstrap-x86_64.tar.xz
     * - i686 → bootstrap-i686.tar.xz
     *
     * @param architecture Device architecture
     *
     * @return Full HTTPS URL for bootstrap download
     *
     * @throws IllegalArgumentException if architecture is unsupported
     */
    fun getDownloadUrl(architecture: String): String

    /**
     * Get expected checksum for architecture.
     *
     * Retrieves the expected SHA-256 checksum for bootstrap archive.
     * Checksums are fetched from Termux repository alongside archives.
     *
     * **Checksum Source** (example, depends on research findings):
     * ```
     * https://github.com/termux/termux-packages/releases/download/
     *     bootstrap-{version}/bootstrap-{architecture}.tar.xz.sha256
     * ```
     *
     * **Caching**:
     * - Checksums cached in memory after first fetch
     * - Cache cleared on version change
     *
     * @param architecture Device architecture
     *
     * @return SHA-256 checksum as hex string (64 characters)
     *
     * @throws NetworkException if checksum cannot be fetched
     * @throws IllegalArgumentException if architecture is unsupported
     */
    suspend fun getExpectedChecksum(architecture: String): String

    /**
     * Check if download resume is supported.
     *
     * Tests if Termux repository supports HTTP range requests by
     * sending HTTP HEAD request and checking Accept-Ranges header.
     *
     * **Performance**: Fast (~100-200ms), one HTTP HEAD request
     *
     * **Caching**: Result cached for app session
     *
     * @return true if range requests supported, false otherwise
     */
    suspend fun isResumeSupported(): Boolean

    /**
     * Get available disk space in bytes.
     *
     * Checks available storage space in app's files directory.
     * Used to verify sufficient space before starting download.
     *
     * **Minimum Required**: 200MB (70MB download + 150MB extraction buffer)
     *
     * @return Available bytes in app files directory
     */
    fun getAvailableDiskSpace(): Long
}
