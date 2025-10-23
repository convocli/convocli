package com.convocli.bootstrap.impl

import android.util.Log
import com.convocli.bootstrap.BootstrapDownloader
import com.convocli.data.model.DownloadProgress
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Bootstrap Downloader Implementation
 *
 * Downloads bootstrap archives from GitHub using Ktor Client.
 * Supports progress tracking, resume capability, and checksum verification.
 *
 * **Feature**: 003 - Termux Bootstrap Installation
 * **Created**: 2025-10-22
 */
class BootstrapDownloaderImpl @Inject constructor() : BootstrapDownloader {

    companion object {
        private const val TAG = "BootstrapDownloader"
        private const val BASE_URL = "https://github.com/termux/termux-packages/releases/download"
        private const val BOOTSTRAP_VERSION = "bootstrap-2025.09.21-r1+apt-android-7"

        // Known checksums for bootstrap ZIP files (from research)
        // These would be fetched from GitHub releases or hardcoded per version
        private val KNOWN_CHECKSUMS = mapOf(
            "aarch64" to "TBD_FROM_GITHUB_RELEASE",
            "arm" to "TBD_FROM_GITHUB_RELEASE",
            "x86_64" to "TBD_FROM_GITHUB_RELEASE",
            "i686" to "TBD_FROM_GITHUB_RELEASE"
        )
    }

    private val httpClient = HttpClient(Android) {
        engine {
            connectTimeout = 30_000
            socketTimeout = 30_000
        }
    }

    override fun download(
        architecture: String,
        destination: File
    ): Flow<DownloadProgress> = flow {
        val url = getDownloadUrl(architecture)
        Log.d(TAG, "Starting download from: $url")

        destination.parentFile?.mkdirs()

        val startTime = System.currentTimeMillis()
        var lastUpdateTime = startTime
        var bytesDownloaded = 0L

        httpClient.prepareGet(url).execute { response ->
            if (!response.status.isSuccess()) {
                throw IllegalStateException("Download failed: ${response.status}")
            }

            val totalBytes = response.headers["Content-Length"]?.toLong() ?: 0L
            Log.d(TAG, "Total download size: $totalBytes bytes")

            val channel = response.bodyAsChannel()
            destination.outputStream().use { output ->
                val buffer = ByteArray(8192)

                while (!channel.isClosedForRead) {
                    val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        output.write(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead

                        // Emit progress every 100KB or every second
                        val now = System.currentTimeMillis()
                        if (bytesDownloaded % 102400 == 0L || (now - lastUpdateTime) > 1000) {
                            val elapsedMs = now - startTime
                            val speed = if (elapsedMs > 0) {
                                (bytesDownloaded * 1000) / elapsedMs
                            } else null

                            val remainingBytes = totalBytes - bytesDownloaded
                            val eta = if (speed != null && speed > 0) {
                                (remainingBytes * 1000) / speed
                            } else null

                            emit(
                                DownloadProgress(
                                    bytesDownloaded = bytesDownloaded,
                                    totalBytes = totalBytes,
                                    speedBytesPerSecond = speed,
                                    estimatedTimeRemaining = eta
                                )
                            )
                            lastUpdateTime = now
                        }
                    }
                }
            }

            // Final progress update
            emit(
                DownloadProgress(
                    bytesDownloaded = bytesDownloaded,
                    totalBytes = totalBytes
                )
            )
        }

        Log.d(TAG, "Download complete: $bytesDownloaded bytes")
    }.flowOn(Dispatchers.IO)

    override suspend fun resumeDownload(
        architecture: String,
        destination: File,
        bytesAlreadyDownloaded: Long
    ): Flow<DownloadProgress> = flow {
        val url = getDownloadUrl(architecture)
        Log.d(TAG, "Resuming download from byte: $bytesAlreadyDownloaded")

        val startTime = System.currentTimeMillis()
        var lastUpdateTime = startTime
        var bytesDownloaded = bytesAlreadyDownloaded

        httpClient.prepareGet(url) {
            header("Range", "bytes=$bytesAlreadyDownloaded-")
        }.execute { response ->
            if (!response.status.isSuccess()) {
                throw IllegalStateException("Resume failed: ${response.status}")
            }

            val contentRange = response.headers["Content-Range"]
            val totalBytes = contentRange?.substringAfter("/")?.toLongOrNull() ?: 0L

            val channel = response.bodyAsChannel()
            FileOutputStream(destination, true).use { output ->
                val buffer = ByteArray(8192)

                while (!channel.isClosedForRead) {
                    val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        output.write(buffer, 0, bytesRead)
                        bytesDownloaded += bytesRead

                        val now = System.currentTimeMillis()
                        if (bytesDownloaded % 102400 == 0L || (now - lastUpdateTime) > 1000) {
                            val elapsedMs = now - startTime
                            val speed = if (elapsedMs > 0) {
                                ((bytesDownloaded - bytesAlreadyDownloaded) * 1000) / elapsedMs
                            } else null

                            val remainingBytes = totalBytes - bytesDownloaded
                            val eta = if (speed != null && speed > 0) {
                                (remainingBytes * 1000) / speed
                            } else null

                            emit(
                                DownloadProgress(
                                    bytesDownloaded = bytesDownloaded,
                                    totalBytes = totalBytes,
                                    speedBytesPerSecond = speed,
                                    estimatedTimeRemaining = eta
                                )
                            )
                            lastUpdateTime = now
                        }
                    }
                }
            }

            emit(
                DownloadProgress(
                    bytesDownloaded = bytesDownloaded,
                    totalBytes = totalBytes
                )
            )
        }

        Log.d(TAG, "Resume complete: $bytesDownloaded total bytes")
    }.flowOn(Dispatchers.IO)

    override suspend fun verifyChecksum(file: File, expectedChecksum: String): Boolean = withContext(Dispatchers.IO) {
        Log.d(TAG, "Verifying checksum for: ${file.name}")

        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }

        val actualChecksum = digest.digest().joinToString("") { "%02x".format(it) }
        val matches = actualChecksum.equals(expectedChecksum, ignoreCase = true)

        Log.d(TAG, "Checksum verification: ${if (matches) "SUCCESS" else "FAILED"}")
        if (!matches) {
            Log.e(TAG, "Expected: $expectedChecksum")
            Log.e(TAG, "Actual: $actualChecksum")
        }

        matches
    }

    override suspend fun isResumeSupported(): Boolean {
        // Ktor Android client supports range requests
        return true
    }

    override fun getDownloadUrl(architecture: String): String {
        return "$BASE_URL/$BOOTSTRAP_VERSION/bootstrap-$architecture.zip"
    }

    override suspend fun getExpectedChecksum(architecture: String): String {
        // In a production implementation, this would fetch from GitHub API
        // or parse from a checksums file. For now, return the known checksums.
        return KNOWN_CHECKSUMS[architecture] ?: "UNKNOWN_CHECKSUM_FOR_$architecture"
    }
}
