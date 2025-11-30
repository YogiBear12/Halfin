package com.github.damontecres.wholphin.ui.detail.movie

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.github.damontecres.wholphin.R
import com.github.damontecres.wholphin.data.ChosenStreams
import com.github.damontecres.wholphin.data.model.BaseItem
import com.github.damontecres.wholphin.preferences.UserPreferences
import com.github.damontecres.wholphin.ui.components.DotSeparatedRow
import com.github.damontecres.wholphin.ui.components.OverviewText
import com.github.damontecres.wholphin.ui.components.VideoStreamDetails
import com.github.damontecres.wholphin.ui.components.SimpleStarRating
import com.github.damontecres.wholphin.ui.isNotNullOrBlank
import com.github.damontecres.wholphin.ui.letNotEmpty
import com.github.damontecres.wholphin.ui.roundMinutes
import com.github.damontecres.wholphin.ui.timeRemaining
import org.jellyfin.sdk.model.api.PersonKind
import org.jellyfin.sdk.model.extensions.ticks

@Composable
fun MovieDetailsHeader(
    preferences: UserPreferences,
    movie: BaseItem,
    chosenStreams: ChosenStreams?,
    bringIntoViewRequester: BringIntoViewRequester,
    overviewOnClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dto = movie.data
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Match homepage structure exactly: Box with fillMaxHeight(.42f), Column with fillMaxSize
    Box(
        modifier = modifier, // Modifier already has fillMaxHeight(.42f) applied from caller
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            
            // Title
            Text(
                text = movie.name ?: "",
                color = MaterialTheme.colorScheme.onBackground, // Match homepage: onBackground instead of onSurface
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(start = 8.dp), // Add 8dp left padding to align with summary text
            )

            // Rating and year with dot separator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp), // Add 8dp left padding to align with summary text
            ) {
                dto.communityRating?.let {
                    SimpleStarRating(
                        it,
                        Modifier.height(20.dp),
                    )
                }
                val details =
                    remember(dto) {
                        buildList {
                            dto.productionYear?.let { add(it.toString()) }
                            val duration = dto.runTimeTicks?.ticks
                            duration
                                ?.roundMinutes
                                ?.toString()
                                ?.let(::add)
                            dto.timeRemaining?.roundMinutes?.let { add("$it left") }
                            dto.officialRating?.let(::add)
                        }
                    }
                if (details.isNotEmpty()) {
                    DotSeparatedRow(
                        texts = details,
                        textStyle = MaterialTheme.typography.bodyLarge, // Match homepage: bodyLarge instead of titleMedium
                        modifier = Modifier,
                    )
                }
            }

            // Video stream details (from upstream)
            VideoStreamDetails(
                preferences = preferences,
                dto = dto,
                itemPlayback = chosenStreams?.itemPlayback,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
            )

            // Genres (comma separated)
            dto.genres?.letNotEmpty {
                Text(
                    text = it.joinToString(", "),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp), // Add 8dp left padding to align with summary text
                )
            }

            // Description (4 lines for movie details) - match homepage: 0.7f width, no padding
            dto.overview?.let { overview ->
                val interactionSource = remember { MutableInteractionSource() }
                val focused = interactionSource.collectIsFocusedAsState().value
                LaunchedEffect(focused) {
                    if (focused) bringIntoViewRequester.bringIntoView()
                }
                OverviewText(
                    overview = overview,
                    maxLines = 4,
                    onClick = overviewOnClick,
                    textBoxHeight = Dp.Unspecified,
                    interactionSource = interactionSource,
                    modifier = Modifier.fillMaxWidth(0.7f).padding(0.dp), // Match homepage exactly
                )
            }
            
            // Directed by (different size than summary)
            movie.data.people
                ?.filter { it.type == PersonKind.DIRECTOR && it.name.isNotNullOrBlank() }
                ?.joinToString(", ") { it.name!! }
                ?.let {
                    Text(
                        text = stringResource(R.string.directed_by, it),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 8.dp), // Add 8dp left padding to align with summary text
                    )
                }
        }
    }
}
