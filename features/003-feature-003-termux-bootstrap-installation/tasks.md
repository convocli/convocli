# Implementation Tasks: Termux Bootstrap Installation

<!-- Tech Stack Validation: PASSED -->
<!-- Validated against: /memory/tech-stack.md v1.0.0 -->
<!-- No prohibited technologies found -->
<!-- All technologies already approved -->

**Feature ID**: 003
**Feature**: Termux Bootstrap Installation
**Created**: 2025-10-22
**Status**: Task Generation Complete
**Total Tasks**: 41
**Estimated Effort**: 12-16 hours (1-2 days)

---

## Overview

This document breaks down Feature 003 (Termux Bootstrap Installation) into executable tasks organized by user story priority. Each phase represents an independently testable increment that delivers value.

**Feature Goal**: Enable Linux command execution in ConvoCLI by installing the Termux bootstrap system, transforming the terminal from infrastructure-only to a fully functional Linux environment.

**Organization Strategy**:
- **Phase 1**: Setup - Project initialization and dependencies
- **Phase 2**: Foundational Infrastructure - Core services needed by ALL user stories
- **Phase 3**: User Story 1 (P1) - First-time installation flow
- **Phase 4**: User Story 2 (P2) - Failure recovery and retry
- **Phase 5**: User Story 3 (P3) - Integration test compatibility
- **Phase 6**: Polish & Cross-Cutting Concerns

---

## Task Summary

| Phase | Tasks | Description | Est. Time |
|-------|-------|-------------|-----------|
| Phase 1 | T001-T004 | Setup & Dependencies | 30 min |
| Phase 2 | T005-T016 | Foundational Infrastructure | 6-7 hours |
| Phase 3 | T017-T026 | US1: First-Time Installation | 4-5 hours |
| Phase 4 | T027-T032 | US2: Failure Recovery | 2-3 hours |
| Phase 5 | T033-T037 | US3: Integration Test Support | 1-2 hours |
| Phase 6 | T038-T041 | Polish & Documentation | 1-2 hours |

**Parallel Opportunities**: 18 tasks can run in parallel (marked [P])

---

## Phase 1: Setup & Dependencies

**Goal**: Initialize project structure and add required dependencies

**Blocking**: Must complete before Phase 2 can start

### T001: Add Gradle Dependencies [P]

**Story**: Setup
**File**: `app/build.gradle.kts`
**Estimated**: 5 minutes

**Description**:
Add required external dependencies for HTTP client and archive extraction.

**Implementation**:
```kotlin
dependencies {
    // HTTP Client
    implementation("io.ktor:ktor-client-android:2.3.5")
    implementation("io.ktor:ktor-client-core:2.3.5")

    // Archive Extraction: None required (using standard Java ZipInputStream)
}
```

**Acceptance**:
- Ktor Client dependencies added to app/build.gradle.kts
- Gradle sync succeeds
- No dependency conflicts

---

### T002: Add Network Permission [P]

**Story**: Setup
**File**: `app/src/main/AndroidManifest.xml`
**Estimated**: 2 minutes

**Description**:
Add INTERNET permission required for bootstrap downloads.

**Implementation**:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

**Acceptance**:
- INTERNET permission added to AndroidManifest.xml
- App can access network

---

### T003: Create Bootstrap Package Structure [P]

**Story**: Setup
**Files**:
- `app/src/main/kotlin/com/convocli/bootstrap/`
- `app/src/main/kotlin/com/convocli/data/model/bootstrap/`
- `app/src/test/kotlin/com/convocli/bootstrap/`
- `app/src/androidTest/kotlin/com/convocli/bootstrap/`

**Estimated**: 5 minutes

**Description**:
Create package structure for bootstrap installation code.

**Acceptance**:
- Package directories created
- Ready for code implementation

---

### T004: Complete Phase 0 Research Tasks

**Story**: Setup
**File**: `features/003-.../research.md`
**Estimated**: 8-12 hours

**Description**:
Complete all 5 research tasks (R0.1 through R0.5) identified in research.md:
- R0.1: Termux Bootstrap Repository Structure
- R0.2: Tar.xz Extraction in Android
- R0.3: Bootstrap Integrity Verification
- R0.4: File Permissions on Android
- R0.5: Download Resume Strategy

**NOTE**: This task MUST complete before Phase 2 begins. Update research.md with findings and technical decisions.

