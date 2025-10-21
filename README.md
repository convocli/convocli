# ConvoCLI

**A modern conversational terminal for Android with cross-device sync**

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Platform](https://img.shields.io/badge/platform-Android-green.svg)](https://www.android.com)

> Code anywhere, anytime - seamlessly continue your development sessions across desktop, mobile, and tablet.

---

## The Problem

You're working with Claude Code on your desktop, implementing a feature. You have to leave, but get an idea on the subway. You pull out your phone - but can't easily continue where you left off. The code state doesn't match, the conversation context is lost, and you're stuck.

**ConvoCLI solves this.**

---

## What is ConvoCLI?

ConvoCLI is a **modern terminal for Android** that lets developers code on-the-go with full context preservation. Built on Termux, enhanced with a conversational interface, and powered by cross-device sync.

### Key Features

🎯 **Conversational Interface**
- Command blocks UI (Warp 2.0-inspired)
- Chat-like terminal experience
- Mobile-optimized gestures

🔄 **Cross-Device Session Sync** (Killer Feature)
- Resume coding sessions across devices
- Automatic git + conversation sync
- Code and AI context stay in lockstep

💬 **AI-Powered**
- Works seamlessly with Claude Code
- Supports Gemini CLI, Qwen Code
- Conversational development workflow

🚀 **Full-Featured Terminal**
- Based on Termux (proven, battle-tested)
- 1000+ packages available
- Full Linux environment on Android

📱 **Mobile-First Design**
- Jetpack Compose UI
- Swipe gestures
- Touch-friendly controls
- Material Design 3

---

## How It Works

### Desktop → Mobile Workflow

```bash
# On Desktop (working with Claude Code)
$ claude-code
[Coding session with Claude...]

# Time to leave
$ convosync save "Implementing OAuth feature"
✓ Code committed and pushed
✓ Conversation synced to cloud
✓ Ready to resume on mobile

# On Mobile (subway ride)
$ convosync resume
✓ Pulling latest code...
✓ Restoring conversation...
✓ You're at the exact same state!

$ convocli
[Continue conversation with Claude exactly where you left off]
```

### The Magic: Atomic Sync

ConvoSync syncs **code + conversation + context** as one unit:
- Git commit hash linked to conversation
- Automatic branch tracking
- Safety checks prevent state mismatches
- Delta compression (96% size reduction)

---

## Architecture

```
ConvoCLI
├── Terminal Core (Termux fork)
│   ├── VT-100/ANSI emulation
│   ├── PTY interface
│   └── Linux package system
├── Modern UI (Jetpack Compose)
│   ├── Command blocks
│   ├── Swipe gestures
│   └── Material Design 3
├── ConvoSync Client
│   ├── Delta compression
│   ├── Git integration
│   └── E2E encryption
└── AI Integration
    ├── Claude Code support
    ├── Conversation tracking
    └── Context preservation
```

---

## Project Status

**Phase 1: MVP (Months 1-3)** - 🚧 In Progress
- [x] Project specification
- [x] Technical architecture
- [x] Branding and naming
- [x] **Feature 001**: Android project foundation setup (Compose, Hilt, Room)
- [ ] **Feature 002**: Termux fork and integration
- [ ] Conversational UI implementation
- [ ] Command blocks

**Phase 2: ConvoSync (Months 4-6)** - 📋 Planned
- [ ] Backend server
- [ ] Delta compression
- [ ] Git-aware sync
- [ ] Cross-device support

**Phase 3: Advanced (Months 7-12)** - 💡 Future
- [ ] AI error explanations
- [ ] Workflow automation
- [ ] Team collaboration
- [ ] Plugin system

---

## Documentation

- **[Complete Specification](docs/specification.md)** - Full project vision and technical details
- **[Roadmap](docs/specification.md#development-roadmap-detailed)** - Week-by-week implementation plan
- **[Architecture](docs/specification.md#technical-architecture)** - System design and decisions

---

## Technology Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose 1.9.3 (BOM 2025.10.00)
- **Terminal Base:** Termux (GPLv3)
- **Backend:** Firebase/Firestore (for sync)
- **Compression:** gzip with delta sync
- **Encryption:** AES-256 end-to-end

---

## Development Setup

ConvoCLI uses modern Android development tools and follows industry-standard practices.

### Quick Start

For detailed setup instructions, see **[features/001-android-project-setup/quickstart.md](features/001-android-project-setup/quickstart.md)**.

**Prerequisites:**
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- 8GB RAM minimum (16GB recommended)

**Setup Steps:**
```bash
# Clone and open project
git checkout feature-project-setup
# Open in Android Studio: File → Open → select convocli directory

# Build project
./gradlew build

# Run on device/emulator
./gradlew installDebug
```

### Common Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Check code style
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat

# Run all quality checks
./gradlew check
```

### Verification

After setup, verify installation:
- [ ] Project syncs successfully in Android Studio
- [ ] `./gradlew build` completes without errors
- [ ] App launches showing "Hello ConvoCLI"
- [ ] Material 3 theme applied (purple primary color)
- [ ] `./gradlew ktlintCheck` passes

See [quickstart.md](features/001-android-project-setup/quickstart.md) for troubleshooting and detailed verification steps.

---

## Why ConvoCLI?

### For Individual Developers
- ⚡ Capture ideas the moment they strike
- 🔄 Never lose context when switching devices
- 💬 Natural conversational workflow
- 📱 Code on any device, anywhere

### For Teams
- 🤝 Share debugging sessions
- 🔐 Secure, encrypted conversations
- 🏢 Self-hosting available
- 📊 Boost mobile productivity

---

## Pricing (Planned)

| Tier | Price | Features |
|------|-------|----------|
| **Free** | $0 | Full terminal, local use |
| **Cloud Sync** | $5/mo | Cross-device sync, 2 devices |
| **Pro** | $10/mo | Unlimited devices, 1 year retention |
| **Enterprise** | $50/user/mo | Team features, SSO, self-hosting |

**Or self-host for free!** - All code is open source.

---

## Contributing

ConvoCLI is open source and built by developers, for developers.

- **Issues:** [Report bugs or request features](https://github.com/convocli/convocli/issues)
- **Discussions:** [Join the conversation](https://github.com/convocli/convocli/discussions)
- **Pull Requests:** Contributions welcome!

See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines (coming soon).

---

## Related Projects

- **[ConvoSync](https://github.com/convocli/convosync)** - Backend server for cross-device sync
- **[Docs](https://github.com/convocli/docs)** - Documentation and guides

---

## License

This project is licensed under **GPLv3** (required by Termux fork).

See [LICENSE](LICENSE) for details.

---

## Acknowledgments

Built on the shoulders of giants:
- **Termux** - Incredible Android terminal foundation
- **Warp** - Inspiration for conversational blocks
- **Claude Code** - Best AI coding assistant

---

## Creator

**Created and maintained by [Your Name](https://github.com/yourusername)**

ConvoCLI is a solo open-source project built to solve real mobile development workflows.

- Portfolio: [yourwebsite.com](https://yourwebsite.com)
- Twitter: [@yourhandle](https://twitter.com/yourhandle)

---

<div align="center">

**[⬆ Back to Top](#convocli)**

Made with ❤️ for developers who code everywhere

</div>
