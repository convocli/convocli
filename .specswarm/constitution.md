# ConvoCLI Project Constitution

> Coding standards, architectural principles, and development guidelines for ConvoCLI
>
> **Last Updated**: 2025-10-20
> **Version**: 1.0.0
> **Status**: Active

---

## Purpose

This constitution establishes the foundational principles and standards for ConvoCLI development. All code, features, and contributions must comply with these guidelines to ensure:

- **Consistency**: Uniform codebase that's easy to navigate and maintain
- **Quality**: High standards for performance, security, and accessibility
- **Maintainability**: Clear patterns that scale as the project grows
- **Collaboration**: Shared understanding of best practices

**Automated Enforcement**: SpecSwarm/SpecLabs workflows validate compliance with this constitution before allowing merges.

---

## 1. Kotlin Coding Standards

### Code Style

**Official Standard**: Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)

**Automated Enforcement**: ktlint with official Android ruleset

```kotlin
// Good
class TerminalViewModel @Inject constructor(
    private val repository: TermuxRepository
) : ViewModel() {

    private val _state = MutableStateFlow<TerminalState>(TerminalState.Idle)
    val state: StateFlow<TerminalState> = _state.asStateFlow()

    fun executeCommand(command: String) {
        viewModelScope.launch {
            _state.value = TerminalState.Executing(command)
            // Implementation
        }
    }
}

// Bad - inconsistent formatting, unclear naming
class TerminalViewModel@Inject constructor(private val repo:TermuxRepository):ViewModel(){
private val st=MutableStateFlow<TerminalState>(TerminalState.Idle)
fun exec(cmd:String){viewModelScope.launch{st.value=TerminalState.Executing(cmd)}}
}
```

### Naming Conventions

**Classes**: PascalCase
- `TerminalViewModel`, `CommandBlockParser`, `TermuxRepository`

**Functions**: camelCase, verb-based
- `executeCommand()`, `parseOutput()`, `updateState()`

**Properties**: camelCase
- `commandBlocks`, `isExecuting`, `terminalOutput`

**Constants**: SCREAMING_SNAKE_CASE
- `MAX_COMMAND_HISTORY`, `DEFAULT_TIMEOUT_MS`

**Composables**: PascalCase (like React components)
- `@Composable fun CommandBlock()`, `@Composable fun TerminalScreen()`

### Formatting

**Line Length**: 120 characters maximum

**Indentation**: 4 spaces (no tabs)

**Import Order**:
1. Android
2. Third-party
3. Project imports
4. Blank line before `java.*` and `kotlin.*`

**Function Length**: Maximum 50 lines (extract to private helpers if longer)

**File Length**: Maximum 500 lines (split into multiple files if larger)

### Documentation

**KDoc Required For**:
- All public classes
- All public functions
- All public properties
- Complex private functions (judgment call)

```kotlin
/**
 * Manages terminal session lifecycle and command execution.
 *
 * This repository provides a clean API for interacting with the Termux
 * terminal backend, handling session creation, command execution, and
 * output streaming.
 *
 * @property context Application context for accessing Termux services
 */
class TermuxRepository @Inject constructor(
    private val context: Context
) {
    /**
     * Creates a new terminal session with bash shell.
     *
     * @param workingDirectory Initial working directory for the session
     * @return Flow emitting session state changes
     */
    fun createSession(
        workingDirectory: String = DEFAULT_HOME_DIR
    ): Flow<TerminalSessionState>
}
```

### Nullability

**Prefer Non-Null**: Use non-nullable types whenever possible

**Explicit Non-Null Assertion (`!!`)**: Only when:
- You have just null-checked
- Framework guarantees non-null (with comment explaining why)
- Acceptable to crash if null (rare)

**Always Prefer**:
- Safe calls (`?.`)
- Elvis operator (`?:`)
- `let` blocks
- Early returns

```kotlin
// Good
fun processCommand(command: String?) {
    val cmd = command ?: return
    execute(cmd)
}

// Good - with explanation
val session = sessionManager.currentSession!! // Just created, guaranteed non-null

// Bad - unnecessary null assertion
fun getCommandText() = commandInput.text!!.toString()

// Better
fun getCommandText() = commandInput.text?.toString() ?: ""
```

