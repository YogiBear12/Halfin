# Backdrop Disable Guide

This document explains how to disable backdrop images and dynamic color extraction on specific pages where they shouldn't be shown (e.g., library view, settings, etc.).

## Overview

The backdrop system in Halfin uses:
- `BackdropService` - Manages backdrop state and color extraction
- `LocalBackdropHandler` - A composition local that allows pages to set backdrop URLs
- `ApplicationContentViewModel.clearBackdrop()` - Clears the backdrop

## Methods to Disable Backdrop

### Method 1: Using `LocalBackdropHandler` (Recommended for detail pages)

For pages that have access to `LocalBackdropHandler`, you can clear the backdrop by setting it to `null`:

```kotlin
import com.github.damontecres.wholphin.ui.nav.LocalBackdropHandler

@Composable
fun YourPage() {
    val onBackdropChange = LocalBackdropHandler.current
    
    // Clear backdrop when this page is displayed
    LaunchedEffect(Unit) {
        onBackdropChange(null)
    }
    
    // Your page content...
}
```

**Example locations where this is used:**
- `CollectionFolderTv.kt` - Library view for TV shows
- `CollectionFolderMovie.kt` - Library view for movies
- `CollectionFolderGeneric.kt` - Generic collection folders

### Method 2: Using `onClearBackdrop` callback (For full-screen pages)

For pages that are marked as `fullScreen` in the `Destination`, the backdrop is automatically cleared in `DestinationContent.kt`:

```kotlin
// In DestinationContent.kt
if (destination.fullScreen) {
    LaunchedEffect(Unit) { onClearBackdrop.invoke() }
}
```

**Pages that use this:**
- Playback pages (`Destination.Playback`, `Destination.PlaybackList`)
- Settings pages (`Destination.Settings`)
- Server/User selection pages (`Destination.ServerList`, `Destination.UserList`)

### Method 3: Not calling `updateBackdrop` in grid views

For library/grid views that use `CollectionFolderGrid`, the backdrop is currently updated when items are focused via the `updateBackdrop` function in the ViewModel:

```kotlin
// In CollectionFolderGrid.kt ViewModel
fun updateBackdrop(item: BaseItem) {
    viewModelScope.launchIO {
        backdropService.submit(item)
    }
}
```

**To disable backdrop in library view:**

1. **Option A**: Remove or comment out the `updateBackdrop` call in `CollectionFolderGrid.kt`:
   ```kotlin
   // In CollectionFolderGrid composable, find where updateBackdrop is called
   // and either remove it or add a condition to skip it for library views
   ```

2. **Option B**: Add a `LaunchedEffect` in `CollectionFolderTv.kt` and `CollectionFolderMovie.kt` to clear backdrop:
   ```kotlin
   import com.github.damontecres.wholphin.ui.nav.LocalBackdropHandler
   
   @Composable
   fun CollectionFolderTv(...) {
       val onBackdropChange = LocalBackdropHandler.current
       
       LaunchedEffect(Unit) {
           onBackdropChange(null) // Clear backdrop on library view
       }
       
       // Rest of the composable...
   }
   ```

## Current Implementation

### Pages that SHOULD show backdrop:
- Home page (`HomePage.kt`) - Shows backdrop for focused items
- Movie details (`MovieDetails.kt`) - Shows movie backdrop
- Series details (`SeriesDetails.kt`) - Shows series backdrop
- Episode details (`EpisodeDetails.kt`) - Shows episode/series backdrop
- Person pages (`PersonPage.kt`) - Shows person backdrop

### Pages that should NOT show backdrop (currently may need fixes):
- Library view (`CollectionFolderTv.kt`, `CollectionFolderMovie.kt`) - Grid of items, no backdrop needed
- Settings pages (`PreferencesPage.kt`) - Already handled via `fullScreen` flag
- Playback pages (`PlaybackPage.kt`) - Already handled via `fullScreen` flag
- Server/User selection - Already handled via `fullScreen` flag

## Implementation Notes

1. **BackdropService.submit()**: This function extracts colors and sets the backdrop. If you don't want backdrop on a page, ensure this isn't called for that page.

2. **LocalBackdropHandler**: This is provided by `NavDrawer.kt` and allows any composable in the composition tree to update the backdrop URL. Setting it to `null` clears the backdrop.

3. **BackdropStyle preference**: Users can disable backdrop entirely via Settings → Interface → Backdrop Display:
   - `BACKDROP_DYNAMIC_COLOR` - Shows backdrop with color extraction (default)
   - `BACKDROP_IMAGE_ONLY` - Shows backdrop image only
   - `BACKDROP_NONE` - No backdrop

## Recommended Changes

To properly disable backdrop on library view pages:

1. Add `LocalBackdropHandler.current(null)` in `LaunchedEffect` for:
   - `CollectionFolderTv.kt` (Library tab)
   - `CollectionFolderMovie.kt` (Library tab)
   - Any other grid/list views where backdrop shouldn't appear

2. Ensure `CollectionFolderGrid` doesn't call `updateBackdrop` for library views, or add a parameter to control this behavior.

3. Test that backdrop clears when navigating to library view and reappears when navigating to detail pages.

