package com.example.temp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.temp.data.TrackEntity
import com.example.temp.ui.MainViewModel
import com.example.temp.ui.Routes
import com.example.temp.ui.components.TrackRow
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackListScreen(
    nav: NavHostController,
    vm: MainViewModel,
    title: String,
    source: Flow<List<TrackEntity>>
) {
    val tracks by source.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { p ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(p)
        ) {
            items(tracks, key = { it.id }) { t ->
                TrackRow(
                    track = t,
                    onClick = {
                        val idx = tracks.indexOf(t)
                        vm.playerController.setQueue(tracks, idx, play = true)
                        nav.navigate(Routes.PLAYER)
                    },
                    onLongClick = {
                        val idx = tracks.indexOf(t)
                        vm.playerController.setQueue(tracks, idx, play = false)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    nav: NavHostController,
    vm: MainViewModel,
    playlistId: Long
) {
    val tracks by vm.playlistTracks(playlistId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Плейлист") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { p ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(p)
        ) {
            items(tracks, key = { it.id }) { t ->
                TrackRow(
                    track = t,
                    onClick = {
                        val idx = tracks.indexOf(t)
                        vm.playerController.setQueue(tracks, idx, play = true)
                        nav.navigate(Routes.PLAYER)
                    },
                    onLongClick = { vm.removeFromPlaylist(playlistId, t.id) }
                )
            }
        }
    }
}

@Composable
fun FolderDetailScreen(nav: NavHostController, vm: MainViewModel) {
    val folder = remember {
        nav.previousBackStackEntry?.savedStateHandle?.get<String>("path").orEmpty()
    }
    val flow = remember(folder) { vm.byFolder(folder) }
    TrackListScreen(nav, vm, folder.substringAfterLast('/').ifBlank { "Папка" }, flow)
}

@Composable
fun AlbumDetailScreen(nav: NavHostController, vm: MainViewModel) {
    val name = remember {
        nav.previousBackStackEntry?.savedStateHandle?.get<String>("name").orEmpty()
    }
    val flow = remember(name) { vm.byAlbum(name) }
    TrackListScreen(nav, vm, name.ifBlank { "Альбом" }, flow)
}

@Composable
fun ArtistDetailScreen(nav: NavHostController, vm: MainViewModel) {
    val name = remember {
        nav.previousBackStackEntry?.savedStateHandle?.get<String>("name").orEmpty()
    }
    val flow = remember(name) { vm.byArtist(name) }
    TrackListScreen(nav, vm, name.ifBlank { "Исполнитель" }, flow)
}