package com.github.damontecres.wholphin.ui.detail.movie

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.github.damontecres.wholphin.R
import com.github.damontecres.wholphin.data.ChosenStreams
import com.github.damontecres.wholphin.data.model.BaseItem
import com.github.damontecres.wholphin.preferences.UserPreferences
import com.github.damontecres.wholphin.ui.components.GenreText
import com.github.damontecres.wholphin.ui.components.MovieQuickDetails
import com.github.damontecres.wholphin.ui.components.OverviewText
import com.github.damontecres.wholphin.ui.components.VideoStreamDetails
import com.github.damontecres.wholphin.ui.isNotNullOrBlank
import com.github.damontecres.wholphin.ui.letNotEmpty
import org.jellyfin.sdk.model.api.PersonKind

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
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        // Title - match SeriesDetails styling
        Text(
            text = movie.name ?: "",
            color = MaterialTheme.colorScheme.onBackground, // Match homepage: onBackground instead of onSurface
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold), // Match SeriesDetails
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp), // Add 8dp left padding to align with summary text
        )

        MovieQuickDetails(
            dto = dto,
            modifier = Modifier.padding(start = 8.dp), // Add 8dp left padding to align with summary text
        )

        VideoStreamDetails(
            chosenStreams = chosenStreams,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
        )

        // Genres - use GenreText component with our styling
        dto.genres?.letNotEmpty {
            GenreText(
                genres = it,
                textStyle = MaterialTheme.typography.bodyLarge, // Our styling: bodyLarge instead of bodyMedium
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
                maxLines = 4, // Keep our 4 lines (upstream uses 3)
                onClick = overviewOnClick,
                textBoxHeight = Dp.Unspecified,
                interactionSource = interactionSource,
                modifier = Modifier.fillMaxWidth(0.7f).padding(0.dp), // Match homepage exactly
            )
        }
        
        // Directed by (different size than summary)
        val directorName =
            remember(movie.data.people) {
                movie.data.people
                    ?.filter { it.type == PersonKind.DIRECTOR && it.name.isNotNullOrBlank() }
                    ?.joinToString(", ") { it.name!! }
            }
        directorName?.let {
            Text(
                text = stringResource(R.string.directed_by, it),
                style = MaterialTheme.typography.bodySmall, // Keep our bodySmall (upstream uses bodyMedium)
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp), // Add 8dp left padding to align with summary text
            )
        }
    }
}
