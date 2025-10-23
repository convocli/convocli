# ConvoCLI Technology Stack

> **Purpose**: This document defines the approved technology stack for ConvoCLI. It prevents technology drift and ensures architectural consistency across all features.
>
> **Version**: 1.0.0
> **Created**: 2025-10-20
> **Last Updated**: 2025-10-22

---

## How to Use This Document

### For Developers

**BEFORE suggesting or using ANY library, framework, or pattern:**
1. Read this document
2. Verify the technology is APPROVED (✅) below
3. If PROHIBITED (❌), use the specified alternative
4. If UNAPPROVED, discuss justification with team

### For SpecSwarm Workflows

**Automatic Validation**: `/specswarm:plan` validates plan.md against this file

**Status Categories**:
- ✅ **APPROVED**: Auto-add to tech-stack.md (MINOR version bump)
- ⚠️ **CONFLICT**: Pause for user decision (overlaps existing technology)
- ❌ **PROHIBITED**: Block planning until removed

**Violation = Constitution Violation** (see `.specswarm/constitution.md` Principle 5)

---

## Core Technologies

### Language & Runtime
- ✅ **Kotlin** 1.9+
  - Official language for Android development
  - Coroutines for async operations
  - Modern, expressive, null-safe

- ✅ **Java** 11+ (compatibility only)
  - Required for Termux core integration
  - Legacy code only, no new Java code

### Build System
- ✅ **Gradle** 8.x with Kotlin DSL
  - Standard for Android projects
  - `build.gradle.kts` format only

- ✅ **Android Gradle Plugin (AGP)** 8.x
  - Latest stable version

### Minimum SDK
- ✅ **API 26 (Android 8.0 Oreo)** minimum
- ✅ **API 34+ (Android 14)** target

---

## UI Framework

### Declarative UI
- ✅ **Jetpack Compose** 1.9.3 (BOM 2025.10.00)
  - Modern declarative UI
  - Bill of Materials for version consistency
  - `compose-ui`, `compose-material3`, `ui-tooling-preview`

- ❌ **XML Layouts** → Use: Jetpack Compose
  - Reason: Project uses 100% Compose for consistency

- ❌ **React Native** → Use: Jetpack Compose
  - Reason: Direct Kotlin integration, zero bridge overhead

- ❌ **Flutter** → Use: Jetpack Compose
  - Reason: Native Android patterns, better Termux integration

### Design System
- ✅ **Material Design 3** (latest)
  - Modern design language
  - Dynamic theming support
  - Built-in dark mode

- ❌ **Custom UI frameworks** → Use: Material 3
  - Reason: Consistency with Android ecosystem

### Navigation
- ✅ **Compose Navigation** (latest compatible with Compose BOM)
  - Type-safe navigation
  - Deep linking support

---

## Architecture Components

### Dependency Injection
- ✅ **Hilt** 2.48+
  - Google's recommended DI framework
  - Kotlin-optimized
  - ViewModel integration

- ❌ **Dagger 2** → Use: Hilt
  - Reason: Hilt is Android-optimized Dagger wrapper

- ❌ **Koin** → Use: Hilt
  - Reason: Compile-time safety over runtime reflection

- ❌ **Manual injection** → Use: Hilt
  - Reason: Testability and scalability

### State Management
- ✅ **StateFlow / SharedFlow** (Kotlin Coroutines)
  - Reactive state management
  - Lifecycle-aware
  - Part of Kotlin stdlib

- ✅ **ViewModel** (AndroidX)
  - Lifecycle-aware state containers
  - MVI pattern implementation

- ❌ **LiveData** → Use: StateFlow
  - Reason: Modern coroutine-based alternative

- ❌ **RxJava** → Use: Kotlin Flow
  - Reason: Native Kotlin support, simpler API

- ❌ **Redux** → Use: MVI with StateFlow
  - Reason: Kotlin-native patterns, no JS dependencies

