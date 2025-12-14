# Color Extraction and Backdrop System Documentation

## Overview

Halfin uses a sophisticated color extraction system that dynamically extracts color palettes from media backdrop images and applies them to create a Plex-like visual experience. The system uses Android's Palette API to analyze images and extract colors that are then rendered as radial gradients in four screen quadrants, creating a dynamic background that complements the backdrop image.

---

## Table of Contents

1. [Color Extraction Logic](#color-extraction-logic)
2. [Palette Selection for Quadrants](#palette-selection-for-quadrants)
3. [Backdrop Fading and Transitions](#backdrop-fading-and-transitions)
4. [Performance Optimizations](#performance-optimizations)
5. [Implementation Details](#implementation-details)

---

## Color Extraction Logic

### File Location
**File**: `app/src/main/java/com/github/damontecres/wholphin/util/ColorExtractor.kt`

### Main Function: `extractColorsFromBackdrop()`

The main entry point for color extraction is the `extractColorsFromBackdrop()` suspend function:

```kotlin
suspend fun extractColorsFromBackdrop(
    imageUrl: String?,
    context: Context
): ExtractedColors?
```

#### Process Flow

1. **Input Validation**: Returns `null` if the image URL is blank or null
2. **Cache Check**: Checks LRU cache first (cache size: 50 entries) to avoid re-extraction
3. **Image Loading**: 
   - Uses Coil ImageLoader to load the image
   - Configures bitmap as `ARGB_8888` format
   - Disables hardware acceleration (`allowHardware(false)`) to ensure accurate color extraction
   - **Uses full resolution bitmap** - scaling changes color extraction results
4. **Color Extraction**: Calls `extractColorsFromBitmap()` to analyze the bitmap
5. **Caching**: Caches the result for future use
6. **Error Handling**: Returns `null` on any failure, with error logging

#### Key Features

- **Runs on IO Dispatcher**: All image loading and processing happens off the main thread
- **LRU Cache**: Prevents re-extraction for the same image URL (50 entry cache)
- **Full Resolution**: Uses full resolution bitmaps to ensure accurate color extraction
- **Non-blocking**: Returns immediately if colors are cached

---

## Palette Selection for Quadrants

### Screen Quadrant Layout

The screen is divided into four quadrants, each using a different extracted color:

```
┌─────────────────┬─────────────────┐
│                 │                 │
│   Top-Left      │   Top-Right     │
│   (Secondary)   │   (Tertiary)    │
│                 │                 │
├─────────────────┼─────────────────┤
│                 │                 │
│   Bottom-Left   │   Bottom-Right  │
│   (Base)        │   (Primary)     │
│                 │                 │
└─────────────────┴─────────────────┘
```

### Android Palette Swatches

The system uses Android's Palette API which provides the following swatches:

- **Vibrant**: Bright, saturated colors
- **Dark Vibrant**: Darker, saturated colors
- **Light Vibrant**: Lighter, saturated colors
- **Muted**: Less saturated, more subdued colors
- **Dark Muted**: Darker, less saturated colors
- **Light Muted**: Lighter, less saturated colors
- **Dominant**: The most common color in the image

### Color Selection Logic

The `extractColorsFromBitmap()` function implements smart color selection:

#### 1. Primary Color (Bottom-Right Quadrant)

**Location**: Bottom-right corner of the screen  
**Selection Priority**:
1. `darkVibrant` (preferred)
2. `darkMuted` (fallback)
3. `Color.Transparent` (default if neither available)

**Alpha**: 0.4 (40% opacity)

**Code**:
```kotlin
val primaryColor = darkVibrant?.rgb
    ?: darkMuted?.rgb
    ?: AndroidColor.TRANSPARENT
```

**Rationale**: Bottom-right uses darker colors to create depth and contrast with the backdrop image, which is typically positioned in the top-right area.

#### 2. Secondary Color (Top-Left Quadrant)

**Location**: Top-left corner of the screen  
**Selection Priority**:
1. `vibrant` if it's a "cool" color (blue/purple/green)
2. `muted` if it's a "cool" color and vibrant is warm
3. `vibrant` (fallback)
4. `muted` (fallback)
5. `Color.Transparent` (default)

**Alpha**: 0.4 (40% opacity)

**Code**:
```kotlin
val secondaryColor = when {
    vibrant != null && isCoolColor(vibrant.rgb) -> vibrant.rgb
    muted != null && isCoolColor(muted.rgb) -> muted.rgb
    vibrant != null -> vibrant.rgb
    muted != null -> muted.rgb
    else -> AndroidColor.TRANSPARENT
}
```

**Cool Color Detection**:
```kotlin
fun isCoolColor(rgb: Int): Boolean {
    val r = (rgb shr 16) and 0xFF
    val g = (rgb shr 8) and 0xFF
    val b = rgb and 0xFF
    // Cool colors have more blue/green than red
    return b > r && (b + g) > (r * 1.5f)
}
```

**Rationale**: Top-left prefers cool colors (blue/purple/green) to create a complementary color scheme. This ensures visual balance when the backdrop image contains warm tones.

#### 3. Tertiary Color (Top-Right Quadrant)

**Location**: Top-right corner (under the backdrop image)  
**Selection Priority**:
1. `vibrant` (preferred)
2. `lightVibrant` (fallback)
3. `Color.Transparent` (default)

**Alpha**: 0.35 (35% opacity - slightly more transparent)

**Code**:
```kotlin
val tertiaryColor = vibrant?.rgb
    ?: lightVibrant?.rgb
    ?: AndroidColor.TRANSPARENT
```

**Rationale**: Top-right uses bright, vibrant colors to complement the backdrop image which is positioned in this area. The slightly lower opacity (35% vs 40%) ensures the backdrop image remains prominent.

#### 4. Bottom-Left Quadrant

**Location**: Bottom-left corner  
**Color**: Uses the base background color (`MaterialTheme.colorScheme.background`)  
**Purpose**: Creates a bridge between the top-left and bottom-right quadrants, maintaining visual continuity.

---

## Backdrop Fading and Transitions

### Backdrop Image Rendering

**File**: `app/src/main/java/com/github/damontecres/wholphin/ui/nav/NavDrawer.kt`

### Backdrop Image Properties

- **Position**: Top-right corner (`Alignment.TopEnd`)
- **Size**: 70% of screen width (`fillMaxWidth(0.7f)`)
- **Aspect Ratio**: 16:9 (1.77:1)
- **Content Scale**: `ContentScale.Crop` (crops to fit while maintaining aspect ratio)

### Fade-In Transition

**Duration**: 800 milliseconds  
**Method**: Coil's built-in `CrossFadeFactory`

```kotlin
.transitionFactory(CrossFadeFactory(800.milliseconds))
```

The backdrop image fades in smoothly over 800ms when a new image is loaded.

### Dynamic Opacity

The backdrop opacity changes based on the navigation drawer state:

- **Drawer Closed**: 95% opacity (`alpha = 0.95f`)
- **Drawer Open**: 70% opacity (`alpha = 0.7f`)

**Animation**: 300ms transition using `animateFloatAsState`

```kotlin
val backdropAlpha by animateFloatAsState(
    targetValue = if (drawerState.isOpen) 0.7f else 0.95f,
    animationSpec = tween(300),
    label = "backdropAlpha"
)
```

### Image Masking (Gradient Fades)

The backdrop image uses two gradient masks to create smooth fade effects:

#### 1. Left Fade (Horizontal Gradient)

**Purpose**: Fades the left edge of the image to transparent, blending with the background  
**Gradient**: `Color.Transparent` → `Color.Black`  
**Range**: From left edge (0) to 60% of image width  
**Blend Mode**: `BlendMode.DstIn` (masks the image)

```kotlin
drawRect(
    brush = Brush.horizontalGradient(
        colors = listOf(Color.Transparent, Color.Black),
        startX = 0f,
        endX = size.width * 0.6f
    ),
    blendMode = BlendMode.DstIn
)
```

#### 2. Bottom Fade (Vertical Gradient)

**Purpose**: Fades the bottom edge of the image to transparent  
**Gradient**: `Color.Black` → `Color.Transparent`  
**Range**: From top (0) to bottom (full height)  
**Blend Mode**: `BlendMode.DstIn` (masks the image)

```kotlin
drawRect(
    brush = Brush.verticalGradient(
        colors = listOf(Color.Black, Color.Transparent),
        startY = 0f,
        endY = size.height
    ),
    blendMode = BlendMode.DstIn
)
```

### Color Gradient Rendering

The extracted colors are rendered as radial gradients in each quadrant:

#### Radial Gradient Properties

- **Center**: Corner of the quadrant
- **Radius**: 80% of screen width (`size.width * 0.8f`)
- **Colors**: Extracted color → `Color.Transparent`
- **Animation**: 1250ms smooth transition using `animateColorAsState`

#### Quadrant Gradients

1. **Top-Left (Secondary)**:
   ```kotlin
   center = Offset(0f, 0f)
   colors = listOf(animSecondary, Color.Transparent)
   ```

2. **Bottom-Right (Primary)**:
   ```kotlin
   center = Offset(size.width, size.height)
   colors = listOf(animPrimary, Color.Transparent)
   ```

3. **Top-Right (Tertiary)**:
   ```kotlin
   center = Offset(size.width, 0f)
   colors = listOf(animTertiary, Color.Transparent)
   ```

4. **Bottom-Left (Base Background)**:
   ```kotlin
   center = Offset(0f, size.height)
   colors = listOf(baseBackgroundColor, Color.Transparent)
   ```

### Color Transition Timing

**Color Animation Duration**: 1250ms  
**Method**: `animateColorAsState` with `tween(1250)`

```kotlin
val animPrimary by animateColorAsState(
    targetPrimary,
    animationSpec = tween(1250),
    label = "primary"
)
```

This creates smooth color transitions when navigating between different media items.

---

## Performance Optimizations

### 1. LRU Cache

- **Size**: 50 entries
- **Key**: Image URL
- **Value**: `ExtractedColors` (primary, secondary, tertiary)
- **Benefit**: Prevents re-extraction for recently viewed images

### 2. Debouncing

**Debounce Delay**: 400ms  
**Purpose**: Waits for navigation to stop before extracting colors

```kotlin
LaunchedEffect(backdropImageUrl) {
    debounceJob?.cancel()
    debounceJob = scope.launch {
        delay(400) // Wait for navigation to stop
        if (backdropImageUrl == currentUrl) {
            stableBackdropUrl = backdropImageUrl
        }
    }
}
```

This prevents unnecessary color extractions during rapid navigation.

### 3. IO Dispatcher

All image loading and color extraction runs on `Dispatchers.IO`, preventing main thread blocking.

### 4. Color Persistence

Colors remain visible during extraction and navigation:
- Previous colors stay visible while new colors are being extracted
- Colors only update if the backdrop URL hasn't changed during extraction
- Prevents flickering or black screens during transitions

### 5. Delayed Color Reset

When navigating away from items:
- **Delay**: 800ms
- **Purpose**: Allows backdrop fade-out to complete before resetting colors
- **Result**: Smooth transition back to default colors

```kotlin
if (currentStableUrl == null && currentBackdropUrl == null) {
    delay(800) // Wait for backdrop fade-out
    if (backdropImageUrl == null) {
        // Reset to default colors
    }
}
```

---

## Implementation Details

### Data Structure

```kotlin
data class ExtractedColors(
    val primary: Color,    // Bottom-right quadrant
    val secondary: Color,  // Top-left quadrant
    val tertiary: Color    // Top-right quadrant
)
```

### Color Extraction Coordination

The system coordinates color extraction with backdrop loading:

1. **Backdrop URL Changes**: Debounced (400ms) to detect stable navigation
2. **Color Extraction**: Triggered when stable URL is set
3. **Color Update**: Only updates if backdrop URL hasn't changed during extraction
4. **Color Reset**: Delayed (800ms) to allow backdrop fade-out

### State Management

Key state variables in `NavDrawer.kt`:

- `stableBackdropUrl`: Debounced backdrop URL (400ms delay)
- `backdropImageUrl`: Current backdrop URL
- `dynamicColorPrimary`: Extracted primary color (bottom-right)
- `dynamicColorSecondary`: Extracted secondary color (top-left)
- `dynamicColorTertiary`: Extracted tertiary color (top-right)
- `backdropLoaded`: Tracks if backdrop image has loaded
- `backdropReadyForColors`: Coordinates color extraction timing

### Default Colors

When no backdrop is available or extraction fails, the system uses generic fallback colors:

```kotlin
val genericPrimary = Color(0xFF1A1A2E).copy(alpha = 0.4f)
val genericSecondary = Color(0xFF16213E).copy(alpha = 0.4f)
val genericTertiary = Color(0xFF0F3460).copy(alpha = 0.35f)
```

### Cache Management

The color cache can be cleared programmatically:

```kotlin
fun clearColorCache() {
    colorCache.evictAll()
    Timber.d("ColorExtractor: Cache cleared")
}
```

---

## Summary

The Halfin color extraction and backdrop system creates a dynamic, Plex-like visual experience by:

1. **Extracting colors** from media backdrop images using Android's Palette API
2. **Selecting appropriate colors** for each screen quadrant based on color temperature and saturation
3. **Rendering radial gradients** in each quadrant with smooth 1250ms transitions
4. **Displaying backdrop images** with 800ms fade-in transitions and gradient masks
5. **Optimizing performance** through caching, debouncing, and async processing

The result is a visually rich, responsive interface that adapts to the content being viewed, creating an immersive viewing experience similar to Plex.

