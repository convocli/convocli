# Feature: Termux Bootstrap Installation

**Feature ID**: 003
**Status**: Specification
**Created**: 2025-10-22
**Last Updated**: 2025-10-22

---

## Overview

Enable Linux command execution in ConvoCLI by installing the Termux bootstrap system. The bootstrap provides a complete Linux environment including bash, core utilities, package management tools, and access to 1000+ Linux packages. This transforms ConvoCLI from a terminal emulator with infrastructure-only capabilities into a fully functional Linux terminal on Android.

### Problem Statement

Feature 002 successfully implemented terminal infrastructure (PTY interface, session management, UI bindings), but the terminal cannot execute any commands because no bash executable or Linux utilities exist. Users see a terminal interface but cannot perform any terminal operations. This blocks all planned UI features and prevents the application from achieving MVP status.

### Solution

Install the Termux bootstrap system (~70MB archive, ~150MB extracted) to the application's private storage directory. The bootstrap contains bash, coreutils (ls, cat, grep, etc.), apt/dpkg for package management, and the complete Linux filesystem structure required for command execution on Android.

---

## User Scenarios

### Scenario 1: First-Time App Launch

**Actor**: New user launching ConvoCLI for the first time

**Goal**: Have a working Linux terminal environment ready to use

**Preconditions**:
- App installed on Android device
- Sufficient storage space available (~200MB minimum)
- Internet connection available

**Flow**:
1. User launches ConvoCLI for the first time
2. App detects no bootstrap installation exists
3. App displays bootstrap installation screen with progress indicators
4. App downloads bootstrap archive from Termux repository
5. App extracts bootstrap to app's files directory
6. App configures file permissions and environment variables
7. App verifies bash executable works
8. App shows completion message and transitions to terminal interface
9. User can immediately execute Linux commands (ls, pwd, echo, etc.)

**Success Outcomes**:
- Bootstrap installed successfully within 2 minutes on typical connection
- User sees clear progress throughout installation
- Terminal ready to use immediately after installation
- All basic commands (ls, cat, grep, bash) work correctly

**Edge Cases**:
- Network interruption during download → Resume capability or clear retry option
- Insufficient storage space → Clear error message with required space info
- Extraction failure → Rollback and retry option with error details
- Permission issues → Clear explanation and potential resolution steps

---

### Scenario 2: Bootstrap Installation Failure Recovery

**Actor**: User experiencing bootstrap installation failure

**Goal**: Successfully install bootstrap after initial failure

**Preconditions**:
- Previous bootstrap installation attempt failed
- Error state saved in app

**Flow**:
1. User relaunches app after failed installation
2. App detects incomplete/failed bootstrap state
3. App displays diagnostic information about the failure
4. App offers retry option with clear action button
5. User taps retry button
6. App cleans up partial installation
7. App attempts fresh installation
8. Installation completes successfully or provides actionable error info

**Success Outcomes**:
- User understands why installation failed
- User can retry without reinstalling app
- Partial installation does not corrupt app state
- Successful retry results in working terminal

---

### Scenario 3: Existing Integration Tests Execution

**Actor**: Developer running integration test suite

**Goal**: All integration tests execute with real bash commands instead of infrastructure-only tests

**Preconditions**:
- Bootstrap successfully installed
- Integration test suite exists (from Feature 002)

**Flow**:
1. Developer runs integration test suite
2. Tests create terminal sessions
3. Tests execute real bash commands (ls, pwd, echo, cat, etc.)
4. Tests verify command output matches expectations
5. Tests confirm error handling works correctly
6. All tests pass with real command execution

**Success Outcomes**:
- All existing integration tests pass with real commands
- No test failures due to missing executables
- Command execution times are reasonable (<1 second for basic commands)
- Error handling works as specified in Feature 002

---

## Functional Requirements

### FR-1: Bootstrap Detection and Validation

The system must detect whether the Termux bootstrap is already installed and validate its integrity.

**Acceptance Criteria**:
- System checks for bash executable at expected path on app launch
- System validates bootstrap directory structure exists
- System verifies core utilities (ls, cat, grep, pwd) are present
- System confirms file permissions are correct (executable binaries)
- If validation fails, system treats bootstrap as not installed
- Validation completes in under 1 second

### FR-2: Bootstrap Download

