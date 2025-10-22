package com.convocli.bootstrap.impl

import android.util.Log
import com.convocli.bootstrap.BootstrapValidator
import com.convocli.data.model.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.File
import javax.inject.Inject

/**
 * Bootstrap Validator Implementation
 *
 * Validates bootstrap installation by executing test commands.
 *
 * **Feature**: 003 - Termux Bootstrap Installation
 * **Created**: 2025-10-22
 */
class BootstrapValidatorImpl @Inject constructor() : BootstrapValidator {

    companion object {
        private const val TAG = "BootstrapValidator"
        private const val COMMAND_TIMEOUT_MS = 5000L
    }

    override suspend fun validateInstallation(bootstrapDir: File): ValidationResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "Validating bootstrap installation: ${bootstrapDir.absolutePath}")

        try {
            // Check directory structure
            if (!verifyDirectoryStructure(bootstrapDir)) {
                return@withContext ValidationResult.Failure(
                    reason = "Bootstrap directory structure is invalid",
                    technicalDetails = "Missing required directories (bin/, lib/, share/)"
                )
            }

            // Check bash exists and is executable
            val bashPath = getBashPath(bootstrapDir)
            val bashFile = File(bashPath)
            if (!bashFile.exists() || !bashFile.canExecute()) {
                return@withContext ValidationResult.Failure(
                    reason = "Bash executable not found or not executable",
                    failedCommand = bashPath,
                    technicalDetails = "Exists: ${bashFile.exists()}, Executable: ${bashFile.canExecute()}"
                )
            }

            // Execute bash --version
            val versionResult = executeTestCommand(bootstrapDir, "bash --version")
            if (versionResult == null) {
                return@withContext ValidationResult.Failure(
                    reason = "Failed to execute bash --version",
                    failedCommand = "bash --version"
                )
            }

            val version = detectBashVersion(bootstrapDir) ?: "unknown"

            // Test core utilities
            val utilities = listOf("ls", "pwd", "cat", "grep", "echo")
            val validatedUtilities = mutableListOf<String>()

            for (utility in utilities) {
                if (validateUtility(bootstrapDir, utility)) {
                    validatedUtilities.add(utility)
                } else {
                    Log.w(TAG, "Utility validation failed: $utility")
                }
            }

            // Require at least 3 out of 5 utilities to work
            if (validatedUtilities.size < 3) {
                return@withContext ValidationResult.Failure(
                    reason = "Too many core utilities failed validation",
                    technicalDetails = "Only ${validatedUtilities.size}/5 utilities validated: $validatedUtilities"
                )
            }

            Log.d(TAG, "Validation successful")
            ValidationResult.Success(
                version = version,
                bashPath = bashPath,
                validatedUtilities = validatedUtilities
            )
        } catch (e: Exception) {
            Log.e(TAG, "Validation exception", e)
            ValidationResult.Failure(
                reason = "Validation failed with exception",
                technicalDetails = e.message
            )
        }
    }

    override suspend fun validateBash(bootstrapDir: File): Boolean = withContext(Dispatchers.IO) {
        val bashPath = getBashPath(bootstrapDir)
        val bashFile = File(bashPath)

        if (!bashFile.exists() || !bashFile.canExecute()) {
            return@withContext false
        }

        val result = executeTestCommand(bootstrapDir, "bash --version")
        result != null
    }

    override suspend fun validateUtility(bootstrapDir: File, utilityName: String): Boolean = withContext(Dispatchers.IO) {
        val testCommand = when (utilityName) {
            "bash" -> "bash --version"
            "ls" -> "ls /"
            "pwd" -> "pwd"
            "cat" -> "cat /dev/null"
            "grep" -> "grep --version"
            "echo" -> "echo test"
            else -> throw IllegalArgumentException("Unsupported utility: $utilityName")
        }

        val result = executeTestCommand(bootstrapDir, testCommand)
        result != null
    }

    override suspend fun detectBashVersion(bootstrapDir: File): String? = withContext(Dispatchers.IO) {
        val output = executeTestCommand(bootstrapDir, "bash --version") ?: return@withContext null

        // Parse version from output like "GNU bash, version 5.1.16(1)-release..."
        val versionRegex = """version\s+(\d+\.\d+\.\d+)""".toRegex()
        val match = versionRegex.find(output)
        match?.groupValues?.get(1)
    }

    override suspend fun verifyDirectoryStructure(bootstrapDir: File): Boolean = withContext(Dispatchers.IO) {
        val requiredDirs = listOf("bin", "lib", "share")
        requiredDirs.all { dirName ->
            val dir = File(bootstrapDir, dirName)
            dir.exists() && dir.isDirectory
        }
    }

    override suspend fun checkBinaryPermissions(bootstrapDir: File): Map<String, Boolean> = withContext(Dispatchers.IO) {
        val binaries = listOf("bin/bash", "bin/ls", "bin/cat", "bin/grep", "bin/echo", "bin/pwd")
        binaries.associate { path ->
            val file = File(bootstrapDir, path)
            path to (file.exists() && file.canExecute())
        }
    }

    override suspend fun executeTestCommand(bootstrapDir: File, command: String): String? = withContext(Dispatchers.IO) {
        try {
            withTimeout(COMMAND_TIMEOUT_MS) {
                val env = getRequiredEnvironment(bootstrapDir)
                val processBuilder = ProcessBuilder()
                    .command("sh", "-c", command)
                    .directory(bootstrapDir)

                // Set environment variables
                processBuilder.environment().clear()
                processBuilder.environment().putAll(env)

                val process = processBuilder.start()
                val output = process.inputStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                if (exitCode == 0) {
                    output
                } else {
                    Log.w(TAG, "Command failed with exit code $exitCode: $command")
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to execute command: $command", e)
            null
        }
    }

    override fun getBashPath(bootstrapDir: File): String {
        return File(bootstrapDir, "bin/bash").absolutePath
    }

    override fun getRequiredEnvironment(bootstrapDir: File): Map<String, String> {
        return mapOf(
            "PREFIX" to bootstrapDir.absolutePath,
            "PATH" to "${bootstrapDir.absolutePath}/bin",
            "HOME" to bootstrapDir.parentFile!!.absolutePath,
            "TMPDIR" to File(bootstrapDir, "tmp").absolutePath,
            "SHELL" to getBashPath(bootstrapDir),
            "LANG" to "en_US.UTF-8"
        )
    }

    override suspend fun validateEnvironment(bootstrapDir: File): Boolean = withContext(Dispatchers.IO) {
        val requiredVars = getRequiredEnvironment(bootstrapDir)
        val testCommand = "echo \$PREFIX"
        val output = executeTestCommand(bootstrapDir, testCommand)

        output?.trim() == bootstrapDir.absolutePath
    }

    override suspend fun countFiles(bootstrapDir: File): Int = withContext(Dispatchers.IO) {
        var count = 0
        bootstrapDir.walk().forEach { file ->
            if (file.isFile) {
                count++
            }
        }
        count
    }

    override suspend fun getInstallationSize(bootstrapDir: File): Long = withContext(Dispatchers.IO) {
        var totalSize = 0L
        bootstrapDir.walk().forEach { file ->
            if (file.isFile) {
                totalSize += file.length()
            }
        }
        totalSize
    }
}
