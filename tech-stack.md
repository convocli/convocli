# ConvoCLI Tech Stack

**Last Updated**: 2025-10-22
**Status**: Established (based on project CLAUDE.md)

---

## Overview

ConvoCLI uses a modern Android development stack centered on Jetpack Compose with Kotlin, integrated with a forked Termux terminal emulator core.

---

## Approved Technologies

### Core Language & Build

**Kotlin** (1.9+)
- **Status**: ✅ Primary language
- **Usage**: All new code, UI, ViewModels, Repositories
- **Justification**: Modern, type-safe, coroutine support, Android first-class

**Java** (11+)
- **Status**: ✅ Legacy support only
- **Usage**: Termux core compatibility only
- **Justification**: Required for Termux fork integration

**Gradle** (8.x with Kotlin DSL)
- **Status**: ✅ Build system
- **Usage**: All build configuration
- **Justification**: Modern Android standard

**AGP** (Android Gradle Plugin 8.x)
- **Status**: ✅ Build tool
- **Usage**: Android compilation
- **Justification**: Required for Android builds

---

### UI Framework

**Jetpack Compose** (1.9.3, BOM 2025.10.00)
- **Status**: ✅ Primary UI framework
- **Usage**: All UI components, screens, layouts
- **Justification**: Modern declarative UI, Material 3 support, reactive
- **Patterns**: Stateless composables, remember for state, LazyColumn with keys

**Material 3**
- **Status**: ✅ Design system
- **Usage**: All UI components, theming, colors
- **Justification**: Modern Android design, dynamic theming
- **Patterns**: MaterialTheme, Surface, Card, TextField

**Compose Navigation**
- **Status**: ✅ Navigation library
- **Usage**: Multi-screen navigation
- **Justification**: Type-safe, Compose-native

**Compose Animation**
- **Status**: ✅ Animation library
- **Usage**: Smooth transitions, UI feedback
- **Justification**: Built-in Compose support

---

### Architecture Components

**ViewModel**
- **Status**: ✅ State management
- **Usage**: All screen-level state, business logic
- **Justification**: Lifecycle-aware, survives configuration changes
- **Patterns**: MVI pattern preferred, unidirectional data flow

**StateFlow / SharedFlow**
- **Status**: ✅ Reactive state
- **Usage**: ViewModel → UI communication
- **Justification**: Coroutine-native, lifecycle-aware collection
- **Patterns**: StateFlow for state, SharedFlow for events

**Coroutines**
- **Status**: ✅ Async operations
- **Usage**: All async code, I/O operations
- **Justification**: Structured concurrency, Android integration
- **Patterns**: viewModelScope, Dispatchers.IO for I/O

**Room**
- **Status**: ✅ Local database
- **Usage**: Command history, session persistence
- **Justification**: Type-safe SQL, coroutine support
- **Patterns**: DAO pattern, Flow queries

**Hilt**
- **Status**: ✅ Dependency injection
- **Usage**: All dependency wiring
- **Justification**: Compile-time safety, Android integration
- **Patterns**: @HiltViewModel, @Inject, modules per layer

**DataStore**
- **Status**: ✅ Settings persistence
- **Usage**: User preferences, app settings
- **Justification**: Type-safe, coroutine support, Preferences replacement

---

### Terminal Core

**Termux Fork** (GPLv3)
- **Status**: ✅ Terminal emulator
- **Usage**: Command execution, VT-100 emulation
- **Justification**: Proven Linux environment, 1000+ packages
- **Components**:
  - TerminalEmulator.java: VT-100/ANSI implementation
  - TerminalSession.java: Session management
  - PTY implementation: JNI to native C code

**apt/dpkg**
- **Status**: ✅ Package system
- **Usage**: Linux package installation
- **Justification**: Standard Debian package tools

---

### Testing (Planned)

**JUnit 4**
- **Status**: ⏳ Planned
- **Usage**: Unit tests
- **Justification**: Android standard

