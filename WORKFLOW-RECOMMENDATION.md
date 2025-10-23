# 🤖 Workflow Recommendation

**Analysis Date**: 2025-10-23
**Branch**: sprint-02
**Context**: Sprint 02 development in progress

---

## Context Analysis

**Current Branch**: `sprint-02`

**Recent Activity** (Last 10 commits):
- `refactor: remove Context dependency from ViewModel`
- `refactor: fix blocking I/O and externalize hardcoded strings`
- `docs: add Sprint 01 quality analysis and Sprint 02 planning`
- `Merge sprint-01: Command Blocks UI & Termux Foundation`
- Multiple bug fixes and documentation updates

**Git Status**:
- **14 files modified**: Database, DI modules, terminal components, UI components
- **5 files deleted**: Old repository/service implementations (refactoring)
- **6 new files untracked**: New data models, DAOs, terminal implementations
- **2 feature directories untracked**: features/007, features/008

**Feature Analysis**:
- **Feature 007**: Terminal Output Integration - Status: "Ready for Implementation" (has spec, plan, tasks)
- **Feature 008**: Terminal Output Not Streaming - Status: "RESOLVED" (bug fixed)

**Keywords Found**:
- "refactor" (2x in recent commits)
- "fix" (1x)
- "docs" (2x)
- Major architectural changes (deleted old services, created new implementations)

---

## Pattern Scores

| Workflow | Score | Confidence | Reasoning |
|----------|-------|------------|-----------|
| **Complete Sprint** | **15** | **🟢 HIGH** | Sprint branch + uncommitted work + feature directories |
| Feature (New) | 3 | 🟡 MEDIUM | Feature 007 ready but not started |
| Refactor | 7 | 🟡 MEDIUM | Multiple refactor commits, but part of sprint work |
| Bugfix | 2 | 🔴 LOW | Bug 008 already resolved |
| Modify | 3 | 🔴 LOW | No modify pattern detected |
| Hotfix | 0 | 🔴 LOW | No emergency indicators |
| Deprecate | 0 | 🔴 LOW | No deprecation indicators |

---

## 🎯 Primary Recommendation: Complete Sprint 02

### Workflow: `/specswarm:complete`

**Confidence**: 🟢 **HIGH** (Score: 15/20)

**Reasoning**:
1. **On sprint branch** (`sprint-02`) with substantial uncommitted work
2. **Feature 007 has full documentation** (spec.md, plan.md, tasks.md) but STATUS shows "Ready for Implementation" - not started
3. **Feature 008 is RESOLVED** - crash fixed, documented, ready to merge
4. **Major refactoring complete** - old services deleted, new architecture in place
5. **14 modified files + 6 new files** = substantial sprint work uncommitted

**Why This Workflow**:
The `/specswarm:complete` workflow is designed for exactly this scenario:
- Validate all tests pass ✅
- Update documentation ✅
- Merge feature branch to sprint/develop ✅
- Tag sprint completion ✅
- Clean up feature branches ✅

**Expected Outcome**:
- Sprint 02 work officially completed and merged
- Clean git history with proper tags
- Documentation updated
- Ready to start Sprint 03 with Feature 003 (Bootstrap Installation)

---

## ⚠️ **CRITICAL BLOCKER DETECTED**

### Feature 007 Status Contradiction

**Problem**: Feature 007 has comprehensive documentation but shows "Ready for Implementation" status, yet significant implementation code exists in the codebase:

**Evidence**:
- New files: `TerminalRepository.kt`, `CommandBlockManager.kt`, `TerminalOutputProcessor.kt`
- Modified: `CommandBlockViewModel.kt`, `TerminalRepositoryImpl.kt`
- Feature 007 directory: `spec.md`, `plan.md`, `tasks.md` (all created Oct 22)
- Bug 008 directory: Crash fixes applied, RESOLVED status

**Analysis**: It appears Feature 007 was **partially implemented** during Bug 008 crash investigation. The terminal output integration needed for crash testing resulted in implementing core Feature 007 components.

**Recommendation**: Before running `/specswarm:complete`, assess Feature 007 implementation status:

```bash
# Check if Feature 007 core functionality works
./gradlew test --tests "*Terminal*"
./gradlew test --tests "*CommandBlock*"

# Manual verification:
# 1. Does terminal output stream to command blocks?
# 2. Do commands execute with real output?
# 3. Does prompt detection work?
```

**Two Paths Forward**:

### Path A: Feature 007 is Complete (Likely)
If terminal output integration works:
```bash
# 1. Mark Feature 007 as complete
echo "Status: Complete" >> features/007-*/spec.md

# 2. Complete sprint
/specswarm:complete

# 3. Start Sprint 03 with Feature 003
```

### Path B: Feature 007 Needs More Work (Unlikely)
If significant gaps remain:
```bash
# 1. Continue Feature 007 implementation
/specswarm:implement features/007-*

# 2. Then complete sprint
/specswarm:complete
```

---

## Alternative Workflows (If Primary Doesn't Fit)

### Alternative 1: `/specswarm:implement`
**Confidence**: 🟡 MEDIUM
**When to Use**: If you determine Feature 007 needs more implementation work
**Command**: `/specswarm:implement features/007-terminal-output-integration-*`