**Acceptance**:
- All 5 research tasks marked 🟢 COMPLETE in research.md
- Technical decisions documented
- Implementation blockers resolved
- URLs, checksum algorithms, and extraction strategies confirmed

---

**Phase 1 Checkpoint**: ✅ All dependencies added, structure created, research complete

---

## Phase 2: Foundational Infrastructure

**Goal**: Implement core services and data models required by ALL user stories

**Blocking**: These components are prerequisites for implementing any user story

**Dependencies**: Phase 1 must complete first

### T005: Implement Data Models [P]

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/data/model/bootstrap/`
**Estimated**: 1 hour

**Description**:
Implement all data model classes defined in data-model.md.

**Implementation**:
Create the following entity classes:
- `BootstrapInstallation.kt` - Installation state
- `InstallationStatus.kt` - Status enum
- `InstallationProgress.kt` - Progress tracking
- `InstallationPhase.kt` - Phase enum
- `BootstrapError.kt` - Sealed error class
- `DownloadProgress.kt` - Download metrics
- `ExtractionProgress.kt` - Extraction metrics
- `ValidationResult.kt` - Validation outcome
- `BootstrapConfiguration.kt` - Config params
- `Extensions.kt` - Utility extensions

**Acceptance**:
- All entity classes implemented
- Immutable data classes (val properties)
- Kotlinx Serialization annotations added
- Sealed classes for type safety
- Extension functions for formatting
- Compiles without errors

---

### T006: Implement BootstrapManager Interface [P]

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/bootstrap/BootstrapManager.kt`
**Estimated**: 15 minutes

**Description**:
Create BootstrapManager interface as defined in contracts/BootstrapManager.kt.

**Implementation**:
Copy interface from contracts/ directory and adjust package structure.

**Acceptance**:
- Interface created with all methods
- KDoc documentation included
- Compiles without errors

---

### T007: Implement BootstrapDownloader Interface [P]

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/bootstrap/BootstrapDownloader.kt`
**Estimated**: 15 minutes

**Description**:
Create BootstrapDownloader interface as defined in contracts/BootstrapDownloader.kt.

**Acceptance**:
- Interface created with all methods
- KDoc documentation included
- Compiles without errors

---

### T008: Implement BootstrapExtractor Interface [P]

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/bootstrap/BootstrapExtractor.kt`
**Estimated**: 15 minutes

**Description**:
Create BootstrapExtractor interface as defined in contracts/BootstrapExtractor.kt.

**Acceptance**:
- Interface created with all methods
- KDoc documentation included
- Compiles without errors

---

### T009: Implement BootstrapValidator Interface [P]

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/bootstrap/BootstrapValidator.kt`
**Estimated**: 15 minutes

**Description**:
Create BootstrapValidator interface as defined in contracts/BootstrapValidator.kt.

**Acceptance**:
- Interface created with all methods
- KDoc documentation included
- Compiles without errors

---

### T010: Implement BootstrapDownloaderImpl

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/BootstrapDownloaderImpl.kt`
**Estimated**: 2.5 hours

**Description**:
Implement BootstrapDownloader using Ktor Client for HTTP downloads.

**Implementation**:
- Use Ktor Client Android for HTTP requests
- Implement download() with Flow<DownloadProgress>
- Add checksum verification (SHA-256)
- Implement resume capability (if supported)
- Calculate download speed and ETA
- Handle network errors with retry logic (3 attempts max)

**Dependencies**:
- T001 (Gradle dependencies)
- T004 (Research - URL patterns)
- T007 (Interface)

**Acceptance**:
- Downloads bootstrap archive for device architecture
- Emits progress updates via Flow
- Verifies checksum after download
- Supports resume (if server supports range requests)
- Handles network errors gracefully
- Unit tests pass (fake HTTP client)

---

