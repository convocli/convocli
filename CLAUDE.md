# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**ConvoCLI** is a modern conversational terminal emulator for Android that combines Termux's powerful Linux environment with a Warp 2.0-inspired command blocks interface. The project aims to revolutionize mobile terminal experiences for developers.

### Core Innovation
- **Command Blocks UI**: Chat-like, conversational terminal interface optimized for touch
- **Full Linux Environment**: Built on Termux fork with 1000+ packages via apt
- **Cross-Device Sync**: ConvoSync feature for seamless desktop↔mobile workflows
- **AI-Powered**: Designed to work seamlessly with Claude Code and other AI coding assistants

### Target Audience
- Mobile developers who code on-the-go
- F-Droid users who value open source
- Terminal power users wanting modern mobile UX
- Anyone using AI coding assistants (Claude Code, etc.)

### Open Source Strategy
- **License**: GPLv3 (required by Termux fork)
- **Distribution**: F-Droid primary, GitHub releases
- **Revenue**: Service-based (ConvoSync cloud), not app sales
- **Philosophy**: Full transparency, community-driven

---

## Architecture

### High-Level Design

```
┌─────────────────────────────────────────┐
│   Jetpack Compose UI Layer (Kotlin)     │
│   ┌─────────────────────────────────┐   │
│   │ Command Blocks Screen           │   │
│   │ Traditional Terminal Screen     │   │
│   │ Settings, Navigation            │   │
│   └──────────────┬──────────────────┘   │
│                  │                       │
│   ┌──────────────▼──────────────────┐   │
│   │ ViewModels (MVI/MVVM)           │   │
│   │ - TerminalViewModel             │   │
│   │ - CommandBlockViewModel         │   │
│   │ - StateFlow for reactive UI     │   │
│   └──────────────┬──────────────────┘   │
│                  │                       │
│   ┌──────────────▼──────────────────┐   │
│   │ Repository Layer                │   │
│   │ - TermuxRepository              │   │
│   │ - CommandHistoryRepository      │   │
│   │ - Direct session management     │   │
│   └──────────────┬──────────────────┘   │
└──────────────────┼──────────────────────┘
                   │ (Direct Kotlin calls)
┌──────────────────▼──────────────────────┐
│   Forked Termux Core (Kotlin/Java)      │
│   ┌─────────────────────────────────┐   │
│   │ TerminalSession.java            │   │
│   │ TerminalEmulator.java           │   │
│   │ - VT-100/ANSI emulation         │   │
│   │ - PTY interface (JNI to C)      │   │
│   │ - Process management            │   │
│   └─────────────────────────────────┘   │
│                                          │
│   ┌─────────────────────────────────┐   │
│   │ Package System                  │   │
│   │ - apt/dpkg integration          │   │
│   │ - Bootstrap system              │   │
│   └─────────────────────────────────┘   │
└──────────────────────────────────────────┘
```

### Key Architectural Decisions

**Direct Integration (Zero Overhead)**
- Compose UI → Kotlin ViewModel → Kotlin Repository → Java/Kotlin Termux Core
- No serialization, no bridge, same process
- ~5-10ms command execution latency (native performance)

**Two-Mode Design**
1. **Command Blocks Mode** (primary): Modern chat-like UI for everyday commands
2. **Traditional Terminal Mode**: Full VT-100 emulation for vim, htop, interactive programs

**State Management**
- Unidirectional data flow (MVI pattern preferred)
- StateFlow/SharedFlow for reactive updates
- ViewModel scope for lifecycle management

---

## Technology Stack (2025 Current)

### Core Technologies

**Language & Build**
- **Kotlin**: 1.9+ (primary language)
- **Java**: 11+ (for Termux core compatibility)
- **Gradle**: 8.x with Kotlin DSL
- **AGP**: 8.x (Android Gradle Plugin)

**UI Framework**
- **Jetpack Compose**: 1.9.3 (BOM 2025.10.00, August 2025 release)
- **Material 3**: Latest (dynamic theming, modern design)
- **Compose Navigation**: For multi-screen navigation
- **Compose Animation**: For smooth transitions

**Architecture Components**
- **ViewModel**: Lifecycle-aware state management
- **StateFlow/SharedFlow**: Reactive state and events
- **Coroutines**: Asynchronous operations
- **Room**: Local database for command history
- **Hilt**: Dependency injection
- **DataStore**: Settings persistence

**Terminal Core**
- **Termux Fork**: Based on termux-app (GPLv3)
  - TerminalEmulator.java: VT-100/ANSI implementation
  - TerminalSession.java: Session management
  - PTY implementation: JNI to native C code