The system must download the Termux bootstrap archive from the official repository.

**Acceptance Criteria**:
- System downloads bootstrap for device's CPU architecture (arm64-v8a, armeabi-v7a, x86, x86_64)
- System displays download progress (percentage, MB downloaded, estimated time remaining)
- System verifies download integrity using checksums
- System handles network interruptions gracefully
- System supports download resume if interrupted
- System provides clear error messages for download failures (network error, server unavailable, insufficient storage)
- System stores partial downloads in temporary location
- Download completes within 5 minutes on typical 3G connection for 70MB archive

### FR-3: Bootstrap Extraction

The system must extract the downloaded bootstrap archive to the application's private storage.

**Acceptance Criteria**:
- System extracts bootstrap archive to app's files directory (`/data/data/com.convocli/files/`)
- System creates proper directory structure (usr/bin, usr/lib, usr/share, etc.)
- System displays extraction progress (files extracted, percentage complete)
- System preserves symbolic links during extraction
- System handles extraction errors gracefully (corrupted archive, insufficient space, permission issues)
- System performs atomic extraction (temporary directory, then rename on success)
- Extraction completes within 3 minutes for typical archive size
- System cleans up temporary files and partial extractions on failure

### FR-4: Permission Configuration

The system must set appropriate file permissions on extracted bootstrap files.

**Acceptance Criteria**:
- System sets execute permissions on all binaries in usr/bin/
- System sets execute permissions on bash shell
- System verifies permissions are applied correctly
- System handles permission setting errors with clear messages
- Permission configuration completes in under 30 seconds

### FR-5: Environment Configuration

The system must configure environment variables required for bash and Linux utilities to function.

**Acceptance Criteria**:
- System sets PREFIX environment variable to bootstrap root path
- System sets PATH to include usr/bin directory
- System sets HOME to app's files directory
- System configures TMPDIR to appropriate temporary directory
- System sets SHELL to bash executable path
- Environment configuration persists across app restarts
- System validates environment configuration on each app launch

### FR-6: Installation Verification

The system must verify that the bootstrap installation is functional before allowing terminal use.

**Acceptance Criteria**:
- System executes test command (bash --version) to verify bash works
- System tests basic utilities (ls, pwd, echo) execute successfully
- System verifies package manager (apt, dpkg) is present
- Verification completes in under 5 seconds
- System provides diagnostic information if verification fails
- Failed verification prevents access to terminal interface

### FR-7: Progress Indication

The system must provide clear visual feedback during all bootstrap installation phases.

**Acceptance Criteria**:
- System displays progress screen during installation
- Progress screen shows current phase (downloading, extracting, configuring, verifying)
- Progress screen shows percentage complete for current phase
- Progress screen shows estimated time remaining
- Progress updates at least once per second during active operations
- System shows success/failure message at completion
- User can see progress without blocking app (no frozen interface)

### FR-8: Error Handling and Recovery

The system must handle bootstrap installation errors gracefully and provide recovery options.

**Acceptance Criteria**:
- System detects and reports specific error types (network, storage, permissions, corruption)
- System provides actionable error messages (not generic failures)
- System offers retry option for transient errors (network, temporary issues)
- System cleans up partial installations before retry
- System prevents app corruption from failed installations
- System logs detailed error information for debugging
- System allows manual bootstrap deletion to force fresh install

### FR-9: Storage Management

The system must manage storage requirements and handle storage-related constraints.

**Acceptance Criteria**:
- System checks available storage before starting download (require 200MB minimum)
- System displays storage requirement information to user before installation
- System cleans up temporary files after successful installation
- System provides option to delete bootstrap if storage space needed
- System warns user if storage space is critically low (under 100MB remaining after installation)

### FR-10: Integration Test Compatibility

The system must enable existing integration tests to execute with real commands.

**Acceptance Criteria**:
- All integration tests from Feature 002 execute successfully with bootstrap installed
- Tests can create terminal sessions that execute real bash commands
- Command output from tests matches expected results
- Error handling in tests works correctly with real command execution
- Test execution time is reasonable (under 10 seconds for full suite)
- No test code changes required (tests work with both infrastructure-only and bootstrap-installed modes)

### FR-11: Installation Cancellation

The system must allow users to cancel bootstrap installation at any point and clean up partial installations.

