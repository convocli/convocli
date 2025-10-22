package com.convocli.data.model

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
     * Extracting ZIP archive to filesystem
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
