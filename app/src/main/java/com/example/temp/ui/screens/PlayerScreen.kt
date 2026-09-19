package com.example.temp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.media3.common.Player
import coil.compose.AsyncImage
import com.example.temp.ui.MainViewModel
import com.example.temp.ui.Routes

@Composable
fun PlayerScreen(nav: NavHostController, vm: MainViewModel) {
    val state by vm.playerController.state.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val track = state.currentTrack

    val infinite = rememberInfiniteTransition(label = "cover")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            tween(20000, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "rot"
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.Default.KeyboardArrowDown, null)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = track?.album ?: "Темп",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { nav.navigate(Routes.VISUALIZER) }) {
                    Icon(Icons.Default.GraphicEq, null)
                }
            }

            Spacer(Modifier.weight(1f))

            Box(
                Modifier
                    .size(300.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .rotate(if (state.isPlaying) rotation else 0f)
            ) {
                if (track?.artworkUri != null) {
                    AsyncImage(
                        model = track.artworkUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MusicNote, null, modifier = Modifier.size(80.dp))
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                track?.title ?: "Ничего не играет",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                listOfNotNull(track?.artist, track?.album).joinToString(" • ").ifBlank { "—" },
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(24.dp))

            Slider(
                value = if (state.durationMs > 0)
                    (state.positionMs.toFloat() / state.durationMs).coerceIn(0f, 1f) else 0f,
                onValueChange = { f ->
                    if (state.durationMs > 0) {
                        vm.playerController.seekTo((f * state.durationMs).toLong())
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(state.positionMs))
                Text(formatTime(state.durationMs))
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { vm.playerController.toggleShuffle() }) {
                    Icon(
                        Icons.Default.Shuffle, null,
                        tint = if (state.shuffle) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { vm.playerController.seekBy(-10_000) }) {
                    Icon(Icons.Default.Replay10, null)
                }
                IconButton(onClick = { vm.playerController.previous() }) {
                    Icon(Icons.Default.SkipPrevious, null, modifier = Modifier.size(40.dp))
                }
                FilledIconButton(
                    onClick = { vm.playerController.playPause() },
                    modifier = Modifier.size(72.dp)
                ) {
                    Icon(
                        if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        null,
                        modifier = Modifier.size(36.dp)
                    )
                }
                IconButton(onClick = { vm.playerController.next() }) {
                    Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(40.dp))
                }
                IconButton(onClick = { vm.playerController.seekBy(10_000) }) {
                    Icon(Icons.Default.Forward10, null)
                }
                IconButton(onClick = {
                    val mode = if (state.repeatMode == Player.REPEAT_MODE_ONE)
                        Player.REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
                    vm.playerController.setRepeatMode(mode)
                }) {
                    Icon(
                        Icons.Default.RepeatOne, null,
                        tint = if (state.repeatMode == Player.REPEAT_MODE_ONE)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isFav = track?.id?.let { it in vm.favorites.collectAsStateWithLifecycle().value } == true
                IconButton(onClick = { track?.let { vm.toggleFavorite(it.id) } }) {
                    Icon(
                        if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        null,
                        tint = if (isFav) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { /* add to playlist dialog */ }) {
                    Icon(Icons.Default.PlaylistAdd, null)
                }
                IconButton(onClick = { nav.navigate(Routes.EQUALIZER) }) {
                    Icon(Icons.Default.Equalizer, null)
                }
                IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) {
                    Icon(Icons.Default.MoreVert, null)
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}
