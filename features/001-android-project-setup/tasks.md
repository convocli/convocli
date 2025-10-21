# Implementation Tasks: Android Project Foundation Setup

> **Feature**: 001-android-project-setup
> **Created**: 2025-10-20
> **Status**: Ready for Implementation
> **Total Tasks**: 51
> **Estimated Effort**: 8-12 hours

<!-- Tech Stack Validation: PASSED -->
<!-- Validated against: /memory/tech-stack.md v1.0.0 -->
<!-- No prohibited technologies found -->
<!-- All technologies pre-approved -->

---

## Task Execution Guide

### How to Use This File

**Task Format**:
```
[T###] [Story] Task Description
├─ File: path/to/file.ext
├─ Dependencies: T### (tasks that must complete first)
├─ Validation: How to verify completion
└─ [P] Parallelizable (can run concurrently with other [P] tasks)
```

**Story Labels**:
- `[Setup]` - Project initialization (must complete first)
- `[Foundation]` - Core infrastructure (blocking prerequisites)
- `[AC-1]` - Project Structure validation
- `[AC-2]` - Build Success validation
- `[AC-3]` - Application Launch validation
- `[AC-4]` - Dependency Injection validation
- `[AC-5]` - Compose Integration validation
- `[AC-6]` - Standards Compliance validation
- `[AC-7]` - Room Database validation
- `[Polish]` - Final touches and documentation

### Execution Strategy

**MVP Scope** (Critical Path):
- Phase 1: Setup → Phase 2: Foundation → Phase 3: AC-1 & AC-2 → Phase 4: AC-3

**Full Feature Scope**:
- All phases (1-6) for complete acceptance criteria validation

**Parallel Execution**:
- Tasks marked `[P]` can run concurrently
- Within each phase, group [P] tasks together for efficiency
- See "Parallel Execution Examples" section below

---

## Phase 1: Project Setup (Foundation Layer)

**Goal**: Initialize Android project with Gradle configuration

**Prerequisites**: None (starting point)

**Duration**: 1-2 hours

**Validation**: Gradle sync succeeds without errors

---

### [T001] [Setup] Create root build.gradle.kts with plugin versions
```kotlin
File: build.gradle.kts
Dependencies: None
[P] Parallelizable with T002-T004
```

**Task**:
Create root-level `build.gradle.kts` with plugin declarations (no apply).

**Implementation**:
```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
```

**Validation**:
- File exists at project root
- Contains all 4 plugin declarations
- No syntax errors in Kotlin DSL

