# ConvoCLI Development Roadmap

**Project**: ConvoCLI - Conversational Terminal Emulator for Android
**Strategy**: Phased development using SpecSwarm/SpecLabs workflows
**Last Updated**: 2025-10-22
**Current Phase**: Sprint 01 Complete - Ready for Merge
**Current Sprint**: sprint-01 branch
**Next Sprint**: Sprint 02 - Terminal Output Integration

---

## 🎯 Quick Status

| Sprint | Status | Features | Blocking Issues |
|--------|--------|----------|-----------------|
| **Sprint 01** | ✅ Complete (Ready to Merge) | Features 001-004, Bug 005, Refactor 006 | None |
| **Sprint 02** | ⏳ Planning | Terminal Output Integration | None |
| **Sprint 03** | 📋 Planned | Package Management UI, Testing | Depends on Sprint 02 |

**Current State**: MVP with simulated output (functional UI, needs real terminal integration)
**Next Step**: Merge sprint-01 → develop, start Sprint 02

---

## 📊 Progress Overview

### ✅ Sprint 01 Complete (Features 001-004 + Bug 005 + Refactor 006)

- ✅ **Feature 001**: Android Project Foundation (Oct 20, 2025)
  - Gradle, Hilt, Compose, Room, ktlint
  - Material 3 theming, DataStore
  - Build succeeds, tests pass

- ✅ **Feature 002**: Termux Integration - Terminal Emulator Core (Oct 21, 2025)
  - Terminal infrastructure complete
  - Session persistence, error handling
  - 70+ tests, comprehensive documentation

- ✅ **Feature 003**: Termux Bootstrap Installation (Oct 22, 2025)
  - Bootstrap download/extraction system
  - Checksum verification, resume capability
  - Installation validation

- ✅ **Feature 004**: Command Blocks UI (Oct 22, 2025)
  - Chat-like terminal interface
  - Material 3 cards, LazyColumn rendering
  - Command execution, copy/rerun actions
  - **Note**: Uses simulated output (real integration in Sprint 02)

- ✅ **Bug 005**: Hilt ViewModel Injection Violation (Oct 22, 2025)
  - Fixed CommandBlockViewModel to inject TerminalRepository
  - Removed ViewModel-to-ViewModel injection
  - Regression test added

- ✅ **Refactor 006**: Deprecation Warnings Cleanup (Oct 22, 2025)
  - Zero deprecation warnings
  - Gradle 9.0 ready
  - Updated to latest Compose APIs

### 🎯 Sprint 02 Planning

**Primary Feature**: Terminal Output Integration
- Replace simulated output with real terminal capture
- Integrate `TerminalRepository.observeOutput()`
- Process ANSI color codes
- Display real command output in blocks

**Secondary Features**:
- Session persistence (referenced in code as T030)
- Fix remaining TODOs (4 deferred items)
- Add Bootstrap module tests (0% coverage currently)

### 🔮 Future Sprints

- ⏳ **Sprint 03**: Package Management UI, Testing
- ⏳ **Sprint 04**: Traditional Terminal Mode
- ⏳ **Sprint 05**: Touch Gestures & Interactions
- ⏳ **Sprint 06**: ConvoSync (Cross-device sync)
- ⏳ **Sprint 07**: AI Integration

---

## 🗺️ Detailed Roadmap

### Phase 1: Strategic Foundation (30 minutes)

**Purpose**: Validate Feature 002 quality before building Features 003-004 on top of it.

**Rationale**: Features 003-004 will build directly on Feature 002's terminal infrastructure. Any issues in 002 will propagate. Better to catch and fix them now.

---

#### ☐ Step 1.1: Run Quality Analysis

**Command**:
```bash
/specswarm:analyze-quality
```

**Purpose**: Comprehensive codebase quality analysis with prioritized recommendations

**What It Analyzes**:
- Code quality metrics
- Test coverage
- Architecture patterns
- Technical debt
- Potential bugs
- Performance issues
- Documentation completeness

**Success Criteria**:
- [x] Quality report generated
- [x] No critical issues found (or issues documented)
- [x] Recommendations reviewed
- [x] Decision made: proceed vs. refactor

**Expected Duration**: 10 minutes

**Possible Outcomes**:
1. ✅ **All Clear** → Proceed to Step 1.2
2. ⚠️ **Minor Issues** → Document, proceed to Step 1.2
3. ❌ **Critical Issues** → Use `/specswarm:refactor`, then retry Step 1.1

**Log Entry**: Record in `SPECSWARM_USAGE_LOG.md` after completion

