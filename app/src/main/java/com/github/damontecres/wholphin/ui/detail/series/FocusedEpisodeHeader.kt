package com.github.damontecres.wholphin.ui.detail.series

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.github.damontecres.wholphin.data.ChosenStreams
import com.github.damontecres.wholphin.data.model.BaseItem
import com.github.damontecres.wholphin.preferences.UserPreferences
import com.github.damontecres.wholphin.ui.components.DotSeparatedRow
import com.github.damontecres.wholphin.ui.components.OverviewText
import com.github.damontecres.wholphin.ui.components.SimpleStarRating
import com.github.damontecres.wholphin.ui.components.VideoStreamDetails
import com.github.damontecres.wholphin.ui.formatDateTime
import com.github.damontecres.wholphin.ui.roundMinutes
import com.github.damontecres.wholphin.ui.seasonEpisode
import com.github.damontecres.wholphin.ui.timeRemaining
import org.jellyfin.sdk.model.extensions.ticks

@Composable
fun FocusedEpisodeHeader(
    preferences: UserPreferences,
    ep: BaseItem?,
    chosenStreams: ChosenStreams?,
    overviewOnClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val dto = ep?.data
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        Text(
            text = dto?.episodeTitle ?: dto?.name ?: "",
            style = MaterialTheme.typography.titleMedium, // Changed from titleLarge to titleMedium to match subtitle style from HomePageHeader
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp),
        )
        // Rating and details in the same row (match MovieDetailsHeader)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp),
        ) {
            dto?.communityRating?.let {
                SimpleStarRating(
                    it,
                    Modifier.height(20.dp),
                )
            }
            val details =
                remember(dto) {
                    buildList {
                        dto?.seasonEpisode?.let(::add)
                        dto?.premiereDate?.let { add(formatDateTime(it)) }
                        val duration = dto?.runTimeTicks?.ticks
                        duration
                            ?.roundMinutes
                            ?.toString()
                            ?.let(::add)
                        dto?.timeRemaining?.roundMinutes?.let { add("$it left") }
                        dto?.officialRating?.let(::add)
                    }
                }
            if (details.isNotEmpty()) {
                DotSeparatedRow(
                    texts = details,
                    textStyle = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        
        // Video stream details (from upstream)
        if (dto != null) {
            VideoStreamDetails(
                preferences = preferences,
                dto = dto,
                itemPlayback = chosenStreams?.itemPlayback,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
        OverviewText(
            overview = dto?.overview ?: "",
            maxLines = 3,
            onClick = overviewOnClick,
            modifier = Modifier.fillMaxWidth(0.7f).padding(start = 0.dp), // Keep 0.dp start padding as OverviewText has internal padding
        )
    }
}
