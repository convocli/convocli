package com.convocli.bootstrap

import com.convocli.data.model.ValidationResult
import java.io.File

/**
 * Bootstrap Validator Interface
 *
 * Validates bootstrap installation integrity by testing bash execution
 * and core utility functionality.
 *
 * **Feature**: 003 - Termux Bootstrap Installation
 * **Created**: 2025-10-22
 * **Pattern**: Suspend functions with ValidationResult return type
 *
 * ## Responsibilities
 * - Execute bash commands to verify functionality
 * - Test core utilities (ls, pwd, cat, grep, echo)
 * - Verify directory structure
 * - Check file permissions
 * - Detect bash version
 *
 * ## Usage Example
 * ```kotlin
 * val bootstrapDir = File(context.filesDir, "usr")
 *
 * // Validate installation
 * when (val result = validator.validateInstallation(bootstrapDir)) {
 *     is ValidationResult.Success -> {
 *         println("Bootstrap version: ${result.version}")
 *         println("Bash path: ${result.bashPath}")
 *         println("Validated utilities: ${result.validatedUtilities}")
 *     }
 *     is ValidationResult.Failure -> {
 *         println("Validation failed: ${result.reason}")
 *         println("Failed command: ${result.failedCommand}")
 *     }
 * }
 * ```
 *
 * ## Implementation Notes
 * - Executes real commands via ProcessBuilder
 * - All commands run on Dispatchers.IO
 * - Timeout: 5 seconds per command
 * - No side effects: Does not modify filesystem
 *
 * @see BootstrapManager for orchestration
 * @see ValidationResult for result details
 */
interface BootstrapValidator {

    /**
     * Validate complete bootstrap installation.
     *
     * Performs comprehensive validation by testing bash execution
     * and core utilities.
     *
     * **Validation Steps**:
     * 1. Check bash executable exists and has execute permission
     * 2. Execute `bash --version` to verify functionality
     * 3. Test core utilities: ls, pwd, cat, grep, echo
     * 4. Verify directory structure (bin/, lib/, share/)
     * 5. Check environment variable configuration
     *
     * **Commands Executed**:
     * ```bash
     * bash --version
     * ls /
     * pwd
     * echo "test"
     * cat /dev/null
     * grep --version
     * ```
     *
     * **Performance**: Slow (~500-1000ms), executes multiple commands
     *
     * **Timeout**: Each command has 5-second timeout
     *
     * **Error Handling**:
     * - Returns ValidationResult.Failure if any command fails
     * - Includes failed command and reason in result
     * - Never throws exceptions
     *
     * @param bootstrapDir Bootstrap installation directory (e.g., /data/.../usr)
     *
     * @return ValidationResult.Success if all checks pass,
     *         ValidationResult.Failure with details if any check fails
     *
     * @see ValidationResult for result structure
     */
    suspend fun validateInstallation(bootstrapDir: File): ValidationResult

    /**
     * Quick validation check (bash only).
     *
     * Performs minimal validation by checking bash executable exists
     * and can execute `bash --version`.
     *
     * **Validation Steps**:
     * 1. Check bash file exists
     * 2. Check bash has execute permission
     * 3. Execute `bash --version`
     *
     * **Performance**: Fast (~100-200ms), single command
     *
     * **Use Cases**:
     * - Quick check on app launch
     * - Check before executing user commands
     * - Health check endpoint
     *
     * @param bootstrapDir Bootstrap installation directory
     *
     * @return true if bash is functional, false otherwise
     */
    suspend fun validateBash(bootstrapDir: File): Boolean

    /**
     * Validate specific utility is functional.
     *
     * Tests if a specific core utility works correctly.
     *
     * **Supported Utilities**:
     * - bash: `bash --version`
     * - ls: `ls /`
     * - pwd: `pwd`
     * - cat: `cat /dev/null`
     * - grep: `grep --version`
     * - echo: `echo "test"`
     *
     * **Performance**: Fast (~50-100ms per utility)
     *
     * **Use Cases**:
     * - Test specific utility after troubleshooting
     * - Validate before using utility in app
     * - Granular validation for debugging
     *
     * @param bootstrapDir Bootstrap installation directory
     * @param utilityName Name of utility to test (e.g., "ls", "cat", "grep")
     *
     * @return true if utility is functional, false otherwise
     *
     * @throws IllegalArgumentException if utilityName is not supported
     */
    suspend fun validateUtility(bootstrapDir: File, utilityName: String): Boolean

    /**
     * Detect bash version.
     *
     * Executes `bash --version` and parses version string.
     *
     * **Version Format**: "GNU bash, version X.Y.Z..."
     *
     * **Performance**: Fast (~100ms)
     *
     * **Use Cases**:
     * - Display bash version in app settings
     * - Log version for troubleshooting
     * - Check version compatibility
     *
     * @param bootstrapDir Bootstrap installation directory
     *
     * @return Bash version string (e.g., "5.1.16") or null if cannot detect
     */
    suspend fun detectBashVersion(bootstrapDir: File): String?