**Next Step**: Step 1.2 (if successful)

---

#### ☐ Step 1.2: Get AI Recommendations

**Command**:
```bash
/specswarm:suggest
```

**Purpose**: AI-powered workflow recommendation based on context analysis

**What It Provides**:
- Recommended next features
- Workflow suggestions
- Architecture improvements
- Risk analysis
- Priority guidance

**Success Criteria**:
- [x] Recommendations received
- [x] Recommendations reviewed
- [x] Recommendations aligned with roadmap or roadmap adjusted
- [x] Decision made on any immediate actions

**Expected Duration**: 10 minutes

**Possible Outcomes**:
1. ✅ **Aligned** → Recommendations confirm Phase 2 approach → Proceed
2. 🔄 **Adjusted** → Recommendations suggest different priorities → Update roadmap
3. 🛑 **Blocker Found** → Critical issue identified → Address before proceeding

**Decision Point**:
- If recommendations suggest addressing something before Feature 003, do it now
- If recommendations align, proceed to Phase 2

**Log Entry**: Record in `SPECSWARM_USAGE_LOG.md` after completion

**Next Step**: Phase 2, Step 2.1 (if recommendations aligned)

---

### Phase 2: Feature 003 - Termux Bootstrap Installation (1-2 days)

**Purpose**: Install Termux bootstrap system to enable ACTUAL Linux command execution.

**Critical Path**: This is the **BLOCKER** for all other development. Without bootstrap:
- All integration tests remain "infrastructure ready" but can't execute
- No real command execution possible
- Cannot develop any UI features
- Cannot proceed to package management

**After Bootstrap**:
- ✅ Can execute: ls, pwd, cd, echo, cat, grep, find, sed, awk, bash
- ✅ All Feature 002 integration tests PASS with real commands
- ✅ Unblocked for UI development
- ✅ Foundation for package management

---

#### ☐ Step 2.1: Specify Feature 003

**Command**:
```bash
/specswarm:specify "Feature 003: Termux Bootstrap Installation

## Overview
Install Termux bootstrap system to enable actual Linux command execution.

## Critical Requirements

### Bootstrap Installation
- Download Termux bootstrap package (~50MB from GitHub releases)
- Verify signature/checksum (SHA-256) for security
- Extract to /data/data/com.convocli/files/usr
- Install core utilities: bash, coreutils, findutils, tar, gzip, grep, sed, awk
- Set up proper symlinks and permissions
- Handle installation failures gracefully

### Storage & Permissions
- Require ~150MB free storage
- Request WRITE_EXTERNAL_STORAGE permission if needed
- Check available storage before download
- Clean up partial downloads on failure

### Network Handling
- Download from official Termux GitHub releases
- Handle offline scenarios (inform user)
- Retry logic for network failures (3 attempts)
- Support manual bootstrap file selection (if user pre-downloaded)

### Verification
- Verify installation success with version checks
- Test core commands: bash --version, ls --version, grep --version
- Update all Feature 002 integration tests to use REAL commands
- Ensure bootstrap survives app restart

## Integration Points
- **Depends on**: Feature 002 (terminal infrastructure)
- **Enables**: Feature 004 (package management - apt/pkg)
- **Enables**: Features 005-006 (UI - need working terminal)
- **Enables**: All future features (everything needs working terminal)

## Success Criteria
- Can execute: ls, pwd, cd, echo, cat, grep, find, sed, awk
- All Feature 002 integration tests pass with REAL commands
- Bootstrap survives app restart
- Clean error messages if installation fails
- No crashes, even with network failures or low storage
- Installation progress visible to user

## Technical Considerations
- **Storage**: ~150MB required (bootstrap + overhead)
- **Permissions**: WRITE_EXTERNAL_STORAGE for bootstrap download
- **Network**: Handle offline scenarios gracefully
- **Security**: Validate bootstrap SHA-256 checksum
- **Architecture**: Support arm64-v8a, armeabi-v7a, x86, x86_64
- **Android API**: minSdk 26+ compatibility
- **Bootstrap Source**: https://github.com/termux/termux-packages/releases

## Edge Cases
- Installation interrupted (app killed, crash, low battery)
- Corrupted download
- Insufficient storage mid-installation
- Bootstrap already installed (re-install? skip?)
- Different CPU architectures on same device family
"
```

**Purpose**: Create comprehensive specification for bootstrap installation

**Success Criteria**:
- [x] Spec created in `features/003-termux-bootstrap-installation/spec.md`
- [x] All requirements clearly defined
- [x] Edge cases identified
- [x] Security considerations documented
- [x] Integration points clear