### Async Operations
- ✅ **Kotlin Coroutines** (latest stable)
  - Structured concurrency
  - Built-in Kotlin support
  - `kotlinx-coroutines-android`

- ❌ **RxJava/RxKotlin** → Use: Kotlin Coroutines
  - Reason: Native Kotlin async primitives

---

## Data Layer

### Local Database
- ✅ **Room** 2.6+
  - Type-safe SQL abstraction
  - Coroutines/Flow support
  - KSP annotation processing

- ❌ **SQLite directly** → Use: Room
  - Reason: Type safety and compile-time verification

- ❌ **Realm** → Use: Room
  - Reason: Official Android recommendation

### Data Persistence
- ✅ **DataStore** (Preferences & Proto)
  - Replaces SharedPreferences
  - Type-safe
  - Coroutines support

- ❌ **SharedPreferences** → Use: DataStore
  - Reason: Modern, type-safe alternative

- ✅ **EncryptedSharedPreferences** (for sensitive data only)
  - Encrypted key-value storage
  - Security library from AndroidX

### Serialization
- ✅ **Kotlinx Serialization** (latest)
  - Kotlin-native JSON parsing
  - Compile-time safety
  - Multiplatform support

- ❌ **Gson** → Use: Kotlinx Serialization
  - Reason: Kotlin-native, better performance

- ❌ **Moshi** → Use: Kotlinx Serialization
  - Reason: Kotlin-native solution preferred

- ❌ **Jackson** → Use: Kotlinx Serialization
  - Reason: Lighter weight, Kotlin-optimized

---

## Termux Integration

### Terminal Backend
- ✅ **Termux Fork** (GPLv3, custom fork)
  - Battle-tested terminal emulator
  - VT-100/ANSI support
  - Linux package system (apt/dpkg)
  - **Important**: Custom modifications tracked separately

- ❌ **Other terminal emulators** → Use: Termux fork
  - Reason: Proven package ecosystem, GPLv3 compatible

### PTY Interface
- ✅ **JNI to native PTY** (from Termux)
  - Low-level terminal control
  - Standard POSIX PTY interface

---

## Network & Sync (Future - ConvoSync Phase)

### HTTP Client
- ✅ **Ktor Client** (future use)
  - Kotlin-native
  - Coroutines support
  - Multiplatform

- ❌ **Retrofit** → Use: Ktor Client
  - Reason: Kotlin-native, modern API

- ❌ **OkHttp directly** → Use: Ktor Client
  - Reason: Higher-level abstraction

- ❌ **Axios** → Use: Ktor Client
  - Reason: JS library, not applicable to Kotlin

### WebSocket (Future)
- ✅ **Ktor WebSocket** (future use)
  - Real-time sync
  - Kotlin-native

---

## Code Quality & Testing

### Testing Framework
- ✅ **JUnit 4 / JUnit 5**
  - Standard testing framework

- ✅ **Kotlin Test**
  - Kotlin-native assertions

- ✅ **Turbine** (for Flow testing)
  - Flow testing utility

### UI Testing
- ✅ **Compose UI Test**
  - Official Compose testing
  - Semantic tree matching

- ✅ **Espresso** (if needed for interop)
  - View-based UI testing

### Mocking
- ✅ **Fake implementations** (preferred)
  - Custom test doubles
  - More maintainable than mocks

- ✅ **MockK** (when fakes impractical)
  - Kotlin-friendly mocking

- ❌ **Mockito** → Use: MockK or fakes
  - Reason: Better Kotlin support in MockK

### Code Style
- ✅ **ktlint** (official Android ruleset)
  - Automated Kotlin formatting
  - CI/CD integration

- ❌ **Custom formatters** → Use: ktlint
  - Reason: Standard Android conventions

### Static Analysis
- ✅ **Detekt** (optional, recommended)
  - Kotlin code smell detection
  - Complexity analysis

### Memory Leak Detection
- ✅ **LeakCanary** (debug builds only)
  - Automatic leak detection
  - Android-specific

