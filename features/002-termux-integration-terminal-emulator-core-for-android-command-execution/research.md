# Research: Termux Integration

**Feature**: 002 - Termux Integration - Terminal Emulator Core
**Date**: 2025-10-21
**Researcher**: Claude Code (SpecSwarm)
**Status**: Complete

---

## T001: Termux Library Integration Research

### Maven Coordinates

**Repository**: JitPack (https://jitpack.io)

Termux libraries are published on JitPack for version 0.116+. Previous versions (0.109-0.114) were on GitHub Packages but this was discontinued per F-Droid policy.

**Latest Stable Version**: v0.118.3

**Critical**: Upgrade to v0.118.0+ is required for security fixes, including a critical world-readable vulnerability.

**Required Dependencies**:

```kotlin
// settings.gradle.kts (root level)
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // Termux terminal-emulator library
    }
}

// App-level build.gradle.kts
dependencies {
    // Termux Terminal Emulator (JitPack) - Note: Use GitHub coordinates with 'v' prefix
    implementation("com.github.termux.termux-app:termux-shared:v0.118.3")
    implementation("com.github.termux.termux-app:terminal-emulator:v0.118.3")
    implementation("com.github.termux.termux-app:terminal-view:v0.118.3")

    // Required to avoid Guava conflicts with Termux
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
}
```

**Note**: `termux-shared` contains constants and utilities. `terminal-view` must be explicitly imported if using terminal UI components.

### License Compliance

**License**: GPLv3 (confirmed via LICENSE.md in repository)

**Implications**:
- ConvoCLI must also be GPLv3 (already planned per CLAUDE.md)
- All source code modifications must be disclosed if distributed
- Full license attribution required in README and app

**Compliance Status**: ✅ Compatible - ConvoCLI is GPLv3

### Android API Compatibility

**Minimum SDK**: Android 7.0 (API 24)
- **ConvoCLI Target**: API 26 (Android 8.0 Oreo)
- **Status**: ✅ Compatible - ConvoCLI's minSdk 26 exceeds Termux minimum

**Full Support**: Android 7+ (API 24+)
**Limited Support**: Android 5-6 (app only, no package updates)

### Library Module Structure

Termux uses modular architecture:

1. **termux-shared** (v0.109+)
   - Shared constants and utilities
   - Eliminates hardcoded paths
   - Required by all other modules

2. **terminal-emulator**
   - Core terminal emulation logic
   - VT-100/ANSI escape sequence handling
   - PTY (pseudo-terminal) management
   - JNI interface to native C code

3. **terminal-view**
   - Terminal UI rendering
   - Canvas-based text display
   - Input handling
   - Touch gesture support

4. **app** (not needed for ConvoCLI)
   - Full Termux application
   - Package manager integration
   - ConvoCLI will only use library modules

### Build System

**Build Tool**: Gradle (Kotlin DSL)
**Versioning**: Semantic versioning (major.minor.patch)
**Commit Convention**: Conventional Commits specification

**ConvoCLI Compatibility**: ✅ Uses same build tools

---

## T002: PTY Implementation Validation

### What is PTY?

**PTY (Pseudo-Terminal)**: Software abstraction emulating a physical terminal, providing bidirectional communication between an application and a shell process.

**Components**:
- **PTY Master**: File descriptor owned by ConvoCLI app (read/write)
- **PTY Slave**: File descriptor connected to shell process (stdin/stdout/stderr)
- **Native Implementation**: C code via JNI (provided by Termux)

### Communication Flow

```
ConvoCLI App → write() → PTY Master
                             ↓
                         PTY Slave → stdin → bash
bash → stdout/stderr → PTY Slave
                             ↓
                         PTY Master → read() → ConvoCLI App
```

### Termux PTY Implementation

**Location**: Native C code in `terminal-emulator` module
**Interface**: JNI bindings for Java/Kotlin
**Standard**: POSIX PTY interface (pty.h, termios.h)

**Key Classes** (from Termux terminal-emulator):

1. **`TerminalSession`** (Java)
   - High-level session management
   - Creates PTY pair
   - Spawns shell process
   - Manages lifecycle

2. **JNI Native Methods**:
   ```java
   private static native int createSubprocess(
       String cmd,
       String cwd,
       String[] args,
       String[] envVars,
       int[] processId,
       int rows,
       int columns
   );
   ```

3. **`TerminalEmulator`** (Java)
   - ANSI/VT-100 escape sequence parser
   - Screen buffer management
   - Cursor positioning
   - Color/attribute handling

### PTY Lifecycle

**Creation**:
1. Call `posix_openpt()` to get PTY master FD
2. Call `grantpt()` and `unlockpt()` to configure slave
3. Get slave path via `ptsname()`
4. Fork process and connect slave to stdin/stdout/stderr
5. Execute shell in child process

**Active State**:
- Background thread monitors PTY master for data
- Read operations are blocking (or use select/poll)
- Write operations send commands to shell

**Termination**:
1. Send SIGHUP to shell process
2. Wait for process termination
3. Close PTY master FD
4. Close PTY slave FD (automatic via process exit)

### Android-Specific Considerations

**Process Isolation**:
- Shell runs in app's security context
- Limited permissions (no root access)
- Can only access app's private directory by default

**File System Access**:
- App sandbox: `/data/data/com.convocli/files/`
- External storage: Requires runtime permissions
- Scoped storage (API 30+): Additional restrictions

**Background Execution**:
- Android may kill background processes to save battery
- Foreground Service required for long-running sessions (future enhancement)
- Session state should be serializable for restoration

### Performance Characteristics

**Latency**: ~1-5ms for PTY operations (native code)
**Throughput**: Limited by terminal emulation parsing, not PTY itself
**Memory**: ~1-2MB per session (screen buffer + process overhead)

**Optimization Strategies**:
- Buffer output reads (4KB chunks)
- Throttle UI updates (16ms intervals for 60fps)
- Use coroutines for non-blocking I/O

### Error Handling

**Common Errors**:
1. **ENOMEM**: Out of memory (PTY creation fails)
2. **EAGAIN**: Resource temporarily unavailable (retry)
3. **EIO**: I/O error (PTY broken, process died)
4. **EPIPE**: Broken pipe (shell terminated)

**Detection**:
- Check return values from native methods
- Monitor process exit status
- Catch `IOException` on read/write operations

**Recovery**:
- Detect broken pipe → offer session restart
- Log native errors for debugging
- Clean up resources even on failure

---

## Prototype Validation

### Basic Integration Test

**Goal**: Verify Termux library can be integrated and basic commands execute

**Approach**:
1. Add dependencies to `build.gradle.kts`
2. Create minimal `TerminalSession` wrapper
3. Execute `echo hello` command
4. Verify output capture

**Expected Result**: "hello" appears in output stream

**Status**: Deferred to implementation phase (T013)

### Performance Baseline

**Test Commands**:
- `echo test` - Simple command (~50ms expected)
- `ls -la` - Directory listing (~100ms expected)
- `seq 1 10000` - Large output (measure throughput)

**Metrics to Measure**:
- Command execution latency
- Output streaming latency
- Memory usage during large outputs
- CPU usage during active I/O

**Status**: Deferred to Phase 4 testing (T037-T039)

---

## Technical Decisions

### Decision 1: Use Termux as Library (Not Fork)

**Rationale**:
- ✅ Minimal integration surface
- ✅ Easy upstream updates
- ✅ Battle-tested reliability (millions of users)
- ✅ Active maintenance (v0.118.3 released recently)
- ❌ Limited customization of internal behavior
- ❌ Dependency on Termux release schedule

**Conclusion**: **Approved** - Benefits outweigh limitations

### Decision 2: JitPack vs GitHub Packages vs Local AAR

**Options**:
1. **JitPack** (current Termux distribution)
   - ✅ Easy integration (just add maven repo)
   - ✅ Official distribution method
   - ❌ External dependency (requires internet for builds)

2. **GitHub Packages** (discontinued by Termux)
   - ❌ Not compatible with F-Droid policy
   - ❌ No longer maintained

3. **Local AAR Files**
   - ✅ No external dependency
   - ✅ Full control over version
   - ❌ Manual updates required
   - ❌ Larger git repository

**Conclusion**: **Use JitPack** - Official method, easy updates

### Decision 3: Direct JNI vs Wrapper Layer

**Options**:
1. **Direct JNI** (use Termux classes directly)
   - ✅ Zero overhead
   - ✅ Simple implementation
   - ❌ Tight coupling to Termux API

2. **Wrapper Layer** (abstract PTY interface)
   - ✅ Future-proof (could swap implementations)
   - ❌ Additional complexity
   - ❌ Performance overhead

**Conclusion**: **Direct JNI** - Termux API is stable, abstraction adds little value

---

## Gotchas and Limitations

### 1. Bootstrap Not Included

**Issue**: Termux library does NOT include Linux binaries or package manager

**Impact**:
- No `apt`, `dpkg`, `bash` by default
- Must install bootstrap separately (Feature 003/004)
- Initial sessions will fail without bootstrap

**Mitigation**:
- Document bootstrap requirement clearly
- Provide bootstrap installation feature separately
- Use system `/bin/sh` as fallback (limited functionality)

### 2. Native Library Architecture

**Issue**: Must include `.so` files for all target architectures

**Impact**: APK size increase (~5-10MB for 4 architectures)
- arm64-v8a (most modern devices)
- armeabi-v7a (older ARM devices)
- x86 (emulators)
- x86_64 (modern emulators)

**Mitigation**:
- Include all 4 architectures for compatibility
- Consider APK splits for Play Store distribution (future)
- F-Droid handles multi-APK automatically

### 3. Permissions Required

**Issue**: Storage access needs runtime permissions (API 23+)

**Impact**:
- `READ_EXTERNAL_STORAGE` - For reading user files
- `WRITE_EXTERNAL_STORAGE` - For writing user files (API ≤28)
- `MANAGE_EXTERNAL_STORAGE` - For full access (API 30+)

**Mitigation**:
- Request permissions only when needed (user-initiated)
- Gracefully handle permission denial
- Work within app sandbox by default

### 4. Termux Session Callbacks

**Issue**: `TerminalSessionClient` interface requires all methods implemented

**Methods**:
- `onTextChanged()` - Called on output
- `onTitleChanged()` - Called when terminal title updates
- `onSessionFinished()` - Called when session exits
- `onClipboardText()` - Called on clipboard operations
- `onBell()` - Called on bell character

**Mitigation**: Implement all methods, even if some are no-ops

### 5. Thread Safety

**Issue**: PTY I/O must be synchronized

**Implications**:
- Read/write operations not thread-safe
- Multiple threads accessing same session → corruption
- UI thread must not block on PTY reads

**Mitigation**:
- Use coroutines with proper dispatchers
- Single-threaded executor for PTY I/O
- Never call PTY operations from main thread

---

## Recommended Configuration

### Environment Variables

```kotlin
val defaultEnv = mapOf(
    "HOME" to "${context.filesDir}/home",
    "PATH" to "${context.filesDir}/usr/bin:/system/bin",
    "SHELL" to "${context.filesDir}/usr/bin/bash",
    "TMPDIR" to "${context.filesDir}/usr/tmp",
    "PREFIX" to "${context.filesDir}/usr",
    "TERM" to "xterm-256color",
    "LANG" to "en_US.UTF-8"
)
```

### Terminal Size

```kotlin
val defaultRows = 24  // Standard terminal height
val defaultColumns = 80  // Standard terminal width
```

**Note**: Should be dynamically calculated based on screen size in production

### Buffer Sizes

```kotlin
val outputBufferSize = 4096  // 4KB chunks
val maxHistoryLines = 10000  // Circular buffer limit
val flushIntervalMs = 16     // 60fps UI updates
```

---

## References

- [Termux GitHub Repository](https://github.com/termux/termux-app)
- [Termux Wiki - Libraries](https://github.com/termux/termux-app/wiki/Termux-Libraries)
- [JitPack](https://jitpack.io)
- [POSIX PTY Documentation](https://man7.org/linux/man-pages/man7/pty.7.html)
- [Android JNI Guide](https://developer.android.com/training/articles/perf-jni)

---

**Research Status**: ✅ Complete

**Next Steps**:
1. Proceed to T003 (Data Model Documentation)
2. Begin Phase 2 implementation (T004+)
3. Validate integration with T013 (Initialize Termux TerminalSession)
