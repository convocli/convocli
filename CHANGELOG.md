# Changelog

All notable changes to the ConvoCLI project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

#### Feature 001: Android Project Foundation Setup (2025-10-20)

**Core Infrastructure:**
- Android project structure with Kotlin DSL build configuration
- Gradle 8.4 build system with performance optimizations (parallel builds, configuration cache)
- Jetpack Compose 1.9.3 (BOM 2025.10.00) for modern declarative UI
- Material Design 3 theming with dynamic color support (Android 12+)
- Hilt 2.48 dependency injection framework
- Room 2.6.0 database with KSP annotation processing (2x faster than kapt)
- DataStore for preferences storage

**UI Foundation:**
- Material 3 color schemes (light/dark modes)
- Typography definitions following Material Design 3
- ConvoCLITheme composable with dynamic theming
- MainActivity with Compose integration
- Placeholder "Hello ConvoCLI" greeting screen

**Data Layer:**
- Command entity (Room) with indexed fields for performance
- CommandDao with Flow-based reactive queries
- AppDatabase class with Room configuration
- DatabaseModule for Hilt dependency provision
- SettingsDataStore for user preferences

**Code Quality:**
- .editorconfig for IDE-agnostic formatting rules
- ktlint integration for automated code style enforcement
- ProGuard rules for R8 optimization in release builds
- Test infrastructure (JUnit, AndroidX Test, Compose UI Testing)
- Example unit tests (CommandDaoTest)
- Example instrumented tests (MainActivityTest)
- Test data builders (TestDataBuilders.kt)

**Documentation:**
- Comprehensive quickstart guide (features/001-android-project-setup/quickstart.md)
- Updated README.md with development setup instructions
- Build commands and verification checklists
- Troubleshooting guides for common issues

**Development Tools:**
- Git repository with Android-specific .gitignore
- Gradle wrapper configuration (Gradle 8.4)
- Code style configuration (120 char line length, 4-space indent)

**Acceptance Criteria Completed:**
- ✅ AC-1: Project structure syncs in Android Studio
- ✅ AC-2: Build completes successfully (./gradlew build)
- ✅ AC-3: Application launches with Material 3 theme
- ✅ AC-4: Hilt dependency injection configured
- ✅ AC-5: Compose integration working
- ✅ AC-6: Code style standards enforced (ktlint)
- ✅ AC-7: Room database compiles successfully

**Technical Stack:**
- Kotlin 1.9.20
- Android Gradle Plugin 8.2.0
- minSdk 26 (Android 8.0 Oreo)
- targetSdk 34 (Android 14)
- Compose BOM 2025.10.00
- Hilt 2.48
- Room 2.6.0
- KSP 1.9.20-1.0.14
- ktlint 11.6.1

**Files Created:** 24 total
- Configuration files: 6 (Gradle, properties, ProGuard)
- Kotlin source files: 16 (Application, Activity, entities, DAOs, theme, DI modules)
- Test files: 3 (unit and instrumented tests)
- Documentation files: 2 (quickstart.md, CHANGELOG.md)

---

## Release History

_No releases yet. Project is in active development._

---

## Legend

- **Added**: New features
- **Changed**: Changes to existing functionality
- **Deprecated**: Soon-to-be-removed features
- **Removed**: Removed features
- **Fixed**: Bug fixes
- **Security**: Security improvements
