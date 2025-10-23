# Quickstart Guide: Android Project Foundation

> **Feature**: 001-android-project-setup
> **Created**: 2025-10-20
> **For**: ConvoCLI Development Team

---

## Getting Started

This guide will help you set up the ConvoCLI Android project for development.

### Prerequisites

**Required Software:**
- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17 (recommended for Gradle 8.x)
- **Git**: For version control operations

**Recommended:**
- Physical Android device or emulator (API 26+)
- 8GB RAM minimum, 16GB recommended
- 10GB free disk space

---

## Setup Instructions

### 1. Clone and Open Project

```bash
# Clone the repository
git checkout feature-project-setup

# Open in Android Studio
# File → Open → select convocli directory
```

### 2. Gradle Sync

Android Studio will automatically:
- Download Gradle 8.4
- Sync dependencies from `build.gradle.kts`
- Generate Hilt and Room code via KSP

**Expected Time**: 2-5 minutes on first sync

**If Sync Fails:**
- Verify internet connection
- Check JDK 17 is configured: File → Settings → Build → Build Tools → Gradle
- Invalidate caches: File → Invalidate Caches / Restart

### 3. Build the Project

```bash
# From Android Studio Terminal
./gradlew build

# Or use Android Studio UI
Build → Make Project (Ctrl+F9)
```

**Expected Result:**
- Build completes in < 2 minutes
- Zero compiler warnings
- APK generated in `app/build/outputs/apk/debug/`

### 4. Run the App

**On Physical Device:**
1. Enable Developer Options and USB Debugging
2. Connect device via USB
3. Click Run (▶️) in Android Studio
4. Select your device

**On Emulator:**
1. Tools → Device Manager
2. Create Virtual Device (API 26+ recommended)
3. Click Run (▶️) in Android Studio
4. Select emulator

**Expected Result:**
- App launches successfully
- "Hello ConvoCLI" text displays
- Material 3 theme applied (purple primary color)
- No crashes or errors in Logcat

---

## Project Structure

```
app/src/main/kotlin/com/convocli/
├── ui/                          # Compose UI components
│   ├── screens/                 # Top-level screens
│   ├── components/              # Reusable components
│   └── theme/                   # Material 3 theming
│       ├── Color.kt            # Color schemes
│       ├── Theme.kt            # ConvoCLITheme
│       └── Type.kt             # Typography
│
├── viewmodels/                  # ViewModels (MVI pattern)
├── repository/                  # Data layer
├── data/                        # Data models & storage
│   ├── model/                  # Data classes
│   │   └── Command.kt          # Room entity
│   ├── db/                     # Room database
│   │   ├── AppDatabase.kt      # Database class
│   │   └── CommandDao.kt       # DAO interface
│   └── datastore/              # DataStore
│       └── SettingsDataStore.kt
│
├── terminal/                    # Termux integration (future)
├── di/                         # Hilt modules
│   ├── AppModule.kt            # Application dependencies
│   └── DatabaseModule.kt       # Database provision
│
├── ConvoCLIApplication.kt      # Application class
└── MainActivity.kt             # Main activity
```

---

## Common Development Commands

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build (with R8 shrinking)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug

# Uninstall from device
./gradlew uninstallDebug
```

### Testing

```bash
# Run unit tests
./gradlew test

# Run unit tests with coverage
./gradlew testDebugUnitTest jacocoTestReport

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests com.convocli.data.db.CommandDaoTest
```

### Code Quality

```bash
# Check Kotlin code style
./gradlew ktlintCheck

# Auto-format Kotlin code
./gradlew ktlintFormat

# Run lint checks
./gradlew lint

# Run all quality checks
./gradlew check
```

### Cleanup

```bash
# Clean build artifacts
./gradlew clean

# View dependencies
./gradlew dependencies

# View available tasks
./gradlew tasks
```

---

## Verifying Installation

Run through these acceptance criteria to verify setup:

### ✅ AC-1: Project Structure
- [ ] Project syncs successfully in Android Studio
- [ ] No "missing dependency" warnings
- [ ] Package structure is `com.convocli`
- [ ] All Gradle files recognized

### ✅ AC-2: Build Success
- [ ] `./gradlew build` completes without errors
- [ ] Zero compiler warnings
- [ ] APK generated in `app/build/outputs/apk/`
- [ ] APK size < 25MB

### ✅ AC-3: Application Launch
- [ ] App installs without errors
- [ ] App launches without crashes
- [ ] Material 3 theme applied (purple primary color)
- [ ] "Hello ConvoCLI" text displays correctly
- [ ] Dark mode works (Settings → Display → Dark theme)

### ✅ AC-4: Dependency Injection
- [ ] Hilt generates code (check `app/build/generated/`)
- [ ] No DI runtime errors in Logcat
- [ ] Application class initializes

### ✅ AC-5: Compose Integration
- [ ] Compose preview works in Android Studio
- [ ] Theme colors apply correctly
- [ ] No Compose runtime errors in Logcat

### ✅ AC-6: Standards Compliance
- [ ] `./gradlew ktlintCheck` passes (0 violations)
- [ ] Code follows constitution.md patterns

### ✅ AC-7: Room Database
- [ ] Room generates DAO implementations
- [ ] Database compiles successfully
- [ ] CommandDaoTest passes

---

## Troubleshooting

### Gradle Sync Fails

**Problem**: "Failed to sync Gradle"

**Solutions:**
1. Check internet connection (dependencies downloaded from Maven)
2. Verify JDK 17: File → Project Structure → SDK Location
3. Clear Gradle cache:
   ```bash
   rm -rf ~/.gradle/caches
   ./gradlew clean
   ```
4. Invalidate Android Studio caches: File → Invalidate Caches

### Build Fails with "Out of Memory"

**Problem**: "Out of heap space" during build

**Solution**: Increase Gradle memory in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4096m
```

### App Crashes on Launch

**Problem**: App crashes immediately after launch

**Solutions:**
1. Check Logcat for stack trace
2. Verify Hilt annotation processor ran:
   ```bash
   ./gradlew clean build
   ```
3. Check AndroidManifest.xml has `.ConvoCLIApplication` as `android:name`

### Compose Preview Not Working

**Problem**: "Failed to instantiate one or more classes"

**Solutions:**
1. Rebuild project: Build → Rebuild Project
2. Sync Gradle files
3. Restart Android Studio
4. Check preview has `@Preview` annotation

### ktlint Violations

**Problem**: Code style violations blocking build

**Solution**: Auto-fix with:
```bash
./gradlew ktlintFormat
```

---

## Next Steps

After completing setup:

1. **Explore the codebase**: Review CLAUDE.md for architecture details
2. **Read constitution**: Familiarize yourself with coding standards
3. **Run tests**: Verify test infrastructure works
4. **Start Feature 002**: Termux integration (coming next)

---

## Additional Resources

**Project Documentation:**
- [CLAUDE.md](/CLAUDE.md) - Complete development guide
- [Constitution](/.specswarm/constitution.md) - Coding standards
- [Tech Stack](/.specswarm/tech-stack.md) - Approved technologies
- [Feature Spec](./spec.md) - Feature requirements

**External Documentation:**
- [Jetpack Compose Docs](https://developer.android.com/jetpack/compose)
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Room Documentation](https://developer.android.com/training/data-storage/room)
- [Material Design 3](https://m3.material.io/)

---

## Support

**Issues?**
1. Check troubleshooting section above
2. Review error messages in Logcat
3. Consult CLAUDE.md for detailed guidance
4. Open GitHub issue if problem persists

---

*Last updated: 2025-10-20*
