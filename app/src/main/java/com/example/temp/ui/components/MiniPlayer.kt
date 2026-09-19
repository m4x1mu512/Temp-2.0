package com.example.temp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.temp.playback.PlayerUiState

@Composable
fun MiniPlayer(
    state: PlayerUiState,
    onOpen: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = state.currentTrack ?: return
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onOpen),
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (track.artworkUri != null) {
                    AsyncImage(
                        model = track.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Default.MusicNote,
                        null,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    track.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    track.artist ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            IconButton(onClick = onPlayPause) {
                Icon(
                    if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    null
                )
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.SkipNext, null)
            }
        }
    }
}