### T011: Implement BootstrapExtractorImpl

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/BootstrapExtractorImpl.kt`
**Estimated**: 2 hours

**Description**:
Implement BootstrapExtractor using standard Java ZipInputStream for ZIP extraction.

**Implementation**:
- Use java.util.zip.ZipInputStream for archive extraction
- Implement extract() with Flow<ExtractionProgress>
- Preserve symlinks via SYMLINKS.txt file (read and create symbolic links)
- Atomic extraction (temp directory → rename)
- Calculate file count and extraction progress
- Handle extraction errors

**Dependencies**:
- T001 (Gradle dependencies - none needed for ZIP)
- T004 (Research - extraction approach)
- T008 (Interface)

**Acceptance**:
- Extracts ZIP archives to destination directory
- Emits progress updates via Flow
- Preserves symlinks via SYMLINKS.txt and directory structure
- Atomic extraction (all-or-nothing)
- Handles corrupted archives gracefully
- Unit tests pass (test archive)

---

### T012: Implement BootstrapValidatorImpl

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/BootstrapValidatorImpl.kt`
**Estimated**: 1.5 hours

**Description**:
Implement BootstrapValidator to verify bootstrap installation functionality.

**Implementation**:
- Execute bash --version to verify bash works
- Test core utilities (ls, pwd, cat, grep, echo)
- Verify directory structure exists
- Check binary permissions
- Return ValidationResult (Success or Failure)

**Dependencies**:
- T004 (Research - command execution approach)
- T009 (Interface)

**Acceptance**:
- Validates bash executable works
- Tests core utilities successfully
- Verifies directory structure
- Returns detailed ValidationResult
- Integration tests pass (requires bootstrap or mock)

---

### T013: Implement BootstrapManagerImpl

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/BootstrapManagerImpl.kt`
**Estimated**: 2 hours

**Description**:
Implement BootstrapManager to orchestrate complete installation flow.

**Implementation**:
- Implement state machine for installation phases
- Orchestrate download → extract → configure → verify
- Emit InstallationProgress updates via Flow
- Handle cancellation and cleanup
- Persist installation status to DataStore
- Detect device architecture (Build.SUPPORTED_ABIS)

**Dependencies**:
- T006 (Interface)
- T010 (Downloader)
- T011 (Extractor)
- T012 (Validator)

**Acceptance**:
- Orchestrates complete installation flow
- Emits progress for all phases
- Handles cancellation gracefully
- Persists installation status
- Cleans up partial installations
- Unit tests pass (fake services)

---

### T014: Implement File Permission Configuration

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/PermissionConfigurator.kt`
**Estimated**: 1 hour

**Description**:
Implement logic to set execute permissions on bootstrap binaries.

**Implementation**:
- Use File.setExecutable() on binaries
- Verify permissions with File.canExecute()
- Handle permission errors
- Set permissions on bash and core utilities

**Dependencies**:
- T004 (Research - permission approach)

**Acceptance**:
- Sets execute permission on all binaries
- Verifies permissions applied correctly
- Handles errors gracefully
- Integration tests pass (Android device)

---

### T015: Implement Environment Configuration

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/EnvironmentConfigurator.kt`
**Estimated**: 1 hour

**Description**:
Implement logic to configure environment variables for bootstrap.

**Implementation**:
- Set PREFIX, PATH, HOME, TMPDIR, SHELL
- Persist configuration to DataStore
- Validate environment on app launch
- Return environment map for command execution

**Dependencies**:
- T004 (Research - environment variables)

**Acceptance**:
- Sets all required environment variables
- Persists configuration
- Validates environment correctly
- Returns environment map
- Unit tests pass

---

### T016: Add Hilt Dependency Injection Modules

**Story**: Foundational
**File**: `app/src/main/kotlin/com/convocli/di/BootstrapModule.kt`
**Estimated**: 30 minutes

**Description**:
Create Hilt modules for bootstrap dependency injection.

**Implementation**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object BootstrapModule {
    @Provides
    @Singleton
    fun provideBootstrapManager(...): BootstrapManager = BootstrapManagerImpl(...)

    @Provides
    @Singleton
    fun provideBootstrapDownloader(...): BootstrapDownloader = BootstrapDownloaderImpl(...)

    // ... other providers
}
```

**Dependencies**:
- T013 (BootstrapManagerImpl)

**Acceptance**:
- Hilt module created
- All services provided
- Singleton scoping applied
- Compiles without errors

---

**Phase 2 Checkpoint**: ✅ All foundational services implemented and ready for use

---

## Phase 3: User Story 1 (P1) - First-Time Installation Flow

**Goal**: Implement complete first-time installation experience

**User Story**: "As a new user launching ConvoCLI for the first time, I want the bootstrap to install automatically so I can immediately use Linux commands."

