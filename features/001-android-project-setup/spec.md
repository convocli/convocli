# Feature: Android Project Foundation Setup

## Overview

Establish the foundational Android project structure for ConvoCLI with modern architecture components and development standards. This feature provides developers with a correctly configured project that follows the team's documented coding standards (constitution.md) and technical specifications (CLAUDE.md), enabling immediate feature development without configuration overhead.

**Business Value**: Accelerates development by providing a pre-configured, standards-compliant project foundation, reducing setup time from days to minutes and ensuring consistency from the first line of code.

## User Scenarios

### Primary User: ConvoCLI Developers

**Scenario 1: Starting Feature Development**
1. Developer checks out the codebase for the first time
2. Developer opens the project in Android Studio
3. Project builds successfully without manual configuration
4. Developer can immediately start implementing features (e.g., Termux integration)
5. All dependencies and tools are available and configured correctly

**Scenario 2: Building the Application**
1. Developer runs the build command
2. Gradle resolves all dependencies without errors
3. Application compiles with zero warnings
4. APK is generated within performance budget (< 30MB)
5. Developer can install and run the app on a physical device or emulator

**Scenario 3: Following Coding Standards**
1. Developer writes new code (e.g., a Composable function)
2. Code follows established patterns (Compose guidelines, MVI architecture)
3. Linting passes automatically
4. Dependencies use correct versions (Compose BOM 2025.10.00)
5. Code integrates seamlessly with existing structure

## Functional Requirements

### FR-1: Gradle Build Configuration
- Build system must use Gradle with Kotlin DSL (build.gradle.kts)
- Must configure Jetpack Compose using Bill of Materials (BOM) version 2025.10.00
- Must include Hilt for dependency injection
- Must include Room for local database support
- All dependency versions must match those specified in CLAUDE.md
- Build must complete successfully without warnings
- Generated APK must be under 30MB (per constitution.md performance budget)

### FR-2: Project Structure
- Must create `app` module as the main application module
- Package structure must be `com.convocli` as the root package
- Source code must be organized in `src/main/kotlin/com/convocli/` directory
- Must include standard Android directories: `res/`, `AndroidManifest.xml`
- Must follow the directory structure defined in CLAUDE.md

### FR-3: Dependency Injection Setup
- Must include Hilt Android library
- Must create an Application class annotated with `@HiltAndroidApp`
- Application class must be registered in AndroidManifest.xml
- Configuration must enable Hilt code generation

### FR-4: Jetpack Compose Configuration
- Must configure Compose with BOM version 2025.10.00
- Must include compose-ui, compose-material3, and ui-tooling-preview dependencies
- Must enable Compose in build configuration
- Kotlin compiler extension version must be compatible with Compose BOM

### FR-5: Material Design 3 Theme
- Must create a base Material 3 theme following Material Design guidelines
- Theme must support both light and dark modes
- Theme must use MaterialTheme.colorScheme (no hardcoded colors, per constitution.md)
- Theme structure must enable easy customization

### FR-6: Main Activity Setup
- Must create MainActivity as the app entry point
- MainActivity must extend ComponentActivity
- Activity must use `setContent {}` with Compose
- Activity must apply the Material 3 theme
- Activity must be declared in AndroidManifest.xml with appropriate intent filters

### FR-7: Room Database Configuration
- Must include Room runtime and compiler dependencies
- Room version must be compatible with project Kotlin version
- Must configure annotation processing for Room (kapt or KSP)
- Database setup must follow repository pattern per constitution.md

### FR-8: Standards Compliance
- All code must follow Kotlin coding standards defined in constitution.md:
  - 4-space indentation
  - 120-character line length
  - PascalCase for classes, camelCase for functions
  - KDoc for public APIs
- Compose code must follow Compose patterns defined in constitution.md:
  - Modifier parameter first
  - Composable function naming (PascalCase)
  - StateFlow for state management
- Architecture must follow MVI pattern defined in constitution.md

### FR-9: Build Verification
- Project must build successfully with `./gradlew build`
- All tests must pass (even if empty test suite initially)
- ktlint checks must pass with zero violations
- No compiler warnings

## Success Criteria

### SC-1: Zero-Configuration Development
- Developer can clone repository and start coding within 5 minutes
- No manual dependency installation required
- No IDE-specific configuration required beyond standard Android Studio setup

### SC-2: Build Performance
- Clean build completes in under 2 minutes on modern development machine
- Incremental builds complete in under 30 seconds
- APK size is under 25MB for initial empty app (with 30MB budget for features)

### SC-3: Standards Enforcement
- 100% of generated code passes ktlint validation
- All build configurations match constitution.md specifications
- All dependency versions match CLAUDE.md tech stack

### SC-4: Developer Onboarding
- New developer can build and run the app on first attempt
- Zero configuration errors during initial setup
- All documentation references (CLAUDE.md paths, package names) are correct

### SC-5: Foundation Completeness
- All architecture components (Hilt, Room, Compose) are functional
- Sample code demonstrates correct usage patterns
- Integration between components works without additional configuration

## Key Entities

