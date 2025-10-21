# ConvoCLI: A Modern Conversational Terminal for Android

## Executive Summary

This document outlines the vision for **ConvoCLI**, a Warp 2.0-inspired terminal emulator for Android that combines the power of Termux's Linux environment with a modern, conversational chat-like interface. The goal is to make terminal interactions more accessible and user-friendly while maintaining full compatibility with command-line tools like Claude Code.

## The Problem

### Initial Goal: GUI Wrapper for Claude Code
The original idea was to create a GUI Android app to run Claude Code. However, research revealed several blockers:

**Legal/Licensing Issues:**
- Claude Code is proprietary software with "all rights reserved"
- Governed by Anthropic's Commercial Terms of Service
- Cannot legally bundle or redistribute without explicit permission
- Would violate trademark if using "Claude" branding

**Technical Challenges:**
- 78.4 MB package size
- Requires Node.js runtime on Android
- Complex dependency management
- Users still need their own API keys

### The Better Solution: Modern Terminal Emulator

Instead of wrapping Claude Code specifically, create a **next-generation terminal emulator** that:
- Provides a modern, conversational UI inspired by Warp 2.0
- Works with ANY command-line tool (Claude Code, npm, git, etc.)
- Maintains full Linux environment compatibility via Termux
- Is fully legal (GPLv3 open source)
- Can be distributed on F-Droid and GitHub

## Key Decisions & Rationale

This section documents the critical architectural and strategic decisions made during the planning phase, along with the reasoning behind each choice.

### Decision 1: Build General Terminal (Not Claude Code Wrapper)

**What we decided:** Create a modern terminal emulator that works with ANY CLI tool, rather than a Claude Code-specific wrapper.

**Why:**
- **Legal:** Claude Code is proprietary with "all rights reserved" - cannot legally redistribute or bundle without Anthropic's permission
- **Market:** Broader opportunity - supports git, npm, Python, Claude Code, and 1000+ other tools
- **User value:** One app for all terminal needs vs. single-purpose wrapper
- **Future-proof:** Not tied to one vendor's product lifecycle
- **Differentiation:** UI/UX innovation is the value proposition, not the wrapped tool

**Trade-offs accepted:**
- ⚠️ Broader scope = more complex project
- ⚠️ Can't market as "Claude Code for Android"
- ✅ But: Better long-term product with wider appeal

**Alternatives considered:**
- Request permission from Anthropic (unlikely to get, not responsive to timeline)
- Separate launcher app (poor UX, licensing still unclear)
- Build Claude Code-specific features anyway (flexible enough to add later)

---

### Decision 2: Fork Termux (Not Build From Scratch)

**What we decided:** Fork the Termux project and replace UI layer with modern Jetpack Compose interface.

**Why:**
- **Speed:** 2-4 months to MVP vs. 6-8 months building from scratch
- **Proven tech:** Termux has 5M+ users, battle-tested over 5+ years
- **Compatibility:** Full VT-100/ANSI emulation already implemented (3000+ lines)
- **Package ecosystem:** Users get access to 1000+ pre-compiled packages via apt
- **PTY implementation:** Complex pseudo-terminal interface already working
- **Community:** Can leverage Termux community knowledge and support

**What we get from Termux:**
- ✅ Terminal emulation layer (TerminalEmulator.java)
- ✅ PTY interface (JNI/C code for pseudo-terminal)
- ✅ Session management (TerminalSession.java)
- ✅ Package system (apt/dpkg integration)
- ✅ Linux userspace environment
- ✅ Android integration (storage, permissions)

**What we replace:**
- ❌ Old TerminalView (Canvas-based rendering)
- ❌ Traditional UI (standard terminal appearance)
- ❌ Basic input handling (simple text field)

**Effort comparison:**

| Component | Fork Termux | Build From Scratch |
|-----------|-------------|-------------------|
| Terminal emulator | ✅ Done (3000 lines) | ❌ 4-6 weeks |
| PTY interface | ✅ Done (500-1000 lines C) | ❌ 2-3 weeks |
| Package system | ✅ Done (5000+ lines) | ❌ 8-12 weeks |
| UI layer | 🔄 Replace (2-3 weeks) | 🔄 Build (2-3 weeks) |
| **Total time** | **2-4 months** | **6-8 months** |

**Trade-offs accepted:**
- ⚠️ Must use GPLv3 license (Termux is GPLv3)
- ⚠️ Inherit some Termux architectural patterns
- ⚠️ Must maintain fork as Termux evolves
- ✅ But: These are acceptable given time savings and proven reliability

**Alternatives considered:**
- **Build from scratch:** Too much time for core plumbing, delays market entry
- **Use Termux libraries only:** Still GPLv3, less integration control
- **Different terminal base (Android Terminal Emulator):** Less maintained, fewer features

---

### Decision 3: Fully Open Source (GPLv3), Not Proprietary

**What we decided:** Release entire application as GPLv3 open source, including UI innovation.

**Why this decision was necessary:**
GPLv3 is "viral" - any app that links to or includes GPLv3 code must itself be GPLv3. Since we're using Termux's terminal emulation libraries, we cannot legally make the UI proprietary.

**Legal analysis:**
```
App includes Termux code (GPLv3)
    ↓
Creates "derivative work" under copyright law
    ↓
Entire app MUST be GPLv3
```

**Why we embraced it (not just accepted):**

**Business advantages:**
- ✅ **Proven revenue model:** Successful open-source companies prove this works
  - Automattic (WordPress): $7.5B valuation, GPL software
  - Red Hat (RHEL): $34B acquisition, GPL software
  - GitLab: $15B valuation, open core
  - Tailscale: $100M+ revenue, BSD license
- ✅ **Service-based revenue:** Cloud sync, premium features, enterprise support
- ✅ **Developer trust:** Terminal users prefer open source for security/privacy
- ✅ **Community contributions:** Faster development with contributors
- ✅ **Better for hiring:** Shows we can build in public (Anthropic application boost)

**Technical advantages:**
- ✅ **F-Droid distribution:** Primary channel for open source Android apps (2M+ users)
- ✅ **Security audits:** Community can review code for vulnerabilities
- ✅ **Faster iteration:** Pull requests from community
- ✅ **Fork protection:** Quality/execution creates moat, not closed source

**Strategic advantages:**
- ✅ **First-mover advantage:** Be the "Warp for Android" before anyone else
- ✅ **Brand & community:** Hard to replicate even with source code
- ✅ **Network effects:** Larger community = more value
- ✅ **Press coverage:** "Open source" is newsworthy in developer tools

**Revenue model:**
```
Free Forever (GPLv3):
├── Core terminal app
├── All UI features
├── Command blocks
├── Local usage
└── F-Droid distribution

Paid Services:
├── Convo Cloud Sync ($5/month)
│   ├── Cross-device history
│   ├── Session backup
│   └── Settings sync
├── Convo Pro ($10/month)
│   ├── AI features (Claude API)
│   ├── Advanced analytics
│   └── Priority support
├── Premium Themes ($2 one-time)
│   └── Community marketplace
└── Enterprise ($50/user/month)
    ├── Team features
    ├── SSO integration
    └── Admin controls
```

**Why competitors won't "just clone" us:**
- Brand recognition & trust
- Community & network effects
- Cloud services infrastructure
- First-mover advantages
- Support & documentation quality
- Speed of innovation

**Alternatives considered:**

**Option: Network/IPC separation (two apps)**
```
App 1: Termux Service (GPLv3) - separate install
App 2: Convo UI (proprietary) - paid app
```
- **Rejected because:**
  - ⚠️ Poor user experience (two installations)
  - ⚠️ Complex IPC overhead
  - ⚠️ Google Play may reject split-app pattern
  - ⚠️ Doesn't align with developer tool values (transparency)

**Option: Dual licensing (negotiate with Termux)**
- **Rejected because:**
  - ⚠️ Termux has many contributors (need all to agree)
  - ⚠️ Time-consuming negotiation
  - ⚠️ Likely expensive or impossible
  - ⚠️ Delays project start

**Option: Clean room implementation**
- **Rejected because:**
  - ⚠️ 6-12 months additional development
  - ⚠️ High risk of bugs/incompatibility
  - ⚠️ Won't be as good as Termux initially
  - ⚠️ Opportunity cost too high

**Conclusion:** Open source is not a limitation—it's a strategic advantage for developer tools.

---

### Decision 4: Jetpack Compose (Not Traditional Views)

**What we decided:** Build UI entirely with Jetpack Compose, Android's modern declarative UI framework.

**Why:**
- **Modern standard:** 60% of top 1,000 apps use Compose (2025 data)
- **Perfect for chat UI:** Declarative state management ideal for message/block interfaces
- **Performance:** Optimized for smooth scrolling (LazyColumn)
- **Material 3:** Built-in support for latest design system
- **Developer experience:** Faster iteration with live preview
- **Future-proof:** Android's recommended approach going forward

**Technical advantages for our use case:**
```kotlin
// Declarative = Easy to reason about
@Composable
fun ConversationalTerminal() {
    LazyColumn {
        items(commandBlocks) { block ->  // Efficient scrolling
            CommandCard(block)           // Reusable component
        }
    }
}

// vs Old View system:
// - Manual layout inflation
// - ViewHolder patterns
// - Adapter boilerplate
// - Harder to customize
```

**Why Compose fits command blocks:**
- ✅ Each block is a composable component
- ✅ State changes automatically update UI
- ✅ Easy animations between states
- ✅ Chat-like interface is natural fit
- ✅ Material 3 cards/surfaces built-in

**Trade-offs:**
- ⚠️ Termux uses old View system (must bridge)
- ⚠️ Learning curve for team (if expanding)
- ✅ But: Worth it for modern UX and development speed

**Alternatives considered:**
- **Traditional Views:** Outdated, more boilerplate, harder to customize
- **Flutter:** Cross-platform but not native Android, larger APK
- **React Native:** Extra JavaScript bridge, performance concerns for terminal

---

### Decision 5: Service-Based Revenue Model

**What we decided:** Free core app with paid cloud services and premium features.

**Revenue streams:**

**1. Convo Cloud Sync ($5/month)**
- Cross-device command history
- Session backup & restore
- Settings synchronization
- Works across phone/tablet/desktop

**2. Convo Pro ($10/month)**
- AI command suggestions (Claude API integration)
- Natural language command generation
- Error explanation & fixing
- Advanced analytics
- Priority support

**3. Premium Themes ($2 one-time)**
- Community-created themes
- Marketplace with revenue sharing
- Curated color schemes

**4. Enterprise Plan ($50/user/month)**
- Team collaboration features
- SSO/SAML integration
- Admin dashboard
- Audit logs
- Custom deployment
- SLA guarantees

**5. Additional revenue:**
- GitHub Sponsors / Patreon (donations)
- Consulting / custom development
- Training / workshops

**Financial projections (conservative):**

**Month 6 (1,000 active users):**
- Cloud Sync: 50 users × $5 = $250/mo
- Pro: 20 users × $10 = $200/mo
- Themes: 100 × $2 = $200 (one-time)
- **Total: ~$650/mo**

**Month 12 (10,000 active users):**
- Cloud Sync: 500 users × $5 = $2,500/mo
- Pro: 200 users × $10 = $2,000/mo
- Enterprise: 2 teams × 10 users × $50 = $1,000/mo
- Themes: 500 × $2 = $1,000/mo (one-time)
- **Total: ~$6,500/mo**

**Month 24 (50,000 active users):**
- Cloud Sync: 3,000 users × $5 = $15,000/mo
- Pro: 1,500 users × $10 = $15,000/mo
- Enterprise: 20 teams × 15 users × $50 = $15,000/mo
- Consulting: $5,000/mo
- **Total: ~$50,000/mo ($600k/year)**

