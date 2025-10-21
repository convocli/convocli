# Research: Android Project Foundation Setup

> **Feature**: 001-android-project-setup
> **Created**: 2025-10-20
> **Status**: Complete

---

## Research Overview

This document captures technical research and decisions for establishing the Android project foundation with modern architecture components.

---

## 1. Gradle Configuration with Kotlin DSL

### Decision
Use Gradle 8.x with Kotlin DSL (`build.gradle.kts`) for all build files.

### Rationale
- **Type Safety**: Kotlin DSL provides compile-time checking for build scripts
- **IDE Support**: Better autocomplete and refactoring in Android Studio
- **Modern Standard**: Google's recommended approach for new projects (2024+)
- **Consistency**: Matches project's Kotlin-first approach

### Implementation Pattern

**Project-level build.gradle.kts**:
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
```

**App-level build.gradle.kts**:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

android {
    namespace = "com.convocli"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.convocli"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
```

### Alternatives Considered
- **Groovy DSL**: Rejected due to lack of type safety and IDE support
- **Version Catalogs**: Deferred to future when project grows (currently manageable)

### References
- [Gradle Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html)
- [Android Gradle Plugin Migration Guide](https://developer.android.com/build/migrate-to-kotlin-dsl)

---

## 2. Jetpack Compose BOM 2025.10.00

### Decision
Use Compose Bill of Materials (BOM) version 2025.10.00 for dependency version management.

### Rationale
- **Version Consistency**: BOM ensures all Compose libraries use compatible versions
- **Simplified Management**: Single version number to update
- **Reduced Conflicts**: Google-tested version combinations
- **Latest Stable**: August 2025 release includes Compose 1.9.3

### Implementation Pattern

```kotlin
dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose libraries (no version numbers needed)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Integration with Activities
    implementation("androidx.activity:activity-compose:1.8.0")
}
```

### BOM Version Mapping
- **BOM 2025.10.00** provides:
  - Compose UI: 1.9.3
  - Material 3: 1.5.0
  - Compose Runtime: 1.9.3
  - Compose Animation: 1.9.3

### Kotlin Compiler Extension Compatibility
- Compose 1.9.3 requires Kotlin Compiler Extension 1.5.4
- Kotlin version: 1.9.20+ recommended

### Alternatives Considered
- **Manual version management**: Rejected due to version conflict risks
- **Older BOM versions**: Rejected to use latest stable features

### References
- [Compose BOM Mapping](https://developer.android.com/jetpack/compose/bom/bom-mapping)
- [Compose Release Notes](https://developer.android.com/jetpack/androidx/releases/compose)

---

## 3. Hilt Dependency Injection Setup

### Decision
Use Hilt 2.48+ for dependency injection with Kotlin and Compose.

### Rationale
- **Android Optimized**: Built on top of Dagger, tailored for Android
- **Reduced Boilerplate**: Simpler setup than vanilla Dagger
- **ViewModel Integration**: Native support for injecting into ViewModels
- **Compile-Time Safety**: Errors caught during compilation

### Implementation Pattern

**Project build.gradle.kts**:
```kotlin
plugins {
    id("com.google.dagger.hilt.android") version "2.48" apply false
}
```

**App build.gradle.kts**:
```kotlin
plugins {
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")

    // Hilt + Compose Navigation
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
}
```

**Application Class**:
```kotlin
@HiltAndroidApp
class ConvoCLIApplication : Application()
```

**AndroidManifest.xml**:
```xml
<application
    android:name=".ConvoCLIApplication"
    ...>
```

**ViewModel Example**:
```kotlin
@HiltViewModel
class TerminalViewModel @Inject constructor(
    private val repository: TermuxRepository
) : ViewModel()
```

### KSP vs KAPT
- **Decision**: Use KSP (Kotlin Symbol Processing)
- **Rationale**: 2x faster than kapt, officially supported by Hilt 2.44+
- **Migration Path**: Hilt 2.48 fully supports KSP

### Alternatives Considered
- **Dagger 2**: Rejected due to boilerplate (Hilt is simpler)
- **Koin**: Rejected due to runtime reflection vs compile-time safety
- **Manual DI**: Rejected due to lack of scalability

### References
- [Hilt Documentation](https://developer.android.com/training/dependency-injection/hilt-android)
- [Hilt + Jetpack Compose](https://developer.android.com/jetpack/compose/libraries#hilt)

---

## 4. Room Database with KSP

### Decision
Use Room 2.6+ with KSP annotation processing for local database.

### Rationale
- **Type-Safe SQL**: Compile-time verification of queries
- **Coroutines Support**: Native suspend functions and Flow support
- **KSP Performance**: 2x faster than kapt for annotation processing
- **Repository Pattern**: Aligns with MVI architecture

### Implementation Pattern

**Dependencies**:
```kotlin
dependencies {
    val roomVersion = "2.6.0"

    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")
}
```

**Database Definition**:
```kotlin
@Database(
    entities = [Command::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
}
```

**Hilt Module**:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "convocli.db"
        ).build()
    }

    @Provides
    fun provideCommandDao(database: AppDatabase): CommandDao {
        return database.commandDao()
    }
}
```

### KSP Configuration
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}
```

### Alternatives Considered
- **SQLite directly**: Rejected due to lack of type safety
- **Realm**: Rejected in favor of official Android recommendation
- **KAPT**: Replaced with KSP for performance

### References
- [Room Documentation](https://developer.android.com/training/data-storage/room)
- [Room with KSP](https://developer.android.com/build/migrate-to-ksp)

---

## 5. Material Design 3 Theme Setup

### Decision
Implement Material 3 (Material You) theme with dynamic color support.

### Rationale
- **Modern Design**: Latest Material Design specification
- **Dynamic Theming**: Adapts to user's wallpaper (Android 12+)
- **Dark Mode**: Built-in light/dark theme support
- **Accessibility**: WCAG-compliant color contrasts
- **Compose Native**: Designed for Jetpack Compose

### Implementation Pattern

**Theme Definition**:
```kotlin
// Color.kt
val md_theme_light_primary = Color(0xFF6750A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
// ... other light theme colors

val md_theme_dark_primary = Color(0xFFD0BCFF)
val md_theme_dark_onPrimary = Color(0xFF381E72)
// ... other dark theme colors

// Theme.kt
@Composable
fun ConvoCLITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme(
            primary = md_theme_dark_primary,
            onPrimary = md_theme_dark_onPrimary,
            // ...
        )
        else -> lightColorScheme(
            primary = md_theme_light_primary,
            onPrimary = md_theme_light_onPrimary,
            // ...
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Typography**:
```kotlin
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    // ... other text styles
)
```

### Color Scheme Strategy
- **Dynamic Colors**: Primary for Android 12+ (user wallpaper-based)
- **Fallback Colors**: Custom palette for Android 11 and below
- **Never Hardcode**: Always use `MaterialTheme.colorScheme.*`

### Alternatives Considered
- **Material 2**: Rejected in favor of latest design system
- **Custom theme system**: Rejected due to accessibility concerns

### References
- [Material Design 3](https://m3.material.io/)
- [Material Theme Builder](https://material-foundation.github.io/material-theme-builder/)

---

## 6. ktlint Configuration

### Decision
Use ktlint 11.6.1 with official Android ruleset for code style enforcement.

### Rationale
- **Zero Configuration**: Works out-of-box with Android conventions
- **Automated Formatting**: `./gradlew ktlintFormat` auto-fixes issues
- **CI Integration**: Easy to integrate in SpecSwarm workflows
- **Constitution Alignment**: Enforces coding standards from constitution.md

### Implementation Pattern

**App build.gradle.kts**:
```kotlin
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
}

ktlint {
    android = true
    version = "0.50.0"
    verbose = true
    outputToConsole = true

    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}
```

**Gradle Tasks**:
```bash
# Check code style
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat
```

### Enforced Rules (from constitution.md)
- **Line Length**: 120 characters maximum
- **Indentation**: 4 spaces (no tabs)
- **Import Order**: Android → Third-party → Project → kotlin/java
- **Function Length**: Maximum 50 lines
- **Naming**: PascalCase classes, camelCase functions

### SpecSwarm Integration
- **Pre-merge validation**: `/specswarm:complete` runs ktlintCheck
- **Quality analysis**: `/specswarm:analyze-quality` includes style checks

### Alternatives Considered
- **Detekt only**: Added ktlint for formatting (Detekt for code smells)
- **Android Studio formatter**: Not CI-friendly, manual process
- **Custom style rules**: Rejected in favor of Android conventions

### References
- [ktlint Documentation](https://pinterest.github.io/ktlint/)
- [ktlint Gradle Plugin](https://github.com/JLLeitschuh/ktlint-gradle)

---

## 7. Project Structure & Package Organization

### Decision
Use feature-first package structure with clear layer separation.

### Rationale
- **Scalability**: Easy to locate code as project grows
- **Separation of Concerns**: Clear boundaries between layers
- **Testability**: Easy to mock dependencies
- **CLAUDE.md Alignment**: Matches documented architecture

### Package Structure

```
com.convocli/
├── ui/                          # Presentation Layer
│   ├── screens/                 # Full-screen composables
│   │   ├── CommandBlocksScreen.kt
│   │   ├── TraditionalTerminalScreen.kt
│   │   └── SettingsScreen.kt
│   ├── components/              # Reusable UI components
│   │   ├── CommandBlock.kt
│   │   ├── TerminalCanvas.kt
│   │   └── CommandInput.kt
│   └── theme/                   # Material 3 theming
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
├── viewmodels/                  # Presentation Logic
│   ├── TerminalViewModel.kt
│   ├── CommandBlockViewModel.kt
│   └── SettingsViewModel.kt
│
├── repository/                  # Data Layer
│   ├── TermuxRepository.kt
│   ├── CommandHistoryRepository.kt
│   └── SettingsRepository.kt
│
├── data/                        # Data Models & Storage
│   ├── model/
│   │   ├── CommandBlock.kt
│   │   ├── TerminalSession.kt
│   │   └── Settings.kt
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   └── CommandDao.kt
│   └── datastore/
│       └── SettingsDataStore.kt
│
├── terminal/                    # Termux Integration
│   ├── TermuxSessionClient.kt
│   ├── CommandBlockParser.kt
│   └── TerminalOutputProcessor.kt
│
├── di/                          # Dependency Injection
│   ├── AppModule.kt
│   ├── DatabaseModule.kt
│   └── RepositoryModule.kt
│
└── ConvoCLIApplication.kt       # Application entry point
```

### Alternatives Considered
- **Layer-first structure** (all ViewModels together): Rejected due to poor scalability
- **Feature modules** (multi-module): Deferred to Phase 2 (overkill for MVP)

### References
- [Android App Architecture Guide](https://developer.android.com/topic/architecture)
- [Guide to App Architecture](https://developer.android.com/jetpack/guide)

---

## 8. Minimum SDK & Target SDK

### Decision
- **Minimum SDK**: API 26 (Android 8.0 Oreo)
- **Target SDK**: API 34 (Android 14)

### Rationale

**Minimum SDK 26**:
- **98.8% market coverage** (as of 2025)
- **java.time API** available (no Joda-Time needed)
- **Notification channels** (better UX)
- **Background execution limits** (better battery)
- **Modern Android features** without complex backports

**Target SDK 34**:
- **Latest stable Android** (Android 14 released October 2023)
- **Play Store requirement** (new apps must target API 33+)
- **Security updates** and privacy features
- **Predictive back gesture** support

### Trade-offs
- **Lower minimum SDK** (API 21): Would increase compatibility but require more backports and testing
- **Higher minimum SDK** (API 28+): Would simplify codebase but lose ~5% users

### Implementation
```kotlin
android {
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
    }
}
```

### References
- [Android Platform Distribution](https://developer.android.com/about/dashboards)
- [Target API Level Requirements](https://support.google.com/googleplay/android-developer/answer/11926878)

---

## 9. Build Configuration Best Practices

### Decision
Implement performance-optimized Gradle configuration from day one.

### Rationale
- **Fast Builds**: Developers iterate faster
- **CI/CD Efficiency**: Shorter pipeline times
- **Battery Friendly**: Less compilation heat/drain

### Optimizations

**gradle.properties**:
```properties
# Kotlin
kotlin.code.style=official

# Gradle
org.gradle.jvmargs=-Xmx2048m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.configuration-cache=true

# Android
android.useAndroidX=true
android.enableJetifier=false
android.nonTransitiveRClass=true

# Compose
org.jetbrains.compose.experimental.uikit.enabled=false
```

**BuildConfig Disabled** (not needed):
```kotlin
android {
    buildFeatures {
        buildConfig = false  // Reduce APK size
    }
}
```

**R8 / ProGuard**:
```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### Performance Targets
- **Clean build**: < 2 minutes (SC-2 requirement)
- **Incremental build**: < 30 seconds (SC-2 requirement)

### References
- [Gradle Performance Guide](https://docs.gradle.org/current/userguide/performance.html)
- [Android Build Optimization](https://developer.android.com/studio/build/optimize-your-build)

---

## 10. Initial Empty Activity Implementation

### Decision
Create minimal MainActivity with Compose that displays a simple "Hello ConvoCLI" message.

### Rationale
- **Validation**: Proves build configuration works
- **Foundation**: Starting point for UI development
- **Testing**: Ensures Hilt, Compose, Material 3 integrate correctly

### Implementation

**MainActivity.kt**:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ConvoCLITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Text(
                        text = "Hello ConvoCLI",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
```

**AndroidManifest.xml**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".ConvoCLIApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@android:style/Theme.NoTitleBar">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

### Validation Criteria
- ✅ App launches without crashes
- ✅ Material 3 theme applied
- ✅ Text renders correctly
- ✅ Dark mode works (toggle system theme)
- ✅ Hilt injection works (@AndroidEntryPoint)

---

## Research Summary

### Key Technical Decisions

1. **Gradle with Kotlin DSL** - Type-safe, modern build configuration
2. **Compose BOM 2025.10.00** - Version consistency for Compose 1.9.3
3. **Hilt 2.48+** - Compile-time DI with Android optimization
4. **Room 2.6+ with KSP** - Type-safe database with fast annotation processing
5. **Material 3 Theme** - Modern design with dynamic color support
6. **ktlint 11.6.1** - Automated code style enforcement
7. **API 26 minimum** - 98.8% coverage with modern features
8. **Feature-first packages** - Scalable project structure

### No Unresolved Questions

All technical decisions have been researched and documented. Implementation can proceed with confidence.

### Risks Mitigated

1. **Version Conflicts**: BOM ensures Compose compatibility
2. **Build Performance**: KSP is 2x faster than kapt
3. **Code Quality**: ktlint integrated from start
4. **Architecture Drift**: Clear package structure and patterns

---

## References

### Official Documentation
- [Android Developers Guide](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Kotlin Language Reference](https://kotlinlang.org/docs/)

### Project Documentation
- [CLAUDE.md](/CLAUDE.md) - Development guide
- [Constitution](/.specswarm/constitution.md) - Coding standards
- [Tech Stack](/.specswarm/tech-stack.md) - Approved technologies
- [Feature Spec](./spec.md) - Feature requirements

---

*Research complete. Ready for implementation planning.*