### Build Configuration
- **build.gradle.kts (Project)**: Root build file with plugin versions
- **build.gradle.kts (App)**: Application module build configuration with all dependencies
- **gradle.properties**: Gradle configuration properties
- **settings.gradle.kts**: Project settings and module configuration

### Application Components
- **ConvoCLIApplication**: Main application class with Hilt setup
- **MainActivity**: Entry point activity with Compose integration
- **ConvoCLITheme**: Material 3 theme definition

### Resource Structure
- **res/values/**: Theme colors, strings, dimensions
- **res/xml/**: Configuration files (if needed)
- **AndroidManifest.xml**: Application manifest with permissions and components

## Assumptions

### Technical Assumptions
- Target Android SDK is API 26+ (Android 8.0 Oreo minimum) as standard for modern apps
- Developers use Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 is available for Gradle 8.x compatibility
- Git is available for version control operations

### Development Environment Assumptions
- Developers have internet access for dependency downloads
- Standard Android SDK tools are installed
- Physical device or emulator is available for testing

### Dependency Assumptions
- Jetpack Compose BOM 2025.10.00 provides compatible versions for all Compose libraries
- Hilt version is 2.48+ for Kotlin compatibility
- Room version is 2.6+ for Kotlin and KSP support
- All dependencies are available from Google Maven repository

### Coding Standards Assumptions
- constitution.md contains complete Kotlin and Compose coding standards
- CLAUDE.md contains accurate dependency versions and package structure
- All referenced standards documents are available in the repository

## Out of Scope

The following are explicitly **not** part of this feature:

- Termux integration (separate feature)
- Command blocks UI (separate feature)
- ConvoSync functionality (future phase)
- Automated testing setup beyond basic structure
- CI/CD pipeline configuration
- Logging framework integration
- Analytics or crash reporting
- ProGuard/R8 rules beyond defaults
- Custom lint rules beyond ktlint
- Git hooks or pre-commit configuration

## Dependencies

### External Dependencies
- Requires constitution.md to exist with complete coding standards
- Requires CLAUDE.md to exist with technology stack specifications
- Requires internet connection for dependency downloads
- Requires Android SDK to be installed

### Internal Dependencies
- None (this is the foundational feature)

## Risks & Mitigations

### Risk: Dependency Version Conflicts
**Impact**: Build failures, incompatible library versions
**Likelihood**: Medium
**Mitigation**: Use Compose BOM for version management, test build immediately after setup

### Risk: Constitution/CLAUDE.md Standards Mismatch
**Impact**: Generated code doesn't follow actual standards
**Likelihood**: Low (documents recently created)
**Mitigation**: Cross-reference constitution.md and CLAUDE.md during implementation, validate with quality checklist

### Risk: Development Environment Variability
**Impact**: Works on one machine, fails on another
**Likelihood**: Medium
**Mitigation**: Document exact JDK/Android Studio versions, use Gradle wrapper for consistency

## Acceptance Criteria

### AC-1: Project Structure
**Given** a fresh clone of the repository
**When** the developer opens the project in Android Studio
**Then**:
- Project syncs successfully without errors
- All Gradle files are recognized correctly
- Package structure matches `com.convocli` pattern
- No "missing dependency" warnings appear

### AC-2: Build Success
**Given** the project is synced in Android Studio
**When** the developer runs `./gradlew build`
**Then**:
- Build completes without errors
- Build completes without warnings
- APK is generated in `app/build/outputs/apk/`
- APK size is under 25MB

### AC-3: Application Launch
**Given** the project has been built successfully
**When** the developer runs the app on a device/emulator
**Then**:
- App installs without errors
- App launches without crashes
- Material 3 theme is applied correctly
- App displays without visual errors

### AC-4: Dependency Injection
**Given** the project uses Hilt
**When** the developer creates a new component with `@Inject`
**Then**:
- Hilt generates necessary code
- Dependencies are injected correctly
- No runtime injection errors occur

### AC-5: Compose Integration
**Given** MainActivity uses Compose
**When** the developer creates a new `@Composable` function
**Then**:
- Compose preview works in Android Studio
- Component renders correctly
- Theme colors are applied
- No Compose runtime errors

### AC-6: Standards Compliance
**Given** the generated code
**When** `./gradlew ktlintCheck` is executed
**Then**:
- All files pass ktlint validation
- Zero style violations reported
- Code follows constitution.md patterns

### AC-7: Room Database
**Given** Room dependencies are configured
**When** the developer creates an `@Entity` and `@Dao`
**Then**:
- Room annotation processor generates code
- Database compiles successfully
- No Room compiler errors

## Notes

### Implementation Guidance
This specification intentionally avoids implementation details (specific class names, code structure) to allow flexibility in HOW the setup is implemented while ensuring WHAT is delivered meets developer needs.

### Testing Notes
While automated testing is out of scope for this feature, the acceptance criteria provide clear manual validation steps that can be converted to automated tests in future iterations.

### Documentation Updates Required
After implementation:
- Update CLAUDE.md if actual package structure differs
- Update constitution.md if new patterns emerge
- Create setup instructions in README.md if not already present