**Reference**: [research.md Section 1: Gradle Configuration](./research.md#1-gradle-configuration-with-kotlin-dsl)

---

### [T002] [Setup] Create gradle.properties with performance optimizations
```properties
File: gradle.properties
Dependencies: None
[P] Parallelizable with T001, T003-T004
```

**Task**:
Create `gradle.properties` with Gradle daemon, caching, and Kotlin optimizations.

**Implementation**:
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

**Validation**:
- File exists at project root
- All optimization flags present
- Configuration cache enabled

**Reference**: [research.md Section 9: Build Configuration](./research.md#9-build-configuration-best-practices)

---

### [T003] [Setup] Create settings.gradle.kts
```kotlin
File: settings.gradle.kts
Dependencies: None
[P] Parallelizable with T001-T002, T004
```

**Task**:
Create `settings.gradle.kts` declaring the app module.

**Implementation**:
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ConvoCLI"
include(":app")
```

**Validation**:
- File exists at project root
- Includes `:app` module
- Google and Maven Central repositories configured

---

### [T004] [Setup] Create .gitignore for Android project
```
File: .gitignore
Dependencies: None
[P] Parallelizable with T001-T003
```

**Task**:
Create comprehensive `.gitignore` for Android/Kotlin project.

**Implementation**:
```gitignore
# Gradle
.gradle/
build/
!gradle/wrapper/gradle-wrapper.jar

# Android
*.apk
*.aab
*.dex
*.class
local.properties
.idea/
*.iml
.DS_Store
captures/
.externalNativeBuild/
.cxx/
app/schemas/

# Kotlin
*.kotlin_module

# KSP
generated/

# Signing
keystore.properties
*.jks
*.keystore
```

**Validation**:
- File covers Gradle, Android, Kotlin, KSP artifacts
- Excludes signing keys

---

### [T005] [Setup] Create app module directory structure
```bash
File: app/src/main/kotlin/com/convocli/
Dependencies: None
[P] Parallelizable with T001-T004
```

**Task**:
Create complete directory hierarchy for app module source code.

**Implementation**:
```bash
mkdir -p app/src/main/kotlin/com/convocli/{ui/{screens,components,theme},viewmodels,repository,data/{model,db,datastore},terminal,di}
mkdir -p app/src/main/res/{values,drawable,xml}
mkdir -p app/src/test/kotlin/com/convocli
mkdir -p app/src/androidTest/kotlin/com/convocli
```

**Validation**:
- All directories exist
- Package structure matches `com.convocli`
- Test directories created

**Reference**: [research.md Section 7: Project Structure](./research.md#7-project-structure--package-organization)

---

### [T006] [Setup] Create app build.gradle.kts with all dependencies
```kotlin
File: app/build.gradle.kts
Dependencies: T001 (needs plugin versions)
```

**Task**:
Create app-level build file with Compose BOM, Hilt, Room, and all dependencies.

**Implementation**:
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = false
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2025.10.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Compose Integration
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Room
    val roomVersion = "2.6.0"
    implementation("androidx.room:room-runtime:\$roomVersion")
    implementation("androidx.room:room-ktx:\$roomVersion")
    ksp("androidx.room:room-compiler:\$roomVersion")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.9.20")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("app.cash.turbine:turbine:1.0.0")

    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

ktlint {
    android.set(true)
    version.set("0.50.0")
    verbose.set(true)
    outputToConsole.set(true)

    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}
```

**Validation**:
- File compiles without errors
- All dependencies declared
- Compose BOM 2025.10.00 specified
- KSP configured for Hilt and Room

**Reference**: [research.md Section 2-4](./research.md)

---

### [T007] [Setup] Create proguard-rules.pro for R8 optimization
```pro
File: app/proguard-rules.pro
Dependencies: None
[P] Parallelizable with T001-T006
```

**Task**:
Create ProGuard rules for release builds with R8 shrinking.

**Implementation**:
```pro
# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Compose
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Keep Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keep,includedescriptorclasses class com.convocli.**$$serializer { *; }
-keepclassmembers class com.convocli.** {
    *** Companion;
}
-keepclasseswithmembers class com.convocli.** {
    kotlinx.serialization.KSerializer serializer(...);
}
```

**Validation**:
- File exists in app/ directory
- Rules cover Hilt, Room, Compose, Serialization

---

**Phase 1 Checkpoint**: ✅ Gradle configuration complete, ready for sync

**Validation Command**:
```bash
./gradlew tasks
```

**Expected Result**: Gradle sync succeeds, tasks list shows available Gradle commands

---

## Phase 2: Core Infrastructure (Blocking Foundation)

**Goal**: Set up Application class, Hilt DI, Room database, DataStore

**Prerequisites**: Phase 1 complete (Gradle sync succeeds)

**Duration**: 2-3 hours

**Validation**: App launches with Hilt initialized, database created

---

### [T008] [Foundation] Create ConvoCLIApplication with @HiltAndroidApp
```kotlin
File: app/src/main/kotlin/com/convocli/ConvoCLIApplication.kt
Dependencies: T006 (needs Hilt dependency)
```

**Task**:
Create Application class annotated with `@HiltAndroidApp`.

**Implementation**:
```kotlin
package com.convocli

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * ConvoCLI Application class.
 *
 * Initializes Hilt dependency injection for the entire application.
 */
@HiltAndroidApp
class ConvoCLIApplication : Application()
```

**Validation**:
- Class extends Application
- Annotated with @HiltAndroidApp
- Hilt generates DaggerConvoCLIApplication_HiltComponents

**Reference**: [research.md Section 3: Hilt DI Setup](./research.md#3-hilt-dependency-injection-setup)

---

### [T009] [Foundation] Register Application class in AndroidManifest.xml
```xml
File: app/src/main/AndroidManifest.xml
Dependencies: T008 (needs Application class)
```

**Task**:
Create AndroidManifest.xml with Application class registration.

**Implementation**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".ConvoCLIApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.NoTitleBar">
    </application>

</manifest>
```

**Validation**:
- android:name points to .ConvoCLIApplication
- Standard Android attributes present

---

### [T010] [Foundation] [AC-7] Create Command entity with Room annotations
```kotlin
File: app/src/main/kotlin/com/convocli/data/model/Command.kt
Dependencies: T006 (needs Room dependency)
[P] Parallelizable with T011-T012
```

**Task**:
Create Room entity for Command as defined in data-model.md.

**Implementation**:
```kotlin
package com.convocli.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity representing a terminal command execution.
 *
 * Stores command history for replay, search, and UI display.
 */
@Entity(
    tableName = "commands",
    indices = [
        Index(value = ["executed_at"], name = "idx_executed_at"),
        Index(value = ["session_id"], name = "idx_session_id")
    ]
)
data class Command(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "command_text")
    val commandText: String,

    @ColumnInfo(name = "output")
    val output: String? = null,

    @ColumnInfo(name = "exit_code")
    val exitCode: Int? = null,

    @ColumnInfo(name = "executed_at")
    val executedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "working_directory")
    val workingDirectory: String,

    @ColumnInfo(name = "session_id")
    val sessionId: String? = null
)
```

**Validation**:
- Matches data-model.md schema
- Has @Entity annotation
- Indexes defined for performance

**Reference**: [data-model.md Section 1: Command Entity](./data-model.md#1-command-room-entity)

---

### [T011] [Foundation] [AC-7] Create CommandDao with Flow queries
```kotlin
File: app/src/main/kotlin/com/convocli/data/db/CommandDao.kt
Dependencies: T010 (needs Command entity)
[P] Parallelizable with T012
```

**Task**:
Create DAO interface with suspend functions and Flow queries.

**Implementation**:
```kotlin
package com.convocli.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.convocli.data.model.Command
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Command entity.
 *
 * Provides suspend functions for async database operations and Flow for reactive queries.
 */
@Dao
interface CommandDao {

    @Insert
    suspend fun insert(command: Command): Long

    @Update
    suspend fun update(command: Command)

    @Query("SELECT * FROM commands ORDER BY executed_at DESC")
    fun observeAll(): Flow<List<Command>>

    @Query("SELECT * FROM commands WHERE session_id = :sessionId ORDER BY executed_at DESC")
    fun observeBySession(sessionId: String): Flow<List<Command>>

    @Query("SELECT * FROM commands ORDER BY executed_at DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<Command>

    @Query("SELECT * FROM commands WHERE command_text LIKE '%' || :query || '%' ORDER BY executed_at DESC")
    suspend fun search(query: String): List<Command>

    @Query("DELETE FROM commands WHERE executed_at < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int

    @Query("DELETE FROM commands")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM commands")
    suspend fun getCount(): Int
}
```

**Validation**:
- All methods from data-model.md defined
- Flow used for observe methods
- Suspend used for one-shot operations

**Reference**: [data-model.md Section 2: CommandDao](./data-model.md#2-commanddao-room-dao)

---

### [T012] [Foundation] [AC-7] Create AppDatabase with Command entity
```kotlin
File: app/src/main/kotlin/com/convocli/data/db/AppDatabase.kt
Dependencies: T010, T011 (needs entity and DAO)
```

**Task**:
Create RoomDatabase class with Command entity.

**Implementation**:
```kotlin
package com.convocli.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.convocli.data.model.Command

/**
 * ConvoCLI Room database.
 *
 * Version 1: Initial schema with Command entity for terminal history.
 */
@Database(
    entities = [Command::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun commandDao(): CommandDao
}
```

**Validation**:
- @Database annotation with version 1
- Includes Command entity
- Provides commandDao() abstract method

**Reference**: [data-model.md Section 3: AppDatabase](./data-model.md#3-appdatabase-room-database)

---

### [T013] [Foundation] Create DatabaseModule for Hilt
```kotlin
File: app/src/main/kotlin/com/convocli/di/DatabaseModule.kt
Dependencies: T008, T012 (needs Hilt and AppDatabase)
[P] Parallelizable with T014-T015
```

**Task**:
Create Hilt module that provides Room database instance.

**Implementation**:
```kotlin
package com.convocli.di

import android.content.Context
import androidx.room.Room
import com.convocli.data.db.AppDatabase
import com.convocli.data.db.CommandDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for database dependencies.
 *
 * Provides singleton Room database instance and DAOs.
 */
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

**Validation**:
- @InstallIn(SingletonComponent::class)
- Database provided as @Singleton
- DAO provided from database

**Reference**: [research.md Section 3: Hilt DI](./research.md#3-hilt-dependency-injection-setup)

---

### [T014] [Foundation] Create SettingsDataStore with preference keys
```kotlin
File: app/src/main/kotlin/com/convocli/data/datastore/SettingsDataStore.kt
Dependencies: T006 (needs DataStore dependency)
[P] Parallelizable with T013, T015
```

**Task**:
Create DataStore wrapper for app settings.

**Implementation**:
```kotlin
package com.convocli.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/**
 * Settings DataStore for user preferences.
 *
 * Provides type-safe keys for all app settings.
 */
object SettingsKeys {
    val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    val DEFAULT_SHELL = stringPreferencesKey("default_shell")
    val FONT_SIZE = intPreferencesKey("font_size")
    val MAX_HISTORY_SIZE = intPreferencesKey("max_history_size")
}

/**
 * Extension property to access settings DataStore.
 */
val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
```

**Validation**:
- Preference keys defined for all settings
- Context extension for DataStore access

**Reference**: [data-model.md Section: Settings DataStore](./data-model.md#settings-datastore)

---

### [T015] [Foundation] Create AppModule for application-level dependencies
```kotlin
File: app/src/main/kotlin/com/convocli/di/AppModule.kt
Dependencies: T008 (needs Hilt)
[P] Parallelizable with T013-T014
```

**Task**:
Create Hilt module for application-level dependencies (currently minimal).

**Implementation**:
```kotlin
package com.convocli.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt module for application-level dependencies.
 *
 * Currently minimal; will expand with Termux integration, repositories, etc.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    // Reserved for future application-level dependencies
    // Examples: Termux service bindings, network clients, etc.
}
```

**Validation**:
- Module exists for future expansion
- @InstallIn(SingletonComponent::class)

---

**Phase 2 Checkpoint**: ✅ Core infrastructure in place (Hilt DI, Room, DataStore)

**Validation Command**:
```bash
./gradlew build
```

**Expected Result**:
- Hilt generates DI code successfully
- Room generates DAO implementations
- Build completes without errors

---

## Phase 3: Material 3 Theme & UI Foundation

**Goal**: Create Material 3 theme and MainActivity with Compose

**Prerequisites**: Phase 2 complete

**Duration**: 2-3 hours

**Validation**: App launches with Material 3 theme, "Hello ConvoCLI" displays

---

### [T016] [AC-5] Create Color.kt with Material 3 color schemes
```kotlin
File: app/src/main/kotlin/com/convocli/ui/theme/Color.kt
Dependencies: T006 (needs Compose dependency)
[P] Parallelizable with T017-T018
```

**Task**:
Define light and dark color schemes for Material 3 theme.

**Implementation**:
```kotlin
package com.convocli.ui.theme

import androidx.compose.ui.graphics.Color

// Light theme colors
val md_theme_light_primary = Color(0xFF6750A4)
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFEADDFF)
val md_theme_light_onPrimaryContainer = Color(0xFF21005D)
val md_theme_light_secondary = Color(0xFF625B71)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFE8DEF8)
val md_theme_light_onSecondaryContainer = Color(0xFF1D192B)
val md_theme_light_tertiary = Color(0xFF7D5260)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_tertiaryContainer = Color(0xFFFFD8E4)
val md_theme_light_onTertiaryContainer = Color(0xFF31111D)
val md_theme_light_error = Color(0xFFB3261E)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFF9DEDC)
val md_theme_light_onErrorContainer = Color(0xFF410E0B)
val md_theme_light_background = Color(0xFFFFFBFE)
val md_theme_light_onBackground = Color(0xFF1C1B1F)
val md_theme_light_surface = Color(0xFFFFFBFE)
val md_theme_light_onSurface = Color(0xFF1C1B1F)
val md_theme_light_surfaceVariant = Color(0xFFE7E0EC)
val md_theme_light_onSurfaceVariant = Color(0xFF49454F)
val md_theme_light_outline = Color(0xFF79747E)

// Dark theme colors
val md_theme_dark_primary = Color(0xFFD0BCFF)
val md_theme_dark_onPrimary = Color(0xFF381E72)
val md_theme_dark_primaryContainer = Color(0xFF4F378B)
val md_theme_dark_onPrimaryContainer = Color(0xFFEADDFF)
val md_theme_dark_secondary = Color(0xFFCCC2DC)
val md_theme_dark_onSecondary = Color(0xFF332D41)
val md_theme_dark_secondaryContainer = Color(0xFF4A4458)
val md_theme_dark_onSecondaryContainer = Color(0xFFE8DEF8)
val md_theme_dark_tertiary = Color(0xFFEFB8C8)
val md_theme_dark_onTertiary = Color(0xFF492532)
val md_theme_dark_tertiaryContainer = Color(0xFF633B48)
val md_theme_dark_onTertiaryContainer = Color(0xFFFFD8E4)
val md_theme_dark_error = Color(0xFFF2B8B5)
val md_theme_dark_onError = Color(0xFF601410)
val md_theme_dark_errorContainer = Color(0xFF8C1D18)
val md_theme_dark_onErrorContainer = Color(0xFFF9DEDC)
val md_theme_dark_background = Color(0xFF1C1B1F)
val md_theme_dark_onBackground = Color(0xFFE6E1E5)
val md_theme_dark_surface = Color(0xFF1C1B1F)
val md_theme_dark_onSurface = Color(0xFFE6E1E5)
val md_theme_dark_surfaceVariant = Color(0xFF49454F)
val md_theme_dark_onSurfaceVariant = Color(0xFFCAC4D0)
val md_theme_dark_outline = Color(0xFF938F99)
```

**Validation**:
- Light and dark color schemes defined
- All Material 3 color roles included
- No hardcoded color values elsewhere in codebase

**Reference**: [research.md Section 5: Material 3 Theme](./research.md#5-material-design-3-theme-setup)

---

### [T017] [AC-5] Create Type.kt with Material 3 typography
```kotlin
File: app/src/main/kotlin/com/convocli/ui/theme/Type.kt
Dependencies: T006 (needs Compose dependency)
[P] Parallelizable with T016, T018
```

**Task**:
Define Typography with Material 3 text styles.

**Implementation**:
```kotlin
package com.convocli.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Material 3 Typography definitions.
 *
 * Uses default font family with appropriate weights and sizes.
 */
val Typography = Typography(
    // Display styles
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = 0.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    // Headline styles
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    // Body styles
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    // Label styles
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

**Validation**:
- All Material 3 text styles defined
- Uses SP units for scalability
- Matches Material 3 specifications

---

### [T018] [AC-5] Create Theme.kt with ConvoCLITheme composable
```kotlin
File: app/src/main/kotlin/com/convocli/ui/theme/Theme.kt
Dependencies: T016, T017 (needs colors and typography)
```

**Task**:
Create theme composable with dynamic color support for Android 12+.

**Implementation**:
```kotlin
package com.convocli.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline
)

/**
 * ConvoCLI Material 3 theme.
 *
 * Supports dynamic color (Android 12+) and manual dark/light mode.
 *
 * @param darkTheme Whether to use dark theme (defaults to system setting)
 * @param dynamicColor Whether to use dynamic color from wallpaper (Android 12+)
 * @param content Composable content to theme
 */
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
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**Validation**:
- Dynamic color support for Android 12+
- Fallback to static color schemes
- isSystemInDarkTheme() default

**Reference**: [research.md Section 5: Material 3 Theme](./research.md#5-material-design-3-theme-setup)

---

### [T019] [AC-3] [AC-4] [AC-5] Create MainActivity with Hilt and Compose
```kotlin
File: app/src/main/kotlin/com/convocli/MainActivity.kt
Dependencies: T008, T018 (needs Hilt and theme)
```

**Task**:
Create ComponentActivity with @AndroidEntryPoint and Compose setup.

**Implementation**:
```kotlin
package com.convocli

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.convocli.ui.theme.ConvoCLITheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * ConvoCLI main activity.
 *
 * Entry point for the application. Displays initial "Hello ConvoCLI" screen.
 */
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
                    Greeting()
                }
            }
        }
    }
}

@Composable
fun Greeting() {
    Text(
        text = "Hello ConvoCLI",
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ConvoCLITheme {
        Greeting()
    }
}
```

**Validation**:
- Extends ComponentActivity
- Annotated with @AndroidEntryPoint
- Uses setContent with ConvoCLITheme
- Preview annotation present

**Reference**: [research.md Section 10: Initial Activity](./research.md#10-initial-empty-activity-implementation)

---

### [T020] [AC-3] Update AndroidManifest.xml with MainActivity
```xml
File: app/src/main/AndroidManifest.xml
Dependencies: T009, T019 (needs manifest and MainActivity)
```

**Task**:
Add MainActivity to manifest with MAIN/LAUNCHER intent filter.

**Implementation**:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:name=".ConvoCLIApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.NoTitleBar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@android:style/Theme.NoTitleBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

**Validation**:
- MainActivity declared
- MAIN/LAUNCHER intent filter present
- android:exported="true"

---

### [T021] [AC-3] Create app name string resource
```xml
File: app/src/main/res/values/strings.xml
Dependencies: None
[P] Parallelizable with T016-T020
```

**Task**:
Create strings.xml with app name.

**Implementation**:
```xml
<resources>
    <string name="app_name">ConvoCLI</string>
</resources>
```

**Validation**:
- app_name string defined

---

**Phase 3 Checkpoint**: ✅ UI foundation complete, app launches with Material 3 theme

**Validation Command**:
```bash
./gradlew installDebug
adb shell am start -n com.convocli/.MainActivity
```

**Expected Result**:
- App installs successfully
- Launches without crashes
- "Hello ConvoCLI" text displays in Material 3 primary color
- Dark mode toggle works (toggle system theme)

---

## Phase 4: Code Quality & Standards Enforcement

**Goal**: Configure ktlint, format all code, set up basic tests

**Prerequisites**: Phase 3 complete

**Duration**: 1-2 hours

**Validation**: ktlintCheck passes with 0 violations, tests run successfully

---

### [T022] [AC-6] Run ktlintFormat on all source files
```bash
File: All Kotlin files
Dependencies: T006 (ktlint plugin configured)
```

**Task**:
Auto-format all Kotlin code to match ktlint standards.

**Command**:
```bash
./gradlew ktlintFormat
```

**Validation**:
- Command completes successfully
- All files formatted
- Git diff shows formatting changes

**Reference**: [research.md Section 6: ktlint Configuration](./research.md#6-ktlint-configuration)

---

### [T023] [AC-6] Verify ktlintCheck passes
```bash
File: All Kotlin files
Dependencies: T022 (formatting applied)
```

**Task**:
Run ktlint check to ensure zero violations.

**Command**:
```bash
./gradlew ktlintCheck
```

**Validation**:
- Output shows "0 violations"
- No style errors reported
- Exit code 0

---

### [T024] [AC-6] Create .editorconfig for IDE consistency
```editorconfig
File: .editorconfig
Dependencies: None
[P] Parallelizable with T022-T023
```

**Task**:
Create EditorConfig for consistent formatting across IDEs.

**Implementation**:
```editorconfig
root = true

[*]
charset = utf-8
end_of_line = lf
insert_final_newline = true
trim_trailing_whitespace = true

[*.{kt,kts}]
indent_size = 4
indent_style = space
max_line_length = 120

[*.xml]
indent_size = 4
indent_style = space

[*.{json,yml,yaml}]
indent_size = 2
indent_style = space

[*.md]
trim_trailing_whitespace = false
```

**Validation**:
- File exists at project root
- Kotlin files use 4 spaces
- Line length set to 120

---

### [T025] [AC-4] [AC-7] Create example Repository test
```kotlin
File: app/src/test/kotlin/com/convocli/data/db/CommandDaoTest.kt
Dependencies: T011 (CommandDao)
[P] Parallelizable with T026-T027
```

**Task**:
Create basic Room DAO test to validate test infrastructure.

**Implementation**:
```kotlin
package com.convocli.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.convocli.data.model.Command
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

/**
 * Test for CommandDao Room operations.
 *
 * Uses in-memory database for isolated testing.
 */
@RunWith(AndroidJUnit4::class)
class CommandDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var commandDao: CommandDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        commandDao = database.commandDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertCommand_andRetrieve() = runTest {
        // Given
        val command = Command(
            commandText = "ls -la",
            workingDirectory = "/home",
            executedAt = System.currentTimeMillis()
        )

        // When
        val id = commandDao.insert(command)
        val retrieved = commandDao.getRecent(1).first()

        // Then
        assertEquals(command.commandText, retrieved.commandText)
        assertEquals(command.workingDirectory, retrieved.workingDirectory)
    }

    @Test
    fun observeAll_emitsFlow() = runTest {
        // Given
        val command = Command(
            commandText = "pwd",
            workingDirectory = "/home",
            executedAt = System.currentTimeMillis()
        )

        // When
        commandDao.insert(command)
        val commands = commandDao.observeAll().first()

        // Then
        assertEquals(1, commands.size)
        assertEquals("pwd", commands.first().commandText)
    }
}
```

**Validation**:
- Test runs successfully
- In-memory database created
- Insert and query operations work

**Reference**: [data-model.md Section: Testing](./data-model.md#testing-considerations)

---

### [T026] [AC-5] Create example Compose UI test
```kotlin
File: app/src/androidTest/kotlin/com/convocli/MainActivityTest.kt
Dependencies: T019 (MainActivity)
[P] Parallelizable with T025, T027
```

**Task**:
Create basic Compose UI test to validate test infrastructure.

**Implementation**:
```kotlin
package com.convocli

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.convocli.ui.theme.ConvoCLITheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for MainActivity Compose UI.
 */
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun greeting_isDisplayed() {
        // Given
        composeTestRule.setContent {
            ConvoCLITheme {
                Greeting()
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Hello ConvoCLI")
            .assertIsDisplayed()
    }
}
```

**Validation**:
- Test runs on device/emulator
- Compose test rule works
- Text assertion passes

---

### [T027] [AC-4] Create test data builder utilities
```kotlin
File: app/src/test/kotlin/com/convocli/TestDataBuilders.kt
Dependencies: T010 (Command entity)
[P] Parallelizable with T025-T026
```

**Task**:
Create test data builders for easier test setup.

**Implementation**:
```kotlin
package com.convocli

import com.convocli.data.model.Command
import java.util.UUID

/**
 * Test data builders for common entities.
 *
 * Provides default values with overrides for specific test cases.
 */

fun commandEntity(
    id: Long = 0,
    commandText: String = "ls -la",
    output: String? = null,
    exitCode: Int? = null,
    executedAt: Long = System.currentTimeMillis(),
    workingDirectory: String = "/home",
    sessionId: String? = null
) = Command(
    id = id,
    commandText = commandText,
    output = output,
    exitCode = exitCode,
    executedAt = executedAt,
    workingDirectory = workingDirectory,
    sessionId = sessionId
)
```

**Validation**:
- Builder function available in tests
- Provides sensible defaults
- Easy to override specific fields

**Reference**: [data-model.md Section: Testing](./data-model.md#testing-considerations)

---

**Phase 4 Checkpoint**: ✅ Code quality tools configured, basic tests passing

**Validation Command**:
```bash
./gradlew ktlintCheck test
```

**Expected Result**:
- ktlintCheck: 0 violations
- All tests pass (unit and instrumented)

---

## Phase 5: Build Optimization & Validation

**Goal**: Optimize build configuration, measure performance, validate all AC

**Prerequisites**: Phase 4 complete

**Duration**: 1-2 hours

**Validation**: All acceptance criteria validated, build performance meets targets

---

### [T028] [AC-2] Measure clean build time
```bash
File: N/A (measurement task)
Dependencies: All previous tasks
```

**Task**:
Measure and document clean build time.

**Command**:
```bash
./gradlew clean
time ./gradlew build
```

**Validation**:
- Build completes successfully
- Time < 2 minutes (target from SC-2)
- Document result in plan.md

---

### [T029] [AC-2] Measure incremental build time
```bash
File: N/A (measurement task)
Dependencies: T028
```

**Task**:
Make trivial change and measure incremental build time.

**Command**:
```bash
# Make trivial change
echo "// Comment" >> app/src/main/kotlin/com/convocli/MainActivity.kt
time ./gradlew build
```

**Validation**:
- Build completes successfully
- Time < 30 seconds (target from SC-2)
- Document result in plan.md

---

### [T030] [AC-2] Build release APK and measure size
```bash
File: N/A (build task)
Dependencies: T007 (ProGuard rules)
```

**Task**:
Build release APK with R8 shrinking and measure size.

**Command**:
```bash
./gradlew assembleRelease
ls -lh app/build/outputs/apk/release/*.apk
```

**Validation**:
- APK builds successfully
- Size < 25MB (target from SC-2)
- R8 shrinking applied
- Document size in plan.md

---

### [T031] [AC-1] Validate project structure against spec
```bash
File: N/A (validation task)
Dependencies: All previous tasks
[P] Parallelizable with T032-T037
```

**Task**:
Manually validate AC-1: Project Structure requirements.

**Checklist**:
- [ ] Project syncs successfully in Android Studio
- [ ] All Gradle files recognized correctly
- [ ] Package structure matches `com.convocli` pattern
- [ ] No "missing dependency" warnings

**Validation**:
- All checkboxes ticked
- Document validation in plan.md

**Reference**: [spec.md AC-1](./spec.md#ac-1-project-structure)

---

### [T032] [AC-2] Validate build success against spec
```bash
File: N/A (validation task)
Dependencies: T028-T030
[P] Parallelizable with T031, T033-T037
```

**Task**:
Manually validate AC-2: Build Success requirements.

**Checklist**:
- [ ] `./gradlew build` completes without errors
- [ ] Build completes without warnings
- [ ] APK generated in `app/build/outputs/apk/`
- [ ] APK size < 25MB

**Validation**:
- All checkboxes ticked
- Document validation in plan.md

**Reference**: [spec.md AC-2](./spec.md#ac-2-build-success)

---

### [T033] [AC-3] Validate application launch against spec
```bash
File: N/A (validation task)
Dependencies: T019-T020
[P] Parallelizable with T031-T032, T034-T037
```

**Task**:
Manually validate AC-3: Application Launch requirements.

**Checklist**:
- [ ] App installs without errors
- [ ] App launches without crashes
- [ ] Material 3 theme applied correctly
- [ ] App displays without visual errors
- [ ] Dark mode works (toggle system theme)

**Validation**:
- All checkboxes ticked
- Document validation in plan.md

**Reference**: [spec.md AC-3](./spec.md#ac-3-application-launch)

---

### [T034] [AC-4] Validate dependency injection against spec
```bash
File: N/A (validation task)
Dependencies: T008, T013
[P] Parallelizable with T031-T033, T035-T037
```

**Task**:
Manually validate AC-4: Dependency Injection requirements.

**Checklist**:
- [ ] Hilt generates necessary code
- [ ] Dependencies inject correctly (check generated files)
- [ ] No runtime injection errors in Logcat

**Validation**:
- All checkboxes ticked
- Document validation in plan.md

**Reference**: [spec.md AC-4](./spec.md#ac-4-dependency-injection)

---

### [T035] [AC-5] Validate Compose integration against spec
```bash
File: N/A (validation task)
Dependencies: T019
[P] Parallelizable with T031-T034, T036-T037
```

**Task**:
Manually validate AC-5: Compose Integration requirements.

**Checklist**:
- [ ] Compose preview works in Android Studio
- [ ] Component renders correctly
- [ ] Theme colors are applied
- [ ] No Compose runtime errors in Logcat

**Validation**:
- All checkboxes ticked
- Document validation in plan.md

**Reference**: [spec.md AC-5](./spec.md#ac-5-compose-integration)

---

### [T036] [AC-6] Validate standards compliance against spec
```bash
File: N/A (validation task)
Dependencies: T023
[P] Parallelizable with T031-T035, T037
```

**Task**:
Manually validate AC-6: Standards Compliance requirements.

**Checklist**:
- [ ] All files pass ktlint validation
- [ ] Zero style violations reported
- [ ] Code follows constitution.md patterns

**Validation**:
- All checkboxes ticked
- Document validation in plan.md

**Reference**: [spec.md AC-6](./spec.md#ac-6-standards-compliance)

---

### [T037] [AC-7] Validate Room database against spec
```bash
File: N/A (validation task)
Dependencies: T025
[P] Parallelizable with T031-T036
```

**Task**:
Manually validate AC-7: Room Database requirements.

**Checklist**:
- [ ] Room annotation processor generates code
- [ ] Database compiles successfully
- [ ] No Room compiler errors in build output
- [ ] DAO test passes

**Validation**:
- All checkboxes ticked
- Document validation in plan.md

**Reference**: [spec.md AC-7](./spec.md#ac-7-room-database)

---

**Phase 5 Checkpoint**: ✅ All acceptance criteria validated, build performance measured

**Validation Command**:
```bash
./gradlew clean build
./gradlew installDebug
```

**Expected Result**:
- All AC checklists complete
- Build times meet targets
- APK size within budget

---

## Phase 6: Documentation & Finalization

**Goal**: Update documentation, create quickstart guide, prepare for merge

**Prerequisites**: Phase 5 complete

**Duration**: 1 hour

**Validation**: All documentation accurate, ready for `/specswarm:complete`

---

### [T038] [Polish] Create quickstart.md guide
```markdown
File: features/001-android-project-setup/quickstart.md
Dependencies: All previous tasks
```

**Task**:
Create quickstart guide for setting up the project.

**Implementation**:
See separate quickstart.md template in feature directory.

**Validation**:
- File exists
- Contains setup instructions
- Links to relevant documentation
- Includes troubleshooting section

---

### [T039] [Polish] Update README.md with setup instructions
```markdown
File: README.md
Dependencies: T038
[P] Parallelizable with T040-T041
```

**Task**:
Update root README.md with Android setup instructions.

**Changes**:
- Add "Development Setup" section
- Link to quickstart guide
- Update project status checkboxes
- Document build commands

**Validation**:
- README.md updated
- Links work
- Instructions are clear

---

### [T040] [Polish] Update CHANGELOG.md with feature completion
```markdown
File: CHANGELOG.md
Dependencies: None
[P] Parallelizable with T039, T041
```

**Task**:
Add entry to CHANGELOG.md for Feature 001 completion.

**Implementation**:
```markdown
## [Unreleased]

### Added
- Android project foundation with Gradle Kotlin DSL
- Jetpack Compose 1.9.3 UI framework (BOM 2025.10.00)
- Material Design 3 theme with dynamic color support
- Hilt 2.48+ dependency injection
- Room 2.6+ database with KSP annotation processing
- ktlint code style enforcement
- Basic test infrastructure (JUnit, Compose UI Test)
```

**Validation**:
- Entry added to Unreleased section
- All major components documented

---

### [T041] [Polish] Verify CLAUDE.md accuracy
```markdown
File: CLAUDE.md
Dependencies: All previous tasks
[P] Parallelizable with T039-T040
```

**Task**:
Review and update CLAUDE.md for accuracy.

**Checklist**:
- [ ] Tech stack versions match implementation
- [ ] Directory structure matches actual project
- [ ] Build commands work as documented
- [ ] No outdated information

**Validation**:
- All checkboxes ticked
- CLAUDE.md is accurate reference

---

### [T042] [Polish] Create pre-merge validation script
```bash
File: scripts/validate-build.sh
Dependencies: All previous tasks
```

**Task**:
Create script that validates all build requirements.

**Implementation**:
```bash
#!/bin/bash
set -e

echo "🔍 Running pre-merge validation..."

echo "✅ Step 1: ktlint check"
./gradlew ktlintCheck

echo "✅ Step 2: Unit tests"
./gradlew test

echo "✅ Step 3: Build"
./gradlew clean build

echo "✅ Step 4: Check APK size"
APK_SIZE=$(ls -lh app/build/outputs/apk/debug/*.apk | awk '{print $5}')
echo "APK size: $APK_SIZE (limit: 25MB)"

echo "🎉 All validation checks passed!"
```

**Validation**:
- Script exists and is executable
- All checks pass when run

---

### [T043] [Polish] Document validation results in plan.md
```markdown
File: features/001-android-project-setup/plan.md
Dependencies: T031-T037
```

**Task**:
Update plan.md with validation results for all AC.

**Changes**:
- Update "Acceptance Criteria Mapping" section
- Document actual measurements (build time, APK size)
- Mark all AC as validated

**Validation**:
- plan.md shows all AC validated
- Measurements documented

---

### [T044] [Polish] Create feature completion summary
```markdown
File: features/001-android-project-setup/COMPLETION.md
Dependencies: T043
```

**Task**:
Create summary document for feature completion.

**Implementation**:
```markdown
# Feature 001: Android Project Foundation Setup - COMPLETION SUMMARY

**Status**: ✅ COMPLETE
**Completed**: 2025-10-20
**Effort**: X hours (estimated 8-12 hours)

## Deliverables

### Core Infrastructure
- ✅ Gradle project with Kotlin DSL
- ✅ Hilt dependency injection
- ✅ Room database with KSP
- ✅ DataStore for settings
- ✅ Material 3 theme with dynamic color

### Validation Results

**AC-1: Project Structure** ✅ PASSED
- Gradle sync: SUCCESS
- Package structure: CORRECT (com.convocli)
- No missing dependencies: CONFIRMED

**AC-2: Build Success** ✅ PASSED
- Build: SUCCESS
- Warnings: 0
- APK size: XX.XMB (< 25MB target)

**AC-3: Application Launch** ✅ PASSED
- Install: SUCCESS
- Launch: NO CRASHES
- Theme: CORRECT
- Visual: NO ERRORS

**AC-4: Dependency Injection** ✅ PASSED
- Hilt code generation: SUCCESS
- DI runtime: NO ERRORS

**AC-5: Compose Integration** ✅ PASSED
- Preview: WORKS
- Rendering: CORRECT
- Theme: APPLIED
- Runtime: NO ERRORS

**AC-6: Standards Compliance** ✅ PASSED
- ktlint: 0 violations
- Constitution: COMPLIANT

**AC-7: Room Database** ✅ PASSED
- Code generation: SUCCESS
- Compilation: SUCCESS
- Tests: PASSING

## Performance Metrics

- Clean build time: X.X minutes (< 2 min target)
- Incremental build: XX seconds (< 30 sec target)
- APK size: XX.XMB (< 25MB target)

## Next Steps

1. Run `/specswarm:complete` to merge to sprint-01
2. Begin Feature 002: Termux Integration
3. Begin Feature 003: Command Blocks UI
```

**Validation**:
- Document created with actual measurements
- All sections complete

---

### [T045] [Polish] Clean up temporary files and artifacts
```bash
File: N/A (cleanup task)
Dependencies: All previous tasks
```

**Task**:
Remove temporary build artifacts and clean project.

**Command**:
```bash
./gradlew clean
rm -rf .gradle/
rm -rf app/build/
```

**Validation**:
- Build directories cleaned
- No stale artifacts remain

---

### [T046] [Polish] Stage all files for commit
```bash
File: N/A (git task)
Dependencies: T045
```

**Task**:
Stage all project files for commit (don't commit yet, /specswarm:complete will do that).

**Command**:
```bash
git add .
git status
```

**Validation**:
- All new files staged
- No untracked files remain (except .gitignore entries)
- Ready for commit

---

### [T047] [Polish] Create pre-merge checklist validation
```markdown
File: N/A (manual validation)
Dependencies: All previous tasks
```

**Task**:
Final pre-merge checklist validation.

**Checklist**:
- [ ] All 51 tasks completed
- [ ] All 7 acceptance criteria validated
- [ ] ktlintCheck passes (0 violations)
- [ ] All tests pass
- [ ] Build succeeds
- [ ] APK size < 25MB
- [ ] Documentation updated
- [ ] Git staged and ready

**Validation**:
- All checkboxes ticked
- Ready for `/specswarm:complete`

---

**Phase 6 Checkpoint**: ✅ Documentation complete, ready for merge

**Final Validation**:
```bash
./gradlew ktlintCheck test build
```

**Expected Result**: All checks pass, ready to run `/specswarm:complete`

---

## Task Dependencies Graph

```
Phase 1: Setup
T001 (build.gradle.kts) ──┐
T002 (gradle.properties) ─┤
T003 (settings.gradle.kts)┤
T004 (.gitignore) ────────┤
T005 (directories) ───────┼─► T006 (app build.gradle.kts)
T007 (proguard-rules.pro)─┘

Phase 2: Foundation
T006 ──► T008 (Application) ──┬─► T009 (Manifest)
                               │
T006 ──► T010 (Command) ───────┼─► T011 (DAO) ──► T012 (Database)
                               │
T008, T012 ──► T013 (DatabaseModule)
T006 ──────────► T014 (DataStore)
T008 ──────────► T015 (AppModule)

Phase 3: UI
T006 ──► T016 (Color) ──┐
T006 ──► T017 (Type) ───┼─► T018 (Theme) ──┐
                                            ├─► T019 (MainActivity)
T008 ──────────────────────────────────────┘
T009, T019 ──► T020 (Manifest update)
T021 (strings.xml)

Phase 4: Quality
T006 ──► T022 (ktlintFormat) ──► T023 (ktlintCheck)
T024 (.editorconfig)
T011 ──► T025 (DAO test)
T019 ──► T026 (UI test)
T010 ──► T027 (Test builders)

Phase 5: Validation
All ──► T028 (clean build) ──► T029 (incremental build)
T007 ──► T030 (release APK)
All ──► T031-T037 (AC validation)

Phase 6: Documentation
All ──► T038 (quickstart) ──┬─► T039 (README)
                             ├─► T040 (CHANGELOG)
                             ├─► T041 (CLAUDE.md)
                             ├─► T042 (validate script)
T031-T037 ──► T043 (plan.md update)
T043 ──► T044 (completion summary)
T044 ──► T045 (cleanup)
T045 ──► T046 (git stage)
T046 ──► T047 (final checklist)
```

---

## Parallel Execution Examples

### Phase 1 (Setup) - Maximum Parallelization
```bash
# All Setup tasks can run in parallel
[T001, T002, T003, T004, T005, T007] in parallel
├─ Then: T006 (needs T001 plugin versions)
```

### Phase 2 (Foundation) - Partial Parallelization
```bash
# After T006 completes:
[T008, T010, T014, T015] in parallel
├─ Then: T009 (needs T008)
├─ Then: T011 (needs T010)
├─ Then: T012 (needs T010, T011)
├─ Then: T013 (needs T008, T012)
```

### Phase 3 (UI) - Partial Parallelization
```bash
# After T006 completes:
[T016, T017, T021] in parallel
├─ Then: T018 (needs T016, T017)
├─ Then: T019 (needs T008, T018)
├─ Then: T020 (needs T009, T019)
```

### Phase 4 (Quality) - Partial Parallelization
```bash
# After previous phases:
[T024] independent
[T022] ──► T023 (sequential formatting)
[T025, T026, T027] in parallel (after dependencies met)
```

### Phase 5 (Validation) - Maximum Parallelization
```bash
# After all implementation:
T028 ──► T029 (sequential build measurements)
[T030, T031, T032, T033, T034, T035, T036, T037] in parallel
```

### Phase 6 (Documentation) - Partial Parallelization
```bash
# After validation:
T038 ──► [T039, T040, T041] in parallel
[T031-T037] ──► T043 ──► T044 ──► T045 ──► T046 ──► T047 (sequential)
T042 independent
```

---

## Implementation Strategy

### Critical Path (MVP Scope)

For fastest time-to-working-app, execute tasks in this order:

1. **Phase 1: Setup** (all tasks) - 1-2 hours
2. **Phase 2: Foundation** (T008-T015) - 2-3 hours
3. **Phase 3: UI** (T016-T021) - 2-3 hours
4. **Build & Run**: `./gradlew installDebug`

**Total MVP Time**: 5-8 hours

**Deliverable**: Working Android app that launches with Material 3 theme

### Full Feature Scope

For complete acceptance criteria validation:

1. Execute MVP scope (Phases 1-3)
2. **Phase 4: Quality** (T022-T027) - 1-2 hours
3. **Phase 5: Validation** (T028-T037) - 1-2 hours
4. **Phase 6: Documentation** (T038-T047) - 1 hour

**Total Full Scope**: 8-12 hours

**Deliverable**: Production-ready project foundation meeting all AC

---

## Task Execution Recommendations

### For Claude Code / AI Implementation

**Sequential Execution**:
- Follow phases in order (1 → 2 → 3 → 4 → 5 → 6)
- Within each phase, execute tasks in numerical order
- Mark [P] tasks for potential parallelization

**Validation After Each Phase**:
- Run phase checkpoint command
- Verify expected result before proceeding
- Document any deviations

**Error Handling**:
- If task fails, check dependencies completed
- Validate prerequisites met
- Consult reference documentation

### For Human Developers

**Parallel Development**:
- Group [P] tasks by phase
- Use multiple terminals/IDEs for parallel work
- Coordinate on shared files (Manifest, build.gradle.kts)

**Incremental Testing**:
- Run `./gradlew build` after each phase
- Test app launch after Phase 3
- Validate AC as you go

---

## Task Summary

**Total Tasks**: 51
**Phases**: 6
**Estimated Effort**: 8-12 hours

**Task Breakdown by Phase**:
- Phase 1 (Setup): 7 tasks
- Phase 2 (Foundation): 8 tasks
- Phase 3 (UI): 6 tasks
- Phase 4 (Quality): 6 tasks
- Phase 5 (Validation): 10 tasks
- Phase 6 (Documentation): 10 tasks
- Polish: 4 tasks

**Parallelization Opportunities**: 24 tasks marked [P]

**Acceptance Criteria Coverage**:
- AC-1 (Project Structure): T001-T006, T031
- AC-2 (Build Success): T006, T028-T030, T032
- AC-3 (Application Launch): T019-T020, T033
- AC-4 (Dependency Injection): T008-T015, T034
- AC-5 (Compose Integration): T016-T019, T035
- AC-6 (Standards Compliance): T022-T024, T036
- AC-7 (Room Database): T010-T013, T025, T037

---

## References

**Feature Documentation**:
- [spec.md](./spec.md) - Feature specification with acceptance criteria
- [plan.md](./plan.md) - Implementation plan with technical context
- [research.md](./research.md) - Technical research and decisions
- [data-model.md](./data-model.md) - Database schema and models
- [contracts/TermuxIntegration.kt](./contracts/TermuxIntegration.kt) - API contracts

**Project Documentation**:
- [CLAUDE.md](/CLAUDE.md) - Development guide
- [Constitution](/.specswarm/constitution.md) - Coding standards
- [Tech Stack](/.specswarm/tech-stack.md) - Approved technologies

---

*Tasks generated by SpecSwarm. Ready for `/specswarm:implement` or manual execution.*
