# Technical Research: Terminal Output Integration

**Feature**: 007 - Terminal Output Integration
**Created**: 2025-10-22

---

## Overview

This document captures technical research and decisions for implementing terminal output integration. Key research areas include ANSI escape sequence parsing, prompt detection patterns, output streaming performance, and session lifecycle management.

---

## 1. ANSI Escape Sequence Parsing

### Background

ANSI escape sequences are special character sequences that terminals use to control formatting, colors, cursor position, and other aspects of text display. The format is:

```
ESC [ <parameters> <command>
```

Where:
- ESC = ASCII 27 (0x1B) or `\033` in octal
- `[` = Control Sequence Introducer (CSI)
- `<parameters>` = semicolon-separated numbers
- `<command>` = single letter indicating action

### Supported ANSI Codes (Sprint 02)

**Scope Decision**: Support standard 16-color palette only. Extended 256-color and true-color are out of scope.

#### Color Codes

**Foreground Colors (30-37)**:
| Code | Color | Compose Color |
|------|-------|---------------|
| 30 | Black | Color(0xFF000000) |
| 31 | Red | Color(0xFFCD3131) |
| 32 | Green | Color(0xFF0DBC79) |
| 33 | Yellow | Color(0xFFE5E510) |
| 34 | Blue | Color(0xFF2472C8) |
| 35 | Magenta | Color(0xFFBC3FBC) |
| 36 | Cyan | Color(0xFF11A8CD) |
| 37 | White | Color(0xFFE5E5E5) |

**Bright Foreground Colors (90-97)**:
| Code | Color | Compose Color |
|------|-------|---------------|
| 90 | Bright Black (Gray) | Color(0xFF666666) |
| 91 | Bright Red | Color(0xFFF14C4C) |
| 92 | Bright Green | Color(0xFF23D18B) |
| 93 | Bright Yellow | Color(0xFFF5F543) |
| 94 | Bright Blue | Color(0xFF3B8EEA) |
| 95 | Bright Magenta | Color(0xFFD670D6) |
| 96 | Bright Cyan | Color(0xFF29B8DB) |
| 97 | Bright White | Color(0xFFFDFDFD) |

**Background Colors**: Same codes +10 (40-47 for standard, 100-107 for bright)

**Color Selection Rationale**: Colors chosen to match VS Code terminal theme for familiarity and readability on both light/dark backgrounds.

#### Text Style Codes

| Code | Style | Compose Equivalent |
|------|-------|-------------------|
| 0 | Reset all | Clear all spans |
| 1 | Bold | fontWeight = FontWeight.Bold |
| 4 | Underline | textDecoration = TextDecoration.Underline |

**Out of Scope** (Sprint 02):
- Italic (3)
- Strikethrough (9)
- Dim (2)
- Blink (5, 6)
- Reverse video (7)

### Parsing Algorithm

**Regex Pattern**:
```kotlin
val ansiRegex = Regex("\u001B\\[(\\d+(;\\d+)*)m")
```

**Parsing Steps**:
1. Find all ANSI sequences in text
2. Extract positions and codes
3. Build list of spans with start/end indices
4. Create AnnotatedString with spans applied
5. Remove ANSI codes from final text

**Example**:
```kotlin
Input:  "\033[31mError:\033[0m Command not found"
Step 1: Find sequences at positions 0-5 and 11-15
Step 2: Extract codes: [31] at 0, [0] at 11
Step 3: Create spans: [SpanStyle(color=Red, start=0, end=6)]
Step 4: Apply spans to "Error: Command not found"
Output: AnnotatedString with "Error:" in red
```

### Edge Cases

**Nested Styles**:
```
\033[1m\033[31mBold Red\033[0m
```
Solution: Stack styles, apply all active styles to range

**Invalid Codes**:
```
\033[999m Invalid
```
Solution: Strip sequence, ignore code, keep text

**Incomplete Sequences**:
```
\033[31 (missing 'm')
```
Solution: Treat as plain text, don't parse

**Multiple Resets**:
```
\033[0m\033[0m Text
```
Solution: No-op, handle gracefully

### Performance Considerations

- **Caching**: Memoize parsed AnnotatedString for repeated renders
- **Lazy Parsing**: Only parse visible output (for collapsed blocks)
- **Regex Optimization**: Pre-compile regex, reuse Matcher

**Benchmark Target**: Parse 1000 lines with ANSI codes in <50ms

---

## 2. Prompt Detection

### Background

Terminal prompts indicate command completion. Detecting prompts allows automatic transition from EXECUTING to SUCCESS/FAILURE status.

### Common Prompt Patterns