**Success Criteria**:
- Bootstrap installs successfully on first launch
- User sees clear progress indicators
- Installation completes within 5 minutes
- Terminal immediately ready after installation

**Dependencies**: Phase 2 must complete first

### T017: Implement InstallationViewModel

**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/InstallationViewModel.kt`
**Estimated**: 2 hours

**Description**:
Implement ViewModel with MVI pattern for installation screen.

**Implementation**:
- Create ViewModel with @HiltViewModel
- Implement state: InstallationUiState (using StateFlow)
- Implement actions: install(), cancel(), retry()
- Map InstallationProgress → InstallationUiState
- Handle errors and retry logic
- Add cancellation support

**Dependencies**:
- T013 (BootstrapManager)
- T016 (Hilt module)

**Acceptance**:
- ViewModel created with Hilt injection
- MVI pattern implemented correctly
- State updates reactively
- Actions trigger installation
- Error handling works
- Unit tests pass (fake BootstrapManager)

---

### T018: Implement InstallationScreen UI [P]

**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/ui/screens/InstallationScreen.kt`
**Estimated**: 2.5 hours

**Description**:
Implement Compose UI for installation screen with progress indicators.

**Implementation**:
- Create Compose screen
- Progress indicators: phase, percentage, ETA
- Cancel button (always responsive)
- Error message display
- Success/failure states
- Material 3 components

**Dependencies**:
- T017 (ViewModel)

**Acceptance**:
- Installation screen displays correctly
- Progress indicators update smoothly
- Cancel button is responsive
- Error messages display clearly
- Success state transitions to terminal
- Compose UI tests pass

---

### T019: Add Installation Check on App Launch

**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/ConvoCLIApplication.kt`
**Estimated**: 30 minutes

**Description**:
Add logic to check bootstrap installation status on app launch.

**Implementation**:
- Check if bootstrap installed in Application.onCreate()
- Navigate to InstallationScreen if not installed
- Navigate to Terminal if installed
- Handle validation failures

**Dependencies**:
- T013 (BootstrapManager)

**Acceptance**:
- Checks installation on app launch
- Navigates to correct screen
- Does not block UI thread
- Logs installation status

---

### T020: Implement Architecture Detection

**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/ArchitectureDetector.kt`
**Estimated**: 30 minutes

**Description**:
Implement device architecture detection for bootstrap downloads.

**Implementation**:
- Read Build.SUPPORTED_ABIS[0]
- Map to Termux architecture:
  - arm64-v8a → aarch64
  - armeabi-v7a → arm
  - x86_64 → x86_64
  - x86 → i686
- Throw exception for unsupported architectures

**Dependencies**:
- T004 (Research - architecture mapping)

**Acceptance**:
- Detects device architecture correctly
- Maps to Termux architecture names
- Handles unsupported architectures
- Unit tests pass

---

### T021: Implement Download Progress Formatting

**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/ui/utils/ProgressFormatters.kt`
**Estimated**: 30 minutes

**Description**:
Implement utility functions to format progress data for UI display.

**Implementation**:
- Format bytes to human-readable (MB, GB)
- Format duration to human-readable (2m 30s)
- Format speed (KB/s, MB/s)
- Calculate percentage

**Acceptance**:
- Formatting functions work correctly
- Human-readable output
- Unit tests pass

---

### T022: Implement Storage Space Check

**Story**: US1 (FR-9)
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/StorageChecker.kt`
**Estimated**: 30 minutes

**Description**:
Implement logic to check available storage before installation.

**Implementation**:
- Check available space in app files directory
- Require minimum 200MB free
- Emit InsufficientStorageError if not enough space
- Display storage requirement to user

**Dependencies**:
- T005 (Data models)

**Acceptance**:
- Checks storage before installation
- Emits error if insufficient
- Displays storage requirement
- Unit tests pass

---

### T023: Implement Navigation to Installation Screen

