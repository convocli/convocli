# Bug 005: Hilt ViewModel Injection Violation

**Status**: Active
**Created**: 2025-10-22
**Priority**: High
**Severity**: Critical (blocks build)

## Symptoms

Build fails with Hilt compilation error:

```
error: [dagger.hilt.android.processor.internal.viewmodel.ViewModelValidationPlugin]
Injection of an @HiltViewModel class is prohibited since it does not create a ViewModel instance correctly.
Access the ViewModel via the Android APIs (e.g. ViewModelProvider) instead.
Injected ViewModel: com.convocli.terminal.viewmodel.TerminalViewModel

    com.convocli.terminal.viewmodel.TerminalViewModel is injected at
        com.convocli.ui.viewmodels.CommandBlockViewModel(…, terminalViewModel, …)
```

- Project cannot compile
- Hilt annotation processor rejects the dependency graph
- Build fails at `compileDebugKotlin` task

## Reproduction Steps

1. Checkout `sprint-01` branch
2. Run `./gradlew assembleDebug`
3. Observe Hilt compilation error

**Expected Behavior**: Project compiles successfully

**Actual Behavior**: Hilt annotation processor rejects ViewModel-to-ViewModel injection

## Root Cause Analysis

### Ultrathinking - Multi-Layer Analysis

**Layer 1: Direct Cause**
- **File**: `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt:36`
- **Issue**: CommandBlockViewModel constructor directly injects TerminalViewModel
```kotlin
@HiltViewModel
class CommandBlockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commandBlockManager: CommandBlockManager,
    private val terminalViewModel: TerminalViewModel,  // ❌ WRONG: Cannot inject ViewModel
    private val ansiColorParser: AnsiColorParser
) : ViewModel()
```

**Layer 2: Why This Is Wrong (Hilt Architecture)**
- Hilt ViewModels have special lifecycle management via `@HiltViewModel` annotation
- They are created by Hilt's ViewModelFactory, not regular dependency injection
- ViewModelProvider API manages their lifecycle (tied to Activity/Fragment lifecycle)
- Injecting ViewModel → ViewModel bypasses this system
- The injected ViewModel wouldn't have proper scoping or lifecycle

**Layer 3: When This Was Introduced**
- **Feature 004** - Command Blocks UI
- **Task**: T045 - Wire up cancellation in CommandBlockViewModel
- **Change**: Added `terminalViewModel.sendInterrupt()` call in `cancelCommand()`
- **Mistake**: Injected entire TerminalViewModel instead of using shared repository

**Layer 4: Proper Architecture Pattern**
ViewModels should follow this pattern:
```
┌─────────────────────────┐
│  CommandBlockViewModel  │
│  @HiltViewModel         │
└───────────┬─────────────┘
            │
            ├──> CommandBlockManager (✓ inject)
            ├──> TerminalRepository (✓ inject)
            └──> AnsiColorParser     (✓ inject)

┌─────────────────────────┐
│  TerminalViewModel      │
│  @HiltViewModel         │
└───────────┬─────────────┘
            │
            └──> TerminalRepository (✓ inject)
```

Both ViewModels should inject the **TerminalRepository**, not each other.

**Layer 5: Design Decision - Why Repositories, Not ViewModels**
- **Single Responsibility**: Repositories handle data operations
- **Reusability**: Multiple ViewModels can share same repository
- **Testability**: Easy to mock repository in tests
- **Lifecycle**: Repositories are singletons, ViewModels are lifecycle-scoped
- **Hilt Compliance**: Repositories can be injected anywhere, ViewModels cannot

## Impact Assessment

**Affected Users**: All developers (build broken)
- Cannot compile project
- Cannot run app
- Cannot test changes

**Affected Features**:
- Feature 004: Command Blocks UI - completely broken
- Cancellation support - non-functional

**Severity Justification**: Critical - blocks all development work

**Workaround Available**: No - build must compile

## Regression Test Requirements

Since this is a build error, the regression test is the build itself:

1. **Test**: Project compiles without Hilt errors
2. **Test**: CommandBlockViewModel can be instantiated by Hilt
3. **Test**: TerminalViewModel can be instantiated by Hilt
4. **Test**: Both ViewModels have independent lifecycles
5. **Test**: Cancellation still works (sendInterrupt via repository)

**Test Success Criteria**:
- ✅ `./gradlew assembleDebug` succeeds
- ✅ No Hilt annotation processor errors
- ✅ ViewModels properly scoped
- ✅ Cancellation functionality preserved

## Proposed Solution

### Changes Required

**File 1**: `CommandBlockViewModel.kt:36`
- **Remove**: Direct injection of TerminalViewModel
- **Add**: Injection of TerminalRepository
- **Change**: `cancelCommand()` calls `repository.sendSignal()` instead of `viewModel.sendInterrupt()`

**Before**:
```kotlin
@HiltViewModel
class CommandBlockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commandBlockManager: CommandBlockManager,
    private val terminalViewModel: TerminalViewModel,  // ❌ WRONG
    private val ansiColorParser: AnsiColorParser
) : ViewModel() {
    fun cancelCommand(blockId: String) {
        viewModelScope.launch {
            terminalViewModel.sendInterrupt()  // ❌ WRONG
            commandBlockManager.cancelBlock(blockId)
        }
    }
}
```

**After**:
```kotlin
@HiltViewModel
class CommandBlockViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val commandBlockManager: CommandBlockManager,
    private val terminalRepository: TerminalRepository,  // ✅ CORRECT
    private val ansiColorParser: AnsiColorParser
) : ViewModel() {

    // Need to track current session ID
    private var currentSessionId: String? = null

    init {
        // Observe terminal state to get session ID
        viewModelScope.launch {
            terminalRepository.observeCurrentSession()
                .collect { sessionId ->
                    currentSessionId = sessionId
                }
        }
    }

    fun cancelCommand(blockId: String) {
        viewModelScope.launch {
            currentSessionId?.let { sessionId ->
                terminalRepository.sendSignal(sessionId, signal = 2)  // ✅ CORRECT
            }
            commandBlockManager.cancelBlock(blockId)
        }
    }
}
```

**File 2**: `TerminalRepository.kt` (may need enhancement)
- **Add**: Method to observe current session ID
- **Reason**: CommandBlockViewModel needs to know which session to send signal to

**Risks**:
- Minimal - just refactoring dependency injection
- No logic changes needed
- Cancellation functionality preserved

**Alternative Approaches**:
1. ~~Pass session ID explicitly~~ - Too complex, couples UI to session management
2. ~~Use shared state holder~~ - Adds complexity, violates MVVM
3. **✅ Inject repository directly** - Clean, follows Hilt patterns

---

## Tech Stack Compliance

**Tech Stack File**: /home/marty/code-projects/convocli/.specswarm/tech-stack.md
**Validation Status**: Compliant

**Relevant Stack Rules**:
- Hilt for dependency injection ✅
- MVI pattern for ViewModels ✅
- Repository pattern for data access ✅
- ViewModels should not inject other ViewModels ✅

---

## Metadata

**Workflow**: Bugfix (regression-test-first)
**Created By**: SpecSwarm Bugfix Workflow
**Integration Mode**: Sequential (basic mode)
