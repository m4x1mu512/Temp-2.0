package com.example.temp.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.temp.data.TrackEntity
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class PlayerUiState(
    val currentTrack: TrackEntity? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val bufferedMs: Long = 0L,
    val queue: List<TrackEntity> = emptyList(),
    val index: Int = 0,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val shuffle: Boolean = false,
    val errorMessage: String? = null
)

class PlayerController(private val context: Context) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var controller: MediaController? = null

    private val _state = MutableStateFlow(PlayerUiState())
    val state: StateFlow<PlayerUiState> = _state.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: kotlinx.coroutines.Job? = null

    private val listener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            syncCurrent()
            syncQueue()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_READY) syncCurrent()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _state.value = _state.value.copy(repeatMode = repeatMode)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.value = _state.value.copy(shuffle = shuffleModeEnabled)
        }

        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            _state.value = _state.value.copy(errorMessage = error.message ?: "Ошибка воспроизведения")
        }
    }

    suspend fun connect() {
        if (controller != null) return
        val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        controllerFuture = future
        controller = suspendCancellableCoroutine { cont ->
            future.addListener({
                runCatching {
                    val c = future.get()
                    c.addListener(listener)
                    cont.resume(c)
                }.onFailure { cont.resume(null as MediaController?) }
            }, { r -> r.run() })
        }
        controller?.let { syncQueue(); syncCurrent(); startProgress() }
    }

    private fun startProgress() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                syncProgress()
                delay(500)
            }
        }
    }

    private fun syncProgress() {
        val c = controller ?: return
        val pos = runCatching { c.currentPosition }.getOrDefault(0L).coerceAtLeast(0L)
        val dur = runCatching { c.duration }.getOrDefault(0L).let { if (it < 0) 0 else it }
        val buf = runCatching { c.bufferedPosition }.getOrDefault(0L)
        _state.value = _state.value.copy(positionMs = pos, durationMs = dur, bufferedMs = buf)
    }

    private fun syncCurrent() {
        val c = controller ?: return
        val idx = c.currentMediaItemIndex
        val queue = _state.value.queue
        val track = queue.getOrNull(idx)
        _state.value = _state.value.copy(currentTrack = track, index = idx)
        syncProgress()
    }

    private fun syncQueue() {
        val c = controller ?: return
        val items = (0 until c.mediaItemCount).map { c.getMediaItemAt(it) }
        _state.value = _state.value.copy(queue = _state.value.queue.ifEmpty {
            items.map { item -> item.toTrackStub() }
        })
        syncCurrent()
    }

    fun setQueue(tracks: List<TrackEntity>, startIndex: Int, play: Boolean) {
        val c = controller ?: return
        _state.value = _state.value.copy(queue = tracks, index = startIndex)
        val items = tracks.map { it.toMediaItem() }
        c.setMediaItems(items, startIndex, 0L)
        if (play) c.play() else c.pause()
    }

    fun playPause() {
        val c = controller ?: return
        if (c.isPlaying) c.pause() else c.play()
    }

    fun play() { controller?.play() }
    fun pause() { controller?.pause() }

    fun next() {
        val c = controller ?: return
        val wasPlaying = c.isPlaying
        c.seekToNextMediaItem()
        if (!wasPlaying) c.pause()
    }

    fun previous() {
        val c = controller ?: return
        val wasPlaying = c.isPlaying
        c.seekToPreviousMediaItem()
        if (!wasPlaying) c.pause()
    }

    fun seekTo(ms: Long) { controller?.seekTo(ms.coerceAtLeast(0L)) }
    fun seekBy(deltaMs: Long) { controller?.let { seekTo(it.currentPosition + deltaMs) } }

    fun setRepeatMode(mode: Int) { controller?.repeatMode = mode }
    fun toggleShuffle() {
        val c = controller ?: return
        c.shuffleModeEnabled = !c.shuffleModeEnabled
    }

    fun stop() { controller?.stop() }

    fun release() {
        progressJob?.cancel()
        controller?.removeListener(listener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
        controller = null
        controllerFuture = null
    }

    private fun MediaItem.toTrackStub(): TrackEntity = TrackEntity(
        id = mediaId.toLongOrNull() ?: -1L,
        uri = localConfiguration?.uri?.toString() ?: "",
        title = mediaMetadata.title?.toString().orEmpty(),
        artist = mediaMetadata.artist?.toString(),
        album = mediaMetadata.albumTitle?.toString(),
        albumId = null,
        artistId = null,
        folder = null,
        durationMs = mediaMetadata.durationMs ?: 0L,
        sizeBytes = 0L,
        dateAdded = 0L,
        mimeType = null,
        artworkUri = mediaMetadata.artworkUri?.toString()
    )
}

fun TrackEntity.toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist ?: "Неизвестный исполнитель")
        .setAlbumTitle(album ?: "Неизвестный альбом")
        .setArtworkUri(artworkUri?.let { android.net.Uri.parse(it) })
        .setDurationMs(durationMs)
        .build()
    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(metadata)
        .build()
}