---

## Utilities

### Logging
- ✅ **Timber** (when added)
  - Android logging utility
  - Tree-based logging

- ❌ **Log.d/Log.e directly** → Use: Timber (when added)
  - Reason: Better debugging control

### Image Loading (Future)
- ✅ **Coil** (when needed)
  - Kotlin-native
  - Compose support
  - Lightweight

- ❌ **Glide** → Use: Coil
  - Reason: Kotlin-first, Compose integration

- ❌ **Picasso** → Use: Coil
  - Reason: Modern alternative with Compose support

### Date/Time
- ✅ **Kotlin stdlib** (java.time on API 26+)
  - Native date/time API
  - No external dependency

- ❌ **Joda-Time** → Use: java.time
  - Reason: Native API available on API 26+

---

## Prohibited Patterns & Libraries

### Anti-Patterns
- ❌ **God Objects** → Use: Single Responsibility Principle
  - Reason: Maintainability

- ❌ **Business logic in Composables** → Use: ViewModels
  - Reason: Testability and separation of concerns

- ❌ **Hardcoded colors** → Use: MaterialTheme.colorScheme
  - Reason: Dark mode support, theming

- ❌ **`!!` null assertions** (without justification) → Use: Safe calls (`?.`)
  - Reason: Avoid crashes

- ❌ **Blocking main thread** → Use: Coroutines / background threads
  - Reason: ANR prevention

### Prohibited Libraries
- ❌ **Anko** → Use: Jetpack Compose
  - Reason: Deprecated, replaced by Compose

- ❌ **ButterKnife** → Use: View binding (or Compose)
  - Reason: Deprecated

- ❌ **EventBus** → Use: SharedFlow
  - Reason: Type-safe alternatives exist

- ❌ **Firebase ML Kit** → Use: TensorFlow Lite (future, if needed)
  - Reason: Privacy, self-hosting capability

---

## Dependency Management

### Version Management
- ✅ **BOM (Bill of Materials)** for Compose
  - Ensures version compatibility
  - Single version source

- ✅ **Gradle Version Catalogs** (recommended for large projects)
  - Centralized dependency versions

### Update Policy
- **Stable releases only** for core dependencies
- **Security patches** applied immediately
- **Major updates** evaluated quarterly
- **Breaking changes** require constitutional amendment discussion

---

## Version History

### Version 1.0.0 (2025-10-20)
**Created**: Initial tech stack definition for ConvoCLI
**Context**: Feature 001 - Android Project Foundation Setup
**Changes**:
- Established core technologies (Kotlin, Compose, Hilt, Room)
- Defined prohibited alternatives
- Set architectural patterns (MVI, StateFlow)

---

## Amendment Process

### Adding New Technology

**Automatic Addition** (via `/specswarm:plan`):
- Non-conflicting utilities/libraries
- Bumps MINOR version (1.0.0 → 1.1.0)

**Manual Addition** (requires discussion):
- Core framework changes
- Architectural pattern changes
- Bumps MAJOR version (1.0.0 → 2.0.0)

### Removing Prohibition

**Process**:
1. Document compelling business justification in feature's `research.md`
2. Update `.specswarm/constitution.md` if needed
3. Remove from Prohibited section
4. Add to Approved section with justification comment
5. Bump MAJOR version (breaking constitutional change)

### Replacing Approved Technology

**When**:
- Security vulnerability with no patch
- Deprecation by vendor
- Significantly better alternative emerges

**Process**:
1. Document in feature's `research.md`
2. Add migration plan
3. Update this file
4. Bump MAJOR version
5. Create refactoring tasks

---

## References

- [Constitution](./constitution.md) - Coding standards and principles
- [CLAUDE.md](/CLAUDE.md) - Development guide
- [Feature 001 Spec](/features/001-android-project-setup/spec.md) - Foundation setup requirements

---

*This document is the single source of truth for technology decisions. When in doubt, refer here first.*
