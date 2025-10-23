# Regression Test: Bug 005 - Hilt ViewModel Injection

**Purpose**: Prove bug exists (build fails), validate fix (build succeeds), prevent future regressions

**Test Type**: Regression Test (Build + Unit Test)
**Created**: 2025-10-22

---

## Test Objective

Write a test that:
1. ✅ **Fails before fix** (build fails with Hilt error)
2. ✅ **Passes after fix** (build succeeds)
3. ✅ **Prevents regression** (catches if ViewModel injection reintroduced)

---

## Test Specification

### Test 1: Build Compilation Test

**Test Setup**:
- Clean build environment
- All source files present
- Gradle wrapper configured

**Test Execution**:
```bash
./gradlew clean assembleDebug
```

**Test Assertions**:
- ✅ Build completes successfully (exit code 0)
- ✅ No Hilt annotation processor errors
- ✅ APK file generated in `app/build/outputs/apk/debug/`

**Before Fix**: ❌ Build fails with Hilt ViewModel injection error
**After Fix**: ✅ Build succeeds

---

### Test 2: ViewModel Instantiation Test

**Test Setup**:
- Hilt test environment
- Mock dependencies (Context, CommandBlockManager, TerminalRepository, AnsiColorParser)

**Test Execution**:
Create unit test that verifies Hilt can instantiate ViewModels independently

**Test File**: `app/src/test/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModelHiltTest.kt`

```kotlin
@HiltAndroidTest
class CommandBlockViewModelHiltTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun commandBlockViewModel_canBeInstantiatedByHilt() {
        // Given: Hilt is configured
        hiltRule.inject()

        // When: ViewModel is requested via Hilt
        // (This would fail before fix due to ViewModel injection violation)
        val viewModel: CommandBlockViewModel = // get from Hilt

        // Then: ViewModel is successfully created
        assertNotNull(viewModel)
    }

    @Test
    fun terminalViewModel_canBeInstantiatedByHilt() {
        // Given: Hilt is configured
        hiltRule.inject()

        // When: ViewModel is requested via Hilt
        val viewModel: TerminalViewModel = // get from Hilt

        // Then: ViewModel is successfully created
        assertNotNull(viewModel)
    }

    @Test
    fun viewModels_haveIndependentLifecycles() {
        // Given: Both ViewModels instantiated
        val commandBlockVM: CommandBlockViewModel = // get from Hilt
        val terminalVM: TerminalViewModel = // get from Hilt

        // Then: They are different instances
        assertNotSame(commandBlockVM, terminalVM)

        // And: They don't hold references to each other
        // (This validates proper architecture)
    }
}
```

**Test Assertions**:
- ✅ CommandBlockViewModel instantiates without errors
- ✅ TerminalViewModel instantiates without errors
- ✅ ViewModels are independent instances
- ✅ No circular dependencies

**Before Fix**: ❌ Test fails - Hilt cannot create CommandBlockViewModel
**After Fix**: ✅ Test passes - Both ViewModels created successfully

---

### Test 3: Cancellation Functionality Test

**Test Setup**:
- Mock TerminalRepository
- CommandBlockViewModel instance (via Hilt)
- Test command block ID

**Test Execution**:
```kotlin
@Test
fun cancelCommand_callsRepositorySendSignal() {
    // Given: Mock repository
    val mockRepository = mock(TerminalRepository::class.java)
    val viewModel = CommandBlockViewModel(
        context = mockContext,
        commandBlockManager = mockManager,
        terminalRepository = mockRepository,  // After fix
        ansiColorParser = mockParser
    )

    // When: Cancel command called
    viewModel.cancelCommand("test-block-id")

    // Then: Repository sendSignal called (not ViewModel method)
    verify(mockRepository).sendSignal(any(), eq(2))
}
```

**Test Assertions**:
- ✅ Cancellation calls repository directly
- ✅ SIGINT signal (2) is sent
- ✅ CommandBlockManager.cancelBlock() is called

**Before Fix**: ❌ Cannot test - ViewModel dependency doesn't work
**After Fix**: ✅ Test passes - Proper repository usage

---

## Test Implementation

### Primary Test: Build Success

**File**: N/A (CI/CD or manual build)
**Command**: `./gradlew assembleDebug`

**Validation Criteria**:
- **Before Fix**: Exit code non-zero, Hilt error in output
- **After Fix**: Exit code 0, APK generated

### Secondary Test: Unit Tests

**File**: `app/src/test/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModelDependencyTest.kt`

**Test Names**:
- `test_bug_005_viewmodel_instantiation_with_repository`
- `test_bug_005_cancellation_uses_repository_not_viewmodel`
- `test_bug_005_no_viewmodel_to_viewmodel_injection`

---

## Edge Cases to Test

1. **Multiple ViewModel instances**: Verify each has independent lifecycle
2. **Session ID tracking**: Ensure CommandBlockViewModel correctly tracks current session
3. **Null session handling**: What happens if no session is active when cancel is called?
4. **Repository injection**: Verify TerminalRepository is properly provided by Hilt

---

## Regression Prevention

**Git Hook** (optional): Pre-push hook to catch ViewModel injection

```bash
# .git/hooks/pre-push
#!/bin/bash

# Search for ViewModel-to-ViewModel injection pattern
if grep -r "@HiltViewModel" app/src/main/kotlin | grep "ViewModel.*@Inject" | grep -q "ViewModel"; then
    echo "❌ ERROR: ViewModel-to-ViewModel injection detected!"
    echo "ViewModels should inject repositories, not other ViewModels."
    exit 1
fi
```

---

## Metadata

**Workflow**: Bugfix (regression-test-first)
**Created By**: SpecSwarm Bugfix Workflow
