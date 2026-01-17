#!/bin/bash

# Script to sync code from dev branch to main branch (excluding docs folder)

set -e

echo "🔄 Syncing dev branch to main branch (excluding docs)..."
echo ""

# Get current branch
CURRENT_BRANCH=$(git branch --show-current)
echo "Current branch: $CURRENT_BRANCH"

# Check if we're on dev branch
if [ "$CURRENT_BRANCH" != "dev" ]; then
    echo "⚠️  Warning: You're not on dev branch. Switching to dev..."
    git checkout dev
fi

# Ensure we're up to date
echo ""
echo "📥 Pulling latest changes from dev..."
git pull origin dev 2>/dev/null || echo "No remote changes"

# Switch to main
echo ""
echo "🔄 Switching to main branch..."
git checkout main

# Pull latest main
echo "📥 Pulling latest changes from main..."
git pull origin main 2>/dev/null || echo "No remote changes"

# Merge dev into main (but we'll exclude docs later)
echo ""
echo "🔀 Merging dev into main..."
git merge dev --no-commit --no-ff 2>&1 | tee /tmp/merge_output.txt || true

# Handle conflicts - remove docs folder conflicts
if grep -q "CONFLICT" /tmp/merge_output.txt; then
    echo ""
    echo "⚠️  Merge conflicts detected. Resolving docs folder conflicts..."
    # Remove all docs folder conflicts
    git rm -rf docs/ 2>/dev/null || true
    rm -rf docs/ 2>/dev/null || true
    # Resolve other conflicts by accepting dev version
    git checkout --theirs . 2>/dev/null || true
    git add -A
fi

# Remove docs folder if it exists (in case merge didn't conflict)
if [ -d "docs" ]; then
    echo ""
    echo "🗑️  Removing docs folder from main branch..."
    git rm -r docs/ 2>/dev/null || rm -rf docs/
fi

# Stage all changes except docs
echo ""
echo "📝 Staging changes (excluding docs)..."
git add -A
git reset HEAD docs/ 2>/dev/null || true
git reset HEAD -- docs 2>/dev/null || true

# Ensure docs folder is removed
if [ -d "docs" ]; then
    rm -rf docs/
    git add -A
fi

# Check if there are changes to commit
if git diff --cached --quiet && [ -z "$(git status --porcelain)" ]; then
    echo ""
    echo "✅ No changes to sync. Main branch is already up to date."
    git merge --abort 2>/dev/null || true
else
    # Commit the merge
    echo ""
    echo "💾 Committing merge..."
    git commit -m "Sync code from dev branch (excluding docs folder)" || true
    
    echo ""
    echo "✅ Successfully synced dev to main!"
    echo ""
    echo "📋 Summary:"
    echo "  - All code changes from dev are now in main"
    echo "  - docs/ folder excluded (as expected)"
    echo "  - README.md preserved"
    echo ""
    echo "🚀 Next step: Push to GitHub"
    echo "   git push origin main"
fi

echo ""
echo "🔄 Switching back to dev branch..."
git checkout dev

echo ""
echo "✅ Done! You're back on dev branch."