---

## 2. Jetpack Compose Patterns

### State Management in Composables

**`remember`**: Use for derived state that doesn't need to survive configuration changes

**`rememberSaveable`**: Use for UI state that should survive configuration changes (scroll position, expanded state)

**`derivedStateOf`**: Use for expensive calculations based on state

```kotlin
@Composable
fun CommandBlocksList(blocks: List<CommandBlock>) {
    // Good - derived state with derivedStateOf
    val filteredBlocks by remember(blocks) {
        derivedStateOf {
            blocks.filter { it.status == CommandStatus.COMPLETED }
        }
    }

    // Good - scroll state survives rotation
    val listState = rememberLazyListState()

    LazyColumn(state = listState) {
        items(filteredBlocks, key = { it.id }) { block ->
            CommandBlock(block)
        }
    }
}
```

### Recomposition Optimization

**Always Use `key()`** in LazyColumn/LazyRow/items():

```kotlin
// Good
LazyColumn {
    items(
        items = commandBlocks,
        key = { block -> block.id }  // Stable key for efficient updates
    ) { block ->
        CommandBlock(block)
    }
}

// Bad - no key, inefficient recomposition
LazyColumn {
    items(commandBlocks) { block ->
        CommandBlock(block)
    }
}
```

**Avoid Creating New Lambdas in Recomposition**:

```kotlin
// Bad - creates new lambda on every recomposition
CommandBlock(
    block = block,
    onCopy = { viewModel.copy(block.id) }
)

// Good - stable callback
val onCopy = remember { { id: String -> viewModel.copy(id) } }
CommandBlock(
    block = block,
    onCopy = { onCopy(block.id) }
)
```

### Composable Function Guidelines

**Naming**: PascalCase, descriptive

**Parameters**: `modifier: Modifier = Modifier` always first (after data parameters)

**Stateless Preferred**: Accept data and callbacks, no internal state logic

```kotlin
// Good - stateless composable
@Composable
fun CommandBlock(
    block: CommandBlock,
    onCopy: () -> Unit,
    onRerun: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        // UI implementation
    }
}

// Good - stateful screen composable
@Composable
fun CommandBlocksScreen(
    viewModel: CommandBlockViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    CommandBlocksContent(
        blocks = state.blocks,
        onExecute = viewModel::executeCommand
    )
}
```

### Side Effects

**`LaunchedEffect`**: One-time effects or effects tied to key changes

**`DisposableEffect`**: Effects that need cleanup

**`rememberCoroutineScope`**: For event handlers that launch coroutines

```kotlin
@Composable
fun TerminalScreen(viewModel: TerminalViewModel) {
    val state by viewModel.state.collectAsState()

    // Good - side effect with key
    LaunchedEffect(state.sessionId) {
        viewModel.observeOutput()
    }

    // Good - cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            viewModel.cleanupSession()
        }
    }

    // Good - event handler coroutine
    val scope = rememberCoroutineScope()
    Button(
        onClick = {
            scope.launch {
                viewModel.executeAsyncOperation()
            }
        }
    ) { Text("Execute") }
}
```

### Preview Requirements

**Every Screen Composable** must have at least one `@Preview`:

```kotlin
@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CommandBlockPreview() {
    ConvoCLITheme {
        CommandBlock(
            block = CommandBlock(
                id = "1",
                command = "ls -la",
                output = "total 48\ndrwxr-xr-x  12 user",
                status = CommandStatus.COMPLETED
            ),
            onCopy = {},
            onRerun = {}
        )
    }
}
```

### Theme Usage

**Never Hardcode Colors**:

```kotlin
// Bad
Text(
    text = "Hello",
    color = Color(0xFF2196F3)
)

// Good
Text(
    text = "Hello",
    color = MaterialTheme.colorScheme.primary
)
```

---

## 3. Architecture & State Management

### MVI (Model-View-Intent) Pattern

ConvoCLI uses **MVI** for predictable state management:

```
User Intent → ViewModel → State Update → UI Render
     ↑                                        ↓
     └────────────── User Action ─────────────┘
```

**Components**:

**View (Composable)**:
- Renders UI based on state
- Emits user intents/events
- No business logic

**ViewModel**:
- Processes intents
- Updates state
- Single source of truth

**Model (State)**:
- Immutable data classes
- Represents UI state

```kotlin
// State
data class TerminalState(
    val blocks: List<CommandBlock> = emptyList(),
    val isExecuting: Boolean = false,
    val error: String? = null
)

// ViewModel
@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val repository: TermuxRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TerminalState())
    val state: StateFlow<TerminalState> = _state.asStateFlow()

    fun executeCommand(command: String) {
        _state.update { it.copy(isExecuting = true) }
        viewModelScope.launch {
            repository.executeCommand(command)
                .onSuccess { output ->
                    _state.update { state ->
                        state.copy(
                            blocks = state.blocks + CommandBlock(command, output),
                            isExecuting = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(error = error.message, isExecuting = false) }
                }
        }
    }
}

// View
@Composable
fun TerminalScreen(viewModel: TerminalViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()

    TerminalContent(
        blocks = state.blocks,
        isExecuting = state.isExecuting,
        onExecute = viewModel::executeCommand
    )
}
```

### Unidirectional Data Flow

**Rule**: Data flows down, events flow up

```
ViewModel (StateFlow) → Composable (render)
    ↑                           ↓
    └─────── Events ───────────┘
```

**Never**:
- Mutate state in Composables
- Call ViewModel methods from Composables except event handlers
- Pass MutableState to child composables

### Repository Pattern

**Single Source of Truth**: Repositories mediate between ViewModel and data sources

```kotlin
class TermuxRepository @Inject constructor(
    private val termuxService: TermuxService,
    private val commandDao: CommandDao
) {
    // Expose immutable flows
    val commandHistory: Flow<List<Command>> = commandDao.observeAll()

    suspend fun executeCommand(command: String): Result<String> {
        // Save to DB
        commandDao.insert(Command(text = command, timestamp = System.currentTimeMillis()))

        // Execute via Termux
        return try {
            val output = termuxService.execute(command)
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Dependency Injection (Hilt)

**ViewModel Injection**:
```kotlin
@HiltViewModel
class MyViewModel @Inject constructor(
    private val repository: MyRepository
) : ViewModel()
```

**Repository Injection**:
```kotlin
@Singleton
class MyRepository @Inject constructor(
    private val api: ApiService,
    private val dao: MyDao
)
```

**Module Example**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "convocli.db"
        ).build()
    }

    @Provides
    fun provideCommandDao(database: AppDatabase): CommandDao {
        return database.commandDao()
    }
}
```

### No Business Logic in Composables

**Bad**:
```kotlin
@Composable
fun CommandInput() {
    var command by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            // Bad - business logic in Composable
            val session = termuxService.createSession()
            session.write(command + "\n")
            val output = session.read()
            // ...
        }
    }) { Text("Execute") }
}
```

**Good**:
```kotlin
@Composable
fun CommandInput(
    onExecute: (String) -> Unit
) {
    var command by remember { mutableStateOf("") }

    Button(onClick = { onExecute(command) }) {
        Text("Execute")
    }
}

// Business logic in ViewModel
@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val repository: TermuxRepository
) : ViewModel() {
    fun executeCommand(command: String) {
        viewModelScope.launch {
            repository.executeCommand(command)
        }
    }
}
```

---

## 4. Testing Requirements

### Coverage Targets

**Minimum Coverage**:
- ViewModels: **80%**
- Repositories: **80%**
- Utilities: **90%**
- Composables: **60%** (UI tests)

**Run Coverage**:
```bash
./gradlew testDebugUnitTest jacocoTestReport
```

### Test Naming

**Format**: `given_when_then` or `should_when`

```kotlin
class TerminalViewModelTest {

    @Test
    fun `given valid command when execute then state updates with result`() {
        // Test implementation
    }

    @Test
    fun `should update state to executing when command starts`() {
        // Test implementation
    }
}
```

### Test Structure

**Use AAA Pattern**: Arrange, Act, Assert