**Bash (default)**:
```bash
$
user@hostname:~$
user@hostname:/current/path$
```

**Root bash**:
```bash
#
root@hostname:~#
```

**Zsh**:
```zsh
%
user@hostname ~ %
```

**Sh (Bourne shell)**:
```sh
$
> (continuation)
```

### Pattern Matching Strategy

**Regex Patterns**:
```kotlin
val patterns = listOf(
    Regex("""^\$ $"""),                        // Simple bash
    Regex("""^# $"""),                         // Root
    Regex("""^% $"""),                         // Zsh
    Regex("""^[\w@\-]+:[~\w/\-]+[\$#%] $""")  // Full prompt
)
```

**Matching Rules**:
1. Only check **last line** of output (prevent false positives)
2. Require trailing space after prompt character
3. Match against all patterns sequentially
4. First match wins

### False Positive Prevention

**Problem**: Mid-command output can resemble prompts

Examples:
```bash
echo "$ "              # Outputs prompt-like string
cat script.sh          # Contains "$ " in shebang comments
```

**Solution**:
- Only check last line (after 100ms of silence)
- Require 2 consecutive lines ending in newline
- Whitelist known safe patterns

### Timeout Fallback

**Why Needed**: Custom prompts may not match patterns

**Implementation**:
```kotlin
private var lastOutputTime = System.currentTimeMillis()

fun checkTimeout(): Boolean {
    val elapsed = System.currentTimeMillis() - lastOutputTime
    return elapsed > 2000 && lastOutputReceived
}
```

**Timeout Settings**:
- **Threshold**: 2 seconds of silence
- **Rationale**: Balance between responsive detection and avoiding premature timeout
- **Trade-off**: 2s is long enough for slow commands, short enough for user patience

### Prompt Detection Flow

```
New output received
    ↓
Update lastOutputTime
    ↓
Is last line a prompt pattern?
    ├─ Yes → Emit completion (pattern matched)
    └─ No → Check timeout
        ├─ Elapsed >2s → Emit completion (timeout)
        └─ Elapsed <2s → Continue waiting
```

### Testing Strategy

**Test Cases**:
1. Standard prompts: `$ `, `# `, `% `
2. Full prompts: `user@host:~$ `
3. Custom prompts: Timeout fallback
4. False positives: Mid-command `$ ` strings
5. Long-running commands: No premature timeout

---

## 3. Output Streaming Performance

### Background

High-volume terminal output (e.g., `npm install`, `git log`) can generate thousands of lines per second, potentially overwhelming the UI.

### Performance Goals

- **Latency**: <100ms from terminal generation to UI display
- **Frame Rate**: Maintain 60fps during streaming
- **Memory**: Limit to 50MB for typical usage
- **Lines**: Handle up to 10,000 lines without lag

### Buffering Strategy

**Problem**: Emitting every character change causes excessive recompositions

**Solution**: Batch updates at 60fps (16ms intervals)

**Implementation**:
```kotlin
private val outputBuffer = mutableListOf<OutputChunk>()

private val bufferFlushJob = viewModelScope.launch {
    while (isActive) {
        delay(16) // ~60fps
        if (outputBuffer.isNotEmpty()) {
            val chunks = outputBuffer.toList()
            outputBuffer.clear()
            emitChunks(chunks)
        }
    }
}

fun bufferOutput(chunk: OutputChunk) {
    outputBuffer.add(chunk)
}
```

**Benefits**:
- Reduces recompositions from ~1000/s to 60/s
- Batches database writes
- Smooths visual updates

### Memory Management

**Problem**: Unbounded output causes OOM

**Solution 1 - Line Truncation**:
```kotlin
const val MAX_LINES = 10_000

fun trimOutput(output: String): String {
    val lines = output.lines()
    return if (lines.size > MAX_LINES) {
        val head = lines.take(5000).joinToString("\n")
        val tail = lines.takeLast(5000).joinToString("\n")
        "$head\n\n[... ${lines.size - MAX_LINES} lines truncated ...]\n\n$tail"
    } else {
        output
    }
}
```

**Solution 2 - Automatic Collapse**:
- Auto-collapse blocks >20 lines
- "Show more" button to expand
- Only render visible lines in LazyColumn

**Solution 3 - Output Size Limit**:
```kotlin
const val MAX_OUTPUT_SIZE = 10_000_000 // 10MB

fun enforceLimit(output: String): String {
    return if (output.length > MAX_OUTPUT_SIZE) {
        output.take(MAX_OUTPUT_SIZE) + "\n\n[Output truncated at 10MB]"
    } else {
        output
    }
}
```

### Compose Optimization

**Key Patterns**:

1. **Stable Keys**:
```kotlin
LazyColumn {
    items(blocks, key = { it.id }) { block ->
        CommandBlockCard(block)
    }
}
```

2. **Remember Computations**:
```kotlin
val parsedOutput = remember(block.output) {
    ansiParser.parseAnsiString(block.output)
}
```

3. **Derived State**:
```kotlin
val isExecuting by remember {
    derivedStateOf { state.blocks.any { it.status == CommandStatus.EXECUTING } }
}
```

4. **Immutable Data**:
```kotlin
data class CommandBlock(...) // data class = structural equality, efficient comparison
```

### Benchmarks

**Target Performance** (Pixel 5, Android 13):

| Scenario | Target | Measurement |
|----------|--------|-------------|
| Parse 1000 lines ANSI | <50ms | Time to create AnnotatedString |
| Render 100 blocks | <16ms | Frame render time |
| Update output (append) | <5ms | StateFlow update latency |
| Database write | <20ms | Room insert/update |
| Memory (1000 blocks) | <30MB | Heap allocation |

---

## 4. Session Lifecycle Management

### Termux Session Integration

**TerminalSession.java API** (from Termux):
```java
public class TerminalSession {
    public void write(String data)
    public void finishIfRunning()
    public boolean isRunning()
    public String getWorkingDirectory()
    public int getExitStatus()
}
```

**Kotlin Wrapper Strategy**:
```kotlin
class TerminalSessionWrapper(private val session: TerminalSession) {
    suspend fun write(command: String) = withContext(Dispatchers.IO) {
        session.write(command)
    }

    suspend fun terminate() = withContext(Dispatchers.IO) {
        session.finishIfRunning()
    }

    val workingDirectory: String
        get() = session.workingDirectory

    val exitStatus: Int
        get() = session.exitStatus
}
```

### Session Lifecycle

**States**:
1. **Not Created**: App launch, before first command
2. **Active**: Session running, accepting commands
3. **Terminated**: Explicitly destroyed
4. **Crashed**: Process died unexpectedly

**State Transitions**:
```
Not Created → Active (createSession)
Active → Terminated (destroySession)
Active → Crashed (process died)
Terminated → Active (recreate session)
Crashed → Active (recreate session)
```

### Session Persistence

