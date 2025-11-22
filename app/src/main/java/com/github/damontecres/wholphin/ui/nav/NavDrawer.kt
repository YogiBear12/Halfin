package com.github.damontecres.wholphin.ui.nav

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.alpha
import androidx.palette.graphics.Palette
import androidx.core.graphics.drawable.toBitmap
import android.graphics.Bitmap
import coil3.asDrawable
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.bitmapConfig
import coil3.request.transitionFactory
import coil3.transition.Transition
import com.github.damontecres.wholphin.ui.CrossFadeFactory
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import androidx.tv.material3.DrawerState
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.Icon
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.NavigationDrawerItemDefaults
import androidx.tv.material3.NavigationDrawerScope
import androidx.tv.material3.ProvideTextStyle
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import androidx.tv.material3.surfaceColorAtElevation
import com.github.damontecres.wholphin.R
import com.github.damontecres.wholphin.data.NavDrawerItemRepository
import com.github.damontecres.wholphin.data.model.JellyfinServer
import com.github.damontecres.wholphin.data.model.JellyfinUser
import com.github.damontecres.wholphin.preferences.AppThemeColors
import com.github.damontecres.wholphin.preferences.UserPreferences
import com.github.damontecres.wholphin.services.NavigationManager
import com.github.damontecres.wholphin.ui.FontAwesome
import com.github.damontecres.wholphin.ui.TimeFormatter
import com.github.damontecres.wholphin.ui.ifElse
import com.github.damontecres.wholphin.ui.launchIO
import com.github.damontecres.wholphin.ui.preferences.PreferenceScreenOption
import com.github.damontecres.wholphin.ui.setValueOnMain
import com.github.damontecres.wholphin.ui.spacedByWithFooter
import com.github.damontecres.wholphin.ui.toServerString
import com.github.damontecres.wholphin.ui.tryRequestFocus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.api.CollectionType
import org.jellyfin.sdk.model.api.DeviceProfile
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import androidx.compose.runtime.CompositionLocalProvider
import kotlin.time.Duration.Companion.milliseconds
import androidx.compose.ui.draw.drawBehind

@HiltViewModel
class NavDrawerViewModel
    @Inject
    constructor(
        private val navDrawerItemRepository: NavDrawerItemRepository,
        val navigationManager: NavigationManager,
    ) : ViewModel() {
        private var all: List<NavDrawerItem>? = null
        val moreLibraries = MutableLiveData<List<NavDrawerItem>>(null)
        val libraries = MutableLiveData<List<NavDrawerItem>>(listOf())
        val selectedIndex = MutableLiveData(-1)
        val showMore = MutableLiveData(false)

        fun init() {
            viewModelScope.launchIO {
                val all = all ?: navDrawerItemRepository.getNavDrawerItems()
                this@NavDrawerViewModel.all = all
                val libraries = navDrawerItemRepository.getFilteredNavDrawerItems(all)
                val moreLibraries = all.toMutableList().apply { removeAll(libraries) }

                withContext(Dispatchers.Main) {
                    this@NavDrawerViewModel.moreLibraries.value = moreLibraries
                    this@NavDrawerViewModel.libraries.value = libraries
                }
                val asDestinations =
                    (libraries + listOf(NavDrawerItem.More) + moreLibraries).map {
                        if (it is ServerNavDrawerItem) {
                            it.destination
                        } else if (it is NavDrawerItem.Favorites) {
                            Destination.Favorites
                        } else {
                            null
                        }
                    }

                val backstack = navigationManager.backStack.toList().reversed()
                for (i in 0..<backstack.size) {
                    val key = backstack[i]
                    if (key is Destination) {
                        val index =
                            if (key is Destination.Home) {
                                -1
                            } else if (key is Destination.Search) {
                                -2
                            } else {
                                val idx = asDestinations.indexOf(key)
                                if (idx >= 0) {
                                    idx
                                } else {
                                    null
                                }
                            }
//                        Timber.v("Found $index => $key")
                        if (index != null) {
                            selectedIndex.setValueOnMain(index)
                            break
                        }
                    }
                }
            }
        }

        fun setIndex(index: Int) {
            selectedIndex.value = index
        }

        fun setShowMore(value: Boolean) {
            showMore.value = value
        }
    }