**Acceptance Criteria**:
- System provides visible cancel button during all installation phases (download, extraction, configuration)
- Cancel button is always responsive (UI not blocked during installation)
- Cancellation stops current operation within 2 seconds
- System removes partial downloads after cancellation
- System removes partial extractions after cancellation
- System resets installation state to "not installed" after cancellation
- User can restart installation after cancellation without app restart
- Cancellation does not corrupt app state or leave orphaned files
- System logs cancellation events for debugging
- Cancel confirmation dialog prevents accidental cancellation (optional, UX decision)

---

## Success Criteria

### SC-1: Installation Completion Rate
**Target**: 95% of installation attempts succeed on first try

**Measurement**: Track installation success/failure ratio across all users

**Rationale**: Installation should be highly reliable to ensure good first-run experience

### SC-2: Installation Time
**Target**: Bootstrap installation completes within 5 minutes on typical 3G/4G connection

**Measurement**: Track time from installation start to completion (95th percentile)

**Rationale**: Users should not wait excessively for initial setup

### SC-3: Command Execution Functionality
**Target**: 100% of basic Linux commands work correctly after installation

**Measurement**: Verify bash, ls, cat, grep, pwd, echo, cd execute successfully

**Rationale**: Core functionality must work reliably

### SC-4: Integration Test Success Rate
**Target**: 100% of existing integration tests pass with bootstrap installed

**Measurement**: Run Feature 002 integration test suite with real commands

**Rationale**: Bootstrap must not break existing functionality

### SC-5: Storage Efficiency
**Target**: Extracted bootstrap size does not exceed 200MB

**Measurement**: Measure disk space used after installation

**Rationale**: Mobile devices have limited storage; minimize footprint

### SC-6: Error Recovery Rate
**Target**: 90% of failed installations succeed on retry

**Measurement**: Track retry success rate for initial failures

**Rationale**: Transient failures (network issues) should be recoverable

### SC-7: User Clarity
**Target**: Users understand installation progress and can identify issues

**Measurement**: Qualitative assessment - progress indicators clear, error messages actionable

**Rationale**: Good UX requires clear communication during critical operations

---

## Key Entities

### Bootstrap Archive

**Description**: Compressed archive file containing complete Termux Linux environment

**Attributes**:
- Architecture variant (arm64-v8a, armeabi-v7a, x86, x86_64)
- Version identifier
- File size (~70MB compressed)
- Checksum for integrity verification
- Download URL

**Relationships**:
- Downloaded from Termux repository
- Extracted to bootstrap installation directory

### Bootstrap Installation

**Description**: Installed Termux environment in app's private storage

**Attributes**:
- Installation path (/data/data/com.convocli/files/)
- Installation status (not installed, installing, installed, failed)
- Installation timestamp
- Version identifier
- Integrity status (valid, corrupted, incomplete)

**Relationships**:
- Contains bash executable
- Contains Linux utilities
- Contains package management tools
- Required by terminal sessions

### Installation Progress

**Description**: Current state of bootstrap installation process

**Attributes**:
- Current phase (downloading, extracting, configuring, verifying)
- Phase progress percentage
- Bytes downloaded/extracted
- Estimated time remaining
- Error information (if any)

**Relationships**:
- Displayed to user during installation
- Persisted across app restarts for resumability

---

## Assumptions

### Technical Assumptions

1. **Termux Repository Availability**: The Termux bootstrap repository remains accessible and stable. Termux is an established open-source project with reliable infrastructure.

2. **Device Architecture Detection**: Android OS provides reliable CPU architecture information (Build.SUPPORTED_ABIS). This is standard Android API functionality.

3. **Storage Access**: App has access to private files directory (/data/data/com.convocli/files/) without requiring runtime permissions. This is guaranteed by Android for app-private storage.

4. **Network Connectivity**: Device has internet access for initial bootstrap download. Offline installation is out of scope for initial implementation.

5. **Archive Format Compatibility**: Bootstrap archive uses standard tar.xz compression format compatible with Android. This is the format Termux uses officially.

### Business Assumptions

6. **User Storage Availability**: Users have at least 200MB free storage when installing ConvoCLI. This is reasonable for a terminal emulator application.

7. **Installation Timing**: Users are willing to wait up to 5 minutes for first-run setup. This is standard for applications with initial setup requirements.