```kotlin
@Test
fun `executeCommand updates state with output`() = runTest {
    // Arrange
    val repository = FakeTermuxRepository()
    val viewModel = TerminalViewModel(repository)

    // Act
    viewModel.executeCommand("ls")
    advanceUntilIdle()

    // Assert
    val state = viewModel.state.value
    assertEquals(1, state.blocks.size)
    assertEquals("ls", state.blocks.first().command)
    assertFalse(state.isExecuting)
}
```

### Fake Over Mock

**Preferred**: Create fake implementations

```kotlin
class FakeTermuxRepository : TermuxRepository {
    private val commands = mutableListOf<String>()
    var shouldFail = false

    override suspend fun executeCommand(command: String): Result<String> {
        commands.add(command)
        return if (shouldFail) {
            Result.failure(Exception("Command failed"))
        } else {
            Result.success("Output for $command")
        }
    }

    fun getExecutedCommands() = commands.toList()
}
```

### UI Testing

**Critical Flows Must Have UI Tests**:
- Command execution flow
- Gesture interactions
- Navigation between screens
- Error states

```kotlin
@RunWith(AndroidJUnit4::class)
class CommandBlocksScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun commandExecution_displaysResult() {
        composeTestRule.setContent {
            ConvoCLITheme {
                CommandBlocksScreen()
            }
        }

        // Type command
        composeTestRule.onNodeWithTag("command_input")
            .performTextInput("echo hello")

        // Execute
        composeTestRule.onNodeWithTag("execute_button")
            .performClick()

        // Verify output appears
        composeTestRule.onNodeWithText("hello")
            .assertIsDisplayed()
    }
}
```

### Test Data Builders

**For Complex Objects**:

```kotlin
fun commandBlock(
    id: String = UUID.randomUUID().toString(),
    command: String = "test command",
    output: String = "test output",
    status: CommandStatus = CommandStatus.COMPLETED,
    timestamp: Long = System.currentTimeMillis()
) = CommandBlock(id, command, output, status, timestamp)

// Usage
@Test
fun `test with multiple blocks`() {
    val blocks = listOf(
        commandBlock(command = "ls"),
        commandBlock(command = "pwd", status = CommandStatus.EXECUTING),
        commandBlock(command = "echo", output = "hello")
    )
    // Test with blocks
}
```

---

## 5. Git Workflow Conventions

### Branch Naming

**Format**: `type-description` (kebab-case)

**Types**:
- `feature-` : New features
- `sprint-##` : Sprint integration branches
- `hotfix-` : Emergency production fixes
- `docs-` : Documentation only changes

**Examples**:
- `feature-command-blocks`
- `feature-git-integration`
- `sprint-01`
- `hotfix-android-14-crash`

### Commit Messages

**Format**: Conventional Commits

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation only
- `refactor`: Code restructuring (no behavior change)
- `test`: Adding/updating tests
- `chore`: Build, dependencies, tooling

**Scopes** (component names):
- `blocks`, `terminal`, `ui`, `state`, `repo`, `termux`, `git`, `sync`

**Examples**:
```
feat(blocks): add swipe gesture support for command blocks

fix(terminal): resolve PTY buffer overflow on rapid output

docs(claude): add git workflow documentation

refactor(state): migrate ViewModels to MVI pattern

test(gestures): add multi-touch integration tests

chore(deps): update Compose BOM to 2025.10.00
```

### Merge Strategy

**Sprint/Develop/Main Merges**: `--no-ff` (preserve history)

```bash
git merge sprint-01 --no-ff -m "Merge sprint-01: Command blocks & Termux integration"
```

**Feature Squashing**: Optional before merge to sprint

```bash
# Squash feature commits before merging
git rebase -i sprint-01
# Or use merge --squash
git merge feature-command-blocks --squash
```

### Commit Signing

**Recommended**: GPG signing for all commits

**Not Required**: Pre-launch (but encouraged)

**Required**: Post-launch for main/develop branches

---

## 6. Performance Budgets

### APK Size

**Target**: < 25MB
**Maximum**: 30MB (initial release)
**Measure**: `./gradlew assembleRelease` and check APK size

**Mitigation if exceeded**:
- Enable ProGuard/R8 shrinking
- Remove unused resources
- Use vector drawables instead of PNGs
- Analyze with APK Analyzer

### App Startup Time