**Compose UI Test**
- **Status**: ⏳ Planned
- **Usage**: UI tests
- **Justification**: Compose-native testing

**Turbine**
- **Status**: ⏳ Planned
- **Usage**: Flow testing
- **Justification**: Clean Flow assertions

**MockK**
- **Status**: ⏳ Planned
- **Usage**: Mocking
- **Justification**: Kotlin-friendly mocking

---

### Future Technologies (ConvoSync)

**Firebase/Firestore** (or self-hosted alternative)
- **Status**: 🔮 Future consideration
- **Usage**: Backend for ConvoSync
- **Justification**: Real-time sync, scalable

**gzip Compression**
- **Status**: 🔮 Future consideration
- **Usage**: Delta sync for ConvoSync
- **Justification**: Bandwidth optimization

**AES-256 Encryption**
- **Status**: 🔮 Future consideration
- **Usage**: End-to-end encryption for ConvoSync
- **Justification**: Security requirement

---

## Prohibited Technologies

**❌ RxJava**
- **Reason**: Prefer Kotlin Coroutines and Flow for reactive programming
- **Alternative**: StateFlow, SharedFlow, Flow

**❌ LiveData** (new code)
- **Reason**: Prefer StateFlow for new code (lifecycle-aware, coroutine-native)
- **Alternative**: StateFlow
- **Note**: Existing LiveData can remain for now

**❌ XML Layouts**
- **Reason**: All UI must use Jetpack Compose
- **Alternative**: Composable functions

**❌ Dagger 2** (direct)
- **Reason**: Use Hilt instead (built on Dagger, better Android integration)
- **Alternative**: Hilt

**❌ AsyncTask**
- **Reason**: Deprecated by Android, use Coroutines
- **Alternative**: viewModelScope.launch with Dispatchers.IO

**❌ Context in ViewModel** (direct dependency)
- **Reason**: Architecture violation, hard to test
- **Alternative**: Inject system services directly via Hilt

---

## Architecture Patterns

### MVI (Model-View-Intent)
- **Status**: ✅ Preferred pattern
- **Usage**: ViewModels with unidirectional data flow
- **Structure**:
  - State: Single immutable state object (data class)
  - Events: User intents/actions
  - Side Effects: One-time events (navigation, toasts)

### Repository Pattern
- **Status**: ✅ Data layer
- **Usage**: Abstract data sources (database, terminal, network)
- **Justification**: Testability, separation of concerns

### Clean Architecture
- **Status**: ✅ Layered architecture
- **Layers**:
  - UI: Compose components, screens
  - ViewModel: State management, business logic
  - Repository: Data abstraction
  - Data: Database, terminal integration

---

## Performance Guidelines

### Compose Optimization
- Use `remember` and `derivedStateOf` for computed values
- Provide stable keys for LazyColumn items
- Avoid lambda allocations in composition
- Use `@Stable` and `@Immutable` annotations

### Coroutine Best Practices
- Use `Dispatchers.IO` for file/network I/O
- Use `Dispatchers.Default` for CPU-intensive work
- Use `viewModelScope` for ViewModel coroutines
- Always use `withContext` for blocking operations

### Memory Management
- Limit terminal output buffer to 10,000 lines
- Use pagination for large datasets
- Cancel coroutines on ViewModel clear

---

## Code Quality Tools

**ktlint**
- **Status**: ⏳ Planned
- **Usage**: Kotlin code style
- **Justification**: Consistent formatting

**Detekt**
- **Status**: ⏳ Planned
- **Usage**: Static analysis
- **Justification**: Code quality enforcement

---

## Notes

- This tech stack is established based on the project's current implementation
- All new features should use approved technologies
- Changes to tech stack require team discussion (for solo project: documented decision)
- Focus on Kotlin-first, Compose-native solutions
- Termux core remains in Java for compatibility