- **Package System**: apt/dpkg for 1000+ Linux packages

**ConvoSync (Future)**
- **Backend**: Firebase/Firestore or self-hosted alternative
- **Compression**: gzip with delta sync
- **Encryption**: AES-256 end-to-end
- **Git Integration**: Automatic commit tracking

---

## Development Setup

### Prerequisites
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17 (recommended for Gradle 8.x)
- **Android SDK**: API 26+ (target API 34+)
- **Git**: For Termux submodule management

### Initial Setup

```bash
# Clone repository
git clone https://github.com/yourusername/convocli.git
cd convocli

# Initialize Termux submodule (if using submodule approach)
git submodule update --init --recursive

# Open in Android Studio
# File > Open > select convocli directory

# Sync Gradle and build
./gradlew build
```

### Common Development Commands

**Building**
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing config)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Uninstall from device
./gradlew uninstallDebug
```

**Testing**
```bash
# Run unit tests
./gradlew test

# Run unit tests with coverage
./gradlew testDebugUnitTest jacocoTestReport

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew test --tests com.convocli.TerminalViewModelTest
```

**Code Quality**
```bash
# Run lint checks
./gradlew lint

# Kotlin code style check
./gradlew ktlintCheck

# Auto-format Kotlin code
./gradlew ktlintFormat

# Run all checks
./gradlew check
```

**Development**
```bash
# Clean build
./gradlew clean

# Generate sources (if using code generation)
./gradlew generateDebugSources

