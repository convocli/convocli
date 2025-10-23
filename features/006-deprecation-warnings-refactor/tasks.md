# Tasks: Refactor 006 - Deprecation Warnings Cleanup

**Workflow**: Refactor (Deprecation Removal)
**Status**: Active
**Created**: 2025-10-22

---

## Execution Strategy

**Mode**: Sequential (incremental fixes)
**Validation**: Build after each fix
**Risk**: Very Low (simple syntax changes)

---

## Phase 1: Baseline Establishment

### T001: Confirm Baseline Warnings
**Description**: Build project and confirm 3 deprecation warnings exist
**Command**: `./gradlew assembleDebug 2>&1 | grep -E "deprecated|Variable.*never used"`
**Expected**: 3 warnings identified
**Status**: ⏳ Pending

---

## Phase 2: Incremental Fixes

### T002: Fix Unused Variable Warning
**Description**: Remove unused `requiredVars` variable from BootstrapValidatorImpl.kt
**File**: `app/src/main/kotlin/com/convocli/bootstrap/impl/BootstrapValidatorImpl.kt:193`
**Changes**:
  - Remove line: `val requiredVars = listOf("HOME", "PREFIX", "TMPDIR", "PATH", "LANG")`
**Validation**: Build succeeds, 2 warnings remain
**Parallel**: No
**Status**: ⏳ Pending

### T003: Build After Fix #1
**Description**: Verify build succeeds with 2 warnings remaining
**Command**: `./gradlew compileDebugKotlin 2>&1 | grep -c "deprecated"`
**Expected**: 2 warnings
**Parallel**: No (depends on T002)
**Status**: ⏳ Pending

---

### T004: Fix Deprecated Icon Warning
**Description**: Update Icons.Filled.Assignment to AutoMirrored version
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt:154`
**Changes**:
  - Import: `androidx.compose.material.icons.automirrored.filled.Assignment`
  - Replace: `Icons.Filled.Assignment` → `Icons.AutoMirrored.Filled.Assignment`
**Validation**: Build succeeds, 1 warning remains
**Parallel**: No
**Status**: ⏳ Pending

### T005: Build After Fix #2
**Description**: Verify build succeeds with 1 warning remaining
**Command**: `./gradlew compileDebugKotlin 2>&1 | grep -c "deprecated"`
**Expected**: 1 warning
**Parallel**: No (depends on T004)
**Status**: ⏳ Pending

---

### T006: Fix Deprecated KeyboardOptions Constructor
**Description**: Update KeyboardOptions to use new constructor with autoCorrectEnabled
**File**: `app/src/main/kotlin/com/convocli/ui/components/CommandInputBar.kt:70`
**Changes**:
  - Replace parameter: `autoCorrect = false` → `autoCorrectEnabled = false`
**Validation**: Build succeeds, 0 warnings
**Parallel**: No
**Status**: ⏳ Pending

### T007: Build After Fix #3 (Final Validation)
**Description**: Verify build succeeds with ZERO warnings
**Command**: `./gradlew clean assembleDebug 2>&1 | grep -E "deprecated|Variable.*never used" | wc -l`
**Expected**: 0 warnings
**Parallel**: No (depends on T006)
**Status**: ⏳ Pending

---

## Phase 3: Final Validation

### T008: Verify APK Generation
**Description**: Confirm APK is generated successfully
**Command**: `ls -lh app/build/outputs/apk/debug/app-debug.apk`
**Expected**: APK file exists (~24MB)
**Parallel**: No
**Status**: ⏳ Pending

### T009: Commit Refactoring
**Description**: Commit all deprecation warning fixes
**Message**:
```
refactor: remove 3 deprecation warnings from Sprint 01

**Fixed**:
- Removed unused variable in BootstrapValidatorImpl (line 193)
- Updated Icon to AutoMirrored version in CommandBlockCard (line 154)
- Updated KeyboardOptions to new constructor in CommandInputBar (line 70)

**Impact**: Zero deprecation warnings, Gradle 9.0 ready

🤖 Generated with [Claude Code](https://claude.com/claude-code)

Co-Authored-By: Claude <noreply@anthropic.com>
```
**Parallel**: No
**Status**: ⏳ Pending

---

## Summary

**Total Tasks**: 9
**Estimated Time**: 15-20 minutes
**Parallel Opportunities**: None (sequential validation required)

**Success Criteria**:
- ✅ Zero deprecation warnings
- ✅ Build succeeds cleanly
- ✅ APK generates successfully
- ✅ No functional changes
- ✅ Committed to sprint-01 branch

---

**Next Step**: Execute T001 to confirm baseline warnings