sealed interface NavDrawerItem {
    val id: String

    fun name(context: Context): String

    object Favorites : NavDrawerItem {
        override val id: String
            get() = "a_favorites"

        override fun name(context: Context): String = context.getString(R.string.favorites)
    }

    object More : NavDrawerItem {
        override val id: String
            get() = "a_more"

        override fun name(context: Context): String = context.getString(R.string.more)
    }
}

data class ServerNavDrawerItem(
    val itemId: UUID,
    val name: String,
    val destination: Destination,
    val type: CollectionType,
) : NavDrawerItem {
    override val id: String = "s_" + itemId.toServerString()

    override fun name(context: Context): String = name
}

/**
 * Display the left side navigation drawer with [DestinationContent] on the right
 */
@Composable
fun NavDrawer(
    destination: Destination,
    preferences: UserPreferences,
    user: JellyfinUser,
    server: JellyfinServer,
    deviceProfile: DeviceProfile,
    modifier: Modifier = Modifier,
    viewModel: NavDrawerViewModel =
        hiltViewModel(
            LocalView.current.findViewTreeViewModelStoreOwner()!!,
            key = "${server?.id}_${user?.id}", // Keyed to the server & user to ensure its reset when switching either
        ),
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val focusRequester = remember { FocusRequester() }
    val searchFocusRequester = remember { FocusRequester() }

    // If the user presses back while on the home page, open the nav drawer, another back press will quit the app
    BackHandler(enabled = (drawerState.currentValue == DrawerValue.Closed && destination is Destination.Home)) {
        drawerState.setValue(DrawerValue.Open)
        focusRequester.requestFocus()
    }
    val moreLibraries by viewModel.moreLibraries.observeAsState(listOf())
    val libraries by viewModel.libraries.observeAsState(listOf())
    LaunchedEffect(Unit) { viewModel.init() }

    val showMore by viewModel.showMore.observeAsState(false)
//    val libraries = if (showPinnedOnly) pinnedLibraries else allLibraries
    // A negative index is a built in page, >=0 is a library
    val selectedIndex by viewModel.selectedIndex.observeAsState(-1)
    var focusedIndex by remember { mutableIntStateOf(Int.MIN_VALUE) }
    val derivedFocusedIndex by remember { derivedStateOf { focusedIndex } }

    fun setShowMore(value: Boolean) {
        viewModel.setShowMore(value)
    }

    BackHandler(enabled = showMore && drawerState.currentValue == DrawerValue.Open) {
        setShowMore(false)
    }

    val onClick = { index: Int, item: NavDrawerItem ->
        when (item) {
            NavDrawerItem.Favorites -> {
                viewModel.setIndex(index)
                viewModel.navigationManager.navigateToFromDrawer(
                    Destination.Favorites,
                )
            }

            NavDrawerItem.More -> {
                setShowMore(!showMore)
            }

            is ServerNavDrawerItem -> {
                viewModel.setIndex(index)
                viewModel.navigationManager.navigateToFromDrawer(item.destination)
            }
        }
    }
    // Temporarily disabled, see https://github.com/damontecres/Wholphin/pull/127#issuecomment-3478058418
    if (false && preferences.appPreferences.interfacePreferences.navDrawerSwitchOnFocus) {
        LaunchedEffect(derivedFocusedIndex) {
            val index = derivedFocusedIndex
            delay(600)
            if (index != selectedIndex) {
                if (index == -1) {
                    viewModel.setIndex(-1)
                    viewModel.navigationManager.goToHome()
                } else if (index in libraries.indices) {
                    if (moreLibraries.isEmpty() || index != libraries.lastIndex) {
                        libraries.getOrNull(index)?.let {
                            onClick.invoke(index, it)
                        }
                    }
                } else {
                    val newIndex = libraries.size - index + 1
                    if (newIndex in moreLibraries.indices) {
                        moreLibraries.getOrNull(newIndex)?.let {
                            onClick.invoke(index, it)
                        }
                    }
                }
            }
        }
    }

    val drawerWidth by animateDpAsState(if (drawerState.isOpen) 260.dp else 40.dp)
    val drawerPadding by animateDpAsState(if (drawerState.isOpen) 0.dp else 8.dp)
    val appTheme = preferences.appPreferences.interfacePreferences.appThemeColors
    val isNanifin = appTheme == AppThemeColors.NANIFIN
    
    // Backdrop state lifted to NavDrawer
    var backdropImageUrl by remember { mutableStateOf<String?>(null) }
    var dynamicColorPrimary by remember { mutableStateOf(Color.Transparent) }
    var dynamicColorSecondary by remember { mutableStateOf(Color.Transparent) }
    var dynamicColorTertiary by remember { mutableStateOf(Color.Transparent) }

    if (isNanifin) {
        LaunchedEffect(backdropImageUrl) {
            // Don't reset colors immediately - keep previous colors visible during loading
            // Only update colors after new backdrop is loaded and colors are extracted
            if (backdropImageUrl != null) {
                val loader = coil3.ImageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(backdropImageUrl)
                    .allowHardware(false)
                    .bitmapConfig(Bitmap.Config.ARGB_8888)
                    .build()
                val result = loader.execute(request)
                if (result is coil3.request.SuccessResult) {
                    val drawable = result.image.asDrawable(context.resources)
                    val bitmap = drawable.toBitmap(config = Bitmap.Config.ARGB_8888)
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
                        ?: android.graphics.Color.TRANSPARENT
                    
                    // Secondary (Top-Left): Smart selection based on color properties
                    // If Vibrant is cool (blue/purple), use it. If Vibrant is warm (yellow/orange) and Muted is cool, use Muted.
                    // This ensures we get cool tones (blue/purple) for top-left when available
                    val secondaryColor = when {
                        vibrant != null && isCoolColor(vibrant.rgb) -> vibrant.rgb // Vibrant is blue/purple - use it
                        muted != null && isCoolColor(muted.rgb) -> muted.rgb // Muted is blue/purple - use it
                        vibrant != null -> vibrant.rgb // Fallback to vibrant
                        muted != null -> muted.rgb // Fallback to muted
                        else -> android.graphics.Color.TRANSPARENT
                    }
                    
                    // Tertiary (Top-Right under image): vibrant -> lightVibrant -> default
                    val tertiaryColor = vibrant?.rgb 
                        ?: lightVibrant?.rgb 
                        ?: android.graphics.Color.TRANSPARENT
                    
                    // Apply alpha dimming (like Gemini) instead of RGB darkening for more natural look
                    // Using lower alpha values to match Plex's subdued appearance
                    // animateColorAsState will handle the smooth transition from previous colors
                    dynamicColorPrimary = if (primaryColor != android.graphics.Color.TRANSPARENT) {
                        Color(primaryColor).copy(alpha = 0.4f) // 40% opacity for bottom-right
                    } else {
                        Color.Transparent
                    }
                    dynamicColorSecondary = if (secondaryColor != android.graphics.Color.TRANSPARENT) {
                        Color(secondaryColor).copy(alpha = 0.4f) // 40% opacity for top-left
                    } else {
                        Color.Transparent
                    }
                    dynamicColorTertiary = if (tertiaryColor != android.graphics.Color.TRANSPARENT) {
                        Color(tertiaryColor).copy(alpha = 0.35f) // 35% opacity for top-right
                    } else {
                        Color.Transparent
                    }
                    
                    timber.log.Timber.d("Color Extraction: Primary=%X (alpha=0.4), Secondary=%X (alpha=0.4), Tertiary=%X (alpha=0.35)", 
                        primaryColor, secondaryColor, tertiaryColor)
                    timber.log.Timber.d("Palette: Vibrant=%X, DarkVibrant=%X, LightVibrant=%X, Muted=%X, DarkMuted=%X, LightMuted=%X, Dominant=%X", 
                        vibrant?.rgb, darkVibrant?.rgb, lightVibrant?.rgb, muted?.rgb, darkMuted?.rgb, lightMuted?.rgb, dominant?.rgb)
                }
            }
            // Note: We don't reset colors to Transparent when backdropImageUrl becomes null
            // This keeps the previous colors visible, matching Plex's behavior
        }
    }

    val drawerBackground by animateColorAsState(
        if (isNanifin) {
            if (drawerState.isOpen) {
                Color.Black.copy(alpha = 0.8f)
            } else {
                Color.Transparent
            }
        } else if (drawerState.isOpen) {
            MaterialTheme.colorScheme.surfaceColorAtElevation(
                1.dp,
            )
        } else {
            MaterialTheme.colorScheme.surface
        },
    )
    val spacedBy = 4.dp

    Box(modifier = modifier.fillMaxSize()) {
        // Background Rendering (Behind content, and extending behind drawer)
        if (isNanifin) {
            val baseBackgroundColor = MaterialTheme.colorScheme.background

            val targetPrimary = if (dynamicColorPrimary != Color.Transparent) dynamicColorPrimary else Color.Transparent
            val targetSecondary = if (dynamicColorSecondary != Color.Transparent) dynamicColorSecondary else Color.Transparent
            val targetTertiary = if (dynamicColorTertiary != Color.Transparent) dynamicColorTertiary else Color.Transparent

            // Smooth color transitions matching Plex's behavior - colors fade in from backdrop
            // Using longer animation duration (1500ms) for a more elegant, spreading effect
            val animPrimary by animateColorAsState(
                targetPrimary,
                animationSpec = tween(1500),
                label = "primary"
            )
            val animSecondary by animateColorAsState(
                targetSecondary,
                animationSpec = tween(1500),
                label = "secondary"
            )
            val animTertiary by animateColorAsState(
                targetTertiary,
                animationSpec = tween(1500),
                label = "tertiary"
            )
            

            if (animPrimary != Color.Transparent || animSecondary != Color.Transparent || animTertiary != Color.Transparent) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawRect(color = baseBackgroundColor)
                            // Top Left (Vibrant/Muted)
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(animSecondary, Color.Transparent),
                                    center = Offset(0f, 0f),
                                    radius = size.width * 0.8f
                                )
                            )
                            // Bottom Right (DarkVibrant/DarkMuted)
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(animPrimary, Color.Transparent),
                                    center = Offset(size.width, size.height),
                                    radius = size.width * 0.8f
                                )
                            )
                            // Bottom Left (Dark / Bridge)
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(baseBackgroundColor, Color.Transparent),
                                    center = Offset(0f, size.height),
                                    radius = size.width * 0.8f
                                )
                            )
                            // Top Right (Under Image - Vibrant/Bright)
                            drawRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(animTertiary, Color.Transparent),
                                    center = Offset(size.width, 0f),
                                    radius = size.width * 0.8f
                                )
                            )
                        }
                )
            }

            // Simple fade-in using Coil's built-in transition
            // Track if image is loaded to prevent black border during transitions
            var imageLoaded by remember { mutableStateOf(false) }
            var currentBackdropKey by remember { mutableStateOf<String?>(null) }
            
            // Reset loaded state when backdrop URL changes (even if it's the same URL)
            LaunchedEffect(backdropImageUrl) {
                if (backdropImageUrl != currentBackdropKey) {
                    imageLoaded = false
                    currentBackdropKey = backdropImageUrl
                }
            }
            
            // Only show backdrop if URL is not null - disappears when moving off item
            if (backdropImageUrl != null) {
                AsyncImage(
                    model =
                    ImageRequest
                        .Builder(context)
                        .data(backdropImageUrl)
                        .transitionFactory(CrossFadeFactory(800.milliseconds)) // Smooth 800ms fade-in
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopEnd,
                    onSuccess = { 
                        // Only set loaded if this is still the current backdrop
                        if (backdropImageUrl == currentBackdropKey) {
                            imageLoaded = true
                        }
                    },
                    onError = { imageLoaded = false },
                    modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .fillMaxWidth(0.7f) // Occupy 70% width
                        .aspectRatio(1.77f) // 16:9 aspect ratio
                        .alpha(0.95f) // Slight transparency to allow background colors to show through
                        .graphicsLayer { alpha = 0.95f }
                        .drawWithContent {
                            // Draw image content first
                            drawContent()
                            // Always apply masking - the image is always loaded when this composable is rendered
                            // The fade-in transition handles the opacity, so masking is safe to apply
                            // Masking - only applies to the image content via DstIn blend mode
                            // Left Fade: Transparent -> Black (Left -> Right)
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, Color.Black),
                                    startX = 0f,
                                    endX = size.width * 0.6f
                                ),
                                blendMode = BlendMode.DstIn
                            )
                            // Bottom Fade: Black -> Transparent (Top -> Bottom)
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color.Black, Color.Transparent),
                                    startY = 0f,
                                    endY = size.height
                                ),
                                blendMode = BlendMode.DstIn
                            )
                        },
                )
            }
        }

        NavigationDrawer(
            modifier = Modifier.background(Color.Transparent),
            drawerState = drawerState,
            drawerContent = {
                ProvideTextStyle(MaterialTheme.typography.labelMedium) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(spacedBy),
                        modifier =
                        Modifier
                            .fillMaxHeight()
                            .width(drawerWidth)
                            .ifElse(!isNanifin, Modifier.background(drawerBackground)), // Only apply solid background if NOT Nanifin
                    ) {
                        // Even though some must be clicked, focusing on it should clear other focused items
                        val interactionSource = remember { MutableInteractionSource() }
                        val focused by interactionSource.collectIsFocusedAsState()
                        LaunchedEffect(focused) { if (focused) focusedIndex = Int.MIN_VALUE }
                        IconNavItem(
                            text = user?.name ?: "",
                            subtext = server?.name ?: server?.url,
                            icon = Icons.Default.AccountCircle,
                            selected = false,
                            drawerOpen = drawerState.isOpen,
                            interactionSource = interactionSource,
                            onClick = {
                                viewModel.navigationManager.navigateToFromDrawer(
                                    Destination.UserList(
                                        server,
                                    ),
                                )
                            },
                            modifier = Modifier.padding(start = drawerPadding),
                        )
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(0.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedByWithFooter(spacedBy),
                            modifier =
                            Modifier
                                .focusGroup()
                                .focusProperties {
                                    onEnter = {
                                        if (requestedFocusDirection == FocusDirection.Down) {
                                            searchFocusRequester.tryRequestFocus()
                                        } else {
                                            focusRequester.tryRequestFocus()
                                        }
                                    }
                                }.fillMaxHeight()
                                .padding(start = drawerPadding),
                        ) {
                            item {
                                val interactionSource = remember { MutableInteractionSource() }
                                val focused by interactionSource.collectIsFocusedAsState()
                                LaunchedEffect(focused) { if (focused) focusedIndex = -2 }
                                IconNavItem(
                                    text = stringResource(R.string.search),
                                    icon = Icons.Default.Search,
                                    selected = selectedIndex == -2,
                                    drawerOpen = drawerState.isOpen,
                                    interactionSource = interactionSource,
                                    onClick = {
                                        viewModel.setIndex(-2)
                                        viewModel.navigationManager.navigateToFromDrawer(Destination.Search)
                                    },
                                    modifier =
                                    Modifier
                                        .focusRequester(searchFocusRequester)
                                        .ifElse(
                                            selectedIndex == -2,
                                            Modifier.focusRequester(focusRequester),
                                        ).animateItem(),
                                )
                            }
                            item {
                                val interactionSource = remember { MutableInteractionSource() }
                                val focused by interactionSource.collectIsFocusedAsState()
                                LaunchedEffect(focused) { if (focused) focusedIndex = -1 }
                                IconNavItem(
                                    text = stringResource(R.string.home),
                                    icon = Icons.Default.Home,
                                    selected = selectedIndex == -1,
                                    drawerOpen = drawerState.isOpen,
                                    interactionSource = interactionSource,
                                    onClick = {
                                        viewModel.setIndex(-1)
                                        if (destination is Destination.Home) {
                                            viewModel.navigationManager.reloadHome()
                                        } else {
                                            viewModel.navigationManager.goToHome()
                                        }
                                    },
                                    modifier =
                                    Modifier
                                        .ifElse(
                                            selectedIndex == -1,
                                            Modifier.focusRequester(focusRequester),
                                        ).animateItem(),
                                )
                            }
                            itemsIndexed(libraries) { index, it ->
                                val interactionSource = remember { MutableInteractionSource() }
                                val focused by interactionSource.collectIsFocusedAsState()
                                LaunchedEffect(focused) {                     if (focused) focusedIndex = index }
                                NavItem(
                                    library = it,
                                    selected = selectedIndex == index,
                                    moreExpanded = showMore,
                                    drawerOpen = drawerState.isOpen,
                                    interactionSource = interactionSource,
                                    onClick = {
                                        onClick.invoke(index, it)
                                        if (it !is NavDrawerItem.More) setShowMore(false)
                                    },
                                    modifier =
                                    Modifier
                                        .ifElse(
                                            selectedIndex == index,
                                            Modifier.focusRequester(focusRequester),
                                        ).animateItem(),
                                )
                            }
                            if (showMore) {
                                itemsIndexed(moreLibraries) { index, it ->
                                    val adjustedIndex = (index + libraries.size + 1)
                                    val interactionSource = remember { MutableInteractionSource() }
                                    val focused by interactionSource.collectIsFocusedAsState()
                                    LaunchedEffect(focused) {
                                        if (focused) focusedIndex = adjustedIndex
                                    }
                                    NavItem(
                                        library = it,
                                        selected = selectedIndex == adjustedIndex,
                                        moreExpanded = showMore,
                                        drawerOpen = drawerState.isOpen,
                                        onClick = { onClick.invoke(adjustedIndex, it) },
                                        containerColor =
                                        if (drawerState.isOpen) {
                                            MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                                        } else {
                                            Color.Unspecified
                                        },
                                        interactionSource = interactionSource,
                                        modifier =
                                        Modifier
                                            .ifElse(
                                                selectedIndex == adjustedIndex,
                                                Modifier.focusRequester(focusRequester),
                                            ).animateItem(),
                                    )
                                }
                            }
                            item {
                                val interactionSource = remember { MutableInteractionSource() }
                                val focused by interactionSource.collectIsFocusedAsState()
                                LaunchedEffect(focused) { if (focused) focusedIndex = Int.MIN_VALUE }
                                IconNavItem(
                                    text = stringResource(R.string.settings),
                                    icon = Icons.Default.Settings,
                                    selected = false,
                                    drawerOpen = drawerState.isOpen,
                                    interactionSource = interactionSource,
                                    onClick = {
                                        viewModel.navigationManager.navigateTo(
                                            Destination.Settings(
                                                PreferenceScreenOption.BASIC,
                                            ),
                                        )
                                    },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                        }
                    }
                }
            },
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                // Drawer content (The actual screen)
                CompositionLocalProvider(
                    LocalBackdropHandler provides { url ->
                        if (isNanifin) {
                            backdropImageUrl = url
                        }
                    }
                ) {
                    DestinationContent(
                        destination = destination,
                        preferences = preferences,
                        deviceProfile = deviceProfile,
                        modifier =
                        Modifier
                            .fillMaxSize(),
                    )
                }
                if (preferences.appPreferences.interfacePreferences.showClock) {
                    var now by remember { mutableStateOf(LocalTime.now()) }
                    LaunchedEffect(Unit) {
                        while (isActive) {
                            now = LocalTime.now()
                            delay(1000L)
                        }
                    }
                    Text(
                        text = TimeFormatter.format(now),
//                    style = MaterialTheme.typography.titleMedium,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier =
                        Modifier
                            .align(Alignment.TopEnd)
                            .padding(vertical = 16.dp, horizontal = 24.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationDrawerScope.IconNavItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    selected: Boolean,
    drawerOpen: Boolean,
    modifier: Modifier = Modifier,
    subtext: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val focused by interactionSource.collectIsFocusedAsState()
    NavigationDrawerItem(
        modifier = modifier,
        selected = false,
        onClick = onClick,
        leadingContent = {
            val color = navItemColor(selected, focused, drawerOpen)
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.padding(0.dp),
            )
        },
        supportingContent =
            subtext?.let {
                {
                    Text(
                        text = it,
                        maxLines = 1,
                    )
                }
            },
        interactionSource = interactionSource,
    ) {
        Text(
            modifier = Modifier,
            text = text,
            maxLines = 1,
        )
    }
}

@Composable
fun NavigationDrawerScope.NavItem(
    library: NavDrawerItem,
    onClick: () -> Unit,
    selected: Boolean,
    moreExpanded: Boolean,
    drawerOpen: Boolean,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    containerColor: Color = Color.Unspecified,
) {
    val context = LocalContext.current
    val useFont = library !is ServerNavDrawerItem || library.type != CollectionType.LIVETV
    val icon =
        when (library) {
            NavDrawerItem.Favorites -> R.string.fa_heart
            NavDrawerItem.More -> R.string.fa_ellipsis

            is ServerNavDrawerItem ->
                when (library.type) {
                    CollectionType.MOVIES -> R.string.fa_film
                    CollectionType.TVSHOWS -> R.string.fa_tv
                    CollectionType.HOMEVIDEOS -> R.string.fa_video
                    CollectionType.LIVETV -> R.drawable.gf_dvr
                    CollectionType.MUSIC -> R.string.fa_music
                    CollectionType.BOXSETS -> R.string.fa_open_folder
                    CollectionType.PLAYLISTS -> R.string.fa_list_ul
                    else -> R.string.fa_film
                }
        }
    val focused by interactionSource.collectIsFocusedAsState()
    NavigationDrawerItem(
        modifier = modifier,
        selected = false,
        onClick = onClick,
        colors =
            NavigationDrawerItemDefaults.colors(
                containerColor = containerColor,
            ),
        leadingContent = {
            val color = navItemColor(selected, focused, drawerOpen)
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (useFont) {
                    Text(
                        text = stringResource(icon),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontFamily = FontAwesome,
                        color = color,
                        modifier = Modifier,
                    )
                } else {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier,
                    )
                }
            }
        },
        trailingContent = {
            if (library is NavDrawerItem.More) {
                                Icon(
                    imageVector = if (moreExpanded) Icons.Default.ArrowDropDown else Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = null,
                )
            }
        },
        interactionSource = interactionSource,
    ) {
        Text(
            modifier = Modifier,
            text = library.name(context),
            maxLines = 1,
        )
    }
}

@Composable
fun navItemColor(
    selected: Boolean,
    focused: Boolean,
    drawerOpen: Boolean,
): Color {
    val alpha =
        when {
            drawerOpen -> .75f
            selected && !drawerOpen -> .5f
            else -> .2f
        }
    return when {
        selected -> MaterialTheme.colorScheme.border
        focused -> LocalContentColor.current
        else -> MaterialTheme.colorScheme.onSurface
    }.copy(alpha = alpha)
}

val DrawerState.isOpen: Boolean get() = this.currentValue == DrawerValue.Open
