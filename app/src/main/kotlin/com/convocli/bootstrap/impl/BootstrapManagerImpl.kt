package com.convocli.bootstrap.impl

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.convocli.bootstrap.BootstrapDownloader
import com.convocli.bootstrap.BootstrapExtractor
import com.convocli.bootstrap.BootstrapManager
import com.convocli.bootstrap.BootstrapValidator
import com.convocli.data.model.BootstrapError
import com.convocli.data.model.BootstrapInstallation
import com.convocli.data.model.InstallationPhase
import com.convocli.data.model.InstallationProgress
import com.convocli.data.model.InstallationStatus
import com.convocli.data.model.ValidationResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

private val Context.bootstrapDataStore: DataStore<Preferences> by preferencesDataStore(name = "bootstrap")

/**
 * Bootstrap Manager Implementation
 *
 * Orchestrates the complete bootstrap installation lifecycle.
 *
 * **Feature**: 003 - Termux Bootstrap Installation
 * **Created**: 2025-10-22
 */
@Singleton
class BootstrapManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val downloader: BootstrapDownloader,
    private val extractor: BootstrapExtractor,
    private val validator: BootstrapValidator
) : BootstrapManager {

    companion object {
        private const val TAG = "BootstrapManager"
        private val INSTALLATION_KEY = stringPreferencesKey("bootstrap_installation")
    }

    private var installationJob: Job? = null
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun isInstalled(): Boolean {
        val bootstrapPath = getBootstrapPath()
        val bootstrapDir = File(bootstrapPath)

        if (!bootstrapDir.exists()) {
            return false
        }

        val bashPath = File(bootstrapDir, "bin/bash")
        if (!bashPath.exists() || !bashPath.canExecute()) {
            return false
        }

        // Quick validation
        return validator.validateBash(bootstrapDir)
    }

    override fun getInstallationStatus(): Flow<BootstrapInstallation> {
        return context.bootstrapDataStore.data.map { preferences ->
            val json = preferences[INSTALLATION_KEY]
            if (json != null) {
                this.json.decodeFromString<BootstrapInstallation>(json)
            } else {
                BootstrapInstallation(status = InstallationStatus.NOT_INSTALLED)
            }
        }
    }

    override fun install(): Flow<InstallationProgress> = flow {
        Log.d(TAG, "Starting bootstrap installation")

        if (installationJob?.isActive == true) {
            throw IllegalStateException("Installation already in progress")
        }

        try {
            // Phase 1: Detection
            emit(
                InstallationProgress(
                    phase = InstallationPhase.DETECTING,
                    message = "Detecting device architecture..."
                )
            )

            val architecture = getDeviceArchitecture()
            Log.d(TAG, "Detected architecture: $architecture")

            updateInstallationStatus(
                BootstrapInstallation(
                    status = InstallationStatus.DOWNLOADING,
                    architecture = architecture
                )
            )

            // Phase 2: Download
            emit(
                InstallationProgress(
                    phase = InstallationPhase.DOWNLOADING,
                    message = "Downloading bootstrap archive..."
                )
            )

            val downloadFile = File(context.filesDir, "bootstrap-$architecture.zip")
            downloader.download(architecture, downloadFile)
                .collect { downloadProgress ->
                    emit(
                        InstallationProgress(
                            phase = InstallationPhase.DOWNLOADING,
                            bytesDownloaded = downloadProgress.bytesDownloaded,
                            totalBytes = downloadProgress.totalBytes,
                            estimatedTimeRemaining = downloadProgress.estimatedTimeRemaining,
                            message = "Downloading bootstrap... ${downloadProgress.progressPercentage}%"
                        )
                    )
                }

            // TODO: Checksum verification (needs actual checksums)

            updateInstallationStatus(
                BootstrapInstallation(
                    status = InstallationStatus.EXTRACTING,
                    architecture = architecture
                )
            )

            // Phase 3: Extraction
            emit(
                InstallationProgress(
                    phase = InstallationPhase.EXTRACTING,
                    message = "Extracting bootstrap files..."
                )
            )

            val bootstrapDir = File(getBootstrapPath())
            extractor.extract(downloadFile, bootstrapDir)
                .collect { extractionProgress ->
                    emit(
                        InstallationProgress(
                            phase = InstallationPhase.EXTRACTING,
                            filesExtracted = extractionProgress.filesExtracted,
                            totalFiles = extractionProgress.totalFiles,
                            message = "Extracting files... ${extractionProgress.progressPercentage}%"
                        )
                    )
                }

            // Verify extraction
            if (!extractor.verifyExtraction(bootstrapDir)) {
                emit(
                    InstallationProgress(
                        phase = InstallationPhase.EXTRACTING,
                        message = "Extraction verification failed",
                        error = BootstrapError.ExtractionError(
                            message = "Bootstrap extraction verification failed"
                        )
                    )
                )
                return@flow
            }

            updateInstallationStatus(
                BootstrapInstallation(
                    status = InstallationStatus.CONFIGURING,
                    architecture = architecture,
                    installedPath = bootstrapDir.absolutePath
                )
            )

            // Phase 4: Configure Permissions
            emit(
                InstallationProgress(
                    phase = InstallationPhase.CONFIGURING_PERMISSIONS,
                    message = "Setting file permissions..."
                )
            )

            configurePermissions(bootstrapDir)

            // Phase 5: Configure Environment
            emit(
                InstallationProgress(
                    phase = InstallationPhase.CONFIGURING_ENVIRONMENT,
                    message = "Configuring environment..."
                )
            )

            // Environment is configured per-process, nothing to persist here

            updateInstallationStatus(
                BootstrapInstallation(
                    status = InstallationStatus.VERIFYING,
                    architecture = architecture,
                    installedPath = bootstrapDir.absolutePath
                )
            )

            // Phase 6: Validation
            emit(
                InstallationProgress(
                    phase = InstallationPhase.VERIFYING,
                    message = "Verifying installation..."
                )
            )

            when (val validationResult = validator.validateInstallation(bootstrapDir)) {
                is ValidationResult.Success -> {
                    Log.d(TAG, "Installation validated successfully")

                    // Final status update
                    updateInstallationStatus(
                        BootstrapInstallation(
                            status = InstallationStatus.INSTALLED,
                            version = validationResult.version,
                            architecture = architecture,
                            installedPath = bootstrapDir.absolutePath,
                            installationDate = System.currentTimeMillis(),
                            lastValidated = System.currentTimeMillis(),
                            installedSizeBytes = validator.getInstallationSize(bootstrapDir)
                        )
                    )

                    // Phase 7: Complete
                    emit(
                        InstallationProgress(
                            phase = InstallationPhase.COMPLETE,
                            message = "Installation complete!"
                        )
                    )

                    // Clean up download file
                    downloadFile.delete()
                }

                is ValidationResult.Failure -> {
                    Log.e(TAG, "Installation validation failed: ${validationResult.reason}")

                    updateInstallationStatus(
                        BootstrapInstallation(status = InstallationStatus.FAILED)
                    )

                    emit(
                        InstallationProgress(
                            phase = InstallationPhase.VERIFYING,
                            message = "Validation failed",
                            error = BootstrapError.ValidationError(
                                message = validationResult.reason,
                                technicalDetails = validationResult.technicalDetails
                            )
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Installation failed with exception", e)

            updateInstallationStatus(
                BootstrapInstallation(status = InstallationStatus.FAILED)
            )

            emit(
                InstallationProgress(
                    phase = InstallationPhase.DETECTING,
                    message = "Installation failed",
                    error = BootstrapError.UnknownError(
                        message = e.message ?: "Unknown error occurred",
                        technicalDetails = e.stackTraceToString()
                    )
                )
            )
        }
    }

    override suspend fun cancelInstallation() {
        Log.d(TAG, "Cancelling installation")
        installationJob?.cancel()

        updateInstallationStatus(
            BootstrapInstallation(status = InstallationStatus.CANCELLED)
        )

        // TODO: Cleanup partial downloads/extractions
    }

    override suspend fun validateInstallation(): ValidationResult {
        val bootstrapDir = File(getBootstrapPath())
        return validator.validateInstallation(bootstrapDir)
    }

    override suspend fun deleteInstallation() {
        Log.d(TAG, "Deleting bootstrap installation")

        val bootstrapDir = File(getBootstrapPath())
        extractor.deleteExtraction(bootstrapDir)

        updateInstallationStatus(
            BootstrapInstallation(status = InstallationStatus.NOT_INSTALLED)
        )
    }

    override fun getDeviceArchitecture(): String {
        val abi = Build.SUPPORTED_ABIS[0]
        return when (abi) {
            "arm64-v8a" -> "aarch64"
            "armeabi-v7a" -> "arm"
            "x86_64" -> "x86_64"
            "x86" -> "i686"
            else -> throw IllegalStateException("Unsupported architecture: $abi")
        }
    }

    override fun getBootstrapPath(): String {
        return File(context.filesDir, "usr").absolutePath
    }

    private suspend fun updateInstallationStatus(installation: BootstrapInstallation) {
        context.bootstrapDataStore.edit { preferences ->
            preferences[INSTALLATION_KEY] = json.encodeToString(installation)
        }
    }

    private suspend fun configurePermissions(bootstrapDir: File) {
        // Set execute permissions on binaries
        val binDir = File(bootstrapDir, "bin")
        binDir.listFiles()?.forEach { file ->
            if (file.isFile) {
                file.setExecutable(true, false)
                Log.d(TAG, "Set executable: ${file.name}")
            }
        }

        val libexecDir = File(bootstrapDir, "libexec")
        if (libexecDir.exists()) {
            libexecDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    file.setExecutable(true, false)
                }
            }
        }
    }
}