**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/ui/navigation/NavGraph.kt`
**Estimated**: 20 minutes

**Description**:
Add navigation route for installation screen.

**Implementation**:
- Add installationScreen route
- Add navigation from splash to installation
- Add navigation from installation to terminal

**Dependencies**:
- T018 (Installation screen)

**Acceptance**:
- Navigation routes added
- Navigation works correctly
- Deep linking supported (if needed)

---

### T024: Implement First Launch Detection

**Story**: US1
**File**: `app/src/main/kotlin/com/convocli/data/repository/FirstLaunchRepository.kt`
**Estimated**: 20 minutes

**Description**:
Implement logic to detect first app launch.

**Implementation**:
- Store first launch flag in DataStore
- Check flag on app launch
- Set flag to false after first launch

**Acceptance**:
- Detects first launch correctly
- Persists flag
- Unit tests pass

---

### T025: Unit Tests for US1 Components [P]

**Story**: US1
**Files**: `app/src/test/kotlin/com/convocli/`
**Estimated**: 1 hour

**Description**:
Write unit tests for User Story 1 components.

**Tests**:
- InstallationViewModel state transitions
- Progress formatting utilities
- Storage check logic
- Architecture detection
- First launch detection

**Acceptance**:
- All unit tests pass
- Code coverage >80%
- Tests use fakes, not mocks

---

### T026: Integration Tests for US1 Flow [P]

**Story**: US1
**Files**: `app/src/androidTest/kotlin/com/convocli/`
**Estimated**: 1 hour

**Description**:
Write integration tests for complete first-time installation flow.

**Tests**:
- Complete installation flow (small test archive)
- Progress indicator updates
- Navigation after installation
- Bootstrap validation after installation

**Acceptance**:
- All integration tests pass
- Tests run on Android device/emulator
- Tests complete within reasonable time

---

**Phase 3 Checkpoint**: ✅ US1 complete - First-time installation flow works end-to-end

---

## Phase 4: User Story 2 (P2) - Failure Recovery

**Goal**: Implement error handling and retry mechanisms

**User Story**: "As a user experiencing installation failure, I want to retry installation so I can successfully install bootstrap without reinstalling the app."

**Success Criteria**:
- User understands why installation failed
- Retry option is clearly presented
- Retry cleans up partial installation
- 90% of retries succeed after transient failures

**Dependencies**: Phase 3 must complete first

### T027: Implement Error State UI

**Story**: US2 (FR-8)
**File**: `app/src/main/kotlin/com/convocli/ui/screens/InstallationScreen.kt`
**Estimated**: 1 hour

**Description**:
Enhance installation screen to display detailed error states.

**Implementation**:
- Display specific error messages (not generic)
- Show actionable error information
- Display retry button for recoverable errors
- Show troubleshooting tips for non-recoverable errors
- Different UI for different error types

**Dependencies**:
- T018 (Installation screen)

**Acceptance**:
- Error states display clearly
- Retry button appears for recoverable errors
- Error messages are user-friendly
- Compose UI tests pass

---

### T028: Implement Retry Logic in ViewModel

**Story**: US2
**File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/InstallationViewModel.kt`
**Estimated**: 1 hour

**Description**:
Add retry action to ViewModel with cleanup.

**Implementation**:
- Add retry() action
- Clean up partial installations before retry
- Reset state to initial
- Restart installation flow
- Track retry attempts

**Dependencies**:
- T017 (ViewModel)

**Acceptance**:
- Retry action implemented
- Cleans up before retry
- Resets state correctly
- Unit tests pass

---

### T029: Implement Partial Installation Cleanup

**Story**: US2 (FR-8)
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/InstallationCleaner.kt`
**Estimated**: 30 minutes

**Description**:
Implement logic to clean up partial installations.

**Implementation**:
- Delete partial download files
- Delete partial extraction directories
- Reset DataStore installation status
- Leave no orphaned files

**Acceptance**:
- Cleans up partial downloads
- Cleans up partial extractions
- Resets state correctly
- No orphaned files remain

---

### T030: Implement Detailed Error Logging

**Story**: US2 (FR-8)
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/InstallationLogger.kt`
**Estimated**: 30 minutes

**Description**:
Add detailed logging for troubleshooting installation failures.

**Implementation**:
- Log installation start/end
- Log each phase transition
- Log errors with stack traces
- Log device info (architecture, Android version, storage)
- Use Timber for logging

**Acceptance**:
- Logs installation events
- Logs errors with details
- Logs device info
- Helps troubleshooting

---

### T031: Unit Tests for Error Handling [P]

**Story**: US2
**Files**: `app/src/test/kotlin/com/convocli/`
**Estimated**: 1 hour

**Description**:
Write unit tests for error handling and retry logic.

