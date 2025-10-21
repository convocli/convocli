#!/bin/bash

# Pre-merge validation script for ConvoCLI features
# Usage: ./scripts/validate-feature.sh [feature-directory]
#
# Example: ./scripts/validate-feature.sh features/001-android-project-setup

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
PASSED=0
FAILED=0
WARNINGS=0

# Feature directory (optional argument)
FEATURE_DIR="${1:-}"

echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  ConvoCLI Pre-Merge Validation Script${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

# Function to print check result
check_pass() {
    echo -e "${GREEN}✓${NC} $1"
    ((PASSED++))
}

check_fail() {
    echo -e "${RED}✗${NC} $1"
    ((FAILED++))
}

check_warn() {
    echo -e "${YELLOW}⚠${NC} $1"
    ((WARNINGS++))
}

check_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# ============================================================================
# 1. ENVIRONMENT CHECKS
# ============================================================================

echo -e "${BLUE}[1/7] Environment Checks${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check if we're in the right directory
if [ ! -f "settings.gradle.kts" ]; then
    check_fail "Not in ConvoCLI root directory"
    echo ""
    echo -e "${RED}Error: Please run this script from the project root directory${NC}"
    exit 1
else
    check_pass "Running from project root"
fi

# Check for Gradle wrapper
if [ -f "gradlew" ]; then
    check_pass "Gradle wrapper found"
else
    check_warn "Gradle wrapper not found (requires Android Studio initialization)"
fi

# Check Java version
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        check_pass "Java $JAVA_VERSION detected (JDK 17+ required)"
    else
        check_fail "Java $JAVA_VERSION detected (JDK 17+ required)"
    fi
else
    check_warn "Java not found in PATH"
fi

echo ""

# ============================================================================
# 2. PROJECT STRUCTURE VALIDATION
# ============================================================================

echo -e "${BLUE}[2/7] Project Structure Validation${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

REQUIRED_FILES=(
    "build.gradle.kts"
    "settings.gradle.kts"
    "gradle.properties"
    "app/build.gradle.kts"
    "app/src/main/AndroidManifest.xml"
    "app/src/main/kotlin/com/convocli/ConvoCLIApplication.kt"
    "app/src/main/kotlin/com/convocli/MainActivity.kt"
    ".editorconfig"
    "README.md"
    "CLAUDE.md"
)

for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$file" ]; then
        check_pass "$file exists"
    else
        check_fail "$file missing"
    fi
done

echo ""

# ============================================================================
# 3. CODE QUALITY CHECKS
# ============================================================================

echo -e "${BLUE}[3/7] Code Quality Checks${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check for TODO/FIXME comments in production code
TODO_COUNT=$(find app/src/main -name "*.kt" -type f -exec grep -l "TODO\|FIXME" {} \; 2>/dev/null | wc -l)
if [ "$TODO_COUNT" -eq 0 ]; then
    check_pass "No TODO/FIXME in production code"
else
    check_warn "Found $TODO_COUNT file(s) with TODO/FIXME comments"
fi

# Check for hardcoded secrets or API keys
SECRET_PATTERNS=("api_key" "password" "secret" "token" "credentials")
SECRETS_FOUND=0
for pattern in "${SECRET_PATTERNS[@]}"; do
    if grep -r -i "$pattern" app/src/main/kotlin --include="*.kt" | grep -v "// " | grep -v "/\*" > /dev/null 2>&1; then
        ((SECRETS_FOUND++))
    fi
done

if [ "$SECRETS_FOUND" -eq 0 ]; then
    check_pass "No potential hardcoded secrets detected"
else
    check_warn "Potential hardcoded secrets detected (manual review recommended)"
fi

# Check .gitignore for sensitive files
if grep -q "local.properties" .gitignore && grep -q "*.keystore" .gitignore; then
    check_pass ".gitignore includes sensitive files"
else
    check_warn ".gitignore may be missing sensitive file patterns"
fi

echo ""

# ============================================================================
# 4. BUILD VALIDATION (if Gradle wrapper available)
# ============================================================================

echo -e "${BLUE}[4/7] Build Validation${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -f "gradlew" ]; then
    check_info "Running Gradle build (this may take a few minutes)..."

    if ./gradlew build --quiet 2>&1 | tee /tmp/convocli-build.log; then
        check_pass "Gradle build completed successfully"

        # Check for warnings
        WARNING_COUNT=$(grep -c "warning:" /tmp/convocli-build.log 2>/dev/null || echo "0")
        if [ "$WARNING_COUNT" -eq 0 ]; then
            check_pass "No compiler warnings"
        else
            check_warn "Build completed with $WARNING_COUNT warning(s)"
        fi
    else
        check_fail "Gradle build failed (see /tmp/convocli-build.log)"
    fi

    rm -f /tmp/convocli-build.log
else
    check_warn "Gradle wrapper not available - skipping build validation"
    check_info "Initialize Gradle wrapper in Android Studio, then re-run this script"
fi

echo ""

# ============================================================================
# 5. TEST VALIDATION (if Gradle wrapper available)
# ============================================================================

echo -e "${BLUE}[5/7] Test Validation${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

if [ -f "gradlew" ]; then
    check_info "Running unit tests..."

    if ./gradlew test --quiet 2>&1; then
        check_pass "All unit tests passed"
    else
        check_fail "Some unit tests failed"
    fi

    # Note about instrumented tests
    check_info "Instrumented tests require Android device/emulator (run manually)"
else
    check_warn "Gradle wrapper not available - skipping test validation"
fi

echo ""

# ============================================================================
# 6. DOCUMENTATION VALIDATION
# ============================================================================

echo -e "${BLUE}[6/7] Documentation Validation${NC}"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check if feature directory was provided
if [ -n "$FEATURE_DIR" ] && [ -d "$FEATURE_DIR" ]; then
    check_pass "Feature directory found: $FEATURE_DIR"

    # Check for required feature documentation
    FEATURE_DOCS=(
        "$FEATURE_DIR/spec.md"
        "$FEATURE_DIR/plan.md"
        "$FEATURE_DIR/tasks.md"
    )

    for doc in "${FEATURE_DOCS[@]}"; do
        if [ -f "$doc" ]; then
            check_pass "$(basename "$doc") exists"
        else
            check_warn "$(basename "$doc") missing"
        fi
    done

    # Check for quickstart guide (if applicable)
    if [ -f "$FEATURE_DIR/quickstart.md" ]; then
        check_pass "quickstart.md exists"
    fi
else
    check_info "No feature directory specified - skipping feature-specific docs"
fi

# Check root documentation
if [ -f "README.md" ]; then
    # Check if README has development setup section
    if grep -q "Development Setup" README.md; then
        check_pass "README.md includes Development Setup"
    else
        check_warn "README.md missing Development Setup section"
    fi
fi

if [ -f "CHANGELOG.md" ]; then
    check_pass "CHANGELOG.md exists"
else
    check_warn "CHANGELOG.md missing"
fi

echo ""

# ============================================================================
# 7. ACCEPTANCE CRITERIA (if feature directory provided)
# ============================================================================

if [ -n "$FEATURE_DIR" ] && [ -f "$FEATURE_DIR/spec.md" ]; then
    echo -e "${BLUE}[7/7] Acceptance Criteria Check${NC}"
    echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

    check_info "Review acceptance criteria in $FEATURE_DIR/spec.md"
    check_info "Ensure all criteria are manually validated before merge"

    # Try to extract AC count from spec.md
    AC_COUNT=$(grep -c "^## AC-" "$FEATURE_DIR/spec.md" 2>/dev/null || echo "0")
    if [ "$AC_COUNT" -gt 0 ]; then
        check_info "Found $AC_COUNT acceptance criteria to verify"
    fi

    echo ""
else
    check_info "Skipping acceptance criteria check (no feature spec.md)"
    echo ""
fi

# ============================================================================
# SUMMARY
# ============================================================================

echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  Validation Summary${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""
echo -e "  ${GREEN}Passed:${NC}   $PASSED"
echo -e "  ${RED}Failed:${NC}   $FAILED"
echo -e "  ${YELLOW}Warnings:${NC} $WARNINGS"
echo ""

if [ "$FAILED" -eq 0 ]; then
    echo -e "${GREEN}✓ Validation completed successfully!${NC}"
    echo ""
    echo -e "${BLUE}Next Steps:${NC}"
    echo "  1. Manually verify all acceptance criteria"
    echo "  2. Run instrumented tests on device/emulator"
    echo "  3. Review code changes one final time"
    echo "  4. Merge feature branch to sprint branch"
    echo ""
    exit 0
else
    echo -e "${RED}✗ Validation failed with $FAILED error(s)${NC}"
    echo ""
    echo -e "${YELLOW}Please fix the errors above before merging${NC}"
    echo ""
    exit 1
fi