**On App Backgrounded**:
1. Persist all command blocks to Room
2. Keep session alive (don't terminate)
3. Continue streaming output (if still executing)

**On App Restored**:
1. Load command blocks from Room
2. Check session state:
   - If active: Resume
   - If crashed: Mark EXECUTING blocks as CANCELED
3. Emit restored blocks to UI

**On App Killed** (force stop):
1. Android terminates process
2. No graceful cleanup possible
3. Next launch: Restore from Room, EXECUTING → CANCELED

### Working Directory Tracking

**PWD Extraction**:
```kotlin
suspend fun getCurrentDirectory(): String = withContext(Dispatchers.IO) {
    val env = session.environment
    env["PWD"] ?: "/data/data/com.convocli/files/home"
}
```

**CD Command Detection**:
```kotlin
val cdRegex = Regex("""^cd\s+(.+)$""")

fun detectCdCommand(command: String): String? {
    return cdRegex.matchEntire(command)?.groupValues?.get(1)
}
```

**Path Resolution**:
```kotlin
fun resolvePath(target: String, current: String): String {
    return when {
        target == "~" -> homeDirectory
        target.startsWith("/") -> target // Absolute
        target == ".." -> File(current).parent ?: current
        target == "." -> current
        else -> File(current, target).absolutePath
    }
}
```

---

## 5. Error Handling & Edge Cases

### Binary Output

**Detection**:
```kotlin
fun isBinary(text: String): Boolean {
    val nonPrintable = text.count { char ->
        char.code < 32 && char !in listOf('\n', '\r', '\t')
    }
    return nonPrintable > text.length * 0.1 // >10% non-printable
}
```

**Handling**:
```kotlin
if (isBinary(output)) {
    return "[Binary output - ${output.length} bytes]"
}
```

### Process Crashes

**Detection**:
```kotlin
session.setSessionCallback(object : TerminalSessionCallback {
    override fun onSessionFinished(session: TerminalSession) {
        if (session.exitStatus != 0) {
            handleCrash(session.exitStatus)
        }
    }
})
```

**Recovery**:
1. Mark current block as FAILED
2. Display error message to user
3. Offer "Restart Session" button
4. Recreate session on user action

### Command Cancellation

**Graceful Termination**:
```kotlin
suspend fun cancelCommand() {
    process.destroy() // SIGTERM
    delay(2000)
    if (process.isAlive) {
        process.destroyForcibly() // SIGKILL
    }
}
```

**Zombie Process Prevention**:
- Always wait for process exit
- Clean up file descriptors
- Update block status to CANCELED

### Long-Running Commands

**Examples**: `ping`, `tail -f`, `watch`

**Handling**:
- Allow cancellation at any time
- Stream output continuously (buffered)
- No timeout for command execution (only for prompt detection)

---

## 6. Database Design Decisions

### Room vs. DataStore

**Decision**: Use Room for command blocks, DataStore for settings

**Rationale**:
- Room: Structured queries (filter by date, status), relations (future multi-session)
- DataStore: Simple key-value (preferences, user settings)

### Schema Design

**Normalization Decision**: Denormalized for Sprint 02

**Current**:
```sql
CREATE TABLE command_blocks (
    id TEXT PRIMARY KEY,
    command TEXT,
    output TEXT,  -- Stored as single text blob
    ...
)
```

**Future (Multi-Session)**:
```sql
CREATE TABLE sessions (
    id TEXT PRIMARY KEY,
    name TEXT,
    created_at INTEGER
)

CREATE TABLE command_blocks (
    ...
    session_id TEXT REFERENCES sessions(id)
)
```

### Indexing Strategy

**Primary Index**: `id` (PRIMARY KEY)
**Secondary Index**: `start_time DESC` for chronological queries

**Query Patterns**:
```sql
-- Most common: Load recent blocks
SELECT * FROM command_blocks ORDER BY start_time DESC LIMIT 50

-- Filter by status
SELECT * FROM command_blocks WHERE status = 'EXECUTING'
```

---

## 7. Testing Approach

### Unit Tests

**AnsiColorParser**:
- Test all 16 colors (foreground + background)
- Test bold, underline styles
- Test nested styles
- Test invalid sequences
- Test edge cases (empty string, only codes, no codes)

**PromptDetector**:
- Test each shell pattern
- Test timeout fallback
- Test false positive prevention
- Test reset behavior

**WorkingDirectoryTracker**:
- Test absolute paths
- Test relative paths (., .., subdirs)
- Test cd with no args (home)
- Test invalid paths

### Integration Tests

**Output Streaming**:
- Execute command with high-volume output
- Verify buffering works (60fps max)
- Verify no output loss
- Verify chronological order

**Session Persistence**:
- Create blocks, background app, restore
- Verify all blocks present
- Verify EXECUTING → CANCELED transition

### Manual Testing

**Test Commands**:
```bash
echo "Hello World"                    # Basic
ls --color=always                     # ANSI colors
npm install                           # High volume
cat /dev/urandom | head -c 1000       # Binary output
ping google.com                       # Long-running (cancel)
cd /tmp && pwd                        # Working directory
sleep 5 && echo "Done"                # Delayed output
cat nonexistent.txt                   # Error handling
```

---

## 8. Open Questions & Future Research

### Questions for Sprint 03+

1. **Multiple Sessions**: How to design session switcher UI?
2. **256-Color Support**: Performance impact of parsing extended color codes?
3. **Output Search**: Full-text search strategy (index output text)?
4. **Export**: Format for exporting history (JSON, plaintext, markdown)?

### Performance Unknowns

- Real-world memory usage with 1000+ blocks?
- Impact of ANSI parsing on low-end devices?
- Database query performance with 10,000+ blocks?

### Future Optimizations

- **Virtual Scrolling**: Only render visible blocks
- **Output Pagination**: Load old blocks on demand
- **Incremental Parsing**: Parse ANSI codes incrementally as output arrives

---

## 9. References

### ANSI Escape Codes
- https://en.wikipedia.org/wiki/ANSI_escape_code
- https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797

### Termux Documentation
- https://github.com/termux/termux-app
- https://wiki.termux.com/

### Compose Best Practices
- https://developer.android.com/jetpack/compose/performance
- https://developer.android.com/jetpack/compose/lists

### Terminal Emulation
- https://invisible-island.net/xterm/ctlseqs/ctlseqs.html
- https://vt100.net/docs/

---

## Summary

This research establishes:
- ✅ ANSI parsing strategy (16-color palette, standard styles)
- ✅ Prompt detection patterns (bash, zsh, sh) with timeout fallback
- ✅ Output streaming performance optimizations (buffering, truncation)
- ✅ Session lifecycle management (persistence, crash recovery)
- ✅ Working directory tracking (PWD extraction, cd detection)
- ✅ Error handling strategies (binary output, crashes, cancellation)
- ✅ Database design (Room with indexing)
- ✅ Testing approach (unit, integration, manual)

All technical decisions are documented and justified. Implementation can proceed with confidence.