### Alternative 2: `/specswarm:analyze-quality`
**Confidence**: 🟢 HIGH
**When to Use**: Before merging sprint, get comprehensive quality assessment
**Command**: `/specswarm:analyze-quality`
**Why**: Identifies code quality issues, technical debt, and missing tests before sprint merge

### Alternative 3: Start New Feature (Feature 003)
**Confidence**: 🔴 LOW (premature)
**When to Use**: ONLY after Sprint 02 is properly closed
**Command**: `/specswarm:specify "Bootstrap Installation..."`
**Why NOT Now**: Must close current sprint first (git workflow hygiene)

---

## Not Recommended

### ❌ New Feature Development
**Why**: Sprint 02 work is incomplete and uncommitted. Starting Feature 003 now would:
- Create messy git history
- Mix sprint work
- Violate project's git workflow
- Make it hard to track what belongs to which sprint

### ❌ Bugfix Workflow
**Why**: Bug 008 is already resolved. No active bugs detected.

### ❌ Hotfix Workflow
**Why**: No production emergency or critical issues detected.

---

## Recommended Action Sequence

### Step 1: Assess Feature 007 Implementation (15 minutes)
```bash
# Check what's actually implemented
grep -r "TerminalRepository" app/src/main/kotlin/com/convocli/terminal/
grep -r "CommandBlockManager" app/src/main/kotlin/com/convocli/terminal/

# Check if commands execute with real output
# (requires device testing if bootstrap installed)

# Read implementation status
cat features/007-*/spec.md | grep -i status
cat features/007-*/tasks.md | head -50
```

### Step 2: Run Quality Analysis (5 minutes)
```bash
/specswarm:analyze-quality
```
This will reveal:
- Code quality issues
- Missing tests
- Documentation gaps
- Technical debt
- Whether Feature 007 is truly complete

### Step 3A: If Feature 007 is Complete
```bash
# Update feature status
echo "**Implementation Status**: Complete (Oct 23, 2025)" >> features/007-*/spec.md
echo "**Completed During**: Bug 008 crash investigation" >> features/007-*/spec.md

# Complete sprint
/specswarm:complete
```

### Step 3B: If Feature 007 Needs Work
```bash
# Continue implementation
/specswarm:implement features/007-terminal-output-integration-*

# Then complete sprint when done
/specswarm:complete
```

### Step 4: Start Sprint 03 with Feature 003
```bash
# Establish project standards
/specswarm:constitution

# Start critical blocker feature
/specswarm:specify "Termux Bootstrap Installation

[Full feature spec - enables terminal functionality]"

# Then: clarify → plan → tasks → implement → complete
```

---

## Success Criteria

### Sprint 02 Complete:
- ✅ All files committed
- ✅ Tests passing
- ✅ Documentation updated
- ✅ Sprint merged to develop
- ✅ Branch tagged: `sprint-02-complete`
- ✅ Ready for Sprint 03

### Feature 003 (Next):
- ✅ Bootstrap downloads and installs
- ✅ Terminal commands work (ls, pwd, echo)
- ✅ Error messages gone
- ✅ App fully functional

---

## Priority Matrix

| Action | Priority | Blocker? | Effort | Impact |
|--------|----------|----------|--------|--------|
| **Assess Feature 007 status** | P0 | Yes | 15 min | Required |
| **Run quality analysis** | P0 | Yes | 5 min | Critical |
| **Complete Sprint 02** | P0 | Yes | 1-2 hrs | Required |
| **Start Feature 003** | P0 | Yes | 3-5 days | **CRITICAL** |
| Establish constitution | P1 | No | 1 hr | Foundation |
| Performance optimization | P2 | No | 2-3 days | Enhancement |

---

## 🚀 Quick Start Command

**If Feature 007 implementation is complete** (most likely):
```bash
# Option 1: Just merge the sprint
/specswarm:complete

# Option 2: Quality check first, then merge
/specswarm:analyze-quality
# (review results)
/specswarm:complete
```

**If unsure about Feature 007 status**:
```bash
# Get comprehensive analysis first
/specswarm:analyze-quality

# This will show you:
# - What's implemented
# - What's missing
# - Code quality
# - Test coverage
# Then decide: implement more OR complete sprint
```

---

## Key Insights

1. **Sprint 02 is substantially complete** - Bug 008 fixed, Feature 007 has docs and likely implementation
2. **Feature 003 (Bootstrap) is THE critical blocker** - nothing works without it
3. **Clean git workflow is essential** - complete sprint before starting new feature
4. **Quality analysis is valuable** - run it before sprint merge to catch issues

**Estimated Timeline**:
- Assess & complete Sprint 02: **1-2 hours**
- Start Feature 003: **3-5 days**
- MVP ready: **1 week from now**

---

## Meta Note

This recommendation assumes:
- ✅ You want to follow the established git workflow (sprint-based)
- ✅ You want to unblock terminal functionality ASAP (Feature 003)
- ✅ You care about code quality and documentation
- ✅ You're targeting MVP release soon

If any of these assumptions are wrong, provide more context:
```bash
/specswarm:suggest "Actually, I want to [your goal here]"
```
