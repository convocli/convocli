# Feature 002: Termux Integration

## Overview

Integrate Termux terminal emulator library to provide a full Linux command-line environment within the ConvoCLI Android application. This feature establishes the foundational terminal infrastructure that enables users to execute shell commands, manage processes, and interact with a Linux environment on their Android device.

**Business Value**: Enables core terminal functionality that differentiates ConvoCLI from standard Android terminal apps by providing a robust, well-maintained terminal emulator with access to thousands of Linux packages via apt.

**Status**: In Development
**Priority**: Critical (Blocking)
**Dependencies**: Feature 001 (Android Project Foundation)

## User Scenarios

### Primary User: Mobile Developer

**Scenario 1: Execute Basic Shell Commands**
1. Developer opens ConvoCLI app on their Android device
2. App initializes a terminal session with a Linux shell (bash)
3. Developer types `ls -la` to list files in current directory
4. App executes the command via PTY and displays output in real-time
5. Developer sees file listing with permissions, ownership, and timestamps
6. Developer continues working with additional commands (cd, pwd, cat, etc.)

**Scenario 2: Navigate Directory Structure**
1. Developer is in `/data/data/com.convocli/files/home` directory
2. Developer types `cd /storage/emulated/0/Documents`
3. App changes working directory and updates environment
4. Developer types `pwd` to confirm location
5. App displays current working directory path
6. All subsequent commands execute in the new directory context

**Scenario 3: View File Contents**
1. Developer wants to read a configuration file
2. Developer types `cat ~/.bashrc`
3. App streams file contents line-by-line through PTY
4. Developer sees the complete file content with proper formatting
5. Developer can pipe output to other commands (e.g., `cat file.txt | grep pattern`)

**Scenario 4: Handle Command Errors**
1. Developer types an invalid command: `invalidcommand123`
2. App executes command through shell
3. Shell returns error: "command not found"
4. App captures stderr output and displays error message to user
5. Terminal session remains active and ready for next command
6. Developer can continue working without session disruption

### Secondary User: Student Learning Linux

**Scenario 5: Interactive Learning Environment**
1. Student installs ConvoCLI to learn Linux commands
2. App provides a real Linux environment (not simulation)
3. Student experiments with commands: `echo`, `date`, `whoami`, `uname -a`
4. App executes all commands correctly with real output
5. Student builds confidence with command-line interface
6. Environment persists across app launches

## Functional Requirements

### FR-1: Termux Library Integration
**Requirement**: Integrate the Termux terminal-emulator library into the ConvoCLI Android project.

**Details**:
- Use Termux terminal-emulator as a library dependency (not fork initially)
- Integrate with Gradle build system
- Ensure GPLv3 license compatibility with ConvoCLI's licensing
- Include necessary native libraries (libterm-emulator.so for PTY support)
- Set up proper JNI configuration for native code communication

**Acceptance Criteria**:
- Termux library successfully compiles with ConvoCLI project
- No build errors or conflicts with existing dependencies
- Native libraries are correctly packaged in APK
- License attribution documented in README and app

### FR-2: PTY (Pseudo-Terminal) Setup
**Requirement**: Establish pseudo-terminal (PTY) for bidirectional communication between app and shell process.

**Details**:
- Create PTY master/slave pair for shell communication
- Configure terminal characteristics (size, encoding, buffering)
- Set up proper file descriptors for stdin, stdout, stderr
- Handle terminal control sequences (ANSI escape codes)
- Support terminal resizing events

**Acceptance Criteria**:
- PTY successfully created when terminal session starts
- Shell process (bash) connects to PTY slave
- App can write input to PTY master
- App can read output from PTY master
- Terminal control sequences are properly handled

### FR-3: Terminal Session Management
**Requirement**: Provide Kotlin API for creating, managing, and destroying terminal sessions.

**Details**:
- `TerminalSession` class wrapping Termux session lifecycle
- Session initialization with configurable shell (default: bash)
- Session state tracking (running, stopped, error)
- Session cleanup and resource disposal
- Support for environment variable configuration
- Working directory management

**Acceptance Criteria**:
- Can create new terminal session programmatically
- Session starts with bash shell in home directory
- Session can be stopped/destroyed cleanly
- Resources (PTY, processes) are properly released on session end
- No memory leaks after repeated session create/destroy cycles

### FR-4: Command Execution
**Requirement**: Execute basic shell commands and capture results.

**Details**:
- Support interactive command execution (ls, pwd, cd, echo, cat, grep, etc.)
- Handle command input with proper encoding (UTF-8)
- Capture both stdout and stderr streams
- Support command chaining (&&, ||, |)
- Handle multiline commands and command history
- Support special characters and escape sequences

**Acceptance Criteria**:
- Commands execute successfully: `ls`, `pwd`, `cd`, `echo`, `cat`, `grep`
- Command output is captured in real-time
- Stderr is captured separately from stdout
- Commands with pipes work correctly: `cat file.txt | grep pattern`
- Commands with redirects work: `echo "test" > file.txt`
- Environment variables are accessible: `echo $HOME`

