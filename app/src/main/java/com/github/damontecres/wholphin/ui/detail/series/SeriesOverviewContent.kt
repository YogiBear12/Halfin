package com.github.damontecres.wholphin.ui.detail.series

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.github.damontecres.wholphin.R
import com.github.damontecres.wholphin.data.ChosenStreams
import com.github.damontecres.wholphin.data.model.BaseItem
import com.github.damontecres.wholphin.data.model.Person
import com.github.damontecres.wholphin.preferences.UserPreferences
import com.github.damontecres.wholphin.ui.AspectRatios
import com.github.damontecres.wholphin.ui.OneTimeLaunchedEffect
import com.github.damontecres.wholphin.ui.cards.BannerCard
import com.github.damontecres.wholphin.ui.cards.PersonRow
import com.github.damontecres.wholphin.ui.components.ErrorMessage
import com.github.damontecres.wholphin.ui.components.LoadingPage
import com.github.damontecres.wholphin.ui.components.TabRow
import com.github.damontecres.wholphin.ui.formatDateTime
import com.github.damontecres.wholphin.ui.ifElse
import com.github.damontecres.wholphin.ui.logTab
import com.github.damontecres.wholphin.ui.playback.isPlayKeyUp
import com.github.damontecres.wholphin.ui.tryRequestFocus
import com.github.damontecres.wholphin.ui.util.rememberDelayedNestedScroll
import kotlinx.coroutines.launch
import org.jellyfin.sdk.model.api.PersonKind
import kotlin.time.Duration

