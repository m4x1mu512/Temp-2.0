package com.example.temp.ui

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.temp.TempApp
import com.example.temp.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val appRef: TempApp get() = getApplication()

    val settings: StateFlow<AppSettingsData> =
        appRef.settingsRepository.flow.stateIn(
            viewModelScope, SharingStarted.Eagerly, AppSettingsData()
        )

    val tracks: StateFlow<List<TrackEntity>> =
        appRef.trackRepository.observeAll().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    val favorites: StateFlow<List<Long>> =
        appRef.trackRepository.observeFavorites().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    val playlists: StateFlow<List<PlaylistEntity>> =
        appRef.playlistRepository.observeAll().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    val folders: StateFlow<List<String>> =
        appRef.trackRepository.observeFolders().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    val albums: StateFlow<List<String>> =
        appRef.trackRepository.observeAlbums().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    val artists: StateFlow<List<String>> =
        appRef.trackRepository.observeArtists().stateIn(
            viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
        )

    private val scanner = MediaScanner(getApplication())

    val playerController = com.example.temp.playback.PlayerController(appRef)

    init {
        viewModelScope.launch { playerController.connect() }
    }

    fun scan(folder: String? = null) {
        viewModelScope.launch {
            val list = scanner.scan(folder)
            appRef.trackRepository.replaceAll(list)
        }
    }

    fun toggleFavorite(id: Long) = viewModelScope.launch {
        appRef.trackRepository.toggleFavorite(id)
    }

    fun createPlaylist(name: String) = viewModelScope.launch {
        appRef.playlistRepository.create(name)
    }

    fun deletePlaylist(id: Long) = viewModelScope.launch {
        appRef.playlistRepository.delete(id)
    }

    fun renamePlaylist(id: Long, name: String) = viewModelScope.launch {
        appRef.playlistRepository.rename(id, name)
    }

    fun addToPlaylist(playlistId: Long, trackId: Long) = viewModelScope.launch {
        appRef.playlistRepository.addTrack(playlistId, trackId)
    }

    fun removeFromPlaylist(playlistId: Long, trackId: Long) = viewModelScope.launch {
        appRef.playlistRepository.removeTrack(playlistId, trackId)
    }

    fun playlistTracks(playlistId: Long): Flow<List<TrackEntity>> =
        appRef.playlistRepository.tracks(playlistId)

    fun byArtist(a: String) = appRef.trackRepository.byArtist(a)
    fun byAlbum(a: String) = appRef.trackRepository.byAlbum(a)
    fun byFolder(f: String) = appRef.trackRepository.byFolder(f)
    fun search(q: String) = appRef.trackRepository.search(q)

    fun setTheme(t: ThemeMode) = viewModelScope.launch {
        appRef.settingsRepository.setTheme(t)
    }

    fun setVisualizerEnabled(v: Boolean) = viewModelScope.launch {
        appRef.settingsRepository.setVisualizerEnabled(v)
    }

    fun setVisualizerType(t: VisualizerType) = viewModelScope.launch {
        appRef.settingsRepository.setVisualizerType(t)
    }

    fun setVisualizerBars(n: Int) = viewModelScope.launch {
        appRef.settingsRepository.setVisualizerBars(n)
    }

    fun setVisualizerSensitivity(f: Float) = viewModelScope.launch {
        appRef.settingsRepository.setVisualizerSensitivity(f)
    }

    fun setSleepTimer(min: Int) = viewModelScope.launch {
        appRef.settingsRepository.setSleepTimerMinutes(min)
    }

    fun resetSettings() = viewModelScope.launch { appRef.settingsRepository.reset() }

    override fun onCleared() {
        playerController.release()
        super.onCleared()
    }
}