    /**
     * Verify directory structure.
     *
     * Checks that critical directories exist:
     * - bin/ (binaries)
     * - lib/ (libraries)
     * - share/ (shared data)
     * - include/ (header files)
     * - tmp/ (temporary files)
     *
     * **Performance**: Fast (~10-20ms), file existence checks only
     *
     * **Use Cases**:
     * - Verify extraction completed successfully
     * - Check before running commands
     * - Troubleshooting missing directories
     *
     * @param bootstrapDir Bootstrap installation directory
     *
     * @return true if all required directories exist, false otherwise
     */
    suspend fun verifyDirectoryStructure(bootstrapDir: File): Boolean

    /**
     * Check file permissions on binaries.
     *
     * Verifies that bash and core utilities have execute permissions.
     *
     * **Files Checked**:
     * - bin/bash
     * - bin/ls
     * - bin/cat
     * - bin/grep
     * - bin/echo
     * - bin/pwd
     *
     * **Performance**: Fast (~20-30ms), permission checks only
     *
     * **Use Cases**:
     * - Verify permissions set correctly after extraction
     * - Troubleshoot "permission denied" errors
     * - Validate before running commands
     *
     * @param bootstrapDir Bootstrap installation directory
     *
     * @return Map of file paths to permission status (true if executable)
     *         Example: {"bin/bash" to true, "bin/ls" to true}
     */
    suspend fun checkBinaryPermissions(bootstrapDir: File): Map<String, Boolean>

    /**
     * Execute test command in bootstrap environment.
     *
     * Runs a custom command in the bootstrap environment and returns output.
     * Useful for testing and debugging.
     *
     * **Environment**:
     * - PREFIX set to bootstrapDir
     * - PATH includes bin/
     * - HOME set to app files directory
     *
     * **Timeout**: 5 seconds
     *
     * **Security**: Only use with trusted commands (no user input)
     *
     * **Performance**: Varies by command
     *
     * **Use Cases**:
     * - Test custom commands during development
     * - Debug bootstrap issues
     * - Validate environment variables
     *
     * @param bootstrapDir Bootstrap installation directory
     * @param command Command to execute (e.g., "bash -c 'echo $PREFIX'")
     *
     * @return Command output as string, or null if command failed
     *
     * @throws SecurityException if command is deemed unsafe
     */
    suspend fun executeTestCommand(bootstrapDir: File, command: String): String?

    /**
     * Get bash executable path.
     *
     * Returns absolute path to bash binary.
     *
     * **Path Format**: `{bootstrapDir}/bin/bash`
     *
     * **Performance**: Instant (~1ms)
     *
     * @param bootstrapDir Bootstrap installation directory
     *
     * @return Absolute path to bash executable
     */
    fun getBashPath(bootstrapDir: File): String

    /**
     * Get required environment variables for bootstrap.
     *
     * Returns map of environment variables needed to use bootstrap.
     *
     * **Required Variables**:
     * - PREFIX: Bootstrap installation directory
     * - PATH: Includes bootstrap bin/
     * - HOME: User home directory
     * - TMPDIR: Temporary files directory
     * - SHELL: Path to bash
     * - LANG: Locale setting
     *
     * **Performance**: Instant (~1ms), computed values
     *
     * **Use Cases**:
     * - Configure environment for command execution
     * - Display environment in settings
     * - Troubleshoot environment issues
     *
     * @param bootstrapDir Bootstrap installation directory
     *
     * @return Map of environment variable names to values
     *         Example: {"PREFIX" to "/data/.../usr", "PATH" to "/data/.../usr/bin", ...}
     */
    fun getRequiredEnvironment(bootstrapDir: File): Map<String, String>

    /**
     * Validate environment variables are configured correctly.
     *
     * Checks that current process environment has correct variables
     * for bootstrap usage.
     *
     * **Performance**: Fast (~10ms)
     *
     * **Use Cases**:
     * - Verify environment after configuration
     * - Troubleshoot environment issues
     * - Health check
     *
     * @param bootstrapDir Bootstrap installation directory
     *
     * @return true if environment is correctly configured, false otherwise
     */
    suspend fun validateEnvironment(bootstrapDir: File): Boolean

    /**
     * Count files in bootstrap installation.
     *
     * Recursively counts all files in bootstrap directory.
     * Used to verify extraction completeness.
     *
     * **Performance**: Slow (~1-2s), walks entire directory tree
     *
     * **Expected Count**: ~1000-1500 files (varies by bootstrap version)
     *
     * **Use Cases**:
     * - Verify extraction completed
     * - Compare with expected file count
     * - Troubleshoot incomplete extraction
     *
     * @param bootstrapDir Bootstrap installation directory
     *
     * @return Total number of files in bootstrap directory
     */
    suspend fun countFiles(bootstrapDir: File): Int

    /**
     * Get bootstrap installation size in bytes.
     *
     * Recursively calculates total size of bootstrap directory.
     *
     * **Performance**: Slow (~1-2s), walks entire directory tree
     *
     * **Expected Size**: ~150MB (varies by bootstrap version)
     *
     * **Use Cases**:
     * - Display storage usage in settings
     * - Verify extraction size matches expected
     * - Monitor storage consumption
     *
     * @param bootstrapDir Bootstrap installation directory
     *
     * @return Total size in bytes
     */
    suspend fun getInstallationSize(bootstrapDir: File): Long
}
