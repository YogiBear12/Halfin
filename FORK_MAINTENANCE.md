# Halfin Fork Maintenance Guide

This document explains how to maintain the Halfin fork in sync with upstream Wholphin while preserving UI customizations.

## Branch Strategy Overview

Halfin uses a two-branch strategy:

- **`main`**: Clean sync point with upstream Wholphin (no customizations)
- **`halfin-customizations`**: Contains all UI customizations and Halfin-specific changes

This separation allows you to:
- Easily compare with upstream changes
- Keep a clean baseline for syncing
- Maintain customizations in a dedicated branch
- Resolve conflicts systematically

## Initial Setup

The branching strategy has been set up with:
1. Upstream remote configured (`upstream` → `https://github.com/damontecres/Wholphin.git`)
2. `halfin-customizations` branch created with all current customizations
3. `main` branch reset to baseline (v0.3.1 - commit `ec85290`)

## Regular Sync Workflow

### Step 1: Fetch Latest from Upstream

```bash
git fetch upstream
```

This downloads the latest commits from Wholphin without modifying your local branches.

### Step 2: Update Main Branch

```bash
git checkout main
git merge upstream/main --no-edit
git push origin main
```

This brings `main` up to date with upstream Wholphin. The `--no-edit` flag uses the default merge commit message.

### Step 3: Merge Upstream Changes into Customizations

```bash
git checkout halfin-customizations
git merge main --no-edit
```

This merges all upstream changes into your customizations branch. **Conflicts may occur here** - see the Conflict Resolution section below.

### Step 4: Resolve Conflicts (if any)

When conflicts occur, Git will mark the conflicted files. You'll need to:

1. Open the conflicted files in your editor
2. Look for conflict markers (`<<<<<<<`, `=======`, `>>>>>>>`)
3. Manually resolve by keeping your customizations while accepting upstream changes
4. Test thoroughly after resolution

See the "Files to Monitor" section below for files that commonly have conflicts.

### Step 5: Push Updates

```bash
git push origin halfin-customizations
```

## Making New Customizations

### Workflow

1. **Switch to customizations branch:**
   ```bash
   git checkout halfin-customizations
   ```

2. **Create a feature branch (recommended):**
   ```bash
   git checkout -b feature/my-new-change
   # Make your changes
   git add .
   git commit -m "Description of your change"
   ```

3. **Merge feature branch back:**
   ```bash
   git checkout halfin-customizations
   git merge feature/my-new-change
   git branch -d feature/my-new-change  # Delete feature branch
   ```

4. **Push updates:**
   ```bash
   git push origin halfin-customizations
   ```

## Files to Monitor for Conflicts

These files contain Halfin customizations and are likely to conflict when upstream changes them:

### Core UI Constants
- **`app/src/main/java/com/github/damontecres/wholphin/ui/UiConstants.kt`**
  - Customization: `Cards.height2x3 = 160.dp` (reduced from 180.dp)
  - Watch for: Any changes to card size constants

### Navigation and Layout
- **`app/src/main/java/com/github/damontecres/wholphin/ui/nav/NavDrawer.kt`**
  - Customizations: 
    - Transparent navigation drawer background
    - Dynamic background colors extracted from media backdrops
    - Backdrop image rendering with transparency
  - Watch for: Navigation drawer changes, theme updates, background handling

### Card Components
- **`app/src/main/java/com/github/damontecres/wholphin/ui/cards/EpisodeCard.kt`**
  - Customization: Episode card height set to 100.dp
  - Watch for: Card component refactoring, size changes

- **`app/src/main/java/com/github/damontecres/wholphin/ui/cards/*.kt`** (various card files)
  - Customizations: Transparent backgrounds, custom sizing
  - Watch for: Card component updates

### Page Components
- **`app/src/main/java/com/github/damontecres/wholphin/ui/main/HomePage.kt`**
  - Customizations: Dynamic backgrounds, gradient overlays
  - Watch for: Home page layout changes

- **`app/src/main/java/com/github/damontecres/wholphin/ui/detail/series/SeriesOverviewContent.kt`**
  - Customization: Episode card height (100.dp)
  - Watch for: Series page layout changes

- **`app/src/main/java/com/github/damontecres/wholphin/ui/detail/movie/MovieDetails.kt`**
  - Customizations: Dynamic backgrounds, gradient overlays
  - Watch for: Movie detail page changes

- **`app/src/main/java/com/github/damontecres/wholphin/ui/main/SearchPage.kt`**
  - Customization: Episode card height (100.dp)
  - Watch for: Search page updates

## Conflict Resolution Strategy

### General Principles

1. **Preserve customizations**: Keep your UI customizations (card sizes, transparency, dynamic backgrounds)
2. **Accept upstream fixes**: Take bug fixes and new features from upstream
3. **Test thoroughly**: After resolving conflicts, test the app to ensure everything works
4. **Document conflicts**: Note any particularly tricky conflicts for future reference