### FR-5: Output Streaming
**Requirement**: Stream command output asynchronously to the UI layer.

**Details**:
- Use Kotlin Coroutines and Flow for async output streaming
- Buffer output efficiently to prevent UI blocking
- Support backpressure handling for rapid output
- Preserve output ordering (stdout/stderr interleaving)
- Handle large output volumes without crashes

**Acceptance Criteria**:
- Command output appears in real-time (< 100ms latency)
- Large outputs (10,000+ lines) stream without lag
- No UI freezing during command execution
- Output order matches actual command execution order
- Memory usage remains stable during streaming

### FR-6: Hilt Dependency Injection Integration
**Requirement**: Integrate terminal functionality with existing Hilt DI architecture.

**Details**:
- Create `@Singleton` scoped `TerminalRepository`
- Inject `TerminalRepository` into ViewModels
- Provide terminal session through DI container
- Handle proper lifecycle management via Hilt scopes
- Support testing with mock terminal implementations

**Acceptance Criteria**:
- `TerminalRepository` is injectable via Hilt
- ViewModels can access terminal functionality via DI
- Repository lifecycle managed correctly by Hilt
- Unit tests can inject mock terminal repository
- No manual dependency management required

### FR-7: Terminal Lifecycle Management
**Requirement**: Manage terminal session lifecycle aligned with Android component lifecycle.

**Details**:
- Handle app backgrounding/foregrounding
- Preserve session state across configuration changes
- Clean up sessions on app termination
- Detect and handle session crashes
- Restart sessions on failure
- Save and restore working directory

**Acceptance Criteria**:
- Terminal session persists when app is backgrounded
- Session survives device rotation
- Sessions are cleaned up when app is destroyed
- Crashed sessions are detected and reported
- Working directory is restored after app restart

### FR-8: Environment Variables and Working Directory
**Requirement**: Support environment variable configuration and working directory management.

**Details**:
- Set default environment variables (HOME, PATH, SHELL, USER, etc.)
- Allow custom environment variable injection
- Track current working directory
- Update working directory on `cd` commands
- Provide API to set initial working directory
- Support environment variable expansion in commands

**Acceptance Criteria**:
- `$HOME` points to app's private directory
- `$PATH` includes standard Linux binaries
- `cd /some/path` changes working directory
- Subsequent commands execute in correct directory
- Custom environment variables can be set
- `echo $VARIABLE` displays correct values

### FR-9: Error Handling
**Requirement**: Handle and report command execution errors gracefully.

**Details**:
- Capture stderr output for error messages
- Detect command failures (non-zero exit codes)
- Handle PTY errors (broken pipe, I/O errors)
- Report session initialization failures
- Provide user-friendly error messages
- Log detailed errors for debugging

**Acceptance Criteria**:
- Invalid commands show "command not found" error
- Failed commands return non-zero exit codes
- PTY errors are caught and reported
- Session failures trigger error callbacks
- Error messages are user-readable
- Technical details are logged for debugging

## Success Criteria

### SC-1: Command Execution Performance
**Metric**: Commands execute with minimal latency
- Simple commands (ls, pwd, echo) complete in < 200ms
- Output appears in UI within 100ms of execution
- No perceived lag for interactive commands

### SC-2: Session Stability
**Metric**: Terminal sessions are robust and reliable
- Sessions run for 1+ hour without crashes
- Handle 1000+ consecutive commands without failure
- Survive app backgrounding and device rotation

### SC-3: Output Handling Capacity
**Metric**: System handles large output volumes
- Stream 10,000+ lines without UI freezing
- Memory usage remains under 50MB during streaming
- Correctly handle outputs up to 10MB in size

### SC-4: User Experience
**Metric**: Terminal feels responsive and native
- Commands respond immediately (< 100ms perceived delay)
- Error messages are clear and actionable
- No visible crashes or freezes during normal use

### SC-5: Linux Environment Completeness
**Metric**: Provides functional Linux environment
- Standard Linux commands work correctly (ls, cd, pwd, cat, grep, echo, etc.)
- Environment variables are properly set
- Working directory changes persist
- File system operations function correctly

## Key Entities

### TerminalSession
**Description**: Represents an active terminal session with a shell process.

**Attributes**:
- `sessionId`: Unique identifier for the session
- `shellPath`: Path to shell executable (e.g., `/bin/bash`)
- `workingDirectory`: Current working directory
- `environment`: Map of environment variables
- `state`: Session state (RUNNING, STOPPED, ERROR)
- `createdAt`: Session creation timestamp

**Relationships**:
- Has one PTY
- Has one shell Process
- Belongs to one user (app)

### PTY (Pseudo-Terminal)
**Description**: Bidirectional communication channel between app and shell.

**Attributes**:
- `masterFd`: File descriptor for PTY master
- `slaveFd`: File descriptor for PTY slave
- `terminalSize`: Terminal dimensions (rows, columns)
- `encoding`: Character encoding (UTF-8)

**Relationships**:
- Belongs to one TerminalSession
- Connects to one shell Process