8. **Feature Priority**: Bootstrap installation is the highest priority (critical path) for achieving MVP. Confirmed by project roadmap and quality analysis.

9. **User Technical Proficiency**: Users understand that a terminal application requires downloading Linux utilities. Target audience is developers and technical users.

10. **Error Recovery Expectations**: Users will retry failed installations rather than immediately uninstalling app. Reasonable for technical user base.

### Dependency Assumptions

11. **Feature 002 Completeness**: Terminal infrastructure from Feature 002 is fully functional and tested. Confirmed by quality analysis (Architecture: 100/100).

12. **Android API Compatibility**: Target devices run Android API 26+ (Android 8.0 Oreo) or higher. This is the minSdk specified in build configuration.

13. **Termux Compatibility**: Termux bootstrap is compatible with the terminal emulator implementation from Feature 002. Both use standard PTY interface.

### Design Assumptions

14. **Single Bootstrap Version**: App installs one bootstrap version; updates/upgrades are out of scope for initial implementation. Future feature to support updates.

15. **Automatic Installation**: Bootstrap installs automatically on first launch without requiring user initiation. Users expect terminal to "just work."

16. **No Customization**: Bootstrap installation uses default configuration without user customization options. Simplifies initial implementation.

17. **English Language UI**: Installation progress and error messages are in English. Internationalization is future enhancement.

---

## Non-Functional Considerations

### Performance

- **Download Performance**: Download should utilize available bandwidth efficiently without blocking UI
- **Extraction Performance**: Archive extraction should use efficient algorithms (streaming extraction vs. loading entire archive into memory)
- **Startup Performance**: Bootstrap validation checks should add minimal overhead to app startup (<1 second)

### Reliability

- **Download Reliability**: Implement retry logic for network failures, support resumable downloads
- **Installation Atomicity**: Installation should be all-or-nothing (complete success or complete failure, no partial state)
- **Data Integrity**: Verify archive checksums to prevent corrupted installations

### Usability

- **Progress Clarity**: Users should always know current installation status and estimated completion time
- **Error Clarity**: Error messages should explain what went wrong and what user can do about it
- **Cancellation**: Users can cancel installation at any point during download or extraction. The system performs automatic cleanup of partial downloads and extractions, allowing users to retry from scratch. Cancellation provides immediate response and prevents force-close scenarios that could leave the app in an inconsistent state.

### Compatibility

- **Android Version Support**: Must work on Android API 26+ (Android 8.0 Oreo through Android 14+)
- **Device Architecture Support**: Support all common architectures (arm64-v8a, armeabi-v7a, x86, x86_64)
- **Storage Type Support**: Work with both internal storage and SD card installations (if Android allows app installation to SD card)

### Security

- **Download Security**: Verify bootstrap archive authenticity using checksums
- **Storage Security**: Bootstrap stored in app's private directory, not accessible to other apps
- **Execution Security**: No root access required, runs within app's sandbox

---

## Dependencies

### External Dependencies

1. **Termux Bootstrap Repository**: Requires access to https://termux.dev or alternate mirror for bootstrap downloads
2. **Network Connectivity**: Internet connection required for bootstrap download
3. **Device Storage**: Minimum 200MB free space required

### Internal Dependencies

1. **Feature 002 - Terminal Emulator Core**: Bootstrap installation requires functional PTY interface and session management
2. **Android Build Configuration**: Requires build.gradle.kts configured with proper NDK settings for multiple architectures
3. **Dependency Injection Setup**: Requires Hilt configuration for injecting installation services

---

## Out of Scope

The following items are explicitly **not included** in this feature:

1. **Bootstrap Updates**: Updating already-installed bootstrap to newer version (future Feature 004 or 007)
2. **Custom Package Installation**: Installing additional packages via apt/dpkg (covered by Feature 004 - Package Management)
3. **Bootstrap Configuration Customization**: Allowing users to customize bootstrap installation options
4. **Offline Installation**: Installing bootstrap from pre-bundled archive in APK (would increase APK size by 70MB)
5. **Multiple Bootstrap Versions**: Supporting multiple bootstrap versions simultaneously
6. **Bootstrap Backup/Restore**: Backing up and restoring bootstrap installations
7. **Bootstrap Migration**: Migrating bootstrap from old app version to new version (if installation path changes)
8. **Internationalization**: Non-English progress messages and error text (future enhancement)
9. **Installation Analytics**: Detailed telemetry about installation failures (future enhancement if needed)
10. **Installation Customization**: Allowing users to choose installation location (always uses app's private files directory)

---

## Risk Assessment

### High Risk

1. **Download Failures**: Network issues, server unavailability, or repository changes could prevent bootstrap downloads
   - **Mitigation**: Implement retry logic, support alternate mirrors, cache last-known-good download URL

2. **Storage Constraints**: Devices with low storage may fail installation or run out of space during extraction
   - **Mitigation**: Check available storage before starting, provide clear storage requirement messaging

3. **Architecture Mismatches**: Incorrect architecture detection could lead to incompatible bootstrap installation
   - **Mitigation**: Use reliable Android APIs for architecture detection, verify architecture before installation

### Medium Risk

4. **Extraction Failures**: Corrupted downloads or filesystem issues could cause extraction to fail
   - **Mitigation**: Verify checksums before extraction, implement atomic installation (temp dir + rename)

5. **Permission Issues**: Android permission changes or restrictions could prevent file operations
   - **Mitigation**: Use app-private storage (no permissions required), handle errors gracefully

6. **Termux Repository Changes**: Termux project could change bootstrap URL structure or format
   - **Mitigation**: Configuration-based URLs, monitoring for repository changes, fallback mirrors

### Low Risk

7. **Bootstrap Compatibility**: Termux bootstrap might not be fully compatible with Feature 002 PTY implementation
   - **Mitigation**: Thorough integration testing, Feature 002 uses standard PTY interface compatible with Termux

8. **Performance Degradation**: Installation might be too slow on older devices or slow connections
   - **Mitigation**: Optimize extraction algorithms, provide accurate progress indicators, set realistic expectations

---

## Notes

### Implementation Priorities

The following implementation order is recommended:

1. **Phase 1**: Bootstrap detection and validation (enables all other phases)
2. **Phase 2**: Download functionality with progress indicators (core user-visible feature)
3. **Phase 3**: Extraction with error handling (complete the installation)
4. **Phase 4**: Permission and environment configuration (make it functional)
5. **Phase 5**: Installation verification (ensure quality)
6. **Phase 6**: Error recovery and retry mechanisms (handle edge cases)
7. **Phase 7**: Integration test updates (validate everything works)

### Quality Considerations

- **Test Coverage**: Aim for 80%+ unit test coverage for installation logic
- **Integration Testing**: All Feature 002 tests must pass with bootstrap installed
- **Manual Testing**: Test on multiple Android versions (API 26, 28, 30, 33, 34) and architectures
- **Error Scenario Testing**: Deliberately trigger failures (network errors, storage full, corrupted archives) to verify error handling

### Future Enhancements

Features that could be added in future iterations:

1. **Incremental Updates**: Update bootstrap without full reinstallation
2. **Download Resume**: Resume interrupted downloads from partial progress
3. **Bandwidth Control**: Allow users to limit download speed to conserve data
4. **Offline Mode**: Bundle bootstrap in APK for offline installation (accept larger APK size)
5. **Installation Customization**: Allow users to choose components to install (minimal vs. full)
6. **Installation Telemetry**: Collect analytics about installation success/failure rates
7. **Multi-Language Support**: Localized installation messages
8. **Installation Scheduling**: Allow users to defer installation to later time (install on next launch)

---

## Acceptance Criteria Summary

This feature is considered complete and ready for production when:

1. ✅ Bootstrap downloads successfully on all supported architectures
2. ✅ Bootstrap extracts correctly to app's private storage
3. ✅ Bash executable and core utilities function correctly
4. ✅ All Feature 002 integration tests pass with real command execution
5. ✅ Installation progress is clearly visible to users
6. ✅ Installation errors are handled gracefully with actionable messages
7. ✅ Installation completes within 5 minutes on typical connections
8. ✅ Failed installations can be retried successfully
9. ✅ Bootstrap validation confirms integrity on each app launch
10. ✅ Unit tests cover 80%+ of installation logic
11. ✅ Manual testing validates functionality on multiple Android versions and architectures
12. ✅ No regressions in Feature 002 functionality

---

**Feature Status**: Ready for Clarification & Planning