@Composable
fun SeriesOverviewContent(
    preferences: UserPreferences,
    series: BaseItem,
    seasons: List<BaseItem?>,
    episodes: EpisodeList,
    chosenStreams: ChosenStreams?,
    peopleInEpisode: List<Person>,
    position: SeriesOverviewPosition,
    firstItemFocusRequester: FocusRequester,
    episodeRowFocusRequester: FocusRequester,
    castCrewRowFocusRequester: FocusRequester,
    guestStarRowFocusRequester: FocusRequester,
    onChangeSeason: (Int) -> Unit,
    onFocusEpisode: (Int) -> Unit,
    onClick: (BaseItem) -> Unit,
    onLongClick: (BaseItem) -> Unit,
    playOnClick: (Duration) -> Unit,
    watchOnClick: () -> Unit,
    favoriteOnClick: () -> Unit,
    moreOnClick: () -> Unit,
    overviewOnClick: () -> Unit,
    personOnClick: (Person) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    var selectedTabIndex by rememberSaveable(position) { mutableIntStateOf(position.seasonTabIndex) }
    LaunchedEffect(selectedTabIndex) {
        logTab("series_overview", selectedTabIndex)
    }
    val tabRowFocusRequester = remember { FocusRequester() }

    val focusedEpisode =
        (episodes as? EpisodeList.Success)?.episodes?.getOrNull(position.episodeRowIndex)
    var pageHasFocus by remember { mutableStateOf(false) }
    var cardRowHasFocus by remember { mutableStateOf(false) }
    val dimming by animateFloatAsState(if (pageHasFocus && !cardRowHasFocus) .4f else 1f)

    val scrollState = rememberScrollState()
    val scrollConnection = rememberDelayedNestedScroll()
    val castAndCrew =
        remember(peopleInEpisode) {
            peopleInEpisode.filterNot {
                it.type == PersonKind.GUEST_STAR
            }
        }
    val guestStars =
        remember(peopleInEpisode) {
            peopleInEpisode.filter {
                it.type == PersonKind.GUEST_STAR
            }
        }
    Box(
        modifier =
            modifier
                .fillMaxWidth(),
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp),
            modifier =
                Modifier
                    .fillMaxSize()
                    .focusGroup()
                    .nestedScroll(scrollConnection)
                    .bringIntoViewRequester(bringIntoViewRequester)
                    .onFocusChanged { pageHasFocus = it.hasFocus },
        ) {
            item {
                    val paddingValues =
                        if (preferences.appPreferences.interfacePreferences.showClock) {
                            PaddingValues(start = 16.dp, end = 100.dp)
                        } else {
                            PaddingValues(start = 16.dp, end = 16.dp)
                        }
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        tabs =
                            seasons.mapNotNull {
                                it?.name
                                    ?: it?.data?.indexNumber?.let { stringResource(R.string.tv_season) + " $it" }
                                    ?: ""
                            },
                        onClick = {
                            selectedTabIndex = it
                            onChangeSeason.invoke(it)
                        },
                        modifier =
                            Modifier
                                .focusRequester(tabRowFocusRequester)
                                .padding(paddingValues)
                                .fillMaxWidth(),
                    )
            }
            item {
                Spacer(Modifier.height(8.dp)) // Push content below tabs down
            }
            item {
                series.name?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 8.dp, top = 24.dp),
                    )
                }
            }
            item {
                FocusedEpisodeHeader(
                    preferences = preferences,
                    ep = focusedEpisode,
                    chosenStreams = chosenStreams,
                    overviewOnClick = overviewOnClick,
                    modifier = Modifier.fillMaxWidth(.66f),
                )
            }

            item(key = position.seasonTabIndex) {
                    when (val eps = episodes) {
                        EpisodeList.Loading -> {
                            LoadingPage()
                        }

                        is EpisodeList.Error -> {
                            ErrorMessage(eps.message, eps.exception)
                        }

                        is EpisodeList.Success -> {
                            val state = rememberLazyListState()
                            OneTimeLaunchedEffect {
                                if (state.firstVisibleItemIndex != position.episodeRowIndex) {
                                    state.scrollToItem(position.episodeRowIndex)
                                }
                                firstItemFocusRequester.tryRequestFocus()
                            }
                            LazyRow(
                                state = state,
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                modifier =
                                    Modifier
                                        .focusRestorer(firstItemFocusRequester)
                                        .focusRequester(episodeRowFocusRequester)
                                        .onFocusChanged {
                                            cardRowHasFocus = it.hasFocus
                                        },
                            ) {
                                itemsIndexed(eps.episodes) { episodeIndex, episode ->
                                    val interactionSource = remember { MutableInteractionSource() }
                                    if (interactionSource.collectIsFocusedAsState().value) {
                                        onFocusEpisode.invoke(episodeIndex)
                                    }
                                    val cornerText =
                                        episode?.data?.indexNumber?.let { "E$it" }
                                            ?: episode?.data?.premiereDate?.let(::formatDateTime)
                                    BannerCard(
                                        name = episode?.name,
                                        item = episode,
                                        aspectRatio =
                                            episode
                                                ?.aspectRatio
                                                ?.coerceAtLeast(AspectRatios.FOUR_THREE)
                                                ?: (AspectRatios.WIDE),
                                        cornerText = cornerText,
                                        played = episode?.data?.userData?.played ?: false,
                                        playPercent =
                                            episode?.data?.userData?.playedPercentage
                                                ?: 0.0,
                                        onClick = { if (episode != null) onClick.invoke(episode) },
                                        onLongClick = {
                                            if (episode != null) {
                                                onLongClick.invoke(
                                                    episode,
                                                )
                                            }
                                        },
                                        modifier =
                                            Modifier
                                                .ifElse(
                                                    episodeIndex == position.episodeRowIndex,
                                                    Modifier.focusRequester(firstItemFocusRequester),
                                                ).ifElse(
                                                    episodeIndex != position.episodeRowIndex,
                                                    Modifier
                                                        .background(
                                                            Color.Black,
                                                            shape = RoundedCornerShape(8.dp),
                                                        ).alpha(dimming),
                                                ).onFocusChanged {
                                                    if (it.isFocused) {
                                                        scope.launch {
                                                            bringIntoViewRequester.bringIntoView()
                                                        }
                                                    }
                                                }.onKeyEvent {
                                                    if (episode != null && isPlayKeyUp(it)) {
                                                        onClick.invoke(episode)
                                                        return@onKeyEvent true
                                                    }
                                                    return@onKeyEvent false
                                                },
                                        interactionSource = interactionSource,
                                        cardHeight = 100.dp,
                                    )
                                }
                            }
                        }
                    }
            }

            item {
                focusedEpisode?.let { ep ->
                    FocusedEpisodeFooter(
                        preferences = preferences,
                        ep = ep,
                        chosenStreams = chosenStreams,
                        playOnClick = playOnClick,
                        moreOnClick = moreOnClick,
                        watchOnClick = {
                            watchOnClick.invoke()
                            episodeRowFocusRequester.tryRequestFocus()
                        },
                        favoriteOnClick = favoriteOnClick,
                        buttonOnFocusChanged = {
                            if (it.isFocused) {
                                scope.launch {
                                    bringIntoViewRequester.bringIntoView()
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                    )
                }
            }

            item {
                if (peopleInEpisode.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        if (castAndCrew.isNotEmpty()) {
                            PersonRow(
                                title = R.string.cast_and_crew,
                                people = castAndCrew,
                                onClick = personOnClick,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .focusRequester(castCrewRowFocusRequester),
                            )
                        }
                        if (guestStars.isNotEmpty()) {
                            PersonRow(
                                title = R.string.guest_stars,
                                people = guestStars,
                                onClick = personOnClick,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .focusRequester(guestStarRowFocusRequester),
                            )
                        }
                    }
                }
            }
        }
    }
}
