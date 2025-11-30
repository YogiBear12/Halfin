@echo off
REM Sync script for Halfin fork (Windows)
REM This script automates fetching and merging upstream Wholphin changes

echo === Halfin Fork Sync Script ===
echo.

REM Check if we're in a git repository
git rev-parse --git-dir >nul 2>&1
if errorlevel 1 (
    echo Error: Not in a git repository
    exit /b 1
)

REM Check if upstream remote exists
git remote | findstr /C:"upstream" >nul 2>&1
if errorlevel 1 (
    echo Error: Upstream remote not found
    echo Please add it with: git remote add upstream https://github.com/damontecres/Wholphin.git
    exit /b 1
)

REM Save current branch
for /f "tokens=*" %%i in ('git branch --show-current') do set CURRENT_BRANCH=%%i

REM Step 1: Fetch latest from upstream
echo Step 1: Fetching latest from upstream...
git fetch upstream
if errorlevel 1 (
    echo Error: Failed to fetch upstream
    exit /b 1
)
echo [OK] Fetched upstream changes
echo.

REM Step 2: Update main branch
echo Step 2: Updating main branch with upstream changes...
git checkout main
if errorlevel 1 (
    echo Error: Failed to checkout main branch
    exit /b 1
)
git merge upstream/main --no-edit
if errorlevel 1 (
    echo Error: Failed to merge upstream/main into main
    exit /b 1
)
echo [OK] Main branch updated
echo.

REM Step 3: Merge upstream changes into customizations
echo Step 3: Merging upstream changes into halfin-customizations...
git checkout halfin-customizations
if errorlevel 1 (
    echo Error: Failed to checkout halfin-customizations branch
    exit /b 1
)

REM Try to merge (dry run to check for conflicts)
git merge --no-commit --no-ff main >nul 2>&1
if errorlevel 1 (
    echo.
    echo [WARNING] MERGE CONFLICTS DETECTED!
    echo.
    echo The merge has been started but not completed due to conflicts.
    echo Please resolve conflicts manually:
    echo.
    echo 1. Review conflicted files:
    echo    git status
    echo.
    echo 2. Open conflicted files and resolve conflicts
    echo.
    echo 3. After resolving, stage files and complete merge:
    echo    git add ^<resolved-files^>
    echo    git commit
    echo.
    echo 4. Test your application thoroughly
    echo.
    echo See FORK_MAINTENANCE.md for conflict resolution guidance.
    exit /b 1
) else (
    git commit --no-edit
    echo [OK] Successfully merged upstream changes into halfin-customizations
    echo.
)

REM Step 4: Show status
echo Step 4: Current status:
echo.
git log --oneline --graph --decorate -5
echo.

REM Ask if user wants to push
set /p PUSH_CHOICE="Push changes to origin? (y/n): "
if /i "%PUSH_CHOICE%"=="y" (
    echo Pushing main branch...
    git checkout main
    git push origin main
    echo [OK] Main branch pushed
    echo.
    echo Pushing halfin-customizations branch...
    git checkout halfin-customizations
    git push origin halfin-customizations
    echo [OK] Customizations branch pushed
    echo.
)

REM Return to original branch if different
if not "%CURRENT_BRANCH%"=="main" if not "%CURRENT_BRANCH%"=="halfin-customizations" (
    git checkout %CURRENT_BRANCH%
    echo Returned to branch: %CURRENT_BRANCH%
)

echo.
echo === Sync Complete ===