**Target**: < 1.5 seconds (cold start)
**Maximum**: 2 seconds
**Measure**: `adb shell am start -W com.convocli/.MainActivity`

**Optimization**:
- Lazy initialization of heavy objects
- Avoid work in Application.onCreate()
- Use Hilt for dependency injection
- Defer non-critical initialization

### Terminal I/O Latency

**Command Execution**: < 50ms from button press to execution start
**Output Rendering**: < 100ms for 1000 lines
**Keystroke Response**: < 16ms (60fps)

**Measure**: Custom benchmarking in tests

### Memory Footprint

**Idle**: < 80MB
**Active (command execution)**: < 150MB
**Maximum**: < 200MB

**Measure**: Android Studio Profiler

**Mitigation**:
- Limit command history size (1000 entries)
- Clear old command output
- Use LeakCanary to detect memory leaks

### UI Performance

**Scrolling**: 0 dropped frames (60fps)
**Animations**: 60fps sustained
**Frame budget**: 16ms per frame

**Requirements**:
- Always use `key()` in LazyColumn
- Avoid expensive operations in composition
- Use `derivedStateOf` for derived state

---

## 7. Accessibility Requirements

### Content Descriptions

**All Interactive Elements** must have contentDescription:

```kotlin
// Good
IconButton(
    onClick = { /*...*/ },
    modifier = Modifier.semantics {
        contentDescription = "Copy command output"
    }
) {
    Icon(Icons.Default.ContentCopy, contentDescription = null)  // Icon is decorative
}

// Bad - missing content description
IconButton(onClick = { /*...*/ }) {
    Icon(Icons.Default.ContentCopy, contentDescription = null)
}
```

### TalkBack Support

**Requirements**:
- Full app navigation possible with TalkBack only
- Logical focus order
- Announce state changes
- Custom actions for complex gestures

**Test**: Enable TalkBack and navigate entire app

### Touch Targets

**Minimum Size**: 48dp x 48dp for all interactive elements

```kotlin
// Good
Button(
    onClick = { /*...*/ },
    modifier = Modifier.size(48.dp)
) { /*...*/ }

// Bad - too small
IconButton(
    onClick = { /*...*/ },
    modifier = Modifier.size(24.dp)  // Below minimum
) { /*...*/ }
```

### Color Contrast

**Standard**: WCAG AA compliance (4.5:1 for normal text, 3:1 for large text)

**Test**: Use Accessibility Scanner app

**Material 3**: Provides accessible color schemes by default (use them!)

### Font Scaling

**Support**: Up to 200% text scale

**Test**: Settings → Display → Font size (set to largest)

**Implementation**:
```kotlin
// Good - scales with user preference
Text(
    text = "Command",
    style = MaterialTheme.typography.bodyLarge  // Uses SP, scales automatically
)

// Bad - fixed size
Text(
    text = "Command",
    fontSize = 16.dp  // Doesn't scale with user settings
)
```

### Semantic Properties

**Use Proper Semantics**:

```kotlin
Switch(
    checked = isEnabled,
    onCheckedChange = { /*...*/ },
    modifier = Modifier.semantics {
        contentDescription = "Enable background execution"
        stateDescription = if (isEnabled) "On" else "Off"
        role = Role.Switch
    }
)
```

---

## 8. Termux Integration Guidelines

### Modification Tracking

**All Termux Changes** must be in separate commits with `termux:` prefix:

```bash
# Good
git commit -m "termux: add command block detection to TerminalEmulator"

# Bad
git commit -m "feat(terminal): add command blocks and modify termux"
```

### Upstream Compatibility

**Document Divergence**: Any changes to Termux classes must be documented in commit message:

```
termux: modify TerminalEmulator to detect command boundaries

Changes:
- Add commandEndDetected() callback
- Track shell prompt patterns
- Emit events on command completion

Upstream impact: LOW
- Changes isolated to new callback
- No modification of core PTY handling
- Can be easily merged with upstream updates
```

### Fork Sync Strategy

**Quarterly Review**: Every 3 months, review upstream Termux changes

**Process**:
1. Check Termux GitHub for updates
2. Review changelog for relevant changes
3. Test merge feasibility
4. Create `chore(termux): sync with upstream vX.X.X` commit
5. Document any conflicts resolved

