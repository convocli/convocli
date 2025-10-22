package com.convocli.bootstrap.impl

import android.util.Log
import com.convocli.bootstrap.BootstrapExtractor
import com.convocli.data.model.ExtractionProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

/**
 * Bootstrap Extractor Implementation
 *
 * Extracts ZIP archives using standard Java ZipInputStream.
 * Preserves symlinks via SYMLINKS.txt file.
 *
 * **Feature**: 003 - Termux Bootstrap Installation
 * **Created**: 2025-10-22
 */
class BootstrapExtractorImpl @Inject constructor() : BootstrapExtractor {

    companion object {
        private const val TAG = "BootstrapExtractor"
    }

    override fun extract(
        archiveFile: File,
        destinationDir: File
    ): Flow<ExtractionProgress> = flow {
        Log.d(TAG, "Starting extraction: ${archiveFile.name} -> ${destinationDir.absolutePath}")

        // Create staging directory for atomic extraction
        val stagingDir = File(destinationDir.parent, "${destinationDir.name}.staging")
        stagingDir.deleteRecursively()
        stagingDir.mkdirs()

        var filesExtracted = 0
        val totalFiles = getArchiveFileCount(archiveFile)

        ZipInputStream(archiveFile.inputStream().buffered()).use { zipIn ->
            var entry: ZipEntry? = zipIn.nextEntry

            while (entry != null) {
                val entryName = entry.name
                val targetFile = File(stagingDir, entryName)

                if (entry.isDirectory) {
                    targetFile.mkdirs()
                } else {
                    // Create parent directories
                    targetFile.parentFile?.mkdirs()

                    // Extract file
                    targetFile.outputStream().use { output ->
                        zipIn.copyTo(output)
                    }
                }

                filesExtracted++

                // Emit progress every 10 files
                if (filesExtracted % 10 == 0 || filesExtracted == totalFiles) {
                    emit(
                        ExtractionProgress(
                            filesExtracted = filesExtracted,
                            totalFiles = totalFiles,
                            currentFile = entryName
                        )
                    )
                }

                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        // Handle symlinks from SYMLINKS.txt (Termux convention)
        val symlinksFile = File(stagingDir, "SYMLINKS.txt")
        if (symlinksFile.exists()) {
            Log.d(TAG, "Processing symlinks from SYMLINKS.txt")
            symlinksFile.readLines().forEach { line ->
                val parts = line.split(" -> ")
                if (parts.size == 2) {
                    val linkPath = parts[0].trim()
                    val targetPath = parts[1].trim()

                    try {
                        val linkFile = File(stagingDir, linkPath)
                        Files.createSymbolicLink(
                            linkFile.toPath(),
                            Paths.get(targetPath)
                        )
                        Log.d(TAG, "Created symlink: $linkPath -> $targetPath")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to create symlink: $linkPath", e)
                    }
                }
            }
        }

        // Atomic move: rename staging to final destination
        if (destinationDir.exists()) {
            destinationDir.deleteRecursively()
        }
        if (!stagingDir.renameTo(destinationDir)) {
            throw IllegalStateException("Failed to rename staging directory to final destination")
        }

        // Final progress
        emit(
            ExtractionProgress(
                filesExtracted = filesExtracted,
                totalFiles = totalFiles,
                currentFile = null
            )
        )

        Log.d(TAG, "Extraction complete: $filesExtracted files extracted")
    }.flowOn(Dispatchers.IO)

    override suspend fun verifyExtraction(destinationDir: File): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Verifying extraction: ${destinationDir.absolutePath}")

        // Check critical directories and files
        val binDir = File(destinationDir, "bin")
        val bashFile = File(binDir, "bash")
        val libDir = File(destinationDir, "lib")

        val isValid = binDir.exists() &&
            binDir.isDirectory &&
            bashFile.exists() &&
            bashFile.isFile &&
            libDir.exists() &&
            libDir.isDirectory

        Log.d(TAG, "Verification result: ${if (isValid) "PASS" else "FAIL"}")
        isValid
    }

    override suspend fun getArchiveFileCount(archiveFile: File): Int = withContext(Dispatchers.IO) {
        var count = 0
        ZipInputStream(archiveFile.inputStream().buffered()).use { zipIn ->
            while (zipIn.nextEntry != null) {
                count++
                zipIn.closeEntry()
            }
        }
        Log.d(TAG, "Archive contains $count entries")
        count
    }

    override suspend fun getExtractedSize(archiveFile: File): Long = withContext(Dispatchers.IO) {
        var totalSize = 0L
        ZipInputStream(archiveFile.inputStream().buffered()).use { zipIn ->
            var entry: ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                totalSize += entry.size
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
        Log.d(TAG, "Estimated extracted size: $totalSize bytes")
        totalSize
    }

    override suspend fun deleteExtraction(destinationDir: File) = withContext(Dispatchers.IO) {
        Log.d(TAG, "Deleting extraction: ${destinationDir.absolutePath}")

        // Safety check: only delete within app files directory
        val filesDir = "/data/data/com.convocli/files"
        if (!destinationDir.absolutePath.startsWith(filesDir)) {
            throw SecurityException("Cannot delete directory outside app files: ${destinationDir.absolutePath}")
        }

        destinationDir.deleteRecursively()
        Log.d(TAG, "Deletion complete")
    }

    override suspend fun areSymlinksSupported(directory: File): Boolean = withContext(Dispatchers.IO) {
        val testLink = File(directory, ".symlink-test")
        val testTarget = File(directory, ".symlink-target")

        try {
            testTarget.createNewFile()
            Files.createSymbolicLink(testLink.toPath(), testTarget.toPath())
            val supported = Files.isSymbolicLink(testLink.toPath())
            testLink.delete()
            testTarget.delete()
            Log.d(TAG, "Symlinks supported: $supported")
            supported
        } catch (e: Exception) {
            Log.d(TAG, "Symlinks not supported", e)
            false
        }
    }

    override suspend fun extractSingleFile(
        archiveFile: File,
        entryPath: String,
        destination: File
    ): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Extracting single file: $entryPath")

        var found = false
        ZipInputStream(archiveFile.inputStream().buffered()).use { zipIn ->
            var entry: ZipEntry? = zipIn.nextEntry

            while (entry != null && !found) {
                if (entry.name == entryPath || entry.name.endsWith("/$entryPath")) {
                    destination.parentFile?.mkdirs()
                    destination.outputStream().use { output ->
                        zipIn.copyTo(output)
                    }
                    found = true
                    Log.d(TAG, "File extracted: $entryPath")
                }
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }

        if (!found) {
            Log.w(TAG, "File not found in archive: $entryPath")
        }
        found
    }

    override suspend fun listArchiveEntries(archiveFile: File): List<String> = withContext(Dispatchers.IO) {
        val entries = mutableListOf<String>()
        ZipInputStream(archiveFile.inputStream().buffered()).use { zipIn ->
            var entry: ZipEntry? = zipIn.nextEntry
            while (entry != null) {
                entries.add(entry.name)
                zipIn.closeEntry()
                entry = zipIn.nextEntry
            }
        }
        Log.d(TAG, "Listed ${entries.size} archive entries")
        entries
    }
}