**Tests**:
- Retry action resets state
- Cleanup removes partial files
- Error logging works correctly
- Different error types handled correctly

**Acceptance**:
- All unit tests pass
- Error scenarios covered
- Code coverage >80%

---

### T032: Integration Tests for Failure Recovery [P]

**Story**: US2
**Files**: `app/src/androidTest/kotlin/com/convocli/`
**Estimated**: 1 hour

**Description**:
Write integration tests for failure recovery scenarios.

**Tests**:
- Retry after network failure
- Retry after extraction failure
- Cleanup works correctly
- Error messages display correctly

**Acceptance**:
- All integration tests pass
- Failure scenarios covered
- Tests run on Android device/emulator

---

**Phase 4 Checkpoint**: ✅ US2 complete - Error recovery and retry works correctly

---

## Phase 5: User Story 3 (P3) - Integration Test Compatibility

**Goal**: Enable existing integration tests to execute with real commands

**User Story**: "As a developer, I want existing integration tests to pass with real command execution so I can verify terminal functionality works correctly."

**Success Criteria**:
- All Feature 002 integration tests pass with bootstrap installed
- Tests execute real bash commands
- Command output matches expectations
- Test execution time <10 seconds

**Dependencies**: Phase 3 must complete first (bootstrap must be installable)

### T033: Update Integration Test Helpers

**Story**: US3 (FR-10)
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/TerminalTestHelpers.kt`
**Estimated**: 30 minutes

**Description**:
Update test helpers to work with both infrastructure-only and bootstrap-installed modes.

**Implementation**:
- Add bootstrap detection in test setup
- Conditionally execute real commands if bootstrap installed
- Fall back to infrastructure-only tests if not installed
- Add helper to wait for bootstrap installation

**Acceptance**:
- Tests work with and without bootstrap
- Real commands execute when bootstrap available
- Test helpers backward compatible

---

### T034: Update Terminal Session Tests

**Story**: US3
**File**: `app/src/androidTest/kotlin/com/convocli/terminal/TerminalSessionTest.kt`
**Estimated**: 1 hour

**Description**:
Update terminal session tests to execute real commands.

**Implementation**:
- Replace infrastructure-only tests with real command tests
- Verify bash --version output
- Test ls, pwd, echo, cat, grep commands
- Verify command output matches expectations
- Test error handling with invalid commands

**Dependencies**:
- T033 (Test helpers)

**Acceptance**:
- Tests execute real commands
- Output verification works
- Error handling tested
- All tests pass

---

### T035: Add Bootstrap Installation Test Helper

**Story**: US3
**File**: `app/src/androidTest/kotlin/com/convocli/bootstrap/BootstrapTestHelper.kt`
**Estimated**: 30 minutes

**Description**:
Create test helper to ensure bootstrap installed before tests run.

**Implementation**:
- Check if bootstrap installed
- Install bootstrap if not present (using small test archive or real bootstrap)
- Wait for installation to complete
- Fail tests if installation fails

**Acceptance**:
- Helper installs bootstrap if needed
- Tests can rely on bootstrap being installed
- Helper reusable across test suites

---

### T036: Verify Test Execution Performance

**Story**: US3 (FR-10)
**File**: `app/src/androidTest/kotlin/com/convocli/`
**Estimated**: 30 minutes

**Description**:
Verify that test execution time is reasonable with real commands.

**Implementation**:
- Run full integration test suite
- Measure execution time
- Ensure <10 seconds total (per FR-10)
- Optimize slow tests if needed

**Acceptance**:
- Test suite completes <10 seconds
- No performance regressions
- Tests run reliably

---

### T037: Update Test Documentation

**Story**: US3
**File**: `features/003-.../test-strategy.md`
**Estimated**: 20 minutes

**Description**:
Document test strategy for bootstrap installation feature.

**Implementation**:
- Document unit test approach
- Document integration test approach
- Document test helpers usage
- Document how to run tests with/without bootstrap

**Acceptance**:
- Test documentation complete
- Clear instructions for running tests
- Helpful for future developers

---

**Phase 5 Checkpoint**: ✅ US3 complete - Integration tests work with real commands

---

## Phase 6: Polish & Cross-Cutting Concerns

**Goal**: Polish user experience and complete non-functional requirements

**Dependencies**: All user stories complete

### T038: Improve Error Messages

**Story**: Polish (FR-8)
**File**: `app/src/main/kotlin/com/convocli/bootstrap/ErrorMessages.kt`
**Estimated**: 1 hour

**Description**:
Polish error messages to be user-friendly and actionable.

**Implementation**:
- Review all error messages
- Make messages user-friendly (avoid technical jargon)
- Provide actionable guidance
- Add specific error codes for support
- Add troubleshooting tips

**Acceptance**:
- All error messages reviewed
- Messages are clear and helpful
- Error codes added
- Users know what to do when errors occur

---

### T039: Add Diagnostic Logging

**Story**: Polish (FR-8)
**File**: Throughout bootstrap package
**Estimated**: 30 minutes

**Description**:
Add comprehensive diagnostic logging for troubleshooting.

**Implementation**:
- Log all installation phases
- Log timing information
- Log device information
- Log error details
- Use appropriate log levels (DEBUG, INFO, ERROR)

**Acceptance**:
- Comprehensive logging added
- Logs help troubleshooting
- No sensitive information logged

---

### T040: Update Project Documentation

**Story**: Polish
**Files**:
- `CLAUDE.md`
- `features/003-.../quickstart.md`
- `features/003-.../troubleshooting.md`

**Estimated**: 1 hour

**Description**:
Update project documentation with bootstrap installation details.

**Implementation**:
- Update CLAUDE.md with bootstrap architecture
- Create quickstart.md for feature
- Document troubleshooting steps
- Add architecture diagrams
- Document API usage examples

**Acceptance**:
- Documentation complete
- Architecture clearly explained
- Troubleshooting guide helpful
- Examples are clear

---

### T041: Final End-to-End Testing

**Story**: Polish
**Devices**: Multiple Android devices/emulators
**Estimated**: 1 hour

**Description**:
Perform final end-to-end testing on multiple device types.

**Testing**:
- Test on arm64 device
- Test on x86 emulator
- Test with slow network
- Test with limited storage
- Test cancellation at each phase
- Test retry after failure
- Verify Feature 002 tests pass

**Acceptance**:
- All scenarios tested
- All tests pass
- No critical bugs found
- Ready for code review

---

**Phase 6 Checkpoint**: ✅ Feature complete and polished

---

## Dependency Graph

```
Phase 1 (Setup)
  ├─ T001: Gradle Dependencies [P]
  ├─ T002: Network Permission [P]
  ├─ T003: Package Structure [P]
  └─ T004: Research Tasks (BLOCKING)
      │
      ▼
