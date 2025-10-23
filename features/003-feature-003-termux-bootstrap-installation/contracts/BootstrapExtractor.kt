package com.convocli.bootstrap

import com.convocli.data.model.ExtractionProgress
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Bootstrap Extractor Interface
 *
 * Handles extraction of bootstrap tar.xz archives to the filesystem
 * with progress tracking, symlink preservation, and permission handling.
 *
 * **Feature**: 003 - Termux Bootstrap Installation
 * **Created**: 2025-10-22
 * **Pattern**: Flow-based progress tracking with suspend functions
 *
 * ## Responsibilities
 * - Extract tar.xz archives to filesystem
 * - Track extraction progress (files, bytes)
 * - Preserve Unix symlinks
 * - Set file permissions during extraction
 * - Ensure atomic extraction (all-or-nothing)
 * - Handle extraction errors gracefully
 *
 * ## Usage Example
 * ```kotlin
 * val archiveFile = File(context.filesDir, "bootstrap.tar.xz")
 * val destinationDir = File(context.filesDir, "usr")
 *
 * // Extract with progress tracking
 * extractor.extract(archiveFile, destinationDir)
 *     .collect { progress ->
 *         println("Extracted: ${progress.filesExtracted} / ${progress.totalFiles}")
 *         println("Progress: ${progress.progressPercentage}%")
 *         println("Current file: ${progress.currentFile}")
 *     }
 *
 * // Verify extraction
 * val isValid = extractor.verifyExtraction(destinationDir)
 * if (isValid) {
 *     println("Bootstrap extracted successfully")
 * }
 * ```
 *
 * ## Implementation Notes
 * - Library: Apache Commons Compress 1.24.0
 * - Threading: All operations run on Dispatchers.IO
 * - Cancellation: Extraction can be cancelled via job cancellation
 * - Atomicity: Extracts to temporary directory, then renames
 *
 * @see BootstrapManager for orchestration
 * @see ExtractionProgress for progress details
 */
interface BootstrapExtractor {

    /**
     * Extract bootstrap archive to destination directory.
     *
     * Extracts the tar.xz archive preserving directory structure,
     * symlinks, and file permissions.
     *
     * **Extraction Process**:
     * 1. Validate archive file exists and is readable
     * 2. Create temporary extraction directory
     * 3. Extract all files to temporary directory
     * 4. Set permissions on binaries
     * 5. Rename temporary directory to final destination (atomic)
     *
     * **Progress Tracking**:
     * - Emits ExtractionProgress updates every ~10 files or every ~1MB
     * - Includes files extracted, total files, current file path
     * - Final emit when extraction completes
     *
     * **File Handling**:
     * - Preserves directory structure
     * - Creates parent directories as needed
     * - Preserves Unix symlinks (when supported)
     * - Sets execute permissions on binaries
     *
     * **Symlink Handling**:
     * - Detects symlink entries in tar archive
     * - Preserves symlinks on Android filesystem
     * - Falls back to copying target file if symlink creation fails
     *
     * **Atomicity**:
     * - Extracts to temporary directory first
     * - Validates extraction completed successfully
     * - Renames to final destination (atomic operation)
     * - Cleans up temporary directory on failure
     *
     * **Cancellation**:
     * - Cancellable via job cancellation
     * - Temporary directory deleted on cancellation
     * - Partial extraction does not affect existing installation
     *
     * **Error Handling**:
     * - IOException: File system errors (disk full, permissions, etc.)
     * - ArchiveException: Corrupted archive, invalid format
     * - InsufficientStorageError: Not enough disk space
     * - Flow throws exception on unrecoverable errors
     *
     * **Performance**:
     * - Streaming extraction (low memory usage)
     * - ~30-60 seconds for 150MB bootstrap
     * - Peak memory: ~20-30MB
     *
     * @param archiveFile Bootstrap tar.xz archive file
     * @param destinationDir Target directory for extraction
     *
     * @return Flow<ExtractionProgress> that emits progress updates
     *         Flow completes when extraction finishes successfully
     *
     * @throws FileNotFoundException if archiveFile does not exist
     * @throws IOException if extraction fails
     * @throws ArchiveException if archive is corrupted or invalid format
     * @throws InsufficientStorageException if not enough disk space
     *
     * @see ExtractionProgress for progress details
     */
    fun extract(
        archiveFile: File,
        destinationDir: File
    ): Flow<ExtractionProgress>