### Common Conflict Scenarios

#### Scenario 1: Card Size Changes
**Upstream changes**: Card size constants in `UiConstants.kt`
**Resolution**: Keep your custom size (160.dp) but check if upstream changed the constant name or structure

#### Scenario 2: Navigation Drawer Updates
**Upstream changes**: Navigation drawer layout or functionality
**Resolution**: Manually merge - keep your transparency/background logic, accept upstream functionality changes

#### Scenario 3: Card Component Refactoring
**Upstream changes**: Card components restructured
**Resolution**: Apply your customizations (sizes, transparency) to the new structure

### Conflict Resolution Steps

1. **Identify the conflict:**
   ```bash
   git status  # Shows conflicted files
   ```

2. **Open conflicted files** and look for markers:
   ```
   <<<<<<< HEAD
   Your customizations
   =======
   Upstream changes
   >>>>>>> main
   ```

3. **Resolve manually:**
   - Keep your customizations where appropriate
   - Accept upstream changes for bug fixes/new features
   - Remove conflict markers

4. **Stage resolved files:**
   ```bash
   git add <resolved-file>
   ```

5. **Complete the merge:**
   ```bash
   git commit  # Complete the merge commit
   ```

6. **Test the application** to ensure everything works correctly

## Version Numbering

Halfin uses a version numbering scheme based on upstream releases: `X.Y.Z-P`, where:
- `X.Y.Z` matches the upstream Wholphin release version (e.g., `0.3.2`)
- `P` is a patch number for Halfin-specific releases (e.g., `1`, `2`, `3`)

### Examples
- `0.3.2-1`: First Halfin release based on upstream v0.3.2
- `0.3.2-2`: Second Halfin release (with additional customizations) before next upstream release
- `0.4.0-1`: First Halfin release based on upstream v0.4.0

### Version Tagging

Version numbers are automatically derived from git tags using the `getAppVersion()` function in `app/build.gradle.kts`. The function:
1. First tries to find an exact tag match (e.g., `v0.3.2-1`)
2. If found, returns just the version number without commit hash (e.g., `0.3.2-1`)
3. Otherwise, falls back to descriptive format with commit info

### Creating a Release Tag

After syncing with upstream and committing your customizations:

```bash
# Commit your merge/customizations
git commit -m "Merge upstream v0.3.2 into halfin-customizations"

# Create the version tag (replace X.Y.Z-P with your version)
git tag v0.3.2-1

# The next build will use this version number
```

**Important**: Always tag releases based on upstream release versions. Don't tag unreleased upstream commits - wait for official upstream releases to be fair to the upstream developers.

### Version Number Conflicts

If upstream changes the version numbering system or the `getAppVersion()` function:
1. Check what changed in `app/build.gradle.kts`
2. Preserve the Halfin versioning scheme (`X.Y.Z-P` format)
3. Ensure the function returns clean version numbers (without commit hash) when on a tagged commit
4. Test that `git describe --tags --exact-match --match=v*` works correctly

## Helper Scripts

Two helper scripts are available to automate common operations (these are local-only, not committed):

- **`scripts/sync-upstream.sh`** (Linux/Mac) or **`scripts/sync-upstream.bat`** (Windows)
  - Automates fetching and merging upstream changes
  - See script comments for usage

## Troubleshooting

### Main branch is ahead/behind upstream

If `main` gets out of sync:
```bash
git checkout main
git fetch upstream
git reset --hard upstream/main  # WARNING: This discards local main changes
git push -f origin main  # Force push only if you're sure
```

### Customizations branch diverged

If `halfin-customizations` has issues:
```bash
git checkout halfin-customizations
git log --oneline --graph  # Review history
# Consider creating a backup branch first
git branch halfin-customizations-backup
# Then reset or rebase as needed
```

### Need to start fresh

If you need to completely reset:
```bash
# Backup current customizations
git checkout halfin-customizations
git branch halfin-customizations-backup

# Reset main to upstream
git checkout main
git reset --hard upstream/main

# Recreate customizations from backup
git checkout -b halfin-customizations-new
git cherry-pick <commit-range-from-backup>
```

## Best Practices

1. **Sync regularly**: Don't let too many upstream commits accumulate
2. **Test after each merge**: Catch issues early
3. **Commit conflict resolutions**: Document what you changed and why
4. **Keep main clean**: Only use `main` for syncing with upstream
5. **Use feature branches**: For new customizations, use feature branches
6. **Review upstream changes**: Before merging, review what changed upstream

## Additional Resources

- [Wholphin Repository](https://github.com/damontecres/Wholphin)
- [Git Merge Documentation](https://git-scm.com/docs/git-merge)
- [Resolving Merge Conflicts](https://git-scm.com/book/en/v2/Git-Tools-Advanced-Merging)

