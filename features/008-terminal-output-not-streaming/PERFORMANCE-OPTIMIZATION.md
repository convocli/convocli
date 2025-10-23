# Performance Optimization Plan - Future Sprint

**Date Created**: 2025-10-23
**Current Status**: Deferred to Sprint 03+
**Priority**: P2 - Performance Enhancement
**Current Startup Time**: ~4000ms
**Target Startup Time**: <500ms

---

## Current Performance Issues

### Measured Metrics (from logcat)

```
Skipped 263 frames!  (~4.4 seconds of frozen UI)
Davey! duration=4672ms
Davey! duration=1823ms
Davey! duration=1667ms
Davey! duration=1043ms
Davey! duration=991ms
```

### Root Causes Identified

1. **Room Database Initialization** (estimated ~1500ms)
   - CommandBlockManager observes database on ViewModel init
   - Initial database query runs during startup
   - No lazy loading implemented

2. **Hilt Dependency Graph Construction** (estimated ~1000ms)
   - Complex dependency tree
   - All dependencies instantiated eagerly
   - No lazy injection used

3. **Compose Initial Composition** (estimated ~1000ms)
   - First composition is expensive
   - Baseline profile not optimized yet
   - No composition skipping optimizations

4. **Terminal Repository Initialization** (fixed to background, but still ~500ms)
   - File system checks (now on background thread)
   - Environment setup
   - Session client creation

---

## Optimization Strategy

### Phase 1: Lazy Initialization (Target: 2000ms → 1000ms)

**Task 1.1: Lazy Room Database Loading**
```kotlin
// Current (eager)
init {
    viewModelScope.launch {
        commandBlockManager.observeBlocks().collect { ... }  // ← Immediate DB query
    }
}

// Proposed (lazy)
private var blocksInitialized = false

fun ensureBlocksLoaded() {
    if (!blocksInitialized) {
        viewModelScope.launch {
            commandBlockManager.observeBlocks().collect { ... }
        }
        blocksInitialized = true
    }
}
```

**Impact**: Defer database access until first screen interaction (~1500ms saved)

**Task 1.2: Lazy ViewModel Creation**
```kotlin
// Use LazyComposable pattern
@Composable
fun CommandBlocksScreen() {
    val viewModel = rememberLazy {
        hiltViewModel<CommandBlockViewModel>()
    }

    // ViewModel only created when first accessed
}
```

**Impact**: Defer ViewModel creation until composition complete (~500ms saved)

---

### Phase 2: Hilt Optimization (Target: 1000ms → 500ms)

**Task 2.1: Use @AssistedInject for Heavy Dependencies**
```kotlin
@HiltViewModel(assistedFactory = CommandBlockViewModelFactory::class)
class CommandBlockViewModel @AssistedInject constructor(
    @Assisted private val lazyRepository: () -> TerminalRepository,  // ← Lazy
    private val commandBlockManager: CommandBlockManager
)
```

**Impact**: Defer expensive dependency creation (~300ms saved)

**Task 2.2: Add @Reusable Scope for Stateless Dependencies**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    @Reusable  // ← Not singleton, but reused when possible
    fun provideTerminalOutputProcessor(): TerminalOutputProcessor {
        return TerminalOutputProcessor()
    }
}
```

**Impact**: Faster DI graph construction (~200ms saved)

---

### Phase 3: Compose Optimization (Target: 500ms → 200ms)

**Task 3.1: Generate Baseline Profile**
```bash
# Use ProfileInstaller to generate baseline profile
./gradlew generateBaselineProfile
```

**Impact**: Faster first composition (~300ms saved)

**Task 3.2: Stability Annotations**
```kotlin
@Stable
data class CommandBlocksUiState(...)

@Immutable
data class CommandBlock(...)
```

**Impact**: Better recomposition skipping (~100ms saved)

**Task 3.3: Deferred Composition**
```kotlin
@Composable
fun CommandBlocksScreen() {
    // Show simple loading state first
    var isFullyComposed by remember { mutableStateOf(false) }

    if (!isFullyComposed) {
        SimpleLoadingScreen()
        LaunchedEffect(Unit) {
            delay(16) // One frame
            isFullyComposed = true
        }
    } else {
        FullCommandBlocksUI()
    }
}
```

**Impact**: Faster perceived load time (~100ms saved)

---

### Phase 4: Terminal Optimization (Target: 200ms → <100ms)

**Task 4.1: Cached File System Checks**
```kotlin
private val shellExistsCache = atomic(null as Boolean?)