    /**
     * Verify extraction completed successfully.
     *
     * Validates that bootstrap extraction is complete and valid by:
     * - Checking critical directories exist (bin/, lib/, etc.)
     * - Checking bash executable exists
     * - Checking core utilities exist (ls, cat, grep, etc.)
     * - Verifying file count matches expected
     *
     * **Validation Checks**:
     * - `destinationDir/bin/` directory exists
     * - `destinationDir/bin/bash` file exists
     * - `destinationDir/lib/` directory exists
     * - At least 500 files extracted (approximate bootstrap file count)
     *
     * **Performance**: Fast (~50-100ms), file existence checks only
     *
     * **Use Cases**:
     * - Verify extraction completed successfully
     * - Validate before setting permissions
     * - Troubleshoot extraction issues
     *
     * @param destinationDir Directory where bootstrap was extracted
     *
     * @return true if extraction appears valid, false otherwise
     *
     * @throws None - returns false on any validation failure
     */
    suspend fun verifyExtraction(destinationDir: File): Boolean

    /**
     * Get total file count in archive without extracting.
     *
     * Scans tar archive to count total entries.
     * Used to provide accurate progress percentage during extraction.
     *
     * **Performance**: Fast (~500ms-1s), scans archive without extraction
     *
     * **Caching**: Result cached after first call for same archive
     *
     * @param archiveFile Bootstrap tar.xz archive file
     *
     * @return Total number of entries in archive (typically ~1000-1500)
     *
     * @throws IOException if archive cannot be read
     * @throws ArchiveException if archive format is invalid
     */
    suspend fun getArchiveFileCount(archiveFile: File): Int

    /**
     * Get extracted size estimate for archive.
     *
     * Estimates the total size after extraction by scanning archive.
     * Used to verify sufficient disk space before extraction.
     *
     * **Performance**: Moderate (~1-2s), scans archive metadata
     *
     * **Accuracy**: Estimate based on uncompressed sizes in tar metadata
     *
     * @param archiveFile Bootstrap tar.xz archive file
     *
     * @return Estimated extracted size in bytes (typically ~150MB)
     *
     * @throws IOException if archive cannot be read
     */
    suspend fun getExtractedSize(archiveFile: File): Long

    /**
     * Delete extraction directory and all contents.
     *
     * Recursively deletes destination directory and all files.
     * Used for cleanup on cancellation or failure.
     *
     * **Safety**:
     * - Only deletes if directory is within app files directory
     * - Validates path to prevent accidental deletion
     * - Safe to call even if directory doesn't exist
     *
     * **Performance**: Slow (~5-10s), deletes many files
     *
     * @param destinationDir Directory to delete
     *
     * @throws SecurityException if path is outside app files directory
     * @throws IOException if deletion fails (rare)
     */
    suspend fun deleteExtraction(destinationDir: File)

    /**
     * Check if symlinks are supported on filesystem.
     *
     * Tests if Android filesystem supports Unix symlinks by
     * attempting to create a test symlink.
     *
     * **Typical Behavior**:
     * - App private storage: Symlinks supported (most devices)
     * - External storage: Symlinks NOT supported
     * - SD card: Symlinks NOT supported
     *
     * **Performance**: Fast (~10-20ms), creates and deletes test file
     *
     * **Caching**: Result cached for app session
     *
     * @param directory Directory to test (typically app files directory)
     *
     * @return true if symlinks supported, false otherwise
     */
    suspend fun areSymlinksSupported(directory: File): Boolean

    /**
     * Extract single file from archive.
     *
     * Extracts a specific file from archive without extracting entire archive.
     * Useful for testing or extracting specific files.
     *
     * **Use Cases**:
     * - Extract bash binary only for quick testing
     * - Extract specific configuration files
     * - Troubleshooting extraction issues
     *
     * **Performance**: Fast (~500ms), only extracts one file
     *
     * @param archiveFile Bootstrap tar.xz archive file
     * @param entryPath Path within archive (e.g., "usr/bin/bash")
     * @param destination Target file for extracted entry
     *
     * @return true if file extracted successfully, false if not found
     *
     * @throws IOException if extraction fails
     * @throws FileNotFoundException if archiveFile does not exist
     */
    suspend fun extractSingleFile(
        archiveFile: File,
        entryPath: String,
        destination: File
    ): Boolean

    /**
     * List all entries in archive without extracting.
     *
     * Returns list of all file paths in archive.
     * Useful for debugging and validation.
     *
     * **Performance**: Moderate (~1-2s), scans archive
     *
     * **Use Cases**:
     * - Verify archive contents before extraction
     * - Debug extraction issues
     * - Generate file manifest
     *
     * @param archiveFile Bootstrap tar.xz archive file
     *
     * @return List of all entry paths in archive (e.g., ["usr/bin/bash", ...])
     *
     * @throws IOException if archive cannot be read
     * @throws ArchiveException if archive format is invalid
     */
    suspend fun listArchiveEntries(archiveFile: File): List<String>
}
