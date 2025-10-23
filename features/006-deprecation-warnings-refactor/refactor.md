# Refactor 006: Deprecation Warnings Cleanup

**Status**: Active
**Created**: 2025-10-22
**Branch**: sprint-01
**Baseline Metrics**: [baseline-metrics.md](./baseline-metrics.md)

---

## Refactoring Goal

**What We're Improving**: Remove 3 deprecation warnings from Sprint 01 codebase

**Why**:
- Prepare for Gradle 9.0 compatibility
- Maintain clean build output
- Follow Kotlin/Compose best practices
- Remove technical debt before merging to develop

**Type**: Code Cleanup / Deprecation Removal

---

## Target Code

**Locations**:
1. `app/src/main/kotlin/com/convocli/bootstrap/impl/BootstrapValidatorImpl.kt:193`
2. `app/src/main/kotlin/com/convocli/ui/components/CommandBlockCard.kt:154`
3. `app/src/main/kotlin/com/convocli/ui/components/CommandInputBar.kt:70`

**Current Issues**:
- Unused variable declaration
- Use of deprecated icon (non-AutoMirrored version)
- Use of deprecated KeyboardOptions constructor

---

## Refactoring Plan

### Step 1: Remove Unused Variable
**File**: `BootstrapValidatorImpl.kt:193`
**Current**:
```kotlin
val requiredVars = listOf("HOME", "PREFIX", "TMPDIR", "PATH", "LANG")
// Variable is declared but never used
```
**Fix**: Remove the unused variable declaration
**Validation**: Build succeeds, no warnings

### Step 2: Update Deprecated Icon
**File**: `CommandBlockCard.kt:154`
**Current**:
```kotlin
imageVector = Icons.Filled.Assignment
```
**Fix**:
```kotlin
imageVector = Icons.AutoMirrored.Filled.Assignment
```
**Validation**: UI renders correctly, build without warning

### Step 3: Update KeyboardOptions Constructor
**File**: `CommandInputBar.kt:70`
**Current**:
```kotlin
KeyboardOptions(
    capitalization = KeyboardCapitalization.None,
    autoCorrect = false,
    keyboardType = KeyboardType.Text,
    imeAction = ImeAction.Done
)
```
**Fix**:
```kotlin
KeyboardOptions(
    capitalization = KeyboardCapitalization.None,
    autoCorrectEnabled = false,  // Renamed parameter
    keyboardType = KeyboardType.Text,
    imeAction = ImeAction.Done
)
```
**Validation**: Keyboard behavior unchanged, build without warning

---

## Behavior Preservation

**Critical**: No functional changes allowed

**Validation Strategy**:
1. Build before fixes (confirm 3 warnings)
2. Apply fix #1, build (confirm 2 warnings remain)
3. Apply fix #2, build (confirm 1 warning remains)
4. Apply fix #3, build (confirm 0 warnings)
5. Verify APK generates successfully

**No Tests Required**: These are syntactic changes only

---

## Expected Improvements

**Metrics**:
| Metric | Before | After | Improvement |
|--------|--------|-------|-------------|
| Deprecation Warnings | 3 | 0 | 100% |
| Build Cleanliness | Warnings present | Clean | ✅ |
| Gradle 9.0 Compat | At risk | Ready | ✅ |

---

## Risks

| Risk | Mitigation |
|------|------------|
| Icon doesn't exist | Check Material Icons Extended is imported |
| KeyboardOptions API changed | Verify parameter name in Compose docs |
| Build breaks | Simple syntax changes, very low risk |

**Overall Risk**: **Very Low** (simple replacements)

---

## Tech Stack Compliance

**Technologies Used**:
- Kotlin 1.9+
- Jetpack Compose (Material 3)
- Gradle 8.13

**Compliance**:
- ✅ Following Compose migration guides
- ✅ Using recommended Material Icons
- ✅ Adopting new Compose APIs

---

## Implementation Notes

### Import Changes Required

**CommandBlockCard.kt**:
```kotlin
// Add:
import androidx.compose.material.icons.automirrored.filled.Assignment
```

**CommandInputBar.kt**:
- No new imports required (parameter rename only)

### Testing Strategy

**Manual Validation**:
- Open CommandBlockCard in app → verify icon renders
- Open CommandInputBar in app → verify keyboard behavior unchanged
- Test autocorrect is disabled as before

---

## Timeline

**Estimated Time**: 15-20 minutes
- Reading warnings: 5 min
- Applying fixes: 5 min
- Building & validating: 5-10 min

---

## Success Criteria

✅ Zero deprecation warnings in build output
✅ Build completes successfully
✅ APK generates without errors
✅ Icons render correctly in UI
✅ Keyboard behavior unchanged
✅ No functional regressions

---

**Generated**: 2025-10-22
**Workflow**: SpecSwarm Refactor
**Type**: Deprecation Cleanup