### Testing Against Termux

**Validate**:
- All existing Termux tests still pass
- New features don't break core terminal functionality
- PTY interface remains compatible

```bash
# Run Termux test suite
./gradlew :termux-core:test
```

### JNI Layer Caution

**Minimize Changes**: Avoid modifying native PTY implementation

**If Necessary**:
- Document thoroughly
- Add tests
- Consider contributing upstream

### API Documentation

**Document All Changes** to Termux public APIs:

```kotlin
/**
 * Custom extension to TerminalEmulator for command block detection.
 *
 * **Termux Modification**: Added in ConvoCLI fork
 * **Upstream Status**: Not in upstream, ConvoCLI-specific
 * **Sync Impact**: Will conflict on upstream merge, resolve by keeping this addition
 */
interface CommandBlockCallback {
    fun onCommandStart(command: String)
    fun onCommandComplete(output: String)
}
```

---

## 9. Security & Privacy

### No Hardcoded Secrets

**Never Commit**:
- API keys
- Tokens
- Passwords
- Private keys

**Use Instead**:
- Environment variables
- Android BuildConfig (with gitignored local.properties)
- Encrypted key storage

```kotlin
// Bad
const val API_KEY = "sk_live_12345abcdef"

// Good
val apiKey = BuildConfig.API_KEY  // From local.properties

// Better (runtime)
val apiKey = encryptedPrefs.getString("api_key", null)
```

### Data Encryption

**User Data**: Use EncryptedSharedPreferences

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val encryptedPrefs = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

### Network Security

**HTTPS Only**: No plaintext HTTP

**Certificate Pinning**: For ConvoSync cloud service

```xml
<!-- res/xml/network_security_config.xml -->
<network-security-config>
    <domain-config>
        <domain includeSubdomains="true">api.convocli.dev</domain>
        <pin-set>
            <pin digest="SHA-256">base64-encoded-pin-here</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

### Permissions

**Minimum Necessary**: Only request permissions actually needed

**Explain Before Requesting**:
```kotlin
// Show explanation dialog first
if (!hasStoragePermission()) {
    showStoragePermissionRationale {
        requestStoragePermission()
    }
}
```

**Required Permissions** (ConvoCLI):
- `INTERNET` (for cloud sync)
- `READ_EXTERNAL_STORAGE` / `WRITE_EXTERNAL_STORAGE` (for file access in terminal)
- `FOREGROUND_SERVICE` (for background execution)

### Data Retention

**Policy**:
- Command history: 30 days (configurable)
- Session backups: 7 days
- Logs: 24 hours

**Implementation**: Automatic cleanup in background worker

### Analytics

**Opt-In Only**: No analytics without user consent

**Privacy-Preserving**:
- No personally identifiable information (PII)
- Anonymized usage metrics only
- Local-first (only sync if user enables cloud)

### Third-Party Libraries

**Security Audit**: Review security implications before adding dependency

**Check**:
- Known vulnerabilities (use Snyk or Dependabot)
- Permissions required
- Data collection practices
- License compatibility (GPLv3)

---

## 10. Code Review Checklist

### Before Merging to Sprint

**Automated Checks**:
- [ ] All tests pass (`./gradlew test connectedAndroidTest`)
- [ ] ktlint passes (`./gradlew ktlintCheck`)
- [ ] No compiler warnings
- [ ] SpecSwarm quality analysis passed (`/specswarm:analyze-quality`)

**Code Quality**:
- [ ] No TODO or FIXME comments (create issues instead)
- [ ] No commented-out code
- [ ] No debug statements (Log.d, println) in production code
- [ ] Error handling implemented for all failure cases
- [ ] Resource cleanup (close files, cancel coroutines)

**Documentation**:
- [ ] KDoc added for public APIs
- [ ] README.md updated if necessary
- [ ] CHANGELOG.md updated with user-facing changes
- [ ] Architecture diagrams updated if structure changed

**Performance**:
- [ ] APK size within budget (< 30MB)
- [ ] No obvious performance issues (tested on device)
- [ ] Memory leaks checked (LeakCanary)
- [ ] LazyColumn uses `key()` parameter

**Accessibility**:
- [ ] Content descriptions added to interactive elements
- [ ] Tested with TalkBack
- [ ] Touch targets minimum 48dp
- [ ] Color contrast verified

**Security**:
- [ ] No hardcoded secrets
- [ ] Sensitive data encrypted
- [ ] Permissions justified and explained
- [ ] Input validation for user data

**Manual Testing**:
- [ ] Feature works on physical device
- [ ] Tested in dark mode
- [ ] Tested with different font sizes
- [ ] Rotation tested (state preserved)
- [ ] Screenshots/recordings for UI changes

**Git**:
- [ ] Commit messages follow Conventional Commits
- [ ] Branch name follows convention
- [ ] No merge conflicts
- [ ] Clean commit history (squashed if needed)

---

## Enforcement & Compliance

### Automated Enforcement

**SpecSwarm Integration**:

`/specswarm:analyze-quality` validates:
- Code style (ktlint)
- Test coverage
- Performance budgets
- Security issues

`/specswarm:complete` checks:
- All tests pass
- Constitution compliance
- Documentation updated

**Git Hooks** (optional):

```bash
# .git/hooks/pre-commit
#!/bin/bash
./gradlew ktlintCheck
if [ $? -ne 0 ]; then
    echo "❌ ktlint check failed. Run ./gradlew ktlintFormat"
    exit 1