fun checkShellExists(): Boolean {
    return shellExistsCache.value ?: run {
        val exists = File(shellPath).exists()
        shellExistsCache.value = exists
        exists
    }
}
```

**Impact**: Eliminate redundant file I/O (~50ms saved)

**Task 4.2: Parallel Initialization**
```kotlin
override fun createSession(): Flow<TerminalSessionState> = flow {
    // Run file checks and environment setup in parallel
    val deferredFileCheck = async(Dispatchers.IO) { checkFiles() }
    val deferredEnvSetup = async(Dispatchers.IO) { setupEnvironment() }

    awaitAll(deferredFileCheck, deferredEnvSetup)
    // ...
}
```

**Impact**: Reduce sequential delays (~50ms saved)

---

## Implementation Roadmap

### Sprint 03: Quick Wins (Target: 4000ms → 2000ms)
- [ ] Task 1.1: Lazy Room database loading
- [ ] Task 2.1: Assisted injection for heavy deps
- [ ] Task 3.1: Generate baseline profile

**Estimated effort**: 2-3 days
**Expected improvement**: 50% reduction in startup time

### Sprint 04: Advanced Optimizations (Target: 2000ms → 500ms)
- [ ] Task 1.2: Lazy ViewModel creation
- [ ] Task 2.2: Reusable scopes
- [ ] Task 3.2: Stability annotations
- [ ] Task 3.3: Deferred composition

**Estimated effort**: 3-4 days
**Expected improvement**: 75% reduction in total startup time

### Sprint 05: Final Polish (Target: 500ms → <200ms)
- [ ] Task 4.1: Cached file system checks
- [ ] Task 4.2: Parallel initialization
- [ ] Performance testing & profiling
- [ ] A/B testing with users

**Estimated effort**: 2-3 days
**Expected improvement**: 95% reduction in total startup time

---

## Measurement Plan

### Before Optimization
```bash
adb shell am start -W com.convocli/.MainActivity
# Baseline: TotalTime ~4000ms
```

### After Each Phase
```bash
# Automated performance test
./gradlew connectedCheck -Pandroid.testInstrumentationRunnerArguments.class=com.convocli.StartupPerformanceTest

# Manual verification
adb shell am start -W com.convocli/.MainActivity
adb logcat | grep -E "Davey|Skipped.*frames"
```

### Target Metrics
- TotalTime: <500ms (currently ~4000ms)
- Skipped frames: <10 (currently 263)
- Davey violations: 0 (currently 5+)

---

## Alternatives Considered

### Option A: Complete Lazy Initialization (Recommended)
- Pros: Fastest perceived startup, best user experience
- Cons: More complex code, deferred errors
- **Decision**: Implement in Sprint 03

### Option B: Eager Initialization with Progress UI
- Pros: Simpler code, all errors shown upfront
- Cons: Slower startup, worse user experience
- **Decision**: Not recommended

### Option C: Background Service Pre-warming
- Pros: Near-instant app startup
- Cons: Battery drain, complex lifecycle management
- **Decision**: Defer to Sprint 06+ (post-MVP)

---

## Success Criteria

### Must Have (Sprint 03)
- ✅ Startup time <2000ms
- ✅ No Davey violations >1000ms
- ✅ Error messages still display correctly

### Should Have (Sprint 04)
- ✅ Startup time <500ms
- ✅ Skipped frames <30
- ✅ Smooth UI animations during startup

### Nice to Have (Sprint 05)
- ✅ Startup time <200ms
- ✅ No skipped frames
- ✅ Perceived instant load

---

## Dependencies

**Blockers**: None (can start anytime after Sprint 02)

**Prerequisites**:
- Performance testing infrastructure
- Baseline profile generation setup
- Profiling tools configured

**Related Work**:
- Bootstrap installation optimization (Feature 003)
- Database migration strategy (Sprint 04)

---

## References

- Android Performance Docs: https://developer.android.com/topic/performance
- Baseline Profiles: https://developer.android.com/topic/performance/baselineprofiles
- Compose Performance: https://developer.android.com/jetpack/compose/performance
- Hilt Best Practices: https://developer.android.com/training/dependency-injection/hilt-android

---

## Notes

**Why defer to Sprint 03?**

The current 4-second startup, while not ideal, does NOT:
- Crash the app
- Block user input permanently
- Hide error messages
- Prevent core functionality

Therefore, it's acceptable for MVP (Sprint 02) and can be optimized as a performance enhancement in Sprint 03 once core features are stable.

**User Impact**:
- Current: App works but feels slow on first launch
- After optimization: App feels instant and responsive
- Estimated user satisfaction improvement: +40%

**Business Impact**:
- Current: Acceptable for early adopters and beta testing
- After optimization: Production-ready for general release
- F-Droid rating potential: 3.5★ → 4.5★+