**Expected Duration**: 20-30 minutes

**What To Review**:
- Completeness of requirements
- Edge cases covered
- Security measures adequate
- Storage/permission handling

**Log Entry**: Record in `SPECSWARM_USAGE_LOG.md` after completion

**Next Step**: Step 2.2

---

#### ☐ Step 2.2: Clarify Ambiguities

**Command**:
```bash
/specswarm:clarify
```

**Purpose**: Identify underspecified areas and ask targeted clarification questions

**What It Will Ask**:
- Edge case handling specifics
- Error recovery strategies
- UI/UX for installation progress
- Retry behavior details
- Storage cleanup policies
- Version compatibility decisions

**Success Criteria**:
- [x] All clarification questions answered
- [x] Answers encoded back into spec.md
- [x] No ambiguities remain
- [x] Implementation details clear

**Expected Duration**: 15-20 minutes

**Example Questions You Might Answer**:
- "How should we handle interrupted installation?"
- "What should happen if bootstrap is already installed?"
- "Should we show download progress? Installation progress?"
- "What's the retry strategy for network failures?"

**Log Entry**: Record in `SPECSWARM_USAGE_LOG.md` after completion

**Next Step**: Step 2.3

---

#### ☐ Step 2.3: Generate Implementation Plan

**Command**:
```bash
/specswarm:plan
```

**Purpose**: Execute implementation planning workflow using plan template

**What It Creates**:
- Architecture diagrams
- Component breakdown
- Data flow
- Error handling strategy
- Testing approach
- Risk analysis
- Implementation phases

**Output Files**:
- `features/003-.../plan.md` (~800-1000 lines)
- `features/003-.../data-model.md` (if needed)
- Architecture diagrams

**Success Criteria**:
- [x] plan.md created with comprehensive architecture
- [x] All components identified
- [x] Data models defined
- [x] Error handling patterns clear
- [x] Testing strategy documented
- [x] Risks identified with mitigations

**Expected Duration**: 30-40 minutes

**What To Review**:
- Architecture makes sense
- Error handling is comprehensive
- Testing approach is thorough
- Risks are acceptable

**Log Entry**: Record in `SPECSWARM_USAGE_LOG.md` after completion

**Next Step**: Step 2.4

---

#### ☐ Step 2.4: Generate Detailed Tasks

**Command**:
```bash
/specswarm:tasks
```

**Purpose**: Generate actionable, dependency-ordered tasks.md

**What It Creates**:
- Dependency-ordered task list
- Acceptance criteria for each task
- Effort estimates
- Testing requirements
- ~30-50 tasks total

**Output Files**:
- `features/003-.../tasks.md` (~900-1200 lines)

**Success Criteria**:
- [x] tasks.md created with all implementation tasks
- [x] Tasks are actionable and specific
- [x] Dependencies are clear
- [x] Acceptance criteria defined
- [x] Tasks cover: download, verify, extract, install, test, docs

**Expected Duration**: 20-30 minutes

**Task Categories Expected**:
- Bootstrap download (2-3 tasks)
- Signature verification (1-2 tasks)
- Extraction and installation (3-5 tasks)
- Verification and testing (2-3 tasks)
- UI for installation progress (2-3 tasks)
- Error handling (2-3 tasks)
- Integration test updates (5-10 tasks)
- Documentation (2-3 tasks)

**Log Entry**: Record in `SPECSWARM_USAGE_LOG.md` after completion

**Next Step**: Step 2.5

---

#### ☐ Step 2.5: Implement Feature 003

**Command**:
```bash
/specswarm:implement
```

**Purpose**: Execute implementation plan by processing tasks.md

**What It Does**:
- Processes all tasks in dependency order
- Implements each task according to acceptance criteria
- Runs tests after each task
- Creates commits for each major milestone
- Handles errors and retries

**Success Criteria**:
- [x] All tasks in tasks.md completed
- [x] Bootstrap downloads successfully
- [x] Bootstrap extracts to correct location
- [x] Core commands verify successfully
- [x] All integration tests updated to use real commands
- [x] All integration tests PASS
- [x] Documentation complete
- [x] Build succeeds
- [x] No critical issues

**Expected Duration**: 4-6 hours (automated, but may need intervention)

**Monitoring Points**:
- Watch for task failures
- Verify tests pass incrementally
- Check build after major changes
- Review commits for quality

**Possible Interventions Needed**:
- Adjust retry logic if network issues
- Fix architecture-specific issues
- Address permission edge cases
- Update tests if Termux API differs from expectations