fi
```

```bash
# .git/hooks/commit-msg
#!/bin/bash
if ! grep -qE '^(feat|fix|docs|refactor|test|chore)(\([a-z]+\))?: .+' "$1"; then
    echo "❌ Commit message must follow Conventional Commits format"
    exit 1
fi
```

### Manual Review

**Sprint Completion**: Review full checklist before merging sprint to develop

**Architecture Review**: Major features require architecture discussion

**Security Review**: Changes touching authentication, data storage, or network require security review

### Violation Handling

**Severity Levels**:

**Critical** (blocks merge):
- Security vulnerabilities
- Performance budget violations (>10% over)
- Failing tests
- Missing accessibility features

**High** (fix before sprint merge):
- Missing documentation
- Code style violations
- Moderate performance issues
- Incomplete error handling

**Medium** (create issue for next sprint):
- Minor performance optimizations
- Code cleanup opportunities
- Documentation improvements

**Low** (nice to have):
- Additional test coverage
- Refactoring suggestions

---

## Amendment Process

### Updating This Constitution

**When**:
- New patterns emerge
- Technology changes (Compose updates)
- Team feedback (if team grows)
- Lessons learned from production

**Process**:
1. **Propose Change**: Create issue with justification
2. **Discussion**: Explain why current standard is insufficient
3. **Documentation**: Update this file
4. **Announcement**: Note in CHANGELOG.md and sprint notes
5. **Retroactive**: Existing code not required to update unless modified

**Version History**:
- v1.0.0 (2025-10-20): Initial constitution

---

## References

**Official Documentation**:
- [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- [Jetpack Compose Guidelines](https://developer.android.com/jetpack/compose/guidelines)
- [Material Design 3](https://m3.material.io/)
- [Android Accessibility](https://developer.android.com/guide/topics/ui/accessibility)

**ConvoCLI Documentation**:
- [CLAUDE.md](/CLAUDE.md) - Development guide
- [docs/specification.md](/docs/specification.md) - Full project specification
- [README.md](/README.md) - Project overview

**Tools**:
- [ktlint](https://ktlint.github.io/) - Kotlin linter
- [LeakCanary](https://square.github.io/leakcanary/) - Memory leak detection
- [Detekt](https://detekt.dev/) - Static analysis

---

## Conclusion

This constitution provides the foundation for building ConvoCLI with consistency, quality, and maintainability. All contributors and SpecSwarm/SpecLabs agents must adhere to these standards.

**Remember**: These are guidelines to help us build better software. If a rule doesn't make sense in a specific context, document the exception and move forward pragmatically.

**Questions or Suggestions?**: Open an issue to discuss constitution amendments.

---

*Last updated: 2025-10-20 by Claude Code*
*Next review: 2026-01-20 (3 months)*