**Why this works:**
- Core app free = maximum adoption
- Services = ongoing value delivery
- Not selling the code (it's open source)
- Selling convenience, features, infrastructure

**Precedents:**
- **Bitwarden:** Free password manager + $10/yr premium = $100M+ revenue
- **Standard Notes:** Free notes + sync = sustainable business
- **Termux itself:** Free + donations = actively maintained

---

### Decision 6: F-Droid Primary Distribution

**What we decided:** Distribute primarily via F-Droid, with GitHub releases as secondary.

**Why F-Droid:**
- ✅ **Natural fit:** Standard channel for FOSS Android apps
- ✅ **Target audience:** Developer tools users already use F-Droid
- ✅ **Termux ecosystem:** Where Termux users are (5M+ downloads)
- ✅ **No restrictions:** Google Play limits terminal apps, F-Droid doesn't
- ✅ **Trust factor:** F-Droid = open source credibility
- ✅ **Automatic builds:** They build from source (reproducible)
- ✅ **No fees:** Free listing (vs $25 Google Play)

**GitHub Releases:**
- Beta testing channel
- Early access to features
- Direct APK downloads
- Full control over timing

**Google Play Store (future consideration):**
- Wider audience (non-technical users)
- Better discovery for casual users
- **Challenges:**
  - Must target API 34+
  - Terminal app restrictions (Termux moved off Play)
  - Review delays
  - $25 fee
- **Decision:** Evaluate after F-Droid traction

**Distribution strategy:**
```
Phase 1 (Months 1-3): F-Droid + GitHub
├── Alpha testing via GitHub
├── F-Droid submission
└── Build initial community

Phase 2 (Months 4-6): F-Droid primary
├── Iterate based on feedback
├── Build reputation
└── Feature in F-Droid lists

Phase 3 (Months 7+): Consider expansion
├── Evaluate Play Store viability
├── Alternative stores (Amazon, Samsung)
└── Direct distribution (website)
```

---

### Decision 7: Command Blocks Architecture (Warp-Inspired)

**What we decided:** Organize terminal output into discrete "command blocks" - each command and its output as an atomic unit.

**Why this is the core innovation:**
```
Traditional terminal:
command1
output1
command2
output2
[all mixed together, hard to parse visually]

Convo terminal:
┌─────────────────┐
│ $ command1      │ ← Block 1
│ output1         │
└─────────────────┘
┌─────────────────┐
│ $ command2      │ ← Block 2
│ output2         │
└─────────────────┘
[clear visual separation, easy to understand]
```

**Benefits:**
- ✅ **Visual clarity:** Each command is isolated
- ✅ **Context preservation:** Clear what output came from what command
- ✅ **Actionable:** Can copy, share, re-run individual blocks
- ✅ **Mobile-friendly:** Touch targets, swipe gestures
- ✅ **History navigation:** Scroll through blocks like messages
- ✅ **Status indication:** Success/error/running states visible

**Implementation:**
```kotlin
data class CommandBlock(
    val id: UUID,
    val command: String,
    val output: String,
    val exitStatus: ExitStatus,
    val timestamp: Instant,
    val metadata: BlockMetadata
)

// Each block gets actions:
- Copy output
- Re-run command
- Share block
- Expand/collapse
- Add to favorites
```

**Inspired by Warp, adapted for mobile:**
- Warp: Mouse-driven, keyboard shortcuts
- Convo: Touch-optimized, swipe gestures, haptic feedback

**Handling edge cases:**
- **Full-screen apps (vim, htop):** Switch to traditional view
- **Interactive programs (ssh, REPL):** Live-updating blocks
- **Long output:** Pagination within blocks
- **Real-time output:** Streaming blocks that finalize on completion

---

### Decision 8: Hybrid Architecture (Termux Core + Custom UI)

**What we decided:** Extract and use Termux's core terminal libraries, replace entire UI layer.

**Architecture:**
```
Convo Application
├── Terminal Core (from Termux - GPLv3)
│   ├── TerminalSession.java
│   ├── TerminalEmulator.java
│   └── PTY interface
├── UI Layer (our code - GPLv3)
│   ├── Jetpack Compose components
│   ├── CommandBlockManager
│   ├── ConversationalInput
│   └── Theme system
└── Integration Layer (our code)
    ├── Bridge: Termux → Compose state
    ├── ANSI → AnnotatedString converter
    └── Block detection logic
```

**What we keep from Termux:**
- ✅ Terminal emulation (3000+ lines, battle-tested)
- ✅ Package system (apt/dpkg integration)
- ✅ Linux environment setup
- ✅ Android integration (storage, permissions)

**What we replace:**
- ❌ TerminalView (Canvas rendering)
- ❌ TermuxActivity (old architecture)
- ❌ ExtraKeys UI (keyboard extras)
- ❌ Settings UI (preferences)

**Why hybrid (not full fork):**
- Faster development (focus on UI innovation)
- Clear separation of concerns
- Easier to upstream bug fixes
- Can replace Termux components later if needed

**Percentage breakdown:**
- ~20% Termux code (terminal plumbing)
- ~80% our code (UI/UX innovation)

---

### Decision 9: Cross-Device Session Sync (The Killer Feature)

**What we decided:** Build seamless cross-device session synchronization as a core differentiator.

**The problem this solves:**

Currently, developers face a painful workflow when switching devices:
```
Desktop: "Claude, build login API"
Desktop: Claude creates files, implements features
Desktop: git commit && git push
[Switch to phone]
Phone: git pull
Phone: "Claude, add mobile UI"
Phone: Claude asks "what API?" (LOST ALL CONTEXT!)
```

**The vision:**
```
Desktop: "Claude, build login API"
Desktop: Claude creates files
Desktop: Session auto-syncs ☁️
[Switch to phone]
Phone: Open Convo → "Continue: Login API"
Phone: "Now add mobile UI"
Claude: "I'll integrate with the API I just created..." (FULL CONTEXT!)
```

**Why this is revolutionary:**

**Problem:** Claude Code sessions are local-only
- Claude.ai (web/mobile/desktop) DOES sync conversations
- Claude Code CLI does NOT sync (sessions stored in `~/.claude/`)
- Active GitHub feature request (#7805) - but not implemented
- Third-party solutions exist (Depot) but not integrated

**Opportunity:** First terminal to solve this
- No other terminal offers cross-device AI conversation sync
- Unique even compared to Claude Code itself
- Natural extension of our cloud sync service
- Justifies premium tier pricing

**Why this works for Convo:**

**Technical feasibility:**
```
Session format: JSONL files in ~/.claude/
{
  "type": "user",
  "content": "Build login feature",
  "timestamp": "..."
}
{
  "type": "assistant",
  "content": "I'll create...",
  "timestamp": "..."
}
{
  "type": "tool_use",
  "tool": "Write",
  "params": {...}
}
```

These files are:
- ✅ Well-structured (JSONL)
- ✅ Self-contained (all context included)
- ✅ Portable (no binary data)
- ✅ Syncable (text files)
- ✅ Append-only (easy conflict resolution)

**Architecture:**
```
Desktop (any platform)
├── Claude Code session
├── Export to Convo format
└── Upload to Convo Cloud

☁️ Convo Sync Service
├── Encrypted storage (E2E)
├── Real-time updates
├── Conflict resolution
└── Web dashboard

Android (Convo App)
├── Download session
├── Import to local terminal
├── Continue conversation
└── Full context preserved!
```

**Implementation phases:**

**Phase 1: Manual Export/Import (Month 2)**
- Export Claude Code sessions as JSON
- Import sessions into Convo
- Manual sync via file transfer/git
- **Value:** Session portability, backup

**Phase 2: Cloud Sync Service (Month 4-5)**
- Automatic background sync
- Encrypted cloud storage
- Web dashboard to browse sessions
- Multi-device support
- **Value:** Seamless device switching

**Phase 3: Real-Time Sync (Month 7-8)**
- WebSocket-based live sync
- See desktop session on phone in real-time
- Multi-device viewing
- Session branching (try different approaches)
- **Value:** True cross-device collaboration

**Revenue impact:**

This feature significantly strengthens the Cloud Sync tier:

**Updated Convo Cloud Sync ($5/month):**
- ✅ **Cross-device Claude Code sessions** ← NEW, huge value
- ✅ Command history sync
- ✅ Settings synchronization
- ✅ Session backup & restore
- ✅ Web dashboard access

**Conversion rate projection:**
- Base case: 5% of users pay for Cloud Sync
- With session sync: **20-30%** of users pay
- Users who use Claude Code: **40-50%** conversion

**Updated financial projections:**

**Month 12 (10,000 active users, 40% use Claude Code):**
- Cloud Sync: 1,600 users × $5 = **$8,000/mo** (up from $2,500)
- Pro: 200 users × $10 = $2,000/mo
- Enterprise: 2 teams × 10 × $50 = $1,000/mo
- **Total: ~$11,000/mo** (vs $6,500 without this feature)

**Month 24 (50,000 active users):**
- Cloud Sync: 10,000 users × $5 = **$50,000/mo** (up from $15,000)
- Pro: 1,500 users × $10 = $15,000/mo
- Enterprise: 20 teams × 15 × $50 = $15,000/mo
- **Total: ~$80,000/mo** (vs $50,000 without this feature)

**Competitive advantages:**

**vs Termux:**
- Termux: No sync at all
- Convo: Full session continuity

**vs Claude Code:**
- Claude Code: Local sessions only
- Convo: Cross-device sync built-in

**vs Warp:**
- Warp: Desktop only
- Convo: True mobile + desktop sync

**vs Depot (third-party):**
- Depot: External service, commercial
- Convo: Integrated, open source core

**Why competitors can't easily replicate:**
- Requires cloud infrastructure
- Need Android + desktop clients
- Complex conflict resolution
- E2E encryption expertise
- Integration with multiple platforms
- First-mover advantage (we define the UX)

**User experience:**

**In Convo app:**
```
Home Screen:
┌──────────────────────────┐
│ 🔄 Synced Sessions      │
├──────────────────────────┤
│ 📱 Login feature         │
│    Desktop → Android     │
│    2 minutes ago         │
│    [Continue] [View]     │
├──────────────────────────┤
│ 💻 API refactor          │
│    Desktop only          │
│    1 hour ago            │
│    [Sync & Continue]     │
├──────────────────────────┤
│ ☁️ Cloud Sessions (12)   │
│    [Browse All]          │
└──────────────────────────┘
```

**Tap "Continue":**
- Full conversation loads
- Claude remembers all context
- Previous files, commands, state preserved
- Pick up exactly where you left off

**Security & privacy:**

- ✅ End-to-end encryption (user-controlled keys)
- ✅ Zero-knowledge architecture
- ✅ Optional: Keep sessions local-only
- ✅ Export sessions anytime (no lock-in)
- ✅ Open source sync client
- ✅ Self-hosting option (future)

**Marketing angle:**

**Tagline:** "Code on desktop. Continue on mobile. Claude remembers everything."

**Use cases:**
- Developer on commute: Start on desktop, finish on phone
- Emergency fixes: Continue desktop work from anywhere
- Multi-device workflow: Desktop + tablet + phone seamlessly
- Team collaboration: Share sessions with teammates
- Learning: Resume coding tutorials across devices

**Why this is THE killer feature:**

1. **Unique in market** - No other terminal does this
2. **Solves real pain** - Developers request this constantly
3. **Natural fit** - Extends our cloud sync offering
4. **Revenue driver** - 3x conversion rate vs basic sync
5. **Network effect** - More devices = more value
6. **Defensible moat** - Hard to build well
7. **Anthropic appeal** - Shows we understand Claude Code users
8. **First-mover** - Define the category

**Risks & mitigations:**

**Risk: Anthropic adds this to Claude Code**
- Mitigation: We'll have it first, better mobile UX
- Our sync works with ANY terminal tool, not just Claude Code
- Can still differentiate on UI/UX

**Risk: Privacy concerns with cloud sync**
- Mitigation: E2E encryption, open source, local-first option
- Users control their data
- Self-hosting option

**Risk: Complex to build**
- Mitigation: Start with manual export/import (Phase 1)
- Proven tech (JSONL parsing, REST APIs)
- Gradual rollout reduces risk

**Conclusion:** This single feature could make or break Convo's success. It transforms Convo from "nice terminal UI" to "must-have developer productivity tool."

---

### Decision 10: Jetpack Compose Reaffirmed (2025 Re-evaluation)

**What we decided:** After deep re-evaluation in late 2025, we confirmed that Jetpack Compose remains the correct choice over React Native despite team's Web/React background.

**Context:** When revisiting the tech stack decision with consideration for team's strong React/Web experience, we performed a comprehensive analysis of React Native 0.76+ (with TurboModules/New Architecture) as an alternative to Jetpack Compose.

**Analysis performed:**

**React Native 0.76+ (2025 State):**
- ✅ Production-ready New Architecture (default in 0.76+)
- ✅ TurboModules (JSI) for synchronous native bridge (~15-25ms latency)
- ✅ Reanimated 4.x + Gesture Handler 2.28.0 (cutting-edge)
- ✅ Team's existing React expertise
- ✅ Potential future iOS port path

**Jetpack Compose 1.9.3 (2025 State):**
- ✅ Latest stable (August 2025 release, BOM 2025.10.00)
- ✅ 60% adoption in top 1000 Android apps
- ✅ Direct Kotlin → Kotlin integration (zero overhead)
- ✅ React-like declarative patterns (lower learning curve than expected)
- ✅ Perfect F-Droid ecosystem fit

**Critical findings that reaffirmed Compose:**

**1. No Proven React Native + Termux Integration Pattern**
- Zero examples of React Native mobile apps with PTY (pseudo-terminal) integration
- No proven patterns for Termux fork integration with React Native
- `node-pty` exists only for Node.js/Electron (desktop), not React Native mobile
- `react-native-terminal-component` is 6 years old, JavaScript simulation only
- Would be pioneering this integration (high technical risk)

**2. Interactive Terminal Performance Concerns**
```
Performance comparison (research-based estimates):

Command execution (ls -la):
- React Native: ~25-35ms (acceptable ✅)
- Jetpack Compose: ~10-15ms (excellent ✅)

Streaming output (npm install, 1MB):
- React Native: ~400ms (needs batching optimization ⚠️)
- Jetpack Compose: ~200ms (direct buffer access ✅)

Interactive programs (vim, nano, htop):
- React Native: Every keystroke ~15-25ms bridge crossing (⚠️ could feel laggy)
- Jetpack Compose: Every keystroke ~0ms overhead (✅ native feel)
```

**3. Learning Curve Reality Check**
Initial assumption: "React Native = immediate productivity"

Actual requirement for BOTH approaches:
- Must learn Kotlin/Java anyway (for Termux fork modification)
- React Native: Learn TurboModules (new territory) + Kotlin for native bridge
- Jetpack Compose: Learn Compose (React-like, familiar mental model) + Kotlin
- **Time to working prototype: Similar (~10-12 weeks for both)**

**4. Architecture Complexity**
```
React Native Architecture:
┌─────────────────────┐
│   React Native UI   │
│   (JavaScript)      │
└─────────┬───────────┘
          │ TurboModules Bridge
          │ (~15-25ms per call)
┌─────────▼───────────┐
│  Native Wrapper     │
│  (Kotlin - NEW)     │
└─────────┬───────────┘
          │
┌─────────▼───────────┐
│   Termux Core       │
│   (Java/Kotlin)     │
└─────────────────────┘

- Two language contexts (JS + Kotlin)
- Bridge serialization overhead
- More complex debugging
- Unproven integration pattern

Jetpack Compose Architecture:
┌─────────────────────┐
│   Compose UI        │
│   (Kotlin)          │
└─────────┬───────────┘
          │ Direct calls
          │ (~0ms overhead)
┌─────────▼───────────┐
│   Termux Core       │
│   (Java/Kotlin)     │
└─────────────────────┘

- Single language context
- Zero serialization
- Standard Android debugging
- Proven native patterns
```

**5. Distribution & Ecosystem Alignment**
- **F-Droid users**: Prefer native Android apps for dev tools
- **React Native in F-Droid**: Less common, larger APK (~50MB vs ~25MB)
- **Developer audience**: Values native performance and open source transparency
- **Jetpack Compose**: Better fit for terminal emulator category

**6. iOS Uncertainty**
- Original requirement: "Android-first, **maybe** iOS later"
- iOS port is uncertain and not a 12-month goal
- Trading proven Android patterns for speculative cross-platform benefit = poor trade-off
- If iOS becomes necessary: Can rebuild UI in SwiftUI (similar effort to maintaining RN iOS)

**Decision rationale:**

**Why Jetpack Compose wins:**
1. ✅ **Direct Termux integration** - Same language, zero overhead, proven patterns
2. ✅ **Interactive terminal performance** - Native feel for vim, htop, etc.
3. ✅ **Simpler architecture** - One language, direct calls, easier debugging
4. ✅ **F-Droid ecosystem fit** - Native apps preferred by developer audience
5. ✅ **Future-forward** - Compose 1.9.3 is cutting-edge (60% adoption)
6. ✅ **Learning investment pays off** - Kotlin skills needed anyway for Termux
7. ✅ **Lower risk** - Proven patterns vs pioneering uncharted integration

**Trade-offs accepted:**
- ⚠️ Higher initial learning curve (3-4 weeks for Compose + Kotlin basics)
- ⚠️ Android-only (no iOS path without complete rewrite)
- ⚠️ Team doesn't leverage existing React expertise immediately
- ✅ But: Investment in native Android skills aligns with product focus
- ✅ Compose's declarative model feels familiar to React developers
- ✅ Long-term maintainability and performance justify the learning investment

**Alternatives considered and rejected:**

**React Native + TurboModules:**
- Familiar tech stack for team
- Potential iOS port
- BUT: No proven Termux integration, bridge overhead, architectural complexity

**Hybrid approach (RN UI + WebView terminal):**
- React Native for command blocks
- WebView + xterm.js for traditional terminal
- BUT: Two rendering engines, memory overhead, added complexity

**Flutter + Platform Channels:**
- True cross-platform
- BUT: New language (Dart), similar bridge issues, smaller ecosystem

**Conclusion:** After thorough analysis including current 2025 state of both React Native and Jetpack Compose, we reaffirm that **Jetpack Compose is the correct architectural choice** for ConvoCLI. The decision prioritizes proven patterns, native performance, and ecosystem alignment over short-term team familiarity.

**Timeline impact:** Decision confirmed before significant development started, avoiding costly mid-project pivot.

**Risk mitigation:** Analysis documented for future reference if circumstances change (e.g., iOS becomes critical requirement).

---

## Summary of Decisions

| Decision | Choice | Key Reason |
|----------|--------|-----------|
| **Product scope** | General terminal | Legal + broader market |
| **Technical base** | Fork Termux | Speed + proven tech |
| **License** | GPLv3 (full OSS) | Legal requirement + strategic advantage |
| **UI framework** | Jetpack Compose 1.9.3 | Modern + perfect for blocks + native performance |
| **Revenue model** | Service-based | Proven OSS business model |
| **Distribution** | F-Droid primary | Developer audience + no restrictions |
| **Core innovation** | Command blocks | Mobile-optimized UX |
| **Architecture** | Hybrid (Termux + custom) | Best of both worlds |
| **Killer feature** | Cross-device session sync | Unique differentiation + revenue driver |
| **Tech stack reaffirmed** | Compose over React Native | Zero overhead + proven Termux integration |

**Timeline impact:** These decisions enable 2-4 month MVP vs 6-8 months alternative approaches.

**Risk mitigation:** Open source + proven tech + clear market need = lower execution risk.

**Revenue multiplier:** Cross-device sync increases Cloud Sync conversion from 5% to 20-30%, potentially tripling revenue.

---

## Branding & Naming

### Official Name: ConvoCLI

**What we decided:** The official product name is **ConvoCLI** (pronounced "convo CLI").

**Why:**
- **Outcome-focused:** Emphasizes conversational interaction paradigm that users already understand from AI coding tools (Claude Code uses "conversations", ChatGPT uses "chats")
- **Clear positioning:** "Convo" = conversational interface, "CLI" = command-line tool
- **Product family potential:** Sets up clean namespace for future products (ConvoSync, ConvoBlocks, etc.)
- **Short and memorable:** 8 characters, easy to type and say
- **Unique:** Distinguishes from mechanism-focused names like "terminal" or "shell"
- **Developer-friendly:** CLI suffix signals technical tool for developers

**Naming Journey:**
We explored several naming directions:
1. **Mechanism names** (what it IS): converseshell, dialogueshell, shellconvo - rejected as clunky
2. **Outcome names** (what you GET): pocketterm, pocketcli, mobilecli - good but generic
3. **Inspiration names** (emotional WHY): sparkcli, flashcli, flowcli - compelling but not core identity
4. **Conversation family** (aligns with AI tools): convocli, convoterm - WINNER

**Final decision rationale:**
- Users already think in "conversations" when using AI coding tools
- ConvoCLI bridges familiar AI tool paradigm to mobile CLI access
- "Convo" is widely understood shorthand for "conversation"
- Product family strategy enables clear brand extension

### Domain Portfolio

**Registered Domains:**
- ✅ **convocli.com** (~$13/year) - Main product website, marketing, downloads
- ✅ **convocli.dev** (~$13/year) - Developer documentation, API reference, CLI installation
- ✅ **convocli.app** (~$13/year) - Alternative/redirect domain

**Future Domains (to be registered):**
- 📋 **convosync.dev** (~$13/year) - To be registered when cross-device sync feature is ready for implementation (Phase 2, Month 4-6)

**Total Domain Cost:** ~$39/year (all registered) + $13/year for ConvoSync when ready

**Domain Strategy:**
- Use .com for consumer-facing brand (main website)
- Use .dev for developer-facing resources (matches GitHub.dev, Deno.dev pattern)
- Use .app for app-specific landing/redirect
- Use .dev for feature products (convosync.dev) to signal technical/developer focus

**Rejected Premium TLDs:**
- ❌ .sh domains ($90-100/year) - Too expensive for vanity
- ❌ .ai domains ($90-100/year) - Not positioning as AI-first, premium not justified
- ✅ .dev provides same developer credibility at 87% cost savings

### Product Family Vision

**Core Products:**
```
Convo
├── ConvoCLI (convocli.com)
│   ├── Docs (convocli.dev)
│   └── App (convocli.app)
└── ConvoSync (convosync.dev) [Future: Phase 2]
```

**Potential Future Expansions:**
- ConvoBlocks - Command blocks marketplace/sharing
- ConvoPro - Professional tier branding
- ConvoHub - Community/plugin ecosystem

**Brand Voice:**
- "ConvoCLI - The conversational CLI for mobile developers"
- "Have conversations with your command line, anywhere"
- "Code from your pocket with ConvoCLI"

### Key Branding Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| **Product name** | ConvoCLI | Aligns with AI tool paradigm, enables product family |
| **Primary domain** | convocli.com | Main brand presence |
| **Developer docs** | convocli.dev | Developer-friendly TLD at low cost |
| **Sync feature** | convosync.dev | Dedicated domain for killer feature |
| **TLD strategy** | .com + .dev (not .sh/.ai) | 87% cost savings without credibility loss |
| **Brand family** | Convo + [Feature] | Clean namespace for expansion |

---

## Complete Feature Set & Roadmap

This section documents the comprehensive feature vision for Convo, organized by category with implementation priorities and technical details.

### Feature Categories Overview

The feature set is organized into 9 core categories:

1. **Visual & UX** - Conversational interface and modern design
2. **Mobile Gestures** - Touch-optimized interactions
3. **Productivity** - Power user features and workflows
4. **AI-Powered** - Intelligent assistance and automation
5. **Collaboration** - Team features and sharing
6. **Mobile-Specific** - Android platform advantages
7. **Integrations** - External service connections
8. **Intelligence** - Context awareness and learning
9. **Security** - Privacy and protection features

---

### 1. Visual & UX Features

#### 1.1 Conversational Block Interface ⭐ MVP
**Priority:** Phase 1 - Core MVP
**Complexity:** Medium
**Time Estimate:** 2-3 weeks

**Description:**
Transform traditional terminal line-by-line output into discrete "command cards" that look like chat messages.

**Visual Example:**
```
Instead of:
$ ls
file1.txt file2.txt
$ cat file1.txt
...

You see:
┌─────────────────────────────┐
│ 💭 You                     │
│ ls                          │
└─────────────────────────────┘
┌─────────────────────────────┐
│ 🤖 Terminal                │
│ ✅ Success • 0.02s          │
│                             │
│ 📄 file1.txt               │
│ 📄 file2.txt               │
│                             │
│ [Copy] [Share] [Re-run]     │
└─────────────────────────────┘
```

**Implementation:**
- Detect command boundaries via prompt detection
- Store each command+output as `CommandBlock` data structure
- Render using LazyColumn with Card composables
- Add status badges (success/error/running)
- Include action buttons (copy, share, re-run)

**Technical Stack:**
- Jetpack Compose `LazyColumn`
- Material3 `Card` components
- Custom `CommandBlock` data class
- `StateFlow` for reactive updates

**Dependencies:**
- Terminal emulation layer must detect prompts
- ANSI parsing for styled output

---

#### 1.2 Smart Command Cards ⭐ MVP
**Priority:** Phase 1 - Core MVP
**Complexity:** Medium
**Time Estimate:** 1-2 weeks

**Features:**
- **Success/Error Status Badges:** Green checkmark ✅ for exit code 0, red X ❌ for failures
- **Execution Time Display:** Show command duration (e.g., "2.3s", "1m 42s")
- **Command Icons:** Auto-detect command type and show relevant icons
  - Git commands → 🔀
  - NPM commands → 📦
  - Python scripts → 🐍
  - File operations → 📁
- **Collapsible Output:** Long outputs collapsed by default with "Show more" button
- **Syntax Highlighting:** Language-aware code formatting in outputs

**Implementation:**
```kotlin
data class CommandBlock(
    val id: UUID,
    val command: String,
    val output: AnnotatedString,
    val exitStatus: ExitStatus,
    val executionTime: Duration,
    val timestamp: Instant,
    val commandType: CommandType, // Git, NPM, Python, etc.
    val isExpanded: Boolean = false
)

enum class CommandType {
    GIT, NPM, PYTHON, DOCKER, GENERAL;

    fun getIcon(): ImageVector = when(this) {
        GIT -> Icons.Custom.Git
        NPM -> Icons.Custom.Npm
        PYTHON -> Icons.Custom.Python
        DOCKER -> Icons.Custom.Docker
        GENERAL -> Icons.Default.Terminal
    }
}
```

**Dependencies:**
- Command type detection logic
- Syntax highlighter library (or custom implementation)

---

#### 1.3 Chat-Like Input Experience ⭐ MVP
**Priority:** Phase 1 - Core MVP
**Complexity:** Easy
**Time Estimate:** 3-5 days

**Features:**
- **Bottom Input Bar:** Fixed at bottom like messaging apps, always accessible
- **Multi-line Support:** Easily write multi-line commands or scripts
- **Send Button:** Large touch target to execute commands
- **Input History:** Swipe up on input to see recent commands
- **Auto-focus:** Keyboard appears immediately when needed

**Future Enhancements (Phase 2):**
- Voice Input: Speak commands naturally
- Quick Actions Bar: Floating buttons for common commands
- Drafts: Save incomplete commands for later

**Implementation:**
```kotlin
@Composable
fun ConversationalInput(
    onCommandSubmit: (String) -> Unit
) {
    var input by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Enter command...") },
                maxLines = 5
            )

            IconButton(
                onClick = {
                    onCommandSubmit(input)
                    input = ""
                }
            ) {
                Icon(Icons.Default.Send, "Execute")
            }
        }
    }
}
```

---

#### 1.4 Visual Customization
**Priority:** Phase 2 - Enhancement
**Complexity:** Easy
**Time Estimate:** 1 week

**Features:**
- **Dynamic Themes:** Auto-match system dark/light mode
- **Color Schemes:** Dracula, Solarized, Nord, One Dark, Custom
- **Font Choices:** JetBrains Mono, Fira Code, Source Code Pro, Custom
- **Spacing Options:** Compact/Comfortable/Spacious layouts
- **Block Styles:** Rounded/Square corners, Shadow/Flat appearance

**Implementation:**
- Material3 dynamic theming
- Custom theme engine with user preferences
- Font loading via downloadable fonts API
- Persistent storage of user preferences

---

### 2. Mobile Gesture Features

#### 2.1 Swipe Gestures ⭐ MVP
**Priority:** Phase 1 - Core MVP
**Complexity:** Medium
**Time Estimate:** 1 week

**Gesture Map:**
- **Swipe Right on Block:** Quick copy output to clipboard
- **Swipe Left on Block:** Delete from history (with confirmation)
- **Swipe Up on Block:** Share as text/image
- **Long Press Block:** Pin to favorites / Show context menu
- **Pinch to Zoom:** Zoom in on code/output (accessibility feature)

**Implementation:**
```kotlin
@Composable
fun SwipeableCommandCard(
    block: CommandBlock,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val swipeState = rememberSwipeableState(0)

    Box(
        modifier = Modifier
            .swipeable(
                state = swipeState,
                anchors = mapOf(
                    -300f to SwipeAction.DELETE,
                    0f to SwipeAction.NONE,
                    300f to SwipeAction.COPY
                ),
                thresholds = { _, _ -> FractionalThreshold(0.3f) },
                orientation = Orientation.Horizontal
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onShare() }
                )
            }
    ) {
        // Background action icons
        SwipeActionsBackground(swipeState.offset)

        // Command card content
        CommandCard(block)
    }
}
```

**UX Considerations:**
- Haptic feedback on gesture completion
- Visual hints for first-time users
- Settings to customize gesture actions
- Accessibility: Always provide button alternatives

---

#### 2.2 Touch-Friendly Actions
**Priority:** Phase 1 - Core MVP
**Complexity:** Easy
**Time Estimate:** 3-5 days

**Features:**
- **Large Touch Targets:** All interactive elements 48x48dp minimum (Material Design spec)
- **Haptic Feedback:** Subtle vibrations for button presses, gesture completions
- **Drag to Reorder:** Long-press and drag to reorganize command history
- **Pull to Refresh:** Update command suggestions or reload session
- **Edge Swipe Navigation:** Swipe from left edge for sessions sidebar

**Implementation:**
- Follow Material Design touch target guidelines
- Use `HapticFeedback` API for vibrations
- `LazyColumn` with reorderable modifier
- `SwipeRefresh` composable for pull-to-refresh

---

### 3. Productivity Features

#### 3.1 Command Palette ⭐ MVP
**Priority:** Phase 1 - Core MVP
**Complexity:** Medium
**Time Estimate:** 1-2 weeks

**Description:**
Global search to quickly find and execute workflows, commands, and actions. Activated by typing "/" anywhere.

**Visual Example:**
```
Type "/" in input:
┌─────────────────────────────┐
│ 🔍 Search commands...       │
├─────────────────────────────┤
│ /git                        │
│   🔀 Git Status            │
│   🌿 Git Branch            │
│   💾 Git Commit            │
│   🔄 Git Pull & Push       │
├─────────────────────────────┤
│ /claude                     │
│   🤖 Start Claude Code     │
│   💬 Continue Session      │
│   📋 List Sessions         │
└─────────────────────────────┘
```

**Features:**
- Fuzzy search across workflows, commands, history
- Keyboard shortcuts (Ctrl+K alternative)
- Recently used commands at top
- Category filtering (Git, NPM, Docker, etc.)
- Command descriptions and parameter hints

**Implementation:**
```kotlin
data class PaletteItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val icon: ImageVector,
    val command: String,
    val parameters: List<Parameter> = emptyList()
)

@Composable
fun CommandPalette(
    items: List<PaletteItem>,
    onItemSelected: (PaletteItem) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val filteredItems = remember(query) {
        items.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.command.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = { /* close */ }) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column {
                SearchBar(
                    query = query,
                    onQueryChange = { query = it }
                )

                LazyColumn {
                    items(filteredItems) { item ->
                        PaletteItemRow(
                            item = item,
                            onClick = { onItemSelected(item) }
                        )
                    }
                }
            }
        }
    }
}
```

---

#### 3.2 Workflows & Templates ⭐ MVP
**Priority:** Phase 2 - Enhancement
**Complexity:** Medium
**Time Estimate:** 2 weeks

**Description:**
Pre-built and custom multi-step command sequences that can be saved, shared, and reused.

**Features:**
- **Pre-built Workflows:** "Deploy to Vercel", "Run full test suite", "Git commit & push"
- **Custom Templates:** Save your own multi-step processes
- **Workflow Marketplace:** Share and discover community workflows (Phase 3)
- **Parameter Inputs:** Fill in variables with a form UI instead of typing

**Example Workflow:**
```yaml
name: "Deploy to Production"
description: "Run tests, build, and deploy to production"
parameters:
  - name: commit_message
    type: string
    required: true
    prompt: "Commit message:"

steps:
  - name: "Run tests"
    command: "npm test"
    continue_on_error: false

  - name: "Build production bundle"
    command: "npm run build"

  - name: "Commit changes"
    command: "git add . && git commit -m '{{commit_message}}'"

  - name: "Push to main"
    command: "git push origin main"

  - name: "Deploy to Vercel"
    command: "vercel --prod"
```

**Implementation:**
- YAML or JSON workflow format
- Workflow parser and executor
- Parameter substitution engine
- Visual workflow editor (Phase 3)
- Local storage + cloud sync

---

#### 3.3 Smart Autocomplete
**Priority:** Phase 2 - Enhancement
**Complexity:** Hard
**Time Estimate:** 2-3 weeks

**Features:**
- **Context-Aware:** Knows your git branches, npm scripts, file names, environment variables
- **Learning System:** Adapts to your most-used commands and patterns
- **Natural Language:** Type "show large files" → suggests `du -sh * | sort -hr`
- **Inline Explanations:** Tap suggestion to see what it does before accepting
- **Multi-line Suggestions:** Complete entire code blocks

**Implementation:**
```kotlin
interface AutocompleteProvider {
    suspend fun getSuggestions(
        input: String,
        context: CommandContext
    ): List<Suggestion>
}

data class CommandContext(
    val currentDirectory: File,
    val gitBranches: List<String>,
    val environmentVariables: Map<String, String>,
    val recentCommands: List<String>,
    val projectType: ProjectType
)

class GitAutocompleteProvider : AutocompleteProvider {
    override suspend fun getSuggestions(
        input: String,
        context: CommandContext
    ): List<Suggestion> {
        if (!input.startsWith("git")) return emptyList()

        return when {
            input.startsWith("git checkout") ->
                context.gitBranches.map { branch ->
                    Suggestion(
                        completion = "git checkout $branch",
                        description = "Switch to $branch branch"
                    )
                }
            // ... more git suggestions
            else -> emptyList()
        }
    }
}
```

**Data Sources:**
- File system scanning
- Git repository analysis
- Command history mining
- Project manifest parsing (package.json, requirements.txt, etc.)
- Optional: Cloud-based NLP model

---

#### 3.4 Block Actions
**Priority:** Phase 1 - Core MVP
**Complexity:** Easy-Medium
**Time Estimate:** 1 week

**Actions:**
- **Copy as Text:** Copy output to clipboard
- **Copy as Image:** Beautiful screenshot of command block
- **Share:** Share via Android share sheet
- **Re-run Command:** Execute command again
- **Edit & Re-run:** Modify command before re-executing
- **Create Snippet:** Save command as reusable template
- **Add to Workflow:** Build multi-step workflows
- **Pin to Top:** Keep important commands accessible
- **Export to Markdown:** Save for documentation

**Implementation:**
```kotlin
@Composable
fun CommandBlockActions(
    block: CommandBlock,
    onAction: (BlockAction) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(onClick = { onAction(BlockAction.Copy) }) {
            Icon(Icons.Default.ContentCopy, "Copy")
        }

        IconButton(onClick = { onAction(BlockAction.Screenshot) }) {
            Icon(Icons.Default.CameraAlt, "Screenshot")
        }

        IconButton(onClick = { onAction(BlockAction.Share) }) {
            Icon(Icons.Default.Share, "Share")
        }

        IconButton(onClick = { onAction(BlockAction.Rerun) }) {
            Icon(Icons.Default.Refresh, "Re-run")
        }

        IconButton(onClick = { onAction(BlockAction.Pin) }) {
            Icon(Icons.Default.PushPin, "Pin")
        }
    }
}

// Screenshot generation
fun CommandBlock.toScreenshot(): Bitmap {
    // Render block to bitmap with beautiful styling
    // Include syntax highlighting, proper fonts, branding
}
```

---

### 4. AI-Powered Features

#### 4.1 AI Command Assistant
**Priority:** Phase 3 - Advanced
**Complexity:** Hard
**Time Estimate:** 2-3 weeks

**Description:**
Natural language to command translation. Type what you want in plain English, get executable commands.

**Example Flow:**
```
User types: "Find all JavaScript files modified today"

AI suggests:
┌─────────────────────────────┐
│ 🤖 AI Suggestion            │
│                             │
│ find . -name "*.js" -mtime 0│
│                             │
│ This command will:          │
│ • Search current directory  │
│ • Find .js files            │
│ • Modified in last 24hrs    │
│                             │
│ [Run] [Edit] [Explain More] │
└─────────────────────────────┘
```

**Implementation:**
```kotlin
class AICommandAssistant(
    private val apiKey: String,
    private val useLocalModel: Boolean = false
) {
    suspend fun translateToCommand(
        naturalLanguageQuery: String,
        context: CommandContext
    ): AICommandSuggestion {

        val systemPrompt = """
            You are a terminal command assistant. Convert natural language
            requests to shell commands. Consider the context:
            - OS: ${context.os}
            - Shell: ${context.shell}
            - Current directory: ${context.currentDirectory}
            - Available tools: ${context.availableCommands}
        """.trimIndent()

        val response = if (useLocalModel) {
            // Use local LLM via llama.cpp or ollama
            localLLM.complete(systemPrompt, naturalLanguageQuery)
        } else {
            // Use Claude API
            anthropicClient.messages.create(
                model = "claude-sonnet-4.5",
                messages = listOf(
                    Message(role = "user", content = naturalLanguageQuery)
                ),
                system = systemPrompt
            )
        }

        return AICommandSuggestion(
            command = parseCommand(response),
            explanation = parseExplanation(response),
            confidence = 0.95
        )
    }
}
```

**Options:**
- **Cloud AI:** Claude API, OpenAI API (requires internet + API key)
- **Local AI:** Ollama, llama.cpp running in Termux (privacy-focused, offline)
- **Hybrid:** Local for simple queries, cloud for complex ones

---

#### 4.2 Error Explanation & Fixing ⭐ HIGH VALUE
**Priority:** Phase 3 - Advanced
**Complexity:** Medium
**Time Estimate:** 2 weeks

**Description:**
When commands fail, AI explains why and suggests fixes.

**Example:**
```
┌─────────────────────────────┐
│ ❌ Error • Exit code 1      │
│                             │
│ npm install                 │
│ EACCES: permission denied   │
│ /usr/local/lib/node_modules │
│                             │
│ 💡 AI Analysis:             │
│ You need sudo permissions   │
│ OR use a Node version       │
│ manager like nvm.           │
│                             │
│ Recommended fix:            │
│ [Run: nvm use 18]           │
│                             │
│ Alternative:                │
│ [Run: sudo npm install]     │
│ [Learn about nvm]           │
└─────────────────────────────┘
```

**Implementation:**
```kotlin
class ErrorAnalyzer(private val ai: AICommandAssistant) {
    suspend fun analyzeError(
        command: String,
        exitCode: Int,
        stderr: String,
        stdout: String
    ): ErrorAnalysis {

        // Common error patterns (fast path, no AI needed)
        val commonError = COMMON_ERRORS.find {
            it.pattern.matches(stderr)
        }

        if (commonError != null) {
            return ErrorAnalysis(
                explanation = commonError.explanation,
                suggestedFixes = commonError.fixes,
                source = AnalysisSource.PATTERN_MATCH
            )
        }

        // Use AI for complex errors
        return ai.analyzeError(command, exitCode, stderr, stdout)
    }
}

// Pre-defined common errors for instant feedback
val COMMON_ERRORS = listOf(
    CommonError(
        pattern = "EACCES.*permission denied".toRegex(),
        explanation = "Permission denied. You don't have access to this resource.",
        fixes = listOf(
            Fix("Use sudo", "sudo $command"),
            Fix("Check file permissions", "ls -la")
        )
    ),
    CommonError(
        pattern = "command not found".toRegex(),
        explanation = "This command isn't installed or not in your PATH.",
        fixes = listOf(
            Fix("Install with apt", "apt install <package>"),
            Fix("Check if installed", "which <command>")
        )
    )
    // ... hundreds more
)
```

**Value Proposition:**
- **Huge beginner help:** Reduces frustration, accelerates learning
- **Time savings:** No googling error messages
- **Educational:** Explains WHY errors happen

---

#### 4.3 AI Agents (Warp 2.0 Style)
**Priority:** Future - Experimental
**Complexity:** Very Hard
**Time Estimate:** 4-6 weeks

**Description:**
Long-running AI agents that perform multi-step tasks autonomously.

**Capabilities:**
- **Code Generation:** "Set up a React project with TypeScript, tests, and CI/CD"
- **Monitoring:** "Watch server logs and alert me if error rate >5%"
- **Refactoring:** "Update all API calls to use the new authentication header"
- **Analysis:** "Find all TODO comments and create GitHub issues for them"

**Agent Management Panel:**
```
┌─────────────────────────────┐
│ 🤖 Active Agents (2)        │
├─────────────────────────────┤
│ ⚙️ Setup React Project     │
│ ├─ Installing dependencies  │
│ ├─ Configuring TypeScript   │
│ └─ Running... 2m 34s        │
│ [Pause] [Stop] [Details]    │
├─────────────────────────────┤
│ 👁️ Monitor Logs            │
│ └─ Watching /var/log/app.log│
│ ✅ No issues (running 1h)   │
│ [Stop] [Configure]          │
└─────────────────────────────┘
```

**Implementation Considerations:**
- Background service for long-running agents
- Agent state persistence
- Safety controls (approval for destructive actions)
- Resource limits (prevent infinite loops)
- Notification integration

---

### 5. Collaboration Features

#### 5.1 Session Sharing
**Priority:** Phase 2 - Enhancement
**Complexity:** Medium
**Time Estimate:** 1-2 weeks

**Features:**
- **Export Session:** Generate shareable link or file
- **Import Session:** Load someone else's session
- **Session Format:** JSON with full command history and context
- **Privacy Controls:** Choose what to share (hide sensitive output)

**Use Cases:**
- Share debugging session with teammate
- Create tutorials with working examples
- Save sessions as documentation
- Reproduce bugs with full context

**Implementation:**
```kotlin
data class SharedSession(
    val id: UUID,
    val title: String,
    val description: String,
    val commands: List<CommandBlock>,
    val environment: Map<String, String>,
    val createdAt: Instant,
    val createdBy: String,
    val privacyLevel: PrivacyLevel
)

enum class PrivacyLevel {
    PUBLIC,      // Anyone with link can view
    UNLISTED,    // Only people with link
    TEAM_ONLY,   // Only team members
    PRIVATE      // Only me
}

class SessionSharing {
    suspend fun exportSession(
        session: TerminalSession,
        options: ExportOptions
    ): SharedSession {
        val sanitized = if (options.removeSecrets) {
            sanitizeCommands(session.commands)
        } else {
            session.commands
        }

        return SharedSession(
            id = UUID.randomUUID(),
            commands = sanitized,
            // ... other fields
        )
    }

    fun sanitizeCommands(commands: List<CommandBlock>): List<CommandBlock> {
        // Remove API keys, passwords, tokens, etc.
        // Use pattern matching + ML for detection
    }
}
```

---

#### 5.2 Team Workflows
**Priority:** Phase 3 - Advanced
**Complexity:** Hard
**Time Estimate:** 3-4 weeks

**Features:**
- **Shared Workflow Library:** Company-wide command templates
- **Team Approval:** Require approval before running sensitive commands
- **Audit Logs:** Track who ran what commands
- **Role-Based Access:** Limit dangerous commands to admins
- **Organization Settings:** Centralized configuration

**Enterprise Value:**
- Standardize development workflows
- Onboard new developers faster
- Enforce security policies
- Track team productivity

---

### 6. Mobile-Specific Features

#### 6.1 Background Execution & Notifications ⭐ HIGH VALUE
**Priority:** Phase 2 - Enhancement
**Complexity:** Medium
**Time Estimate:** 2 weeks

**Description:**
Long-running commands continue even when app is backgrounded. Push notifications alert you when complete.

**Features:**
- **Background Service:** Commands run in Android Foreground Service
- **Push Notifications:** "npm install completed ✅" or "Build failed ❌"
- **Progress Notifications:** Show real-time progress for long tasks
- **Smart Notifications:** Only notify for important events
- **Notification Actions:** Quick actions like "View output" or "Re-run"

**Example:**
```
User starts: npm install
User switches to browser
10 minutes later:
📱 Notification: "npm install completed (10m 32s) ✅"
Tap to view output
```

**Implementation:**
```kotlin
class BackgroundCommandService : Service() {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val command = intent?.getStringExtra("command") ?: return START_NOT_STICKY

        // Show foreground notification
        startForeground(
            NOTIFICATION_ID,
            createProgressNotification(command)
        )

        scope.launch {
            val result = executeCommand(command)

            // Update notification with result
            notificationManager.notify(
                NOTIFICATION_ID,
                createCompletionNotification(result)
            )

            stopSelf()
        }

        return START_STICKY
    }
}

fun createCompletionNotification(result: CommandResult): Notification {
    return NotificationCompat.Builder(context, CHANNEL_ID)
        .setContentTitle(
            if (result.exitCode == 0)
                "${result.command} completed ✅"
            else
                "${result.command} failed ❌"
        )
        .setContentText("Duration: ${result.duration.format()}")
        .setSmallIcon(R.drawable.ic_terminal)
        .addAction(
            R.drawable.ic_view,
            "View output",
            createViewIntent(result)
        )
        .build()
}
```

**Android APIs:**
- Foreground Service
- NotificationManager
- WorkManager (for scheduled commands)

---

#### 6.2 Android Widgets
**Priority:** Phase 3 - Advanced
**Complexity:** Medium
**Time Estimate:** 1-2 weeks

**Description:**
Home screen widgets for quick command execution.

**Widget Types:**

**Quick Command Widget:**
```
┌─────────────────┐
│ 🚀 Convo Quick  │
├─────────────────┤
│ [Git Status]    │
│ [Deploy Prod]   │
│ [Run Tests]     │
│ [Open Session]  │
└─────────────────┘
```

**Status Widget:**
```
┌─────────────────┐
│ 📊 Project      │
├─────────────────┤
│ Branch: main    │
│ Status: Clean   │
│ Last: 2h ago    │
└─────────────────┘
```

**Implementation:**
- Glance API (Jetpack Compose for widgets)
- Widget configuration activity
- Background updates via WorkManager

---

#### 6.3 Android-Specific Integrations
**Priority:** Phase 2 - Enhancement
**Complexity:** Easy-Medium
**Time Estimate:** 1 week

**Features:**
- **Share Sheet Integration:** Share files from any app into terminal
- **App Shortcuts:** Long-press app icon for quick commands
- **Split Screen Support:** Terminal + browser/IDE side-by-side
- **Picture-in-Picture:** Monitor logs while using other apps
- **Quick Settings Tile:** Toggle terminal from notification shade

**Implementation:**
```xml
<!-- App Shortcuts -->
<shortcuts>
    <shortcut
        android:shortcutId="new_session"
        android:icon="@drawable/ic_add"
        android:shortcutShortLabel="@string/new_session"
        android:shortcutLongLabel="@string/new_session_long">
        <intent ... />
    </shortcut>
    <shortcut android:shortcutId="git_status" ... />
    <shortcut android:shortcutId="run_tests" ... />
</shortcuts>

<!-- Share Target -->
<activity android:name=".ShareReceiverActivity">
    <intent-filter>
        <action android:name="android.intent.action.SEND"/>
        <data android:mimeType="*/*"/>
    </intent-filter>
</activity>
```

---

### 7. Integration Features

#### 7.1 Claude Code Session Sync ⭐ KILLER FEATURE
**Priority:** Phase 2 - Enhancement (already documented in Decision 9)
**Complexity:** Hard
**Time Estimate:** 3-4 weeks

See **Decision 9: Cross-Device Session Sync** for complete documentation.

**Quick Summary:**
- Sync Claude Code conversations across desktop and Android
- Parse JSONL session files from `~/.claude/`
- E2E encrypted cloud storage
- Real-time sync capabilities
- Revenue multiplier: 3x Cloud Sync conversion

---

#### 7.2 Git Visual Integration
**Priority:** Phase 3 - Advanced
**Complexity:** Hard
**Time Estimate:** 3-4 weeks

**Features:**
- **Visual Branch Tree:** See git branches as interactive graph
- **Inline Diffs:** Tap file to see changes with syntax highlighting
- **Commit Browser:** Beautiful UI for git log
- **Staging Area:** Visual file staging (like Git GUI)
- **PR Integration:** Create/review PRs from terminal (via gh CLI)

**Example:**
```
Git Status Card:
┌─────────────────────────────┐
│ 🔀 Git Repository           │
├─────────────────────────────┤
│ 🌿 Branch: feature/login    │
│ ⬆️ 2 ahead, ⬇️ 1 behind    │
│                             │
│ 📝 Modified (3):            │
│   ● src/auth.ts             │
│   ● src/login.tsx           │
│   ● package.json            │
│                             │
│ ➕ Untracked (1):           │
│   ● tests/auth.test.ts      │
│                             │
│ [Stage All] [Commit] [Push] │
└─────────────────────────────┘
```

**Implementation:**
- Parse `git status --porcelain`
- Git log parsing for history
- libgit2 bindings for Kotlin (or shell git commands)
- Diff rendering with syntax highlighting

---

#### 7.3 Cloud Service Integrations
**Priority:** Future - Experimental
**Complexity:** Hard
**Time Estimate:** Variable per integration

**Potential Integrations:**
- **Deploy Services:** Vercel, Netlify, Railway, Fly.io
  - One-tap deploys
  - Deployment status monitoring
  - Rollback capabilities

- **Container Management:** Docker, Podman
  - List/start/stop containers
  - View logs
  - Resource monitoring

- **Database Tools:**
  - Visual query builder
  - Schema browser
  - Connection manager

- **API Testing:**
  - Built-in HTTP client
  - Save request collections
  - Environment variables

---

### 8. Intelligence & Learning Features

#### 8.1 Project Detection ⭐ HIGH VALUE
**Priority:** Phase 2 - Enhancement
**Complexity:** Easy
**Time Estimate:** 1 week

**Description:**
Auto-detect project type and suggest relevant commands.

**Example:**
```
When you cd into a Node.js project:

┌─────────────────────────────┐
│ 📦 Node.js Project Detected │
├─────────────────────────────┤
│ Suggested commands:         │
│ • npm install               │
│ • npm run dev               │
│ • npm test                  │
│ • npm run build             │
│                             │
│ Package.json scripts:       │
│ • dev: vite                 │
│ • build: tsc && vite build  │
│ • test: vitest              │
└─────────────────────────────┘
```

**Detection Logic:**
```kotlin
enum class ProjectType {
    NODE_JS,      // package.json
    PYTHON,       // requirements.txt, setup.py
    RUST,         // Cargo.toml
    GO,           // go.mod
    JAVA,         // pom.xml, build.gradle
    DOCKER,       // Dockerfile
    GIT,          // .git directory
    UNKNOWN
}

class ProjectDetector {
    fun detectProjectType(directory: File): ProjectType {
        return when {
            directory.resolve("package.json").exists() -> ProjectType.NODE_JS
            directory.resolve("requirements.txt").exists() -> ProjectType.PYTHON
            directory.resolve("Cargo.toml").exists() -> ProjectType.RUST
            directory.resolve("go.mod").exists() -> ProjectType.GO
            directory.resolve(".git").exists() -> ProjectType.GIT
            else -> ProjectType.UNKNOWN
        }
    }

    fun getSuggestedCommands(type: ProjectType): List<String> {
        return when (type) {
            ProjectType.NODE_JS -> listOf(
                "npm install",
                "npm run dev",
                "npm test",
                "npm run build"
            )
            ProjectType.PYTHON -> listOf(
                "pip install -r requirements.txt",
                "python manage.py runserver",
                "pytest"
            )
            // ... etc
        }
    }
}
```

**Value:**
- Reduces cognitive load
- Helps beginners learn common patterns
- Speeds up common workflows

---

#### 8.2 Environment Awareness
**Priority:** Phase 2 - Enhancement
**Complexity:** Easy
**Time Estimate:** 3-5 days

**Features:**
- **Auto-activate virtualenv:** Detect and activate Python/Node environments
- **Git Branch Display:** Always show current branch in prompt
- **Resource Monitoring:** CPU/Memory/Battery usage indicators
- **Directory Breadcrumbs:** Visual path with quick navigation
- **Project Context:** Show project name, type, git status in header

**Example Header:**
```
┌─────────────────────────────┐
│ 📁 my-app (Node.js)         │
│ 🌿 feature/auth             │
│ 📍 ~/projects/my-app/src    │
│ ⚡ 12% battery • 3GB RAM   │
└─────────────────────────────┘
```

---

#### 8.3 Learning System
**Priority:** Phase 3 - Advanced
**Complexity:** Medium
**Time Estimate:** 2-3 weeks

**Features:**
- **Command History Analytics:** "You use `git push` most at 5pm Fridays"
- **Efficiency Tips:** "You can use `git p` alias to save time"
- **Pattern Recognition:** "You often run tests after commits—create a workflow?"
- **Skill Tracking:** Track which commands/tools you're mastering
- **Achievement System:** Gamification for engagement

**Analytics Dashboard:**
```
📈 This Week:
• 247 commands executed
• 12.3 hours active coding
• Most used: git (89x), npm (43x), cd (67x)
• Fastest workflow: Testing pipeline (saved 2h)
• 🏆 Achievement unlocked: 5-day coding streak!

💡 Suggestions:
• Create alias: alias gp="git push"
• Workflow idea: Combine test + commit
• New tool: Try 'exa' instead of 'ls'
```

**Implementation:**
- Local analytics database (Room)
- Pattern detection algorithms
- Privacy-focused (all local, optional cloud sync)

---

### 9. Security & Privacy Features

#### 9.1 Biometric Authentication
**Priority:** Phase 3 - Advanced
**Complexity:** Easy
**Time Estimate:** 3-5 days

**Features:**
- **App Lock:** Require fingerprint/face to open app
- **Session Lock:** Lock specific sensitive sessions
- **Auto-lock:** Lock after inactivity period
- **Sensitive Command Protection:** Require auth before `sudo` or destructive commands

**Implementation:**
```kotlin
class BiometricAuth(private val context: Context) {
    fun authenticate(
        title: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricPrompt = BiometricPrompt(
            context as FragmentActivity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult
                ) {
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    onError("Authentication failed")
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
```

---

#### 9.2 Secret Detection & Protection
**Priority:** Phase 2 - Enhancement
**Complexity:** Medium
**Time Estimate:** 1-2 weeks

**Features:**
- **API Key Detection:** Warn before committing files with API keys
- **Password Prevention:** Detect passwords in command history
- **Secure Input Mode:** Hide input for sensitive commands
- **Secret Redaction:** Automatically redact secrets from shared sessions
- **Environment Variable Encryption:** Encrypt stored env vars

**Detection Patterns:**
```kotlin
object SecretDetector {
    private val patterns = listOf(
        "AWS.*KEY".toRegex(),
        "API.*KEY".toRegex(),
        "TOKEN".toRegex(),
        "PASSWORD".toRegex(),
        "SECRET".toRegex(),
        "[a-zA-Z0-9]{32,}".toRegex() // Potential API keys
    )

    fun detectSecrets(text: String): List<SecretMatch> {
        return patterns.flatMap { pattern ->
            pattern.findAll(text).map { match ->
                SecretMatch(
                    value = match.value,
                    pattern = pattern.pattern,
                    position = match.range
                )
            }
        }
    }

    fun redact(text: String): String {
        var redacted = text
        detectSecrets(text).forEach { secret ->
            redacted = redacted.replace(
                secret.value,
                "***REDACTED***"
            )
        }
        return redacted
    }
}
```

**User Experience:**
```
User types: git commit -m "Add API_KEY=sk_1234567890"

⚠️ Warning:
┌─────────────────────────────┐
│ 🔒 Potential secret detected│
│                             │
│ This commit message contains│
│ what looks like an API key. │
│                             │
│ [Remove Secret]             │
│ [Commit Anyway]             │
│ [Cancel]                    │
└─────────────────────────────┘
```

---

#### 9.3 Privacy Controls
**Priority:** Phase 2 - Enhancement
**Complexity:** Easy
**Time Estimate:** 3-5 days

**Features:**
- **Encrypted Sync:** E2E encryption for cloud features (already planned)
- **Local-First Mode:** Disable all cloud features
- **History Clearing:** Selective or complete history deletion
- **Incognito Sessions:** Sessions that don't save history
- **Data Export:** Export all your data anytime
- **Self-Hosting Option:** Run your own sync server (Phase 3)

**Settings UI:**
```
🔒 Privacy Settings:

[ ] Cloud Sync Enabled
    End-to-end encrypted

[ ] Analytics Enabled
    Local only, never shared

[ ] Crash Reports
    Anonymous error reporting

[Clear History...]
[Export My Data...]
[Delete Account...]
```

---

## Feature Prioritization Matrix

### Phase 1: MVP (Months 1-3)
**Goal:** Usable terminal with modern UX
**Target:** 1,000+ downloads

| Feature | Complexity | Impact | Priority |
|---------|-----------|--------|----------|
| Conversational Block Interface | Medium | Very High | ⭐⭐⭐ |
| Smart Command Cards | Medium | Very High | ⭐⭐⭐ |
| Chat-Like Input | Easy | High | ⭐⭐⭐ |
| Swipe Gestures | Medium | High | ⭐⭐⭐ |
| Touch-Friendly Actions | Easy | High | ⭐⭐⭐ |
| Command Palette | Medium | Very High | ⭐⭐⭐ |
| Block Actions (basic) | Easy | High | ⭐⭐ |

**MVP Deliverables:**
- ✅ Modern conversational UI
- ✅ Full Termux compatibility
- ✅ Touch-optimized gestures
- ✅ Command palette for discovery
- ✅ Basic sharing/copying
- ✅ F-Droid ready

**Time Estimate:** 2-3 months solo development

---

### Phase 2: Enhancement (Months 4-6)
**Goal:** Productivity boost + cloud features
**Target:** 10,000+ downloads

| Feature | Complexity | Impact | Priority |
|---------|-----------|--------|----------|
| **Cross-Device Session Sync** | Hard | Very High | ⭐⭐⭐ |
| Workflows & Templates | Medium | High | ⭐⭐⭐ |
| Smart Autocomplete | Hard | Medium | ⭐⭐ |
| Background Execution & Notifications | Medium | Very High | ⭐⭐⭐ |
| Project Detection | Easy | High | ⭐⭐⭐ |
| Environment Awareness | Easy | Medium | ⭐⭐ |
| Secret Detection | Medium | High | ⭐⭐ |
| Privacy Controls | Easy | High | ⭐⭐ |
| Visual Customization | Easy | Medium | ⭐⭐ |
| Session Sharing | Medium | Medium | ⭐⭐ |
| Android Integrations | Easy | Medium | ⭐⭐ |

**Phase 2 Deliverables:**
- ✅ Cloud sync service launched (revenue!)
- ✅ Cross-device Claude Code sessions
- ✅ Background command execution
- ✅ Customizable themes
- ✅ Workflow system
- ✅ Security features

**Revenue Target:** $2,000-3,000/mo
**Time Estimate:** 3-4 months

---

### Phase 3: Advanced (Months 7-12)
**Goal:** AI features + enterprise capabilities
**Target:** 50,000+ downloads

| Feature | Complexity | Impact | Priority |
|---------|-----------|--------|----------|
| AI Command Assistant | Hard | High | ⭐⭐⭐ |
| Error Explanation & Fixing | Medium | Very High | ⭐⭐⭐ |
| Git Visual Integration | Hard | Medium | ⭐⭐ |
| Android Widgets | Medium | Low | ⭐ |
| Team Workflows | Hard | High | ⭐⭐⭐ |
| Learning System | Medium | Medium | ⭐⭐ |
| Biometric Auth | Easy | Low | ⭐ |

**Phase 3 Deliverables:**
- ✅ AI-powered assistance
- ✅ Enterprise features
- ✅ Advanced Git integration
- ✅ Team collaboration

**Revenue Target:** $10,000-15,000/mo
**Time Estimate:** 4-6 months

---

### Future/Experimental (12+ months)
**Goal:** Cutting-edge features

| Feature | Complexity | Impact | Priority |
|---------|-----------|--------|----------|
| AI Agents (Warp 2.0 style) | Very Hard | High | ⭐⭐ |
| Cloud Service Integrations | Hard | Medium | ⭐⭐ |
| Workflow Marketplace | Hard | Medium | ⭐ |
| Self-Hosting Option | Hard | Low | ⭐ |
| Real-Time Collaboration | Very Hard | Medium | ⭐⭐ |

---

## Technical Architecture



### Core Components

#### 1. Terminal Emulation Layer
**Approach: Fork Termux**

Termux provides:
- Complete Linux environment in Android user space
- VT-100/ANSI terminal emulation
- PTY (pseudo-terminal) interface
- Package management (apt)
- No root required

Key libraries:
- `terminal-view` (v0.118.0) - Terminal widget
- `terminal-emulator` - Emulation logic
- Available on JitPack: `com.termux.termux-app:terminal-view:0.118.0`

**How Termux Works:**
- Operates entirely in user space (/data/data/com.termux/files/)
- Packages cross-compiled with Android NDK for ARM/AARCH64
- Uses apt package manager (Debian/Ubuntu style)
- No kernel modifications required

#### 2. Modern UI Framework: Jetpack Compose

**Why Jetpack Compose?**
- Modern declarative UI (60% of top 1000 apps in 2025)
- Perfect for chat-like interfaces
- Material Design 3 with dynamic theming
- Excellent performance
- Native Android

**Key Features:**
- LazyColumn for efficient scrolling
- Rich text rendering
- Markdown support
- Smooth animations
- Material3 components

#### 3. Command Block System (Warp-Inspired)

**Command Blocks Architecture:**
```kotlin
data class CommandBlock(
    val id: UUID,
    val command: String,
    val output: String,
    val exitStatus: ExitStatus,
    val timestamp: Instant,
    val metadata: BlockMetadata
)

enum class ExitStatus {
    SUCCESS,
    ERROR,
    RUNNING
}
```

**Features:**
- Each command + output is an atomic "card"
- Visual status indicators (success/error)
- Swipe actions (copy, share, re-run)
- Syntax highlighting
- Markdown rendering for output

#### 4. Conversational Input Interface

**Design Principles:**
- Chat bubble for commands (user messages)
- Response blocks for output (assistant messages)
- Inline code highlighting
- Auto-complete suggestions
- Natural language command assistance (optional AI)

**UI Components:**
```kotlin
@Composable
fun ConversationalTerminal() {
    LazyColumn {
        items(commandBlocks) { block ->
            CommandCard(
                command = block.command,
                output = block.output.toAnnotatedString(),
                status = block.exitStatus
            )
        }
    }
    ConversationalInput(
        onCommand = { handleCommand(it) }
    )
}
```

## Technical Stack

### Languages & Frameworks
- **Kotlin** - Primary language
- **Jetpack Compose** - UI framework
- **Coroutines** - Asynchronous operations
- **Room** - Command history database
- **Material3** - Design system

### Dependencies
```gradle
// Terminal emulation
implementation "com.termux.termux-app:terminal-view:0.118.0"
implementation "com.termux.termux-app:terminal-emulator:0.118.0"

// UI - Using Compose BOM for version management
implementation platform("androidx.compose:compose-bom:2025.10.00")
implementation "androidx.compose.ui:ui"
implementation "androidx.compose.material3:material3"
implementation "androidx.compose.ui:ui-tooling-preview"

// Architecture
implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0"

// Storage
implementation "androidx.room:room-runtime:2.6.1"
```

### Architecture Pattern
```
TermuxService (existing)
    ↓
TerminalSession (PTY interface)
    ↓
CommandBlockManager (new)
    ↓
ConversationalTerminalView (Compose UI)
    ↓
LazyColumn of CommandCards
```

## Development Roadmap (Detailed)

This roadmap aligns with the Feature Prioritization Matrix above, providing specific implementation timelines for all documented features.

---

### Phase 1: MVP - Foundation (Months 1-3)
**Goal:** Launch usable terminal with modern conversational UX
**Target Users:** 1,000+ downloads
**Revenue:** $0 (free only)

#### Month 1: Core Terminal & Blocks (Weeks 1-4)

**Week 1-2: Project Setup & Integration**
- Fork Termux app repository
- Set up Android Studio project with Jetpack Compose
- Configure Gradle dependencies
- Integrate Termux terminal libraries
- Create hybrid architecture (Termux core + Compose UI)
- Set up version control and CI/CD

**Week 3-4: Basic Block UI**
- Implement CommandBlock data structure
- Create CommandBlockManager to bridge Termux → Compose
- Build basic LazyColumn with Card composables
- Add prompt detection for command boundaries
- Implement basic ANSI parsing for colored output
- Create simple status indicators (success/error/running)

**Deliverables:**
- ✅ Conversational Block Interface (basic)
- ✅ Terminal emulation working via Termux
- ✅ Commands appear as discrete cards
- ✅ Full Linux environment functional

---

#### Month 2: Enhanced Blocks & Input (Weeks 5-8)

**Week 5: Smart Command Cards**
- Add execution time display
- Implement command type detection (Git, NPM, Python, Docker)
- Add command icons based on type
- Create collapsible output for long responses
- Basic syntax highlighting for code in output

**Week 6: Chat-Like Input**
- Bottom input bar (fixed position)
- Multi-line command support
- Large send button with Material3 styling
- Input history (swipe up to see recent commands)
- Keyboard management (auto-focus)

**Week 7: Touch Gestures**
- Swipe right → copy output
- Swipe left → delete from history
- Long press → context menu
- Haptic feedback on actions
- Visual hints for first-time users

**Week 8: Block Actions**
- Copy as text
- Share via Android share sheet
- Re-run command
- Edit & re-run
- Pin to favorites (basic)

**Deliverables:**
- ✅ Smart Command Cards
- ✅ Chat-Like Input Experience
- ✅ Swipe Gestures
- ✅ Touch-Friendly Actions
- ✅ Block Actions (basic)

---

#### Month 3: Command Palette & Polish (Weeks 9-12)

**Week 9-10: Command Palette**
- "/" trigger for command search
- Fuzzy search implementation
- Pre-built command library (Git, NPM, common utils)
- Category filtering
- Recently used commands
- Parameter hints

**Week 11: Polish & Testing**
- Animations & transitions
- Loading states
- Error handling
- Performance optimization (60fps target)
- Memory leak detection
- Battery usage testing

**Week 12: F-Droid Preparation**
- F-Droid metadata (description, screenshots)
- Privacy policy
- Basic documentation (README, user guide)
- Accessibility audit (TalkBack support)
- GitHub repository setup
- Marketing materials (website landing page)

**MVP Deliverables:**
- ✅ Conversational Block Interface
- ✅ Smart Command Cards
- ✅ Chat-Like Input
- ✅ Swipe Gestures
- ✅ Touch-Friendly Actions
- ✅ Command Palette
- ✅ Block Actions (basic)
- ✅ F-Droid ready for submission

**Success Criteria:**
- Can execute all basic terminal commands
- Feels like a modern chat app
- 60fps UI performance
- <100ms command latency
- F-Droid submission accepted
- 1,000+ downloads in first month

---

### Phase 2: Enhancement - Productivity & Cloud (Months 4-6)
**Goal:** Add productivity features + launch cloud sync service (REVENUE!)
**Target Users:** 10,000+ downloads
**Revenue Target:** $2,000-3,000/month

#### Month 4: Workflows & Cloud Sync Foundation (Weeks 13-16)

**Week 13-14: Workflows & Templates**
- YAML workflow format definition
- Workflow parser and executor
- Pre-built workflows (deploy, test, git operations)
- Custom workflow editor
- Parameter input forms
- Local workflow storage

**Week 15-16: Cross-Device Session Sync - Phase 1 (Manual)**
- JSONL session export from Termux
- Claude Code session parser
- Session import functionality
- Manual file transfer support
- Session metadata (title, timestamp, tags)
- Privacy controls (redact secrets)

**Deliverables:**
- ✅ Workflows & Templates (basic)
- ✅ Session export/import (manual)

---

#### Month 5: Cloud Sync Service & Context Features (Weeks 17-20)

**Week 17-18: Cross-Device Session Sync - Phase 2 (Cloud)**
- Backend API development (session CRUD)
- E2E encryption implementation
- Android client integration
- Automatic background sync
- Conflict resolution
- Web dashboard (view sessions online)

**Week 19: Project Detection & Environment Awareness**
- File-based project type detection (package.json, requirements.txt, etc.)
- Suggested commands per project type
- Auto-read package.json scripts
- Git branch display in header
- Directory breadcrumbs
- Resource monitoring (CPU, memory, battery)

**Week 20: Visual Customization**
- Theme system (Material3 dynamic theming)
- Pre-built color schemes (Dracula, Solarized, Nord, One Dark)
- Custom font support (JetBrains Mono, Fira Code, etc.)
- Spacing presets (Compact/Comfortable/Spacious)
- Block style options (Rounded/Square, Shadow/Flat)
- User preferences storage

**Deliverables:**
- ✅ Cloud Sync Service launched ($5/month tier)
- ✅ Cross-device Claude Code sessions
- ✅ Project Detection
- ✅ Environment Awareness
- ✅ Visual Customization

---

#### Month 6: Background Execution & Security (Weeks 21-24)

**Week 21-22: Background Execution & Notifications**
- Android Foreground Service implementation
- Background command execution
- Push notifications (completion, errors)
- Progress notifications
- Notification actions (view output, re-run)
- WorkManager for scheduled commands

**Week 23: Secret Detection & Privacy**
- Regex patterns for API keys, tokens, passwords
- Real-time secret detection
- Warning UI before committing secrets
- Secret redaction from shared sessions
- Environment variable encryption
- Incognito session mode

**Week 24: Android Integrations & Session Sharing**
- Share Sheet integration (receive files)
- App Shortcuts (long-press icon)
- Split screen support optimization
- Session sharing (export shareable links)
- Privacy level controls (Public/Unlisted/Team/Private)

**Phase 2 Deliverables:**
- ✅ Workflows & Templates
- ✅ Cross-Device Session Sync (full cloud sync)
- ✅ Background Execution & Notifications
- ✅ Project Detection
- ✅ Environment Awareness
- ✅ Secret Detection
- ✅ Privacy Controls
- ✅ Visual Customization
- ✅ Session Sharing
- ✅ Android Integrations

**Success Criteria:**
- 10,000+ active users
- 20-30% Cloud Sync conversion (2,000-3,000 paying users)
- $2,000-3,000/month revenue
- <0.1% crash rate
- 4.5+ star rating on F-Droid
- Background commands work reliably

---

### Phase 3: Advanced - AI & Enterprise (Months 7-12)
**Goal:** AI-powered assistance + enterprise features
**Target Users:** 50,000+ downloads
**Revenue Target:** $10,000-15,000/month

#### Month 7-8: AI Command Assistant (Weeks 25-32)

**Week 25-27: Natural Language → Command**
- Claude API integration
- System prompt engineering for command generation
- Context-aware command suggestions
- Command explanation UI
- "Run / Edit / Explain More" actions
- Confidence scoring

**Week 28-30: Error Explanation & Fixing**
- Common error pattern database
- Pattern matching for instant fixes
- AI-powered complex error analysis
- Multi-step fix suggestions
- Educational explanations ("why this happened")
- "Learn more" links to documentation

**Week 31-32: Cross-Device Session Sync - Phase 3 (Real-Time)**
- WebSocket implementation
- Real-time session updates
- Multi-device viewing
- Session branching (try different approaches)
- Merge conflict UI

**Deliverables:**
- ✅ AI Command Assistant
- ✅ Error Explanation & Fixing
- ✅ Real-Time Session Sync

---

#### Month 9-10: Git Visual Integration (Weeks 33-40)

**Week 33-35: Visual Git Status**
- Parse `git status --porcelain`
- Beautiful Git Status Card UI
- File staging interface
- Visual branch indicator
- Ahead/behind tracking

**Week 36-38: Git History & Diffs**
- Git log parsing and visualization
- Commit browser UI
- Inline diff viewer with syntax highlighting
- Visual branch tree (graph)
- Tap file → see changes

**Week 39-40: PR Integration**
- GitHub CLI integration
- Create PR from terminal
- Review PRs visually
- Merge/close PRs
- PR status in Git Status Card

**Deliverables:**
- ✅ Git Visual Integration
- ✅ PR Integration

---

#### Month 11-12: Enterprise & Learning System (Weeks 41-48)

**Week 41-43: Team Workflows (Enterprise)**
- Organization accounts
- Shared workflow library
- Team approval workflows
- Audit logs
- Role-based access control
- Admin dashboard (web)
- SSO/SAML integration

**Week 44-46: Learning System**
- Command history analytics (Room database)
- Pattern detection algorithms
- Usage statistics dashboard
- Efficiency tips ("You can use alias...")
- Skill tracking
- Achievement system (optional gamification)

**Week 47: Android Widgets**
- Quick Command Widget (Glance API)
- Project Status Widget
- Widget configuration activity
- Background updates via WorkManager

**Week 48: Biometric Auth**
- Fingerprint/Face ID integration
- App lock
- Session lock (sensitive sessions)
- Auto-lock after inactivity
- Sensitive command protection (require auth for sudo)

**Phase 3 Deliverables:**
- ✅ AI Command Assistant
- ✅ Error Explanation & Fixing
- ✅ Git Visual Integration
- ✅ Android Widgets
- ✅ Team Workflows (Enterprise)
- ✅ Learning System
- ✅ Biometric Auth
- ✅ Real-Time Session Sync

**Success Criteria:**
- 50,000+ active users
- $10,000-15,000/month revenue
- 5+ enterprise customers
- AI features actively used (50%+ of users)
- Error fix suggestions 80%+ helpful
- Team workflows save 5+ hours/week per team

---

### Future/Experimental (Months 13+)
**Goal:** Cutting-edge features for differentiation

#### AI Agents (Warp 2.0 Style) - 4-6 weeks
- Long-running background agents
- Multi-step autonomous tasks
- Agent Management Panel
- Safety controls (approval for destructive actions)
- Resource limits
- Code generation, monitoring, refactoring capabilities

#### Cloud Service Integrations - Variable timing
- **Vercel/Netlify/Railway** (2 weeks each)
  - One-tap deploys
  - Deployment status monitoring
  - Rollback capabilities

- **Docker/Podman** (3 weeks)
  - Container list/start/stop UI
  - Log viewing
  - Resource monitoring

- **Database Tools** (4 weeks)
  - Visual query builder
  - Schema browser
  - Connection manager

#### Workflow Marketplace - 4-6 weeks
- Public workflow sharing
- Workflow discovery feed
- Rating & comments
- Revenue sharing with creators
- Moderation tools

#### Self-Hosting Option - 3-4 weeks
- Docker compose setup
- Self-hosted sync server
- Documentation for deployment
- Database migration tools

#### Real-Time Collaboration - 6-8 weeks
- Live session sharing
- Multi-user terminals
- Cursor presence
- Session annotations & comments
- Permissions & access control

---

## Roadmap Summary

| Phase | Duration | Key Features | Target Users | Revenue |
|-------|----------|--------------|--------------|---------|
| **Phase 1: MVP** | Months 1-3 | Conversational blocks, gestures, palette | 1,000+ | $0 |
| **Phase 2: Enhancement** | Months 4-6 | Cloud sync, workflows, background exec | 10,000+ | $2-3K/mo |
| **Phase 3: Advanced** | Months 7-12 | AI features, enterprise, Git integration | 50,000+ | $10-15K/mo |
| **Future** | Months 13+ | Agents, integrations, marketplace | 100,000+ | $30-50K/mo |

**Total Time to Phase 3 Complete:** 12 months
**Estimated Solo Development Time:** ~480 hours per phase (1,440 hours total)
**With 1-2 Contributors:** Could reduce to 8-9 months

---

## Development Velocity Assumptions

**Solo Developer (20 hrs/week):**
- Phase 1: 3 months (240 hours)
- Phase 2: 3 months (240 hours)
- Phase 3: 6 months (480 hours)
- **Total: 12 months**

**With Team (2-3 devs, full-time):**
- Phase 1: 1 month
- Phase 2: 1.5 months
- Phase 3: 3 months
- **Total: 5.5 months**

---

## Risk Mitigation

**Risk: Feature creep delays MVP**
- Mitigation: Strict adherence to Phase 1 scope, cut features if needed

**Risk: Termux compatibility breaks**
- Mitigation: Regular testing with Termux updates, modular architecture

**Risk: F-Droid submission rejection**
- Mitigation: Follow F-Droid guidelines exactly, get early feedback from community

**Risk: Cloud sync is complex to build**
- Mitigation: Start with manual export/import (Phase 2 Month 4), iterate gradually

**Risk: Low adoption/revenue**
- Mitigation: Focus on cross-device sync as killer feature, strong marketing, community building

---

## User Experience Flows

This section documents the detailed user journeys for key features, ensuring a smooth and intuitive experience.

---

### Flow 1: First-Time User Onboarding

**Goal:** Help new users understand the conversational interface and discover key features.

```
1. User installs Convo from F-Droid
   └→ Opens app for first time

2. Welcome Screen
   ┌─────────────────────────────┐
   │ 👋 Welcome to Convo         │
   │                             │
   │ The conversational terminal │
   │ for Android                 │
   │                             │
   │ [Get Started]               │
   │ [Import from Termux]        │
   └─────────────────────────────┘

3. Quick Tutorial (3 screens, swipe)

   Screen 1: Commands as Conversations
   ┌─────────────────────────────┐
   │ 💬 Chat with your terminal  │
   │                             │
   │ [Visual: Command block demo]│
   │                             │
   │ Every command is a          │
   │ beautiful card you can      │
   │ copy, share, and re-run.    │
   │                             │
   │ [Next] [Skip]               │
   └─────────────────────────────┘

   Screen 2: Swipe to Act
   ┌─────────────────────────────┐
   │ 👆 Quick actions            │
   │                             │
   │ [Visual: Swipe animation]   │
   │                             │
   │ Swipe right → Copy          │
   │ Swipe left → Delete         │
   │ Long press → More options   │
   │                             │
   │ [Next] [Skip]               │
   └─────────────────────────────┘

   Screen 3: Command Palette
   ┌─────────────────────────────┐
   │ / Search everything         │
   │                             │
   │ [Visual: Palette demo]      │
   │                             │
   │ Type "/" to search          │
   │ commands, workflows,        │
   │ and your history.           │
   │                             │
   │ [Start Using Convo]         │
   └─────────────────────────────┘

4. Main Terminal Screen
   └→ Shows example command block
   └→ Floating tooltip: "Try typing 'ls' or '/' to search"
```

**Duration:** 30-60 seconds
**Skip Option:** Yes (accessible from settings later)

---

### Flow 2: Executing a Command (Core Experience)

**Goal:** Make command execution feel like a conversation.

```
1. User sees main terminal screen
   ┌─────────────────────────────┐
   │ 📁 ~ (home)                 │
   │ 🌿 main                     │
   ├─────────────────────────────┤
   │                             │
   │ [Previous command blocks]   │
   │                             │
   │                             │
   │                             │
   ├─────────────────────────────┤
   │ [Enter command...]    [▶]  │
   └─────────────────────────────┘

2. User types "ls -la"
   └→ Autocomplete suggestions appear
   └→ Command type icon shows (📁 file operations)

3. User taps Send button (or Enter)
   └→ Command appears as user message block

   ┌─────────────────────────────┐
   │ 💭 You                     │
   │ ls -la                      │
   │ 📁 Just now                 │
   └─────────────────────────────┘

4. Command executes (realtime)
   └→ "Running..." indicator appears

   ┌─────────────────────────────┐
   │ 🤖 Terminal                │
   │ ⏳ Running... 0.1s          │
   │                             │
   └─────────────────────────────┘

5. Command completes
   └→ Output appears in response block

   ┌─────────────────────────────┐
   │ 🤖 Terminal                │
   │ ✅ Success • 0.24s          │
   │                             │
   │ drwxr-xr-x  5 user  users  │
   │ -rw-r--r--  1 user  users  │
   │ ...                         │
   │                             │
   │ [Copy] [Share] [Re-run]     │
   └─────────────────────────────┘

6. Input field auto-focuses
   └→ Ready for next command
```

**Key Interactions:**
- Tap input → Keyboard appears
- Type "/" → Command Palette opens
- Swipe on block → Quick actions
- Long press block → Context menu
- Tap "Re-run" → Executes command again

---

### Flow 3: Swipe Gestures (Touch-Optimized)

**Goal:** Provide quick actions without cluttering the UI.

```
1. User sees command block
   ┌─────────────────────────────┐
   │ 🤖 Terminal                │
   │ ✅ Success • 0.1s           │
   │                             │
   │ file1.txt                   │
   │ file2.txt                   │
   └─────────────────────────────┘

2. User swipes right (30% of screen width)
   └→ Reveals copy icon
   └→ Haptic feedback (light tap)

   ┌───────────────────────┐
   │ 📋                    │ ← Copy icon revealed
   ├───────────────────────┤
   │ 🤖 Terminal          │
   │ ✅ Success • 0.1s     │
   │                       │
   │ file1.txt             │
   └───────────────────────┘

3. User completes swipe (>50% threshold)
   └→ Block snaps back with animation
   └→ Haptic feedback (success vibration)
   └→ Toast: "Copied to clipboard ✅"

Alternative: User swipes left
   └→ Delete icon revealed
   └→ Complete swipe → Confirmation dialog

   ┌─────────────────────────────┐
   │ Delete from history?        │
   │                             │
   │ This cannot be undone.      │
   │                             │
   │ [Cancel] [Delete]           │
   └─────────────────────────────┘
```

**Gesture Map:**
- Swipe Right → Copy output
- Swipe Left → Delete (with confirmation)
- Swipe Up → Share
- Long Press → Context menu
- Pinch → Zoom (accessibility)

---

### Flow 4: Command Palette (Discovery)

**Goal:** Help users discover commands without memorizing syntax.

```
1. User types "/" in input field
   └→ Command Palette immediately opens

   ┌─────────────────────────────┐
   │ 🔍 /                        │
   ├─────────────────────────────┤
   │ 📂 Popular Commands         │
   │   📁 List files (ls)        │
   │   🔀 Git status             │
   │   📦 NPM install            │
   │                             │
   │ ⚡ Recent                   │
   │   npm run dev               │
   │   git commit -m...          │
   │                             │
   │ 🔄 Workflows                │
   │   Deploy to Production      │
   └─────────────────────────────┘

2. User types "/git"
   └→ Results filter in real-time

   ┌─────────────────────────────┐
   │ 🔍 /git                     │
   ├─────────────────────────────┤
   │ 🔀 Git Status               │
   │   git status                │
   │                             │
   │ 🌿 Git Branch               │
   │   git branch                │
   │                             │
   │ 💾 Git Commit               │
   │   git commit -m "[message]" │
   │                             │
   │ 🔄 Git Pull & Push          │
   │   git pull && git push      │
   └─────────────────────────────┘

3. User taps "Git Commit"
   └→ Parameter form appears

   ┌─────────────────────────────┐
   │ 💾 Git Commit               │
   ├─────────────────────────────┤
   │ Commit message:             │
   │ [Add user authentication]   │
   │                             │
   │ Options:                    │
   │ [ ] Include unstaged files  │
   │ [ ] Amend previous commit   │
   │                             │
   │ [Cancel] [Commit]           │
   └─────────────────────────────┘

4. User fills parameters and taps Commit
   └→ Command executes with filled parameters
   └→ "git commit -m 'Add user authentication'"
```

**Search Features:**
- Fuzzy matching ("gts" → "git status")
- Category filters
- Recently used commands prioritized
- Command descriptions visible
- Parameter hints

---

### Flow 5: Cross-Device Session Sync (Killer Feature)

**Goal:** Seamlessly continue Claude Code conversations across devices.

**Scenario:** Developer starts work on desktop, continues on Android

```
=== ON DESKTOP (Any OS) ===

1. Developer uses Claude Code on desktop
   Desktop Terminal:
   $ claude-code "Build user authentication API"
   Claude: I'll create the authentication system...
   [Creates files, implements features]

   Session: ~/.claude/projects/my-app/session_abc123.jsonl
   Contains: 47 messages, full context

2. Convo syncs session automatically
   └→ Desktop Convo client detects new Claude Code session
   └→ Uploads to Convo Cloud (E2E encrypted)
   └→ Real-time sync to all devices

=== ON ANDROID (Convo App) ===

3. Developer opens Convo on phone (10 mins later)
   └→ Notification: "Desktop session synced ✅"

   ┌─────────────────────────────┐
   │ 🔄 Synced Sessions          │
   ├─────────────────────────────┤
   │ 🔥 Active Now               │
   │                             │
   │ 💻 User Auth API            │
   │    Desktop → Android        │
   │    2 minutes ago            │
   │    47 messages              │
   │    [Continue] [View]        │
   │                             │
   │ ☁️ Cloud Sessions (12)      │
   │    [Browse All]             │
   └─────────────────────────────┘

4. User taps [Continue]
   └→ Full session loads with context

   ┌─────────────────────────────┐
   │ 💬 User Auth API Session    │
   │ 💻 Started on Desktop       │
   ├─────────────────────────────┤
   │                             │
   │ [Message history visible]   │
   │ ...                         │
   │                             │
   │ 🤖 Claude: I've created the │
   │ authentication system with  │
   │ JWT tokens, password hash-  │
   │ ing, and rate limiting.     │
   │                             │
   ├─────────────────────────────┤
   │ [Your message...]     [▶]  │
   └─────────────────────────────┘

5. User continues conversation
   User: "Now add mobile OAuth login"
   Claude: "I'll add OAuth support for Google and Apple.
           Based on the API I just created..."

   ✅ FULL CONTEXT PRESERVED!
   └→ Claude remembers all previous work
   └→ References files created on desktop
   └→ Understands project structure

6. Changes sync back to desktop
   └→ Android session updates upload to cloud
   └→ Desktop Claude Code sees continuation
   └→ Developer can pick up on desktop again
```

**Sync Flow Details:**

```
Desktop → Cloud:
1. Detect new/updated Claude Code session files
2. Parse JSONL format
3. Encrypt with user's key (E2E)
4. Upload to Convo Cloud API
5. Notify other devices via WebSocket

Cloud → Android:
1. Receive sync notification
2. Download encrypted session
3. Decrypt locally (zero-knowledge)
4. Import into local Convo session
5. Display in "Synced Sessions" list
6. Ready to continue instantly
```

**Key UX Elements:**
- **Automatic:** No manual export/import
- **Fast:** Sessions appear in <5 seconds
- **Secure:** E2E encrypted, user controls keys
- **Offline-first:** Local copy, syncs when online
- **Conflict resolution:** UI for manual merging if needed

---

### Flow 6: AI Error Explanation (Beginner-Friendly)

**Goal:** Help users understand and fix errors without googling.

```
1. User runs command that fails
   User: npm install

   ┌─────────────────────────────┐
   │ 💭 You                     │
   │ npm install                 │
   └─────────────────────────────┘

2. Command fails with error
   ┌─────────────────────────────┐
   │ 🤖 Terminal                │
   │ ❌ Error • Exit code 1      │
   │                             │
   │ npm ERR! code EACCES        │
   │ npm ERR! syscall access     │
   │ npm ERR! path /usr/local/   │
   │ lib/node_modules            │
   │ npm ERR! errno -13          │
   │                             │
   │ EACCES: permission denied   │
   └─────────────────────────────┘

3. Convo analyzes error (instant, pattern match)
   └→ Matches "EACCES" + "node_modules"
   └→ 💡 Shows explanation card

   ┌─────────────────────────────┐
   │ 💡 Error Explained          │
   ├─────────────────────────────┤
   │ **Permission Denied**       │
   │                             │
   │ You don't have permission   │
   │ to install packages globally│
   │ in /usr/local/              │
   │                             │
   │ **Why this happened:**      │
   │ npm tries to install to a   │
   │ system directory that       │
   │ requires admin access.      │
   │                             │
   │ **Recommended Fix:**        │
   │ Use a Node version manager  │
   │ like nvm instead.           │
   │                             │
   │ [Install nvm]               │
   │                             │
   │ **Alternative:**            │
   │ Install with sudo (not      │
   │ recommended for security)   │
   │                             │
   │ [Run: sudo npm install]     │
   │ [Learn More About nvm]      │
   └─────────────────────────────┘

4. User taps [Install nvm]
   └→ Workflow executes automatically

   Running workflow: Install Node Version Manager
   ✅ Download nvm script
   ✅ Run installation
   ✅ Update shell profile
   ✅ Source nvm

   ┌─────────────────────────────┐
   │ ✅ nvm installed!           │
   │                             │
   │ Try this now:               │
   │ [nvm install 18]            │
   │ [nvm use 18]                │
   │ [npm install]               │
   └─────────────────────────────┘

5. User follows suggestions
   └→ Commands work without errors
   └→ Positive learning experience
```

**AI Error Features:**

**Common Errors (Instant, No API):**
- Pattern database with 1000+ common errors
- Instant explanations
- Pre-built fix suggestions
- No internet required

**Complex Errors (AI-Powered):**
- Send to Claude API (if user has Convo Pro)
- Contextual analysis
- Multiple fix suggestions
- Educational explanations

**Educational Elements:**
- "Why this happened" section
- Links to documentation
- Best practices noted
- Security warnings for dangerous fixes

---

### Flow 7: Creating a Workflow (Power User)

**Goal:** Let users save multi-step processes for reuse.

```
1. User completes a multi-step task
   Commands executed:
   - npm test
   - npm run build
   - git add .
   - git commit -m "Release v1.2"
   - git push
   - npm publish

2. User long-presses on first command block
   └→ Context menu appears

   ┌─────────────────────────────┐
   │ Copy                        │
   │ Share                       │
   │ Re-run                      │
   │ ────────────────            │
   │ 💫 Create Workflow from     │
   │    last 6 commands          │
   └─────────────────────────────┘

3. User taps "Create Workflow"
   └→ Workflow editor opens

   ┌─────────────────────────────┐
   │ 💫 New Workflow             │
   ├─────────────────────────────┤
   │ Name:                       │
   │ [Release & Publish]         │
   │                             │
   │ Description:                │
   │ [Run tests, build, commit,  │
   │  and publish to npm]        │
   │                             │
   │ Steps: (6)                  │
   │ 1. ✅ npm test              │
   │ 2. ✅ npm run build         │
   │ 3. ✅ git add .             │
   │ 4. 📝 git commit -m "..."   │
   │       └→ Parameter needed   │
   │ 5. ✅ git push              │
   │ 6. ✅ npm publish           │
   │                             │
   │ [Edit Steps] [Save]         │
   └─────────────────────────────┘

4. User taps [Edit Steps] → Step 4
   └→ Parameter configuration

   ┌─────────────────────────────┐
   │ Edit Step 4                 │
   ├─────────────────────────────┤
   │ Command:                    │
   │ git commit -m "{{message}}" │
   │                             │
   │ Parameter: message          │
   │ Type: [String ▼]            │
   │ Prompt: "Commit message:"   │
   │ Required: [✓]               │
   │ Default: "Release update"   │
   │                             │
   │ [Cancel] [Save]             │
   └─────────────────────────────┘

5. User saves workflow
   └→ Workflow appears in Command Palette

   ┌─────────────────────────────┐
   │ 🔍 /release                 │
   ├─────────────────────────────┤
   │ 💫 Your Workflows           │
   │                             │
   │   Release & Publish         │
   │   6 steps • Requires input  │
   │   [Run]                     │
   └─────────────────────────────┘

6. Next time: User types "/release"
   └→ Parameter form appears
   └→ Fills in commit message
   └→ All 6 steps execute automatically
   └→ Time saved: ~2 minutes
```

---

### Flow 8: Background Command Execution (Mobile Advantage)

**Goal:** Long-running commands continue even when app is closed.

```
1. User starts long-running command
   User: npm install   (300+ packages, 10+ minutes)

   ┌─────────────────────────────┐
   │ 🤖 Terminal                │
   │ ⏳ Running... 3s            │
   │                             │
   │ npm http fetch GET 200...   │
   │ [output streaming]          │
   └─────────────────────────────┘

2. System detects long-running command
   └→ Notification appears

   📱 Notification:
   "npm install is running in background"
   [Tap to view] [Stop]

3. User switches to browser
   └→ Convo moves to background
   └→ Foreground Service keeps command running
   └→ Notification shows progress

4. 10 minutes later...
   └→ Command completes
   └→ Notification updates

   📱 Notification:
   "npm install completed ✅ (10m 32s)"
   [View output]

5. User taps notification
   └→ Returns to Convo
   └→ Full output visible

   ┌─────────────────────────────┐
   │ 🤖 Terminal                │
   │ ✅ Success • 10m 32s        │
   │                             │
   │ added 847 packages in 10m   │
   │ 123 packages are looking... │
   │                             │
   │ [Copy] [Share]              │
   └─────────────────────────────┘
```

**Background Execution Features:**
- Foreground Service (Android)
- Real-time progress in notification
- Can stop from notification
- Full output preserved
- Works with screen off
- Battery optimized

---

## Key UX Principles

Throughout all flows, Convo follows these design principles:

### 1. **Conversational**
- Commands feel like messages
- Terminal "responds" like an assistant
- Natural back-and-forth flow

### 2. **Touch-First**
- Large touch targets (48x48dp minimum)
- Swipe gestures for common actions
- Haptic feedback for all interactions
- Mobile-optimized layouts

### 3. **Discoverable**
- Command Palette for finding features
- Tooltips on first use
- Progressive disclosure (don't overwhelm)
- "Undo" for destructive actions

### 4. **Forgiving**
- Clear error messages with solutions
- Confirmation for destructive operations
- Undo/redo support where possible
- Auto-save user work

### 5. **Beautiful**
- Material Design 3
- Smooth animations (60fps)
- Syntax highlighting
- Consistent visual language

### 6. **Fast**
- <100ms command latency
- Instant search results
- Optimistic UI updates
- Background sync

---

## Key Implementation Details

### 1. Bridging Terminal Output to Compose

**Challenge:** Terminal emulators use custom views with direct canvas drawing. We need to convert this to Compose.

**Solution:**
```kotlin
class CommandBlockManager(
    private val terminalSession: TerminalSession
) {
    private val _blocks = MutableStateFlow<List<CommandBlock>>(emptyList())
    val blocks: StateFlow<List<CommandBlock>> = _blocks.asStateFlow()

    init {
        // Listen to terminal output
        terminalSession.addOutputListener { output ->
            updateCurrentBlock(output)
        }

        // Detect command completion
        terminalSession.addPromptListener {
            finalizeCurrentBlock()
        }
    }
}
```

### 2. ANSI Escape Sequence Parsing

**Challenge:** Convert ANSI color codes and formatting to Compose AnnotatedString.

**Solution:**
```kotlin
fun String.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        var index = 0
        val ansiPattern = "\u001B\\[[0-9;]*m".toRegex()

        ansiPattern.findAll(this@toAnnotatedString).forEach { match ->
            // Add text before escape sequence
            append(substring(index, match.range.first))

            // Parse escape sequence
            val code = match.value
            val style = parseAnsiCode(code)

            // Apply style to subsequent text
            pushStyle(style)

            index = match.range.last + 1
        }

        // Add remaining text
        append(substring(index))
    }
}
```

### 3. Command Detection

**Challenge:** Detect when user enters a command vs when it's still typing.

**Solution:**
```kotlin
TextField(
    value = input,
    onValueChange = { input = it },
    keyboardOptions = KeyboardOptions(
        imeAction = ImeAction.Done
    ),
    keyboardActions = KeyboardActions(
        onDone = {
            createNewCommandBlock(input)
            terminalSession.write("$input\n")
            input = ""
        }
    )
)
```

### 4. Block Actions

**Swipe to Reveal Actions:**
```kotlin
@Composable
fun CommandCard(block: CommandBlock) {
    val swipeState = rememberSwipeableState(0)

    Box(
        modifier = Modifier
            .swipeable(
                state = swipeState,
                anchors = mapOf(0f to 0, -300f to 1),
                thresholds = { _, _ -> FractionalThreshold(0.3f) }
            )
    ) {
        // Actions revealed on swipe
        Row {
            IconButton(onClick = { copyToClipboard(block.output) }) {
                Icon(Icons.Default.ContentCopy)
            }
            IconButton(onClick = { reRunCommand(block.command) }) {
                Icon(Icons.Default.Refresh)
            }
            IconButton(onClick = { shareBlock(block) }) {
                Icon(Icons.Default.Share)
            }
        }

        // Command card
        CommandBlockContent(block)
    }
}
```

## Features Comparison

### Traditional Terminal (Termux) vs Convo

| Feature | Termux | Convo |
|---------|--------|-------|
| Full Linux environment | ✅ | ✅ |
| Package management | ✅ | ✅ |
| Command blocks | ❌ | ✅ |
| Chat-like interface | ❌ | ✅ |
| Modern UI | ❌ | ✅ |
| Touch-optimized | ❌ | ✅ |
| AI assistance | ❌ | ✅ (optional) |
| Markdown rendering | ❌ | ✅ |
| Command history search | Basic | Advanced |
| **Cross-device sessions** | ❌ | ✅ **UNIQUE** |
| Themes | Limited | Full Material3 |
| Accessibility | Basic | Full support |

### Warp Desktop vs Convo Mobile

| Feature | Warp Desktop | Convo Mobile |
|---------|--------------|--------------|
| Command blocks | ✅ | ✅ |
| AI features | ✅ | ✅ (planned) |
| Agent support | ✅ (2.0) | 🔄 (future) |
| **Cross-device sync** | ❌ | ✅ **UNIQUE** |
| Native UI | macOS/Linux | Android |
| Touch interface | ❌ | ✅ |
| Mobile optimized | ❌ | ✅ |
| Package ecosystem | macOS | Linux (Termux) |

## Warp 2.0 Features to Implement

Based on Warp's 2025 updates, plus our unique additions:

1. **Command Blocks** ✅ Core feature
2. **AI Command Generation** 🔄 Phase 3
3. **Inline Suggestions** 🔄 Phase 2
4. **Natural Language Input** 🔄 Phase 3
5. **Block Editing** 🔄 Phase 2
6. **Rich Output Rendering** ✅ Phase 1
7. **Command History with Context** 🔄 Phase 2
8. **Agent Support** ⏳ Future enhancement
9. **Cross-Device Session Sync** ✅ **UNIQUE TO CONVO** - Phases 2-3

## Challenges & Solutions

### Challenge 1: ANSI Parsing to Compose
**Problem:** Terminal output contains ANSI escape sequences that need to be converted to Compose styling.

**Solution:**
- Build ANSI parser that converts to AnnotatedString
- Cache parsed output for performance
- Support common color schemes
- Handle edge cases (ncurses, progress bars)

### Challenge 2: Performance with Large Outputs
**Problem:** Commands like `cat large_file.txt` could generate thousands of lines.

**Solution:**
- Implement output pagination within blocks
- "Show more/less" toggles
- Virtual scrolling with LazyColumn
- Limit stored history (configurable)

### Challenge 3: PTY Compatibility
**Problem:** Some programs (vim, htop) require full terminal control.

**Solution:**
- Detect full-screen apps (ncurses)
- Switch to traditional terminal view
- Smooth transition back to blocks mode
- Allow users to choose default behavior

### Challenge 4: Interactive Programs
**Problem:** Programs expecting real-time input (REPL, ssh).

**Solution:**
- Detect interactive mode
- Enable streaming input
- Show "live" block that updates in real-time
- Finalize block when program exits

### Challenge 5: Play Store Distribution
**Problem:** Google Play restricts apps targeting API < 34 and has restrictions on terminal emulators.

**Solution:**
- Target API 34+
- Primary distribution via F-Droid (recommended for FOSS)
- GitHub releases with APK
- Document Play Store submission process
- Consider Termux's approach (they moved to F-Droid)

## Licensing & Distribution

### License: GPLv3
**Why GPLv3?**
- Termux is GPLv3, so derivatives must be too
- Ensures project remains open source
- Strong copyleft protections
- Compatible with most FOSS libraries

**What This Means:**
- ✅ Free to use, modify, distribute
- ✅ Can charge for distribution (but source must be available)
- ✅ Can be forked by others
- ⚠️ All modifications must be GPLv3
- ⚠️ Must provide source code

### Distribution Channels

#### F-Droid (Primary)
**Advantages:**
- Standard channel for FOSS Android apps
- No API restrictions
- Trusted by privacy-conscious users
- Termux ecosystem lives here
- Automatic builds from source

**Requirements:**
- All dependencies must be FOSS
- Builds must be reproducible
- Metadata file required

#### GitHub Releases
**Advantages:**
- Direct APK downloads
- Beta testing channel
- Full control over release process

**Process:**
- Tag releases
- Build signed APKs
- Create GitHub release
- Attach APK files

#### Google Play Store
**Challenges:**
- Must target API 34+
- Strict review process
- May restrict terminal functionality
- Requires developer account ($25)

**Recommendation:** Start with F-Droid and GitHub. Consider Play Store later if there's demand and no policy conflicts.

## Technology Research Notes

### Termux Architecture
- **Location:** `/data/data/com.termux/files/`
- **Filesystem:** Minimal Linux with `home/` and `usr/` directories
- **Package management:** `apt` (Debian-style)
- **Cross-compilation:** Android NDK for ARM/AARCH64
- **No root:** Runs entirely in user space
- **Version note:** Last Play Store update v0.101 (Nov 2020), actively maintained on F-Droid

### Jetpack Compose (2025 State)
- **Latest version:** 1.8.0 (April 2025)
- **Adoption:** 60% of top 1000 apps
- **New features:** Autofill, text improvements, visibility tracking, size/location animations
- **Performance:** Optimized for 60fps
- **Tooling:** Preview, inspection tools

### Android Terminal Emulators Landscape
**Existing Apps:**
- Termux (most popular, Linux environment)
- JuiceSSH (SSH focused)
- ConnectBot (SSH/telnet)
- Android Terminal Emulator (older, VT-100)

**Market Gap:**
- No modern UI terminal on Android
- No Warp-style blocks implementation
- No conversation-style interface
- Opportunity for innovation

## AI Integration (Optional)

### Potential Integrations

#### 1. Claude API (via Anthropic)
```kotlin
class ClaudeAssistant(private val apiKey: String) {
    suspend fun suggestCommand(intent: String): String {
        return anthropicClient.messages.create(
            model = "claude-sonnet-4.5",
            messages = listOf(
                Message(
                    role = "user",
                    content = "Suggest a terminal command for: $intent"
                )
            )
        )
    }

    suspend fun explainError(command: String, error: String): String {
        // Generate helpful explanation
    }
}
```

#### 2. Local Models (via Termux)
- Install `llama.cpp` or `ollama` in Termux
- Run models locally
- No API costs
- Privacy-focused

#### 3. Command Prediction (No AI)
- Analyze command history
- Learn usage patterns
- Suggest based on context
- Fully offline

### AI Features Roadmap

**Phase 3 (Initial):**
- Command suggestions from natural language
- Error explanation
- Syntax help

**Future Enhancements:**
- Smart auto-complete
- Command chaining suggestions
- Script generation
- Log analysis
- Security suggestions

## Name Ideas

### Top Recommendations

#### 1. Convo (Primary Choice)
**Full names:** Convo, ConvoShell, ConvoTerm

**Pros:**
- Immediately communicates conversational UI
- Short, modern, memorable
- Easy to pronounce internationally
- Good domain availability
- Package: `com.convoshell.app`

**Tagline options:**
- "The conversational terminal for Android"
- "Terminal that talks back"
- "Your command line, reimagined"

#### 2. Blox
**Full names:** Blox, Blox Terminal

**Pros:**
- Perfect metaphor for command blocks
- Strong visual branding potential
- Tech-forward but accessible
- Short and punchy
- Package: `com.blox.terminal`

**Tagline options:**
- "Your commands, beautifully blocked"
- "Terminal in blocks"
- "Block by block, command by command"

#### 3. Echo
**Full names:** Echo, Echo Terminal

**Pros:**
- Terminal heritage (echo command)
- Conversation metaphor (echoing back)
- One syllable, easy recall
- Simple icon potential
- Package: `com.echo.terminal`

**Tagline options:**
- "Terminal that talks back"
- "Echo your commands, amplify your productivity"
- "More than just echo"

### Other Creative Options

**Chat/Conversation:**
- Dialogue - Elegant, emphasizes interaction
- Chatter - Playful, approachable
- ShellChat - Descriptive but longer

**Modern/Speed:**
- Flow - Smooth continuous interaction
- Glide - Effortless experience
- Surge - Energy and power

**Structure/Blocks:**
- Stack / Stackterm - Commands stack up
- CardShell - Command cards + shell

**Unix Humor:**
- /dev/talk - Clever device naming
- sudo talk - Permission-based humor

**Portmanteaus:**
- Warpux (Warp + Termux)
- Termex (Terminal + Experience)
- Chatux (Chat + Termux)
- Termova (Terminal + Nova)

### Name Selection Criteria

Consider:
1. **Memorability** - Easy to remember and spell
2. **Clarity** - Conveys purpose
3. **Uniqueness** - Stands out in app stores
4. **Domain availability** - .com, .dev, .sh
5. **Package name** - Clean reverse-domain format
6. **International** - Works globally
7. **Brandable** - Logo potential
8. **SEO** - Searchable, not too generic

## Next Steps

### Immediate Actions
1. **Choose name** - Finalize branding
2. **Set up repository** - GitHub with GPLv3 license
3. **Fork Termux** - Clone termux-app repository
4. **Create project** - Android Studio with Compose
5. **Basic UI** - Hello world with Compose

### Week 1 Goals
- Project skeleton with Compose
- Termux integration working
- First command executed
- Basic block UI rendering

### Month 1 Goals
- MVP complete (Phase 1)
- Can use as daily terminal
- F-Droid metadata ready
- Initial user testing

### Month 3 Goals
- Enhanced UX complete (Phase 2)
- Beta testing with users
- F-Droid submission
- Documentation site

## Resources & References

### Documentation
- [Termux GitHub](https://github.com/termux/termux-app)
- [Termux Wiki](https://wiki.termux.com)
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Material 3 Guidelines](https://m3.material.io)
- [Warp Blog](https://www.warp.dev/blog)

### Libraries
- Terminal View: https://jitpack.io/com/github/termux/termux-app/terminal-view/
- Compose Samples: https://github.com/android/compose-samples
- Chat UI Examples: Multiple tutorials available

### Community
- Termux on F-Droid
- r/termux on Reddit
- Android dev communities
- Open source channels

## Success Metrics

Comprehensive metrics to track Convo's success across all feature categories.

---

### Technical Performance Metrics

**Core Performance:**
- **UI Performance:** Maintain 60fps during all interactions, <2 dropped frames per second
- **Command Latency:** <100ms from command submit to execution start
- **Block Rendering:** <50ms to render new command block
- **Scroll Performance:** Smooth 60fps scrolling with 1000+ command blocks
- **Memory Usage:** <200MB RAM for typical session (50 command blocks)
- **APK Size:** <50MB total app size
- **Startup Time:** <2 seconds cold start, <500ms warm start

**Stability & Reliability:**
- **Crash Rate:** <0.1% crash-free sessions
- **ANR Rate:** <0.01% (Application Not Responding)
- **Background Service Stability:** 99.9% uptime for long-running commands
- **Sync Success Rate:** 99% of session syncs complete successfully
- **Data Integrity:** Zero data loss incidents

**Battery & Resource Efficiency:**
- **Battery Drain:** <5% additional drain vs stock Termux
- **Background Battery:** <1% drain per hour for background commands
- **Network Usage:** <100KB per session sync (excluding command output)
- **Storage Efficiency:** <50MB per 1000 command blocks

---

### User Adoption & Engagement Metrics

**Phase 1 Targets (Months 1-3):**
- **Total Downloads:** 1,000+ (F-Droid)
- **Active Users:** 500+ DAU (Daily Active Users)
- **Retention:**
  - Day 1: 60%
  - Day 7: 40%
  - Day 30: 25%
- **Session Length:** Average 15+ minutes per session
- **Commands per Session:** Average 20+ commands
- **F-Droid Rating:** 4.0+ stars (>50 reviews)

**Phase 2 Targets (Months 4-6):**
- **Total Downloads:** 10,000+
- **Active Users:** 4,000+ DAU
- **Retention:**
  - Day 1: 65%
  - Day 7: 45%
  - Day 30: 30%
- **Power User Ratio:** 20% use app daily
- **F-Droid Rating:** 4.3+ stars (>200 reviews)

**Phase 3 Targets (Months 7-12):**
- **Total Downloads:** 50,000+
- **Active Users:** 15,000+ DAU
- **Retention:**
  - Day 1: 70%
  - Day 7: 50%
  - Day 30: 35%
- **Power User Ratio:** 30% use app daily
- **F-Droid Rating:** 4.5+ stars (>500 reviews)

---

### Feature-Specific Metrics

**Conversational Blocks (MVP):**
- **Block Interaction Rate:** 80%+ users interact with block actions (copy, share, re-run)
- **Swipe Gesture Usage:** 60%+ users use swipe gestures within first week
- **Copy Action:** Average 5+ copies per session for active users
- **Re-run Action:** 30%+ of users re-run at least one command per session

**Command Palette (MVP):**
- **Discovery:** 70%+ users discover Command Palette within first 3 sessions
- **Usage Frequency:** 50%+ of users use "/" search at least once per session
- **Workflow Discovery:** 40%+ users try a pre-built workflow from palette
- **Search Success Rate:** 85%+ searches result in command execution

**Workflows & Templates (Phase 2):**
- **Workflow Creation:** 20%+ of active users create at least one custom workflow
- **Workflow Execution:** Average 10+ workflow executions per day (across all users)
- **Time Savings:** Average 30+ seconds saved per workflow execution
- **Workflow Sharing:** 5%+ of created workflows shared with others

**Cross-Device Session Sync (Phase 2 - Killer Feature):**
- **Cloud Sync Adoption:** 20-30% of active users subscribe ($5/month)
- **Cross-Device Usage:** 60%+ of Cloud Sync subscribers use multiple devices
- **Session Continuity:** 80%+ of synced sessions successfully resumed on different device
- **Sync Speed:** Average <5 seconds from desktop save to Android notification
- **Sync Reliability:** 99%+ of session syncs complete without errors
- **Desktop→Mobile Ratio:** 70% sessions start on desktop, continue on mobile
- **Mobile→Desktop Ratio:** 30% sessions start on mobile, continue on desktop
- **Multi-Device Sessions:** 40%+ of Cloud Sync users have 2+ devices connected
- **Churn Rate:** <5% monthly churn for Cloud Sync subscribers
- **NPS (Net Promoter Score):** 50+ for Cloud Sync feature

**Background Execution (Phase 2):**
- **Usage:** 40%+ users execute at least one background command per week
- **Notification Engagement:** 80%+ users tap completion notifications
- **Completion Rate:** 95%+ background commands complete successfully
- **Average Duration:** 5+ minutes per background command
- **Battery Impact:** <2% additional drain vs foreground execution

**Project Detection (Phase 2):**
- **Detection Accuracy:** 90%+ projects correctly identified
- **Suggestion Click-Through:** 50%+ users execute suggested commands
- **Time Savings:** Average 10+ seconds saved per detected project
- **User Satisfaction:** 85%+ find suggestions helpful (survey)

**AI Command Assistant (Phase 3):**
- **Adoption:** 40%+ of Convo Pro users try AI assistant
- **Success Rate:** 70%+ natural language queries result in correct command
- **Usage Frequency:** Average 5+ AI suggestions per week for active users
- **Conversion to Execution:** 60%+ AI suggestions actually executed
- **User Satisfaction:** 4.2+ star rating for AI feature

**Error Explanation (Phase 3):**
- **Pattern Match Rate:** 60%+ errors matched to common patterns (instant fix)
- **AI Analysis Rate:** 40% errors require AI analysis
- **Fix Success Rate:** 75%+ users successfully resolve error with suggestions
- **Educational Value:** 80%+ users report learning from explanations (survey)
- **Time Savings:** Average 2+ minutes saved vs googling error

**Git Visual Integration (Phase 3):**
- **Adoption:** 50%+ users with Git repos use visual features
- **Usage Frequency:** Average 3+ visual Git actions per session
- **PR Creation:** 20%+ users create at least one PR from Convo
- **Time Savings:** Average 30+ seconds saved per Git operation
- **User Preference:** 70%+ prefer visual Git vs command-line

**Team Workflows (Phase 3 - Enterprise):**
- **Enterprise Customers:** 5+ companies (Month 12), 20+ (Month 24)
- **Team Size:** Average 15+ users per enterprise team
- **Workflow Adoption:** 80%+ team members use shared workflows
- **Time Savings:** Average 5+ hours per week saved per team
- **ROI:** 10x return on investment (time saved vs subscription cost)
- **Retention:** <3% monthly enterprise churn

---

### Revenue Metrics

**Phase 1 (Months 1-3):**
- **MRR (Monthly Recurring Revenue):** $0 (free only)
- **Total Revenue:** $0
- **Focus:** Build user base and gather feedback

**Phase 2 (Months 4-6):**
- **Cloud Sync Subscribers:** 500-1,000 users
- **Convo Pro Subscribers:** 50-100 users
- **MRR:** $2,000-3,000
- **Conversion Rate:** 10-15% of active users → Cloud Sync
- **ARPU (Average Revenue Per User):** $5.00
- **LTV (Lifetime Value):** $180 (30-month retention)
- **CAC (Customer Acquisition Cost):** <$10 (organic growth)

**Phase 3 (Months 7-12):**
- **Cloud Sync Subscribers:** 3,000-5,000 users
- **Convo Pro Subscribers:** 500-1,000 users
- **Enterprise Customers:** 5-10 companies (~75-150 seats)
- **MRR:** $10,000-15,000
- **Conversion Rate:** 20-25% of active users → paid plans
- **ARPU:** $7.50 (mix of tiers)
- **LTV:** $270 (36-month retention)
- **Enterprise ARPU:** $750/month (15 seats × $50)

**Year 2 Projections:**
- **Total Users:** 100,000+
- **Paying Customers:** 20,000+ (20% conversion)
- **MRR:** $30,000-50,000
- **ARR (Annual Recurring Revenue):** $360,000-600,000
- **Enterprise:** 20+ companies (300+ seats)

**Revenue Breakdown (Month 12):**
| Tier | Users | Price | MRR |
|------|-------|-------|-----|
| Cloud Sync | 4,000 | $5 | $20,000 |
| Pro | 800 | $10 | $8,000 |
| Enterprise | 10 teams × 15 | $50/seat | $7,500 |
| **Total** | **~5,000 paying** | **-** | **$35,500** |

---

### Community & Growth Metrics

**Open Source Engagement:**
- **GitHub Stars:** 500+ (Month 6), 2,000+ (Month 12)
- **Contributors:** 10+ regular contributors
- **Pull Requests:** 20+ merged per month
- **Issues:** <20 open bugs at any time
- **Documentation:** 95% features documented

**Community Growth:**
- **Discord/Matrix Members:** 1,000+ (Month 12)
- **Reddit Community:** 500+ subscribers
- **Monthly Blog Posts:** 4+ (feature announcements, tutorials, use cases)
- **YouTube Tutorials:** 10+ community-created videos
- **Press Coverage:** 5+ tech blog mentions (Android Police, XDA, etc.)

**Developer Advocacy:**
- **Conference Talks:** 2+ presentations at Android/DevOps conferences
- **Podcast Appearances:** 3+ developer podcasts
- **Case Studies:** 5+ detailed user success stories
- **Integration Partnerships:** 3+ tool integrations (Claude Code plugin, etc.)

---

### Quality Metrics

**Code Quality:**
- **Test Coverage:** 80%+ overall, 95%+ for core features
- **Code Review:** 100% of PRs reviewed by 2+ developers
- **Static Analysis:** Zero critical issues (SonarQube/Detekt)
- **Security:** Zero known vulnerabilities
- **Accessibility:** WCAG AA compliance (TalkBack support)

**User Experience:**
- **Onboarding Completion:** 80%+ users complete tutorial
- **Feature Discovery:** 70%+ users discover 5+ features in first week
- **Support Tickets:** <10 per week, 90% resolved within 24 hours
- **User Satisfaction (CSAT):** 85%+ satisfied or very satisfied
- **Net Promoter Score (NPS):** 40+ (good), 60+ (excellent)

**Operational:**
- **Uptime (Cloud Sync):** 99.9% (< 45min downtime/month)
- **API Response Time:** P95 <200ms, P99 <500ms
- **Database Performance:** All queries <100ms
- **Backup Success:** 100% daily backups complete
- **Incident Response:** <1 hour for P0, <4 hours for P1

---

### Success Criteria by Phase

**Phase 1 MVP - Success Definition:**
✅ 1,000+ F-Droid downloads
✅ 4.0+ star rating
✅ 40% 7-day retention
✅ 60fps UI performance
✅ <0.1% crash rate
✅ Core features working (blocks, gestures, palette)

**Phase 2 Enhancement - Success Definition:**
✅ 10,000+ active users
✅ $2,000+ MRR
✅ 20% Cloud Sync conversion
✅ 80% session sync success rate
✅ 4.3+ star rating
✅ Background execution working reliably

**Phase 3 Advanced - Success Definition:**
✅ 50,000+ active users
✅ $10,000+ MRR
✅ 5+ enterprise customers
✅ AI features actively used (40%+ adoption)
✅ 75% error fix success rate
✅ 4.5+ star rating

---

### Monitoring & Tracking

**Tools:**
- **Analytics:** Mixpanel or Amplitude (user behavior)
- **Crash Reporting:** Firebase Crashlytics
- **Performance:** Firebase Performance Monitoring
- **Backend:** Prometheus + Grafana
- **User Feedback:** In-app surveys, App Store reviews
- **Revenue:** Stripe Dashboard, custom analytics

**Review Cadence:**
- **Daily:** Crash rate, active users, revenue
- **Weekly:** Feature adoption, retention cohorts
- **Monthly:** Full metrics review, goal adjustments
- **Quarterly:** Strategic review, roadmap updates

## Conclusion

Building a modern, conversational terminal for Android is not only technically feasible but addresses a real gap in the mobile developer experience. By combining Termux's powerful Linux environment with Warp-inspired UI innovations and Jetpack Compose's modern framework, we can create a terminal that feels native to 2025 while maintaining the power and flexibility developers need.

The project is:
- ✅ **Legally viable** (GPLv3, no licensing issues)
- ✅ **Technically achievable** (3-4 months solo, proven technologies)
- ✅ **Market ready** (clear differentiation, growing mobile dev community)
- ✅ **Open source** (can benefit entire Android ecosystem)

This isn't just a Termux clone with a new coat of paint—it's a reimagining of what a mobile terminal can be, making powerful command-line tools like Claude Code, git, npm, and thousands of others accessible to a broader audience through thoughtful UX design.

---

**Document Version:** 3.1
**Date:** January 2025 (Updated October 2025)
**Status:** Comprehensive Project Specification, Feature Documentation & Implementation Guide

**Changelog:**
- **v3.1 (October 2025):** Branding & naming decisions finalized
  - Official product name: **ConvoCLI**
  - Domain portfolio strategy documented (convocli.com, .dev, .app)
  - Future sync feature domain reserved (convosync.dev)
  - Added complete branding rationale and naming journey
  - Documented product family vision and expansion strategy
  - Cost-effective TLD strategy (.dev over premium .sh/.ai)
- **v3.0 (October 2025):** Major expansion with complete feature set documentation
  - Added 35+ feature specifications organized in 9 categories
  - Created detailed Feature Prioritization Matrix (MVP/Phase 2/Phase 3/Future)
  - Expanded Development Roadmap with week-by-week implementation plan
  - Added 8 detailed User Experience Flows with visual mockups
  - Comprehensive Success Metrics for all features (100+ specific metrics)
  - Updated financial projections with detailed revenue breakdowns
  - Added UX design principles and key interaction patterns
  - Documented technical implementation details for major features
- **v2.0 (January 2025):** Added "Key Decisions & Rationale" section documenting all architectural and strategic decisions
- **v1.0 (January 2025):** Initial project specification

**Document Stats:**
- Total Features Documented: 35+
- Implementation Phases: 3 (+ Future)
- Total Estimated Development Time: 12 months (solo) / 5.5 months (team)
- Projected Month 12 Revenue: $35,500 MRR
- Target Users (Month 12): 50,000+ active users