# View dependencies
./gradlew dependencies
```

---

## Directory Structure

```
convocli/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/com/convocli/
│   │   │   │   ├── ui/              # Compose UI components
│   │   │   │   │   ├── screens/     # Top-level screens
│   │   │   │   │   │   ├── CommandBlocksScreen.kt
│   │   │   │   │   │   ├── TraditionalTerminalScreen.kt
│   │   │   │   │   │   └── SettingsScreen.kt
│   │   │   │   │   ├── components/  # Reusable components
│   │   │   │   │   │   ├── CommandBlock.kt
│   │   │   │   │   │   ├── TerminalCanvas.kt
│   │   │   │   │   │   └── CommandInput.kt
│   │   │   │   │   └── theme/       # Material 3 theming
│   │   │   │   │       ├── Color.kt
│   │   │   │   │       ├── Theme.kt
│   │   │   │   │       └── Type.kt
│   │   │   │   │
│   │   │   │   ├── viewmodels/      # ViewModels
│   │   │   │   │   ├── TerminalViewModel.kt
│   │   │   │   │   ├── CommandBlockViewModel.kt
│   │   │   │   │   └── SettingsViewModel.kt
│   │   │   │   │
│   │   │   │   ├── repository/      # Data layer
│   │   │   │   │   ├── TermuxRepository.kt
│   │   │   │   │   ├── CommandHistoryRepository.kt
│   │   │   │   │   └── SettingsRepository.kt
│   │   │   │   │
│   │   │   │   ├── data/            # Data models & DB
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── CommandBlock.kt
│   │   │   │   │   │   ├── TerminalSession.kt
│   │   │   │   │   │   └── Settings.kt
│   │   │   │   │   ├── db/
│   │   │   │   │   │   ├── AppDatabase.kt
│   │   │   │   │   │   └── CommandDao.kt
│   │   │   │   │   └── datastore/
│   │   │   │   │       └── SettingsDataStore.kt
│   │   │   │   │
│   │   │   │   ├── terminal/        # Termux integration
│   │   │   │   │   ├── TermuxSessionClient.kt
│   │   │   │   │   ├── CommandBlockParser.kt
│   │   │   │   │   └── TerminalOutputProcessor.kt
│   │   │   │   │
│   │   │   │   ├── di/              # Dependency injection
│   │   │   │   │   ├── AppModule.kt
│   │   │   │   │   ├── DatabaseModule.kt
│   │   │   │   │   └── RepositoryModule.kt
│   │   │   │   │
│   │   │   │   └── ConvoCLIApplication.kt
│   │   │   │
│   │   │   ├── res/                 # Resources
│   │   │   │   ├── values/
│   │   │   │   ├── drawable/
│   │   │   │   └── xml/
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   ├── test/                    # Unit tests
│   │   │   └── kotlin/com/convocli/
│   │   │       ├── viewmodels/
│   │   │       ├── repository/
│   │   │       └── terminal/
│   │   │
│   │   └── androidTest/             # Instrumented tests
│   │       └── kotlin/com/convocli/
│   │           ├── ui/
│   │           └── db/
│   │
│   └── build.gradle.kts
│
├── termux-core/                     # Forked Termux (submodule or fork)
│   ├── src/main/java/com/termux/
│   │   ├── terminal/
│   │   │   ├── TerminalEmulator.java
│   │   │   └── TerminalSession.java
│   │   └── ...
│   └── ...
│
├── docs/
│   ├── specification.md             # Full project spec
│   └── architecture/                # Architecture diagrams
│
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md
├── CLAUDE.md                        # This file
├── LICENSE                          # GPLv3
└── .gitignore
```

---

## Key Implementation Patterns

### 1. Terminal Session Management

```kotlin
// TermuxRepository.kt
class TermuxRepository @Inject constructor(
    private val context: Context
) {
    private var currentSession: TerminalSession? = null
    private val outputFlow = MutableSharedFlow<String>()

    fun createSession(): Flow<TerminalSessionState> = flow {
        val callback = object : TerminalSessionClient {
            override fun onTextChanged(session: TerminalSession) {
                outputFlow.tryEmit(session.emulator.screen.transcriptText)
            }

            override fun onTitleChanged(session: TerminalSession) {
                // Handle title changes
            }
        }

        currentSession = TermuxService.getInstance().createTerminalSession(
            executablePath = "/data/data/com.convocli/files/usr/bin/bash",
            workingDirectory = "/data/data/com.convocli/files/home",
            arguments = arrayOf(),
            client = callback
        )

        emit(TerminalSessionState.Ready(currentSession!!))
    }

    suspend fun executeCommand(command: String) {
        currentSession?.write(command + "\n")
    }

    fun observeOutput(): Flow<String> = outputFlow.asSharedFlow()
}
```

### 2. Command Blocks ViewModel

```kotlin
// CommandBlockViewModel.kt
@HiltViewModel
class CommandBlockViewModel @Inject constructor(
    private val termuxRepository: TermuxRepository,
    private val commandHistoryRepository: CommandHistoryRepository
) : ViewModel() {

    private val _commandBlocks = MutableStateFlow<List<CommandBlock>>(emptyList())
    val commandBlocks: StateFlow<List<CommandBlock>> = _commandBlocks.asStateFlow()

    private val _isExecuting = MutableStateFlow(false)
    val isExecuting: StateFlow<Boolean> = _isExecuting.asStateFlow()

    init {
        observeTerminalOutput()
    }

    private fun observeTerminalOutput() {
        viewModelScope.launch {
            termuxRepository.observeOutput()
                .collect { output ->
                    processOutput(output)
                }
        }
    }

    fun executeCommand(command: String) {
        viewModelScope.launch {
            _isExecuting.value = true

            // Add command block (pending state)
            val newBlock = CommandBlock(
                id = UUID.randomUUID().toString(),
                command = command,
                output = "",
                status = CommandStatus.EXECUTING,
                timestamp = System.currentTimeMillis()
            )
            _commandBlocks.value += newBlock

            // Execute via Termux
            termuxRepository.executeCommand(command)

            // Save to history
            commandHistoryRepository.saveCommand(command)
        }
    }

    private fun processOutput(output: String) {
        // Parse output and update latest command block
        val blocks = _commandBlocks.value.toMutableList()
        val lastBlock = blocks.lastOrNull()

        if (lastBlock?.status == CommandStatus.EXECUTING) {
            blocks[blocks.lastIndex] = lastBlock.copy(
                output = output,
                status = CommandStatus.COMPLETED
            )
            _commandBlocks.value = blocks
            _isExecuting.value = false
        }
    }
}
```

### 3. Command Block Composable

```kotlin
// CommandBlock.kt
@Composable
fun CommandBlock(
    block: CommandBlock,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onRerun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Command input section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = block.command,
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )

                if (block.status == CommandStatus.EXECUTING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }

            // Output section
            if (block.output.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = block.output,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onCopy) {
                    Icon(Icons.Default.ContentCopy, "Copy")
                }
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, "Share")
                }
                IconButton(onClick = onRerun) {
                    Icon(Icons.Default.Refresh, "Rerun")
                }
            }
        }
    }
}
```

### 4. Terminal Canvas Rendering (Traditional Mode)

```kotlin
// TerminalCanvas.kt
@Composable
fun TerminalCanvas(
    terminalSession: TerminalSession,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val emulator = terminalSession.emulator

    Canvas(modifier = modifier.fillMaxSize()) {
        val screen = emulator.screen
        val lineHeight = 20.dp.toPx()
        val charWidth = 12.dp.toPx()

        // Draw each line
        for (row in 0 until screen.activeTranscriptRows) {
            val line = screen.getTranscriptLineAtIndex(row)

            drawText(
                textMeasurer = textMeasurer,
                text = line.text,
                topLeft = Offset(0f, row * lineHeight),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    color = Color.White
                )
            )
        }

        // Draw cursor
        val cursorRow = screen.cursorRow
        val cursorCol = screen.cursorCol
        drawRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(cursorCol * charWidth, cursorRow * lineHeight),
            size = Size(charWidth, lineHeight)
        )
    }
}
```

---

## Testing Strategies

### Unit Tests

**ViewModels**
```kotlin
// TerminalViewModelTest.kt
@ExperimentalCoroutinesApi
class TerminalViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `executeCommand updates command blocks`() = runTest {
        val viewModel = CommandBlockViewModel(
            termuxRepository = FakeTermuxRepository(),
            commandHistoryRepository = FakeCommandHistoryRepository()
        )

