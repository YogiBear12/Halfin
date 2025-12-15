package com.github.damontecres.wholphin.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import androidx.core.graphics.drawable.toBitmap
import coil3.ImageLoader
import coil3.asDrawable
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.request.bitmapConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

/**
 * Data class to hold the three extracted colors for dynamic background rendering
 */
data class ExtractedColors(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color
)

/**
 * LRU cache for extracted colors, keyed by image URL.
 * Cache size of 50 should be sufficient for typical navigation patterns.
 */
private val colorCache = LruCache<String, ExtractedColors>(50)

/**
 * Extracts colors from a backdrop image URL for use in dynamic background rendering.
 * 
 * Performance optimizations:
 * - Uses LRU cache to avoid re-extracting colors for the same image
 * - Runs on IO dispatcher to avoid blocking the main thread
 * 
 * Note: Uses full resolution bitmap to match original behavior. Scaling changes color extraction results.
 * 
 * @param imageUrl The URL of the backdrop image
 * @param context The Android context
 * @return ExtractedColors containing primary, secondary, and tertiary colors, or null if extraction fails
 */
suspend fun extractColorsFromBackdrop(
    imageUrl: String?,
    context: Context
): ExtractedColors? = withContext(Dispatchers.IO) {
    if (imageUrl.isNullOrBlank()) {
        return@withContext null
    }

    // Check cache first
    colorCache.get(imageUrl)?.let {
        Timber.d("ColorExtractor: Using cached colors for $imageUrl")
        return@withContext it
    }

    try {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl)
            .allowHardware(false)
            .bitmapConfig(Bitmap.Config.ARGB_8888)
            .build()

        val result = loader.execute(request)
        if (result is SuccessResult) {
            val drawable = result.image.asDrawable(context.resources)
            val bitmap = drawable.toBitmap(config = Bitmap.Config.ARGB_8888)
            
            // Use full resolution bitmap to match original behavior exactly
            // Scaling changes which colors are dominant and affects extraction results
            val colors = extractColorsFromBitmap(bitmap)
            
            // Cache the result
            if (colors != null) {
                colorCache.put(imageUrl, colors)
                Timber.d("ColorExtractor: Extracted and cached colors for $imageUrl")
            }
            
            colors
        } else {
            Timber.w("ColorExtractor: Failed to load image from $imageUrl")
            null
        }
    } catch (e: Exception) {
        Timber.e(e, "ColorExtractor: Error extracting colors from URL: $imageUrl")
        null
    }
}

/**
 * Extracts colors from a bitmap using Android's Palette API.
 * 
 * Uses Halfin's smart color selection logic:
 * - Primary (Bottom-Right): darkVibrant -> darkMuted -> default
 * - Secondary (Top-Left): Smart selection based on color temperature (prefers cool colors)
 * - Tertiary (Top-Right): vibrant -> lightVibrant -> default
 * 
 * @param bitmap The bitmap to extract colors from
 * @return ExtractedColors containing primary, secondary, and tertiary colors
 */
private fun extractColorsFromBitmap(bitmap: Bitmap): ExtractedColors? {
    return try {
        // Use default palette generation (no maximumColorCount limit) to match original behavior
        // This ensures we get the same color extraction results as before
        val palette = Palette.from(bitmap).generate()

        val vibrant = palette.vibrantSwatch
        val darkVibrant = palette.darkVibrantSwatch
        val lightVibrant = palette.lightVibrantSwatch
        val muted = palette.mutedSwatch
        val darkMuted = palette.darkMutedSwatch
        val lightMuted = palette.lightMutedSwatch
        val dominant = palette.dominantSwatch

        // Smart color selection: Choose colors based on color temperature and diversity
        // Helper function to determine if a color is "cool" (blue/purple/green) vs "warm" (red/orange/yellow)
        fun isCoolColor(rgb: Int): Boolean {
            val r = (rgb shr 16) and 0xFF
            val g = (rgb shr 8) and 0xFF
            val b = rgb and 0xFF
            // Cool colors have more blue/green than red
            return b > r && (b + g) > (r * 1.5f)
        }

        // Primary (Bottom-Right): darkVibrant -> darkMuted -> default
        val primaryColor = darkVibrant?.rgb
            ?: darkMuted?.rgb
            ?: AndroidColor.TRANSPARENT

        // Secondary (Top-Left): Smart selection based on color properties
        // If Vibrant is cool (blue/purple), use it. If Vibrant is warm (yellow/orange) and Muted is cool, use Muted.
        // This ensures we get cool tones (blue/purple) for top-left when available
        val secondaryColor = when {
            vibrant != null && isCoolColor(vibrant.rgb) -> vibrant.rgb // Vibrant is blue/purple - use it
            muted != null && isCoolColor(muted.rgb) -> muted.rgb // Muted is blue/purple - use it
            vibrant != null -> vibrant.rgb // Fallback to vibrant
            muted != null -> muted.rgb // Fallback to muted
            else -> AndroidColor.TRANSPARENT
        }

        // Tertiary (Top-Right under image): vibrant -> lightVibrant -> default
        val tertiaryColor = vibrant?.rgb
            ?: lightVibrant?.rgb
            ?: AndroidColor.TRANSPARENT

        // Apply alpha dimming (like Gemini) instead of RGB darkening for more natural look
        // Using lower alpha values to match Plex's subdued appearance
        ExtractedColors(
            primary = if (primaryColor != AndroidColor.TRANSPARENT) {
                Color(primaryColor).copy(alpha = 0.4f) // 40% opacity for bottom-right
            } else {
                Color.Transparent
            },
            secondary = if (secondaryColor != AndroidColor.TRANSPARENT) {
                Color(secondaryColor).copy(alpha = 0.4f) // 40% opacity for top-left
            } else {
                Color.Transparent
            },
            tertiary = if (tertiaryColor != AndroidColor.TRANSPARENT) {
                Color(tertiaryColor).copy(alpha = 0.35f) // 35% opacity for top-right
            } else {
                Color.Transparent
            }
        ).also {
            Timber.d(
                "ColorExtractor: Primary=%X (alpha=0.4), Secondary=%X (alpha=0.4), Tertiary=%X (alpha=0.35)",
                primaryColor, secondaryColor, tertiaryColor
            )
            Timber.d(
                "ColorExtractor: Palette: Vibrant=%X, DarkVibrant=%X, LightVibrant=%X, Muted=%X, DarkMuted=%X, LightMuted=%X, Dominant=%X",
                vibrant?.rgb, darkVibrant?.rgb, lightVibrant?.rgb, muted?.rgb, darkMuted?.rgb, lightMuted?.rgb, dominant?.rgb
            )
        }
    } catch (e: Exception) {
        Timber.e(e, "ColorExtractor: Error extracting palette colors")
        null
    }
}

/**
 * Clears the color cache. Useful for memory management or testing.
 */
fun clearColorCache() {
    colorCache.evictAll()
    Timber.d("ColorExtractor: Cache cleared")
}