### CommandOutput
**Description**: Output stream from command execution.

**Attributes**:
- `content`: Text content of output
- `streamType`: STDOUT or STDERR
- `timestamp`: When output was received
- `sessionId`: Associated terminal session

**Relationships**:
- Belongs to one TerminalSession
- Generated by one command execution

## Technical Constraints

### TC-1: Termux Licensing
**Constraint**: Termux terminal-emulator library is GPLv3 licensed.

**Implication**: ConvoCLI must also be GPLv3 licensed or use a license-compatible alternative. All source code modifications must be disclosed if distributed.

**Mitigation**: ConvoCLI is already planned as GPLv3 for F-Droid distribution (per CLAUDE.md).

### TC-2: Native Code Dependencies
**Constraint**: PTY functionality requires native C code via JNI.

**Implication**: Must include platform-specific native libraries (.so files) for arm64-v8a, armeabi-v7a, x86, x86_64 architectures.

**Mitigation**: Termux library already provides pre-built native libraries for all Android architectures.

### TC-3: Android Permissions
**Constraint**: Terminal operations may require storage permissions.

**Implication**: Must request READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE permissions to access user files outside app directory.

**Mitigation**: Request permissions at runtime when user attempts to access external storage.

### TC-4: Process Isolation
**Constraint**: Android isolates processes; shell runs in app's security context.

**Implication**: Shell has limited permissions and cannot access system resources without proper Android permissions.

**Mitigation**: Document limitations clearly. Most terminal operations work within app sandbox.

### TC-5: Background Execution
**Constraint**: Android restricts background process execution to save battery.

**Implication**: Terminal sessions may be killed if app is backgrounded for extended periods.

**Mitigation**: Use Android Foreground Service for long-running terminal sessions (future enhancement).

## Dependencies

### Termux Terminal Emulator Library
**Version**: Latest stable (termux-terminal-emulator)
**License**: GPLv3
**Purpose**: Provides terminal emulation, PTY management, and ANSI escape code handling
**Integration**: Gradle dependency or Git submodule

### Kotlin Coroutines
**Version**: 1.7+
**Purpose**: Asynchronous output streaming and command execution
**Status**: Already included in Feature 001

### Hilt (Dependency Injection)
**Version**: 2.48
**Purpose**: Terminal repository injection and lifecycle management
**Status**: Already configured in Feature 001

## Assumptions

### A-1: Shell Availability
**Assumption**: Termux provides a bash shell binary that runs on Android.

**Rationale**: Termux is mature and widely used for Android terminal emulation. The bootstrap system includes bash by default.

**Risk**: Low - Termux is proven technology with millions of users.

### A-2: PTY Implementation
**Assumption**: Termux's native PTY implementation works reliably across Android versions API 26+.

**Rationale**: Termux has been tested on Android 8.0 through Android 14+.

**Risk**: Low - Extensive real-world usage validates stability.

### A-3: Performance Characteristics
**Assumption**: Terminal operations have negligible performance overhead compared to native terminal apps.

**Rationale**: PTY and process management are lightweight kernel operations. Kotlin/Java overhead is minimal for I/O operations.

**Risk**: Low - Terminal performance is I/O-bound, not CPU-bound.

### A-4: Storage Access
**Assumption**: Users expect to access files in standard Android directories (/storage/emulated/0/, /sdcard/).

**Rationale**: Terminal users commonly work with files outside the app sandbox.

**Risk**: Medium - Requires runtime permissions; user may deny access.

**Mitigation**: Gracefully handle permission denials; provide clear error messages.

### A-5: Initial Working Directory
**Assumption**: Terminal session starts in app's private home directory (`/data/data/com.convocli/files/home`).

**Rationale**: Standard Unix convention; provides safe default location for terminal operations.

**Risk**: None - Can be changed via `cd` command.

## Out of Scope

The following are explicitly **NOT** included in this feature:

### Bootstrap System Installation
Installing the full Termux Linux environment (apt, dpkg, packages) is deferred to a future feature. This feature only integrates the terminal emulator itself.

### Terminal UI Components
The visual terminal display (ANSI rendering, cursor, colors) is handled by future features (Command Blocks UI, Traditional Terminal Mode).

### Package Management
Installing, updating, or removing Linux packages via apt/pkg is a separate feature.

### Multi-Session Management
Managing multiple concurrent terminal sessions is a future enhancement.

### Session Persistence
Saving terminal session history and restoring sessions after app restart is a future feature.

### Advanced Terminal Features
- Tab completion
- Command history search
- Syntax highlighting
- Terminal themes
- Custom key bindings

### ConvoSync Integration
Cloud synchronization of terminal sessions and command history is deferred to Phase 2 (per CLAUDE.md).

## Related Documentation

- **CLAUDE.md**: Architecture and technical specifications
- **Feature 001**: Android Project Foundation Setup (dependency)
- **Termux Documentation**: https://wiki.termux.com/wiki/Main_Page
- **Termux Terminal Emulator**: https://github.com/termux/termux-app (reference implementation)
