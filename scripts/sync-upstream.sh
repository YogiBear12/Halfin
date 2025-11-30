#!/bin/bash
# Sync script for Halfin fork
# This script automates fetching and merging upstream Wholphin changes

set -e  # Exit on error

echo "=== Halfin Fork Sync Script ==="
echo ""

# Check if we're in a git repository
if ! git rev-parse --git-dir > /dev/null 2>&1; then
    echo "Error: Not in a git repository"
    exit 1
fi

# Check if upstream remote exists
if ! git remote | grep -q "^upstream$"; then
    echo "Error: Upstream remote not found"
    echo "Please add it with: git remote add upstream https://github.com/damontecres/Wholphin.git"
    exit 1
fi

# Step 1: Fetch latest from upstream
echo "Step 1: Fetching latest from upstream..."
git fetch upstream
echo "✓ Fetched upstream changes"
echo ""

# Step 2: Update main branch
echo "Step 2: Updating main branch with upstream changes..."
CURRENT_BRANCH=$(git branch --show-current)
git checkout main
git merge upstream/main --no-edit
echo "✓ Main branch updated"
echo ""

# Step 3: Merge upstream changes into customizations
echo "Step 3: Merging upstream changes into halfin-customizations..."
git checkout halfin-customizations

# Check if there will be conflicts
if ! git merge --no-commit --no-ff main > /tmp/merge_output 2>&1; then
    echo ""
    echo "⚠️  MERGE CONFLICTS DETECTED!"
    echo ""
    echo "The merge has been started but not completed due to conflicts."
    echo "Please resolve conflicts manually:"
    echo ""
    echo "1. Review conflicted files:"
    echo "   git status"
    echo ""
    echo "2. Open conflicted files and resolve conflicts"
    echo ""
    echo "3. After resolving, stage files and complete merge:"
    echo "   git add <resolved-files>"
    echo "   git commit"
    echo ""
    echo "4. Test your application thoroughly"
    echo ""
    echo "See FORK_MAINTENANCE.md for conflict resolution guidance."
    exit 1
else
    git commit --no-edit
    echo "✓ Successfully merged upstream changes into halfin-customizations"
    echo ""
fi

# Step 4: Show status
echo "Step 4: Current status:"
echo ""
git log --oneline --graph --decorate -5
echo ""

# Ask if user wants to push
read -p "Push changes to origin? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Pushing main branch..."
    git checkout main
    git push origin main
    echo "✓ Main branch pushed"
    echo ""
    echo "Pushing halfin-customizations branch..."
    git checkout halfin-customizations
    git push origin halfin-customizations
    echo "✓ Customizations branch pushed"
    echo ""
fi

# Return to original branch if different
if [ "$CURRENT_BRANCH" != "main" ] && [ "$CURRENT_BRANCH" != "halfin-customizations" ]; then
    git checkout "$CURRENT_BRANCH"
    echo "Returned to branch: $CURRENT_BRANCH"
fi

echo ""
echo "=== Sync Complete ==="