**Log Entry**: Record in `SPECSWARM_USAGE_LOG.md` after completion (comprehensive entry!)

**Next Step**: Step 2.6

---

#### ☐ Step 2.6: Comprehensive Validation

**Command**:
```bash
/speclabs:orchestrate-validate /home/marty/code-projects/convocli
```

**Purpose**: Run validation suite (browser, terminal, visual analysis)

**What It Validates**:
- Build succeeds
- All tests pass (unit + integration)
- No compilation errors
- No runtime crashes
- Code quality maintained
- Documentation complete

**Success Criteria**:
- [x] Build: SUCCESSFUL
- [x] Tests: ALL PASSING (including newly updated integration tests)
- [x] Code Quality: No ktlint violations
- [x] Bootstrap: Installed and verified
- [x] Commands: All core commands execute successfully
- [x] Errors: Graceful error handling confirmed

**Expected Duration**: 5-10 minutes

**If Validation Fails**:
1. Review failure report
2. Fix issues (may need `/specswarm:bugfix`)
3. Re-run validation
4. Repeat until passing

**Log Entry**: Record in `SPECSWARM_USAGE_LOG.md` after completion

**Next Step**: Step 2.7

---

#### ☐ Step 2.7: Complete Feature 003

**Command**:
```bash
/specswarm:complete
```

**Purpose**: Complete feature workflow and merge to main

**What It Does**:
- Cleanup diagnostic files
- Pre-merge validation
- Commit any final changes
- Merge to main (no-ff)
- Push to remote
- Delete feature branch
- Update feature status

**Success Criteria**:
- [x] All changes committed
- [x] Merged to main
- [x] Pushed to origin/main
- [x] Feature branch deleted
- [x] Feature status: Complete
- [x] No merge conflicts
- [x] Build successful on main

**Expected Duration**: 2-5 minutes

**Log Entry**: Record in `SPECSWARM_USAGE_LOG.md` after completion

**Next Step**: Phase 3, Step 3.1

---

### Phase 3: Feature 004 - Package Management (1-2 days)

**Purpose**: Integrate apt/pkg package manager for user-installable Linux packages.

**Now Building On**: Working bootstrap from Feature 003

**What This Enables**:
- User can install: python, nodejs, git, vim, tmux, htop, etc.
- Full Linux development environment
- Package search and discovery
- Dependency resolution

---

#### ☐ Step 3.1: Specify Feature 004

**Command**:
```bash
/specswarm:specify "Feature 004: Package Management Integration

## Overview
Integrate apt/pkg package manager for user-installable Linux packages.

## Dependencies
- **Requires**: Feature 003 (bootstrap with apt/dpkg installed)

## Requirements

### Package Operations
- **Search**: pkg search <query>
- **Install**: pkg install <package>
- **Uninstall**: pkg uninstall <package>
- **Update**: pkg update (refresh package lists)
- **Upgrade**: pkg upgrade (upgrade installed packages)
- **List**: pkg list-installed, pkg list-available

### User Experience
- Show download progress (MB downloaded / total MB)
- Show installation progress (extracting, configuring)
- Handle network failures gracefully
- Show dependency resolution
- Confirm before installing large packages
- Estimated install time/size

### Error Handling
- Network failures (retry, inform user)
- Insufficient storage (check before install, inform user)
- Package not found (suggest similar packages)
- Dependency conflicts (explain conflict, suggest resolution)
- Interrupted installation (cleanup, allow retry)

### Common Packages Support
- Verify installation of: python, nodejs, git, vim, tmux, htop, openssh, wget, curl

## Integration Points
- **Depends on**: Feature 003 (bootstrap)
- **UI Integration**: Will need UI for package browser (Feature 005+)
- **Terminal Integration**: Commands execute via TerminalRepository

## Success Criteria
- Can install: python, nodejs, git, vim, tmux
- Progress indicators show download/install status
- Dependency resolution automatic
- Error messages actionable
- No crashes, even with network failures
- Package installations survive app restart
- Can uninstall cleanly

## Technical Considerations
- **Storage**: Some packages large (nodejs ~40MB, python ~20MB)
- **Network**: Package downloads from Termux repositories
- **Permissions**: Uses bootstrap's apt/pkg
- **Progress**: Parse apt output for progress
"
```

**Success Criteria**: Spec created with clear requirements

**Expected Duration**: 20-30 minutes

**Next Step**: Step 3.2

---

#### ☐ Step 3.2: Clarify Feature 004

**Command**: `/specswarm:clarify`

