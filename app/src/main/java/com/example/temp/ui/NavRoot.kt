package com.example.temp.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.temp.ui.screens.*

object Routes {
    const val HOME = "home"
    const val PLAYER = "player"
    const val SETTINGS = "settings"
    const val EQUALIZER = "equalizer"
    const val VISUALIZER = "visualizer"
    const val PLAYLIST_DETAIL = "playlist/{id}"
    const val FOLDER_DETAIL = "folder"
    const val ALBUM_DETAIL = "album"
    const val ARTIST_DETAIL = "artist"
    const val FAVORITES = "favorites"
    const val ALL_TRACKS = "all_tracks"

    fun playlist(id: Long) = "playlist/$id"
}

@Composable
fun NavRoot(
    nav: NavHostController = rememberNavController(),
    vm: MainViewModel = viewModel()
) {
    Scaffold { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) { HomeScreen(nav, vm) }
            composable(Routes.PLAYER) { PlayerScreen(nav, vm) }
            composable(Routes.SETTINGS) { SettingsScreen(nav, vm) }
            composable(Routes.EQUALIZER) { EqualizerScreen(nav) }
            composable(Routes.VISUALIZER) { VisualizerScreen(nav, vm) }
            composable(Routes.ALL_TRACKS) { TrackListScreen(nav, vm, "Все треки", vm.tracks) }
            composable(Routes.FAVORITES) {
                val favs = vm.favorites.collectAsStateValue()
                val allTracks = vm.tracks.collectAsStateValue()
                val filtered = allTracks.filter { it.id in favs }
                TrackListScreen(nav, vm, "Избранное", kotlinx.coroutines.flow.flowOf(filtered))
            }
            composable(Routes.PLAYLIST_DETAIL) { back ->
                val id = back.arguments?.getString("id")?.toLongOrNull() ?: -1L
                PlaylistDetailScreen(nav, vm, id)
            }
            composable(Routes.FOLDER_DETAIL) { FolderDetailScreen(nav, vm) }
            composable(Routes.ALBUM_DETAIL) { AlbumDetailScreen(nav, vm) }
            composable(Routes.ARTIST_DETAIL) { ArtistDetailScreen(nav, vm) }
        }
    }
}

@Composable
private fun <T> kotlinx.coroutines.flow.Flow<T>.collectAsStateValue(): T =
    androidx.compose.runtime.collectAsState(
        initial = when (this) {
            is kotlinx.coroutines.flow.StateFlow<T> -> value
            else -> @Suppress("UNCHECKED_CAST") (emptyList<Any>() as T)
        }
    ).value