Phase 2 (Foundational)
  ├─ T005: Data Models [P]
  ├─ T006-T009: Interfaces [P]
  ├─ T010: Downloader (depends on T001, T004, T007)
  ├─ T011: Extractor (depends on T001, T004, T008)
  ├─ T012: Validator (depends on T004, T009)
  ├─ T013: Manager (depends on T006, T010, T011, T012)
  ├─ T014: Permissions (depends on T004) [P]
  ├─ T015: Environment (depends on T004) [P]
  └─ T016: Hilt Module (depends on T013)
      │
      ▼
Phase 3 (US1: First-Time Installation)
  ├─ T017: ViewModel (depends on T013, T016)
  ├─ T018: UI Screen (depends on T017) [P]
  ├─ T019: App Launch Check (depends on T013) [P]
  ├─ T020: Architecture Detection (depends on T004) [P]
  ├─ T021: Progress Formatting [P]
  ├─ T022: Storage Check (depends on T005) [P]
  ├─ T023: Navigation (depends on T018)
  ├─ T024: First Launch Detection [P]
  ├─ T025: Unit Tests [P]
  └─ T026: Integration Tests [P]
      │
      ▼
Phase 4 (US2: Failure Recovery)
  ├─ T027: Error UI (depends on T018)
  ├─ T028: Retry Logic (depends on T017)
  ├─ T029: Cleanup [P]
  ├─ T030: Error Logging [P]
  ├─ T031: Unit Tests [P]
  └─ T032: Integration Tests [P]
      │
      ▼
Phase 5 (US3: Integration Tests)
  ├─ T033: Test Helpers
  ├─ T034: Session Tests (depends on T033)
  ├─ T035: Bootstrap Helper [P]
  ├─ T036: Performance Verification [P]
  └─ T037: Test Documentation [P]
      │
      ▼