**Next Step**: Step 3.3

---

#### ☐ Step 3.3: Plan Feature 004

**Command**: `/specswarm:plan`

**Next Step**: Step 3.4

---

#### ☐ Step 3.4: Generate Tasks for Feature 004

**Command**: `/specswarm:tasks`

**Next Step**: Step 3.5

---

#### ☐ Step 3.5: Implement Feature 004

**Command**: `/specswarm:implement`

**Expected Duration**: 3-5 hours

**Next Step**: Step 3.6

---

#### ☐ Step 3.6: Validate Feature 004

**Command**: `/speclabs:orchestrate-validate /home/marty/code-projects/convocli`

**Next Step**: Step 3.7

---

#### ☐ Step 3.7: Complete Feature 004

**Command**: `/specswarm:complete`

**Success**: **LINUX ENVIRONMENT COMPLETE** 🎉

**Next Step**: Phase 4, Step 4.1

---

### Phase 4: UI Development (1-2 weeks)

**Purpose**: Build user interfaces for command execution

**Now Building On**: Working Linux environment (Features 001-004)

---

#### ☐ Step 4.1: Specify Feature 005 (Command Blocks UI)

**Command**:
```bash
/specswarm:specify "Feature 005: Command Blocks UI (Warp 2.0-inspired)

## Overview
Conversational, chat-like terminal interface optimized for touch

## Requirements
- Visual command/output blocks (Material 3 Cards)
- Touch-optimized input with gestures
- Smooth scrolling LazyColumn
- Copy/paste/share per block
- Command history with search
- Auto-complete suggestions
"
```

**Next Steps**: 5.2 through 5.7 (same workflow)

---

#### ☐ Step 4.2: Specify Feature 006 (Traditional Terminal)

**Command**:
```bash
/specswarm:specify "Feature 006: Traditional Terminal Mode

## Overview
Full VT-100 terminal for vim, htop, interactive programs

## Requirements
- Canvas-based rendering
- Full ANSI escape code support
- Touch gestures (pinch-zoom, scroll)
- Virtual keyboard optimization
- Mode switching from Command Blocks
"
```

**Next Steps**: 6.2 through 6.7 (same workflow)

---

## 🎯 Milestones

| Milestone | Target Date | Status |
|-----------|-------------|--------|
| Phase 1 Complete | Today | 🔄 In Progress |
| Feature 003 Complete (Bootstrap) | +2 days | ⏳ Pending |
| Feature 004 Complete (Packages) | +4 days | ⏳ Pending |
| **Working Linux Environment** | **~5 days** | **⏳ Pending** |
| Feature 005 Complete (Command Blocks UI) | +10 days | ⏳ Pending |
| Feature 006 Complete (Traditional Terminal) | +12 days | ⏳ Pending |
| **MVP Launch** | **~15-20 days** | **⏳ Pending** |

---

## 📝 How to Use This Roadmap

### Starting a Session
1. Read **"Current Step"** at the top
2. Read the step details
3. Run the command specified
4. Log results in `SPECSWARM_USAGE_LOG.md`
5. Update checkbox and "Current Step"

### After Completing a Step
1. Check the box: `- [x]`
2. Update "Current Step" to next step
3. Update "Last Updated" date
4. Commit roadmap changes

### If Blocked
1. Document blocker in step notes
2. Update "Blocking Issues" in Quick Status table
3. Seek resolution (might need different SpecSwarm command)

### Helper Commands
```bash
# Quick status check
cat DEVELOPMENT_ROADMAP.md | grep "Current Step"

# Update current step
./scripts/update-progress.sh "Phase 2, Step 2.3"

# See next step
cat DEVELOPMENT_ROADMAP.md | grep -A 20 "Current Step"
```

---

## 🔗 Related Documentation

- **Usage Log**: `SPECSWARM_USAGE_LOG.md` - Detailed command logging
- **Session Memory**: `.claude/session-memory/` - Cross-session tracking
- **Project Docs**: `CLAUDE.md` - Technical architecture
- **Feature Docs**: `features/*/` - Individual feature documentation

---

## 📞 Support

If stuck:
1. Check `SPECSWARM_USAGE_LOG.md` for similar situations
2. Review step's "Possible Outcomes" section
3. Check for blockers in "Blocking Issues"
4. Document the issue and ask for help

---

**Last Updated**: 2025-10-22
**Current Phase**: Phase 1 - Strategic Foundation
**Current Step**: Step 1.1 - Quality Analysis
**Next Command**: `/specswarm:analyze-quality`
