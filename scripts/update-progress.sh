#!/bin/bash
#
# update-progress.sh - ConvoCLI Development Progress Updater
#
# Updates development progress tracking files with current step information
#
# Usage:
#   ./scripts/update-progress.sh "Phase 1, Step 1.1 - Quality Analysis"
#   ./scripts/update-progress.sh "Phase 2, Step 2.3 - Implementation Planning"
#

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if step description provided
if [ -z "$1" ]; then
    echo -e "${YELLOW}Usage: $0 \"Phase X, Step X.X - Description\"${NC}"
    echo ""
    echo "Example:"
    echo "  $0 \"Phase 1, Step 1.1 - Quality Analysis\""
    echo ""
    exit 1
fi

STEP="$1"
TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

echo -e "${BLUE}Updating ConvoCLI development progress...${NC}"
echo ""

# Update current step file
CURRENT_STEP_FILE="$PROJECT_ROOT/.claude/session-memory/current-step.txt"
echo "$STEP" > "$CURRENT_STEP_FILE"
echo -e "${GREEN}✓${NC} Updated current step: $STEP"

# Display current status
echo ""
echo -e "${BLUE}Current Development Status:${NC}"
echo "  Step: $STEP"
echo "  Updated: $TIMESTAMP"
echo ""

# Show what to do next
echo -e "${YELLOW}Next Actions:${NC}"
echo "  1. Check DEVELOPMENT_ROADMAP.md for detailed instructions"
echo "  2. Execute the command specified for this step"
echo "  3. Document results in SPECSWARM_USAGE_LOG.md (if using SpecSwarm/SpecLabs)"
echo "  4. Run this script again when moving to the next step"
echo ""

# Quick reference commands
echo -e "${BLUE}Quick Reference:${NC}"
echo "  View roadmap:     cat DEVELOPMENT_ROADMAP.md | head -50"
echo "  Check blockers:   cat .claude/session-memory/blockers.md"
echo "  View last done:   cat .claude/session-memory/last-completed.txt"
echo ""

echo -e "${GREEN}✓ Progress update complete!${NC}"