Phase 6 (Polish)
  ├─ T038: Error Messages [P]
  ├─ T039: Diagnostic Logging [P]
  ├─ T040: Documentation [P]
  └─ T041: Final E2E Testing
```

---

## Parallel Execution Opportunities

**Phase 1**: T001, T002, T003 can run in parallel (T004 must complete alone)

**Phase 2**:
- T005, T006, T007, T008, T009 can run in parallel
- After T004 completes: T010, T011, T012, T014, T015 can run in parallel
- T013 waits for T010, T011, T012
- T016 waits for T013

**Phase 3**:
- After T017: T018, T019, T020, T021, T022, T024, T025 can run in parallel
- T023 waits for T018
- T026 can run in parallel with most tasks

**Phase 4**:
- T029, T030, T031, T032 can run in parallel after T027, T028

**Phase 5**:
- After T033: T035, T036, T037 can run in parallel
- T034 waits for T033

**Phase 6**:
- T038, T039, T040 can run in parallel
- T041 waits for all others

---

## Implementation Strategy

### MVP Scope (Recommended)

For MVP, implement **User Story 1 only** (Phase 1 + Phase 2 + Phase 3):
- Total tasks: T001-T026 (26 tasks)
- Estimated time: 10-12 hours
- Delivers: Working first-time installation flow
- Value: Users can install bootstrap and use terminal

**Post-MVP**: Add US2 (error recovery) and US3 (test compatibility)

### Incremental Delivery

Each phase is independently deployable:
1. **Phase 1+2**: Foundational services (no UI yet)
2. **Phase 1+2+3**: MVP - First-time installation works
3. **Phase 1+2+3+4**: Error recovery added
4. **Phase 1+2+3+4+5**: Integration tests work
5. **Full Feature**: All phases complete

### Testing Strategy

**Unit Tests**: Run after each task that creates testable logic
**Integration Tests**: Run at end of each phase
**E2E Tests**: Run after Phase 3, Phase 5, and Phase 6

### Code Review Strategy

**Review Points**:
- After Phase 2 (foundational services complete)
- After Phase 3 (US1 complete - MVP ready)
- After Phase 6 (full feature complete)

---

## Success Criteria

**Phase 3 Complete (MVP)**:
- ✅ Bootstrap installs successfully on first launch
- ✅ User sees clear progress indicators
- ✅ Installation completes within 5 minutes
- ✅ Terminal ready immediately after installation
- ✅ All basic commands work (ls, cat, grep, bash)

**Phase 4 Complete**:
- ✅ Retry works after failures
- ✅ Error messages are clear and actionable
- ✅ Cleanup works correctly
- ✅ 90% of retries succeed

**Phase 5 Complete**:
- ✅ All Feature 002 integration tests pass
- ✅ Tests execute real commands
- ✅ Test execution time <10 seconds

**Phase 6 Complete**:
- ✅ Error messages polished
- ✅ Comprehensive logging added
- ✅ Documentation complete
- ✅ E2E testing complete on multiple devices

---

## Risk Mitigation

**Risk 1**: Research tasks (T004) take longer than expected
- **Mitigation**: Start research immediately, update estimates if needed

**Risk 2**: Apache Commons Compress has Android compatibility issues
- **Mitigation**: T011 tests this early, fallback to alternative library if needed

**Risk 3**: File permissions don't work on some Android versions
- **Mitigation**: T014 integration tests catch this, document workarounds

**Risk 4**: Bootstrap download is unreliable
- **Mitigation**: T010 implements retry logic, resume capability

**Risk 5**: Installation time exceeds 5 minutes
- **Mitigation**: T036 measures performance, optimize extraction if needed

---

## Next Steps

1. **Complete T004** (Research tasks) - BLOCKING for Phase 2
2. **Review this task breakdown** with team
3. **Confirm MVP scope** (Phase 1+2+3 recommended)
4. **Begin implementation** with Phase 1
5. **Track progress** using this task list

---

**Tasks Generated**: 2025-10-22
**Ready for**: `/specswarm:implement` or manual implementation
**Estimated Total Time**: 12-16 hours (1-2 days)
**MVP Time**: 10-12 hours
**Parallel Opportunities**: 18 tasks (44% of total)

---

*This task breakdown is immediately executable. Each task has clear acceptance criteria, file paths, and dependencies. Begin with Phase 1 and proceed sequentially through phases.*
