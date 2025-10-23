# Tasks: Bug 005 - Hilt ViewModel Injection Violation

**Workflow**: Bugfix (Regression-Test-First)
**Status**: Active
**Created**: 2025-10-22

---

## Execution Strategy

**Mode**: Sequential (basic mode)
**Smart Integration**: None (SpecSwarm/SpecTest not in use for this session)

---

## Phase 1: Regression Test Creation

### T001: Verify Build Fails (Baseline)
**Description**: Confirm build currently fails with Hilt error (proves bug exists)
**Command**: `./gradlew assembleDebug 2>&1 | grep -A 5 "HiltViewModel"`
**Expected**: Build fails with ViewModel injection error message
**Validation**: Error message matches bug symptoms
**Status**: ⏳ Pending

---

## Phase 2: Bug Fix Implementation

### T002: Remove ViewModel-to-ViewModel Injection
**Description**: Refactor CommandBlockViewModel to inject TerminalRepository instead of TerminalViewModel
**Files**:
  - `app/src/main/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModel.kt`
**Changes**:
  - Remove `terminalViewModel: TerminalViewModel` from constructor
  - Add `terminalRepository: TerminalRepository` to constructor
  - Add session ID tracking in init block
  - Update `cancelCommand()` to call `repository.sendSignal()` instead of `viewModel.sendInterrupt()`
**Parallel**: No (core fix)
**Status**: ⏳ Pending

### T003: Verify Build Succeeds (Regression Test)
**Description**: Confirm build succeeds after fix (proves bug fixed)
**Command**: `./gradlew clean assembleDebug`
**Expected**: Build completes successfully, APK generated
**Validation**:
  - Exit code 0
  - No Hilt errors in output
  - APK exists at `app/build/outputs/apk/debug/app-debug.apk`
**Parallel**: No (depends on T002)
**Status**: ⏳ Pending

---

## Phase 3: Functional Validation

### T004: Test Cancellation Still Works
**Description**: Verify cancellation functionality preserved after refactor
**Method**: Manual test or add unit test
**Expected**: Cancel button sends SIGINT to terminal session
**Validation**: Command is interrupted when cancel button clicked
**Parallel**: No
**Status**: ⏳ Pending

### T005: Add Unit Test for Repository Usage
**Description**: Create regression test to prevent future ViewModel injection
**File**: `app/src/test/kotlin/com/convocli/ui/viewmodels/CommandBlockViewModelDependencyTest.kt`
**Test**: Verify CommandBlockViewModel injects repository, not ViewModel
**Parallel**: Yes (can be done alongside T004)
**Status**: ⏳ Pending

---

## Phase 4: Final Validation

### T006: Run Full Test Suite
**Description**: Verify no regressions in existing tests
**Command**: `./gradlew test`
**Expected**: All tests pass
**Validation**: 100% test pass rate
**Parallel**: No (final validation)
**Status**: ⏳ Pending

---

## Summary

**Total Tasks**: 6
**Estimated Time**: 30-60 minutes
**Parallel Opportunities**: Limited (T004 and T005 can run in parallel)

**Success Criteria**:
- ✅ Build fails initially (proves bug exists)
- ✅ Fix refactors ViewModel dependencies
- ✅ Build succeeds after fix
- ✅ Cancellation functionality preserved
- ✅ Unit test added (prevents regression)
- ✅ No test suite regressions

---

**Next Step**: Execute T001 to verify current build failure
