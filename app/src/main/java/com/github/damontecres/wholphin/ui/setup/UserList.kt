package com.github.damontecres.wholphin.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.github.damontecres.wholphin.R
import org.jellyfin.sdk.api.client.ApiClient
import org.jellyfin.sdk.api.client.extensions.imageApi
import com.github.damontecres.wholphin.data.model.JellyfinUser
import java.util.UUID
import com.github.damontecres.wholphin.ui.FontAwesome
import com.github.damontecres.wholphin.ui.components.DialogItem
import com.github.damontecres.wholphin.ui.components.DialogPopup

/**
 * Display a list of users plus option to add a new one or switch servers
 * Redesigned to match streaming service style with horizontal scrollable user icons
 */
@Composable
fun UserList(
    users: List<JellyfinUser>,
    currentUser: JellyfinUser?,
    onSwitchUser: (JellyfinUser) -> Unit,
    onAddUser: () -> Unit,
    onRemoveUser: (JellyfinUser) -> Unit,
    onSwitchServer: () -> Unit,
    modifier: Modifier = Modifier,
    apiClient: ApiClient? = null,
) {
    var showDeleteDialog by remember { mutableStateOf<JellyfinUser?>(null) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Horizontal scrollable list of user icons - centered
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp), // Increased spacing between users
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                modifier = Modifier.wrapContentWidth(),
            ) {
                items(users) { user ->
                    UserIconCard(
                        user = user,
                        isCurrentUser = user.id == currentUser?.id,
                        onClick = { onSwitchUser.invoke(user) },
                        onLongClick = { showDeleteDialog = user },
                        apiClient = apiClient,
                    )
                }
            }
        }

        // Buttons below user list - centered and same width
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
        ) {
            Button(
                onClick = { onAddUser.invoke() },
                modifier = Modifier.width(200.dp), // Fixed width to match switch servers button
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = stringResource(R.string.add_user),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { onSwitchServer.invoke() },
                modifier = Modifier.width(200.dp), // Fixed width for consistency
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.fa_arrow_left_arrow_right),
                        fontFamily = FontAwesome,
                        modifier = Modifier.padding(end = 8.dp),
                    )
                    Text(
                        text = stringResource(R.string.switch_servers),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
    showDeleteDialog?.let { user ->
        DialogPopup(
            showDialog = true,
            title = user.name ?: user.id.toString(),
            dialogItems =
                listOf(
                    DialogItem(
                        stringResource(R.string.switch_user),
                        R.string.fa_arrow_left_arrow_right,
                    ) {
                        onSwitchUser.invoke(user)
                    },
                    DialogItem(
                        stringResource(R.string.delete),
                        Icons.Default.Delete,
                        Color.Red.copy(alpha = .8f),
                    ) {
                        onRemoveUser.invoke(user)
                    },
                ),
            onDismissRequest = { showDeleteDialog = null },
            dismissOnClick = true,
            waitToLoad = true,
            properties = DialogProperties(),
            elevation = 5.dp,
        )
    }
}

/**
 * User icon card component - displays user profile picture with name below
 */
/**
 * Generate a consistent color for a user based on their ID
 */
@Composable
private fun getUserColor(userId: UUID): Color {
    return remember(userId) {
        // Generate a color based on the user ID hash
        val hash = userId.hashCode()
        val hue = (hash % 360).toFloat()
        val saturation = 0.6f + ((hash / 360) % 40).toFloat() / 100f // 0.6-1.0
        val brightness = 0.4f + ((hash / 14400) % 30).toFloat() / 100f // 0.4-0.7 (darker colors)
        
        // Convert HSV to RGB
        val c = brightness * saturation
        val x = c * (1 - kotlin.math.abs((hue / 60f) % 2f - 1))
        val m = brightness - c
        
        val (r, g, b) = when {
            hue < 60 -> Triple(c, x, 0f)
            hue < 120 -> Triple(x, c, 0f)
            hue < 180 -> Triple(0f, c, x)
            hue < 240 -> Triple(0f, x, c)
            hue < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        
        Color(
            red = (r + m).coerceIn(0f, 1f),
            green = (g + m).coerceIn(0f, 1f),
            blue = (b + m).coerceIn(0f, 1f)
        )
    }
}

@Composable
private fun UserIconCard(
    user: JellyfinUser,
    isCurrentUser: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    apiClient: ApiClient? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    
    // Generate unique color for this user
    val userColor = getUserColor(user.id)
    
    // Get user profile image URL from Jellyfin API
    val userImageUrl = remember(user.id, apiClient) {
        apiClient?.imageApi?.getUserImageUrl(user.id)
    }
    
    // Track image loading errors
    var imageError by remember { mutableStateOf(false) }
    
    // Round card - circular shape, with proper focus growth
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Square card
        Card(
            onClick = onClick,
            onLongClick = onLongClick,
            interactionSource = interactionSource,
            modifier = Modifier.size(140.dp),
            colors = CardDefaults.colors(
                containerColor = Color.Transparent, // Transparent so only content is visible
            ),
        ) {
            // Square content inside Card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isCurrentUser && focused) {
                            userColor.copy(alpha = 0.9f)
                        } else if (isCurrentUser) {
                            userColor.copy(alpha = 0.7f)
                        } else if (focused) {
                            userColor.copy(alpha = 0.6f)
                        } else {
                            userColor.copy(alpha = 0.5f)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (userImageUrl != null && !imageError) {
                    AsyncImage(
                        model = userImageUrl,
                        contentDescription = user.name,
                        contentScale = ContentScale.Crop,
                        onError = { imageError = true },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    // Show Person silhouette icon
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = user.name,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        
        // Username below the round selector - bolded/thicker
        Text(
            text = user.name ?: user.id.toString(),
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(140.dp) // Match card width
                .padding(vertical = 4.dp),
        )
    }
}
