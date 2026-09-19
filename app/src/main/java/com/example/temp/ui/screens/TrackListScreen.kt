package com.example.temp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        LazyColumn(Modifier.fillMaxSize().padding(p)) {
            items(tracks, key = { it.id }) { t ->
                TrackRow(
                    track = t,
                    onClick = {
                        vm.playerController.setQueue(tracks, tracks.indexOf(t), play = true)
                        nav.navigate(Routes.PLAYER)
                    },
                    onLongClick = {
                        vm.playerController.setQueue(tracks, tracks.indexOf(t), play = false)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(nav: NavHostController, vm: MainViewModel, playlistId: Long) {
    val tracks by vm.playlistTracks(playlistId).collectAsStateWithLifecycle(initialValue = emptyList())
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
        LazyColumn(Modifier.fillMaxSize().padding(p)) {
            items(tracks, key = { it.id }) { t ->
                TrackRow(
                    track = t,
                    onClick = {
                        vm.playerController.setQueue(tracks, tracks.indexOf(t), play = true)
                        nav.navigate(Routes.PLAYER)
                    },
                    onLongClick = { vm.removeFromPlaylist(playlistId, t.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(nav: NavHostController, vm: MainViewModel) {
    val path = nav.currentBackStackEntry?.arguments?.getString("path")
        ?: nav.previousBackStackEntry?.savedStateHandle?.get<String>("path")
    val folder = path ?: ""
    val flow = remember(folder) { vm.byFolder(folder) }
    TrackListScreen(nav, vm, folder.substringAfterLast('/'), flow)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumDetailScreen(nav: NavHostController, vm: MainViewModel) {
    val name = nav.currentBackStackEntry?.arguments?.getString("name").orEmpty()
    val flow = remember(name) { vm.byAlbum(name) }
    TrackListScreen(nav, vm, name, flow)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(nav: NavHostController, vm: MainViewModel) {
    val name = nav.currentBackStackEntry?.arguments?.getString("name").orEmpty()
    val flow = remember(name) { vm.byArtist(name) }
    TrackListScreen(nav, vm, name, flow)
}