        viewModel.executeCommand("ls -la")
        testDispatcher.scheduler.advanceUntilIdle()

        val blocks = viewModel.commandBlocks.value
        assertEquals(1, blocks.size)
        assertEquals("ls -la", blocks.first().command)
    }
}
```

**Repository Layer**
```kotlin
// TermuxRepositoryTest.kt
class TermuxRepositoryTest {

    @Test
    fun `createSession returns valid session state`() = runTest {
        val repository = TermuxRepository(ApplicationProvider.getApplicationContext())

        repository.createSession().test {
            val state = awaitItem()
            assertTrue(state is TerminalSessionState.Ready)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

### Integration Tests

**UI Tests with Compose**
```kotlin
// CommandBlocksScreenTest.kt
@RunWith(AndroidJUnit4::class)
class CommandBlocksScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun commandInput_executesCommand() {
        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlocksScreen()
            }
        }

        // Type command
        composeTestRule.onNodeWithTag("command_input")
            .performTextInput("echo hello")

        // Press execute
        composeTestRule.onNodeWithTag("execute_button")
            .performClick()

        // Verify command block appears
        composeTestRule.onNodeWithText("echo hello")
            .assertIsDisplayed()
    }
}
```

### Manual Testing Checklist

**Terminal Functionality**
- [ ] Basic command execution (ls, echo, pwd)
- [ ] Interactive programs (vim, nano, htop)
- [ ] Long-running commands (sleep, watch)
- [ ] Package installation (pkg install python)
- [ ] Multiple sessions
- [ ] Terminal colors (ANSI escape codes)
- [ ] Unicode character support

**UI/UX**
- [ ] Command blocks scroll smoothly
- [ ] Touch gestures work correctly
- [ ] Keyboard input handling
- [ ] Copy/paste functionality
- [ ] Theme switching (light/dark)
- [ ] Orientation changes

**Performance**
- [ ] No lag during rapid output
- [ ] Memory usage remains stable
- [ ] Battery impact acceptable
- [ ] App startup time <2 seconds

---

## Performance Guidelines

### Compose Optimization

**1. Use remember and derivedStateOf**
```kotlin
@Composable
fun CommandBlocksList(blocks: List<CommandBlock>) {
    // Bad: Creates new list on every recomposition
    // val filteredBlocks = blocks.filter { it.status == CommandStatus.COMPLETED }

    // Good: Only recalculates when blocks change
    val filteredBlocks = remember(blocks) {
        blocks.filter { it.status == CommandStatus.COMPLETED }
    }

    LazyColumn {
        items(filteredBlocks, key = { it.id }) { block ->
            CommandBlock(block)
        }
    }
}
```

**2. Lazy layouts with stable keys**
```kotlin
LazyColumn {
    items(
        items = commandBlocks,
        key = { block -> block.id }  // Stable key for efficient updates
    ) { block ->
        CommandBlock(block)
    }
}
```

**3. Avoid unnecessary recompositions**
```kotlin
// Bad: Lambda creates new instance every time
CommandBlock(
    block = block,
    onCopy = { viewModel.copy(block.id) }
)

// Good: Stable callback reference
val onCopy = remember { { id: String -> viewModel.copy(id) } }
CommandBlock(
    block = block,
    onCopy = { onCopy(block.id) }
)
```

### Terminal I/O Performance

**1. Buffer terminal output**
```kotlin
private val outputBuffer = StringBuilder()
private val bufferFlushJob = viewModelScope.launch {
    while (true) {
        delay(16) // ~60fps
        if (outputBuffer.isNotEmpty()) {
            _terminalOutput.value = outputBuffer.toString()
            outputBuffer.clear()
        }
    }
}
```

**2. Use Flow operators efficiently**
```kotlin
termuxRepository.observeOutput()
    .debounce(16) // Throttle rapid updates
    .distinctUntilChanged() // Skip duplicate outputs
    .collect { output ->
        updateUI(output)
    }
```

**3. Limit rendered lines**
```kotlin
// Only render visible lines + buffer
val maxVisibleLines = 100
val trimmedOutput = output.lines().takeLast(maxVisibleLines).joinToString("\n")
```

---

## Common Development Tasks

### Adding a New Compose Screen

1. Create composable in `ui/screens/`
2. Add route to navigation graph
3. Create ViewModel if needed
4. Update navigation from other screens
5. Add tests

### Modifying Termux Core

1. Navigate to `termux-core/` directory
2. Make changes to Java/Kotlin files
3. Rebuild core module: `./gradlew :termux-core:build`
4. Test integration with app
5. Document changes for future upstream sync

### Adding a New Command Block Feature

1. Update `CommandBlock` data model
2. Modify parser in `CommandBlockParser.kt`
3. Update composable UI
4. Add ViewModel logic
5. Test with various command outputs

### Implementing ConvoSync

1. Design data model for sync (sessions, commands, git state)
2. Create backend API (Firebase or self-hosted)
3. Implement delta compression
4. Add Git integration layer
5. Create sync UI and controls
6. Test cross-device scenarios

---

## Termux Integration Notes

### Fork Management

**Upstream Sync Strategy**
- Termux is actively maintained on F-Droid
- Plan quarterly merges of upstream changes
- Keep custom changes in separate commits for easier rebasing
- Document all modifications in commit messages

**Key Files to Modify**
- `TerminalEmulator.java`: Add command block detection logic
- `TerminalSession.java`: Enhance session management
- UI components: Completely replaced with Compose

**Files to Keep Unchanged**
- PTY implementation (C/JNI layer)
- Package system (apt/dpkg integration)
- Bootstrap system

### Command Block Detection

Parse terminal output to identify command boundaries:

```kotlin
class CommandBlockParser {
    private val promptPattern = Regex("^\\$ |^# |^> ")

    fun detectCommandEnd(output: String): Boolean {
        val lines = output.lines()
        return lines.lastOrNull()?.matches(promptPattern) == true
    }
}
```

### Package Installation

Termux packages work out-of-the-box:
```bash
pkg install python
pkg install nodejs
pkg install git
```

Handle in app:
```kotlin
suspend fun installPackage(packageName: String): Flow<InstallProgress> = flow {
    executeCommand("pkg install -y $packageName")
    // Parse output for progress
    termuxRepository.observeOutput()
        .map { parseInstallProgress(it) }
        .collect { emit(it) }
}
```

---

## Troubleshooting

### Build Issues

**Problem**: "Failed to resolve: com.termux:terminal-emulator"
**Solution**: Ensure Termux submodule is initialized or dependency is correctly configured

**Problem**: Compose version conflicts
**Solution**: Use Compose BOM for version management:
```kotlin
implementation(platform("androidx.compose:compose-bom:2025.10.00"))
```

### Runtime Issues

**Problem**: Terminal session crashes on creation
**Solution**: Check file permissions, ensure bootstrap is installed correctly

**Problem**: Commands not executing
**Solution**: Verify working directory exists, check shell path

**Problem**: UI lag during rapid output
**Solution**: Implement output buffering and throttling (see Performance Guidelines)

---

## Resources

### Documentation
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Termux Wiki](https://wiki.termux.com)
- [Material Design 3](https://m3.material.io)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

### Tools
- [Android Studio](https://developer.android.com/studio)
- [Gradle Build Tool](https://gradle.org)
- [ktlint](https://ktlint.github.io) - Kotlin linter
- [Detekt](https://detekt.dev) - Static analysis

### Community
- [Termux GitHub](https://github.com/termux/termux-app)
- [F-Droid](https://f-droid.org)
- [r/termux](https://reddit.com/r/termux)

---

## Project Status

**Current Phase**: MVP Development (Phase 1)
- [x] Project specification complete
- [x] Architecture documented
- [x] Tech stack decided (Jetpack Compose)
- [ ] Termux fork integrated
- [ ] Command blocks UI implementation
- [ ] Traditional terminal mode
- [ ] ConvoSync (Phase 2)

See [docs/specification.md](docs/specification.md) for complete roadmap and timeline.
