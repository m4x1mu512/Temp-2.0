package com.example.temp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.temp.data.TrackEntity
import com.example.temp.ui.MainViewModel
import com.example.temp.ui.Routes
import com.example.temp.ui.components.MiniPlayer
import com.example.temp.ui.components.TrackRow
import com.example.temp.ui.components.TopIconsRow

@Composable
fun HomeScreen(nav: NavHostController, vm: MainViewModel) {
    val tracks by vm.tracks.collectAsStateWithLifecycle()
    val playerState by vm.playerController.state.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showScanDialog by remember { mutableStateOf(false) }
    var showSleepDialog by remember { mutableStateOf(false) }
    var contextMenuOpen by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxSize()) {
            TopIconsRow(
                onQueue = { selectedTab = 0 },
                onFolders = { selectedTab = 1 },
                onPlaylists = { selectedTab = 2 },
                onAlbums = { selectedTab = 3 },
                onArtists = { selectedTab = 4 },
                onSearch = { selectedTab = 5 },
                onMore = { contextMenuOpen = true }
            )

            Text(
                "Темп",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            when (selectedTab) {
                0 -> QueueTab(nav, vm, tracks)
                1 -> FoldersTab(nav, vm)
                2 -> PlaylistsTab(nav, vm)
                3 -> AlbumsTab(nav, vm)
                4 -> ArtistsTab(nav, vm)
                5 -> SearchTab(nav, vm)
            }
        }

        MiniPlayer(
            state = playerState,
            onOpen = { nav.navigate(Routes.PLAYER) },
            onPlayPause = vm.playerController::playPause,
            onNext = vm.playerController::next,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        DropdownMenu(
            expanded = contextMenuOpen,
            onDismissRequest = { contextMenuOpen = false }
        ) {
            DropdownMenuItem(
                text = { Text("Эквалайзер") },
                onClick = { contextMenuOpen = false; nav.navigate(Routes.EQUALIZER) }
            )
            DropdownMenuItem(
                text = { Text("Таймер сна") },
                onClick = { contextMenuOpen = false; showSleepDialog = true }
            )
            DropdownMenuItem(
                text = { Text("Сканировать") },
                onClick = { contextMenuOpen = false; showScanDialog = true }
            )
            DropdownMenuItem(
                text = { Text("Настройки") },
                onClick = { contextMenuOpen = false; nav.navigate(Routes.SETTINGS) }
            )
        }
    }

    if (showScanDialog) {
        AlertDialog(
            onDismissRequest = { showScanDialog = false },
            confirmButton = {
                TextButton(onClick = { vm.scan(); showScanDialog = false }) { Text("Вся медиатека") }
            },
            dismissButton = { TextButton(onClick = { showScanDialog = false }) { Text("Отмена") } },
            title = { Text("Сканирование") },
            text = { Text("Выполнить сканирование всех аудиофайлов на устройстве?") }
        )
    }

    if (showSleepDialog) {
        SleepTimerDialog(vm = vm, onDismiss = { showSleepDialog = false })
    }
}

@Composable
private fun QueueTab(nav: NavHostController, vm: MainViewModel, tracks: List<TrackEntity>) {
    if (tracks.isEmpty()) EmptyLibrary()
    else LazyColumn(Modifier.fillMaxSize()) {
        items(tracks, key = { it.id }) { t ->
            TrackRow(
                track = t,
                onClick = {
                    val idx = tracks.indexOf(t)
                    vm.playerController.setQueue(tracks, idx, play = true)
                    nav.navigate(Routes.PLAYER)
                },
                onLongClick = {
                    vm.playerController.setQueue(tracks, tracks.indexOf(t), play = false)
                }
            )
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun FoldersTab(nav: NavHostController, vm: MainViewModel) {
    val folders by vm.folders.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize()) {
        items(folders, key = { it }) { folder ->
            ListItem(
                headlineContent = { Text(folder.substringAfterLast('/')) },
                supportingContent = {
                    Text(folder, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                leadingContent = { Icon(Icons.Default.Folder, null) },
                modifier = Modifier.clickable {
                    nav.navigate(Routes.FOLDER_DETAIL + "?path=" + android.net.Uri.encode(folder))
                }
            )
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun PlaylistsTab(nav: NavHostController, vm: MainViewModel) {
    val playlists by vm.playlists.collectAsStateWithLifecycle()
    var showCreate by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }

    LazyColumn(Modifier.fillMaxSize()) {
        item {
            ListItem(
                headlineContent = { Text("Все треки") },
                leadingContent = { Icon(Icons.Default.LibraryMusic, null) },
                modifier = Modifier.clickable { nav.navigate(Routes.ALL_TRACKS) }
            )
        }
        item {
            ListItem(
                headlineContent = { Text("Избранное") },
                leadingContent = { Icon(Icons.Default.Favorite, null) },
                modifier = Modifier.clickable { nav.navigate(Routes.FAVORITES) }
            )
        }
        item {
            Text(
                "Ваши плейлисты",
                modifier = Modifier.padding(16.dp, 8.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
        item {
            Button(
                onClick = { showCreate = true },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) { Text("Создать плейлист") }
        }
        items(playlists, key = { it.id }) { p ->
            ListItem(
                headlineContent = { Text(p.name) },
                leadingContent = { Icon(Icons.Default.QueueMusic, null) },
                modifier = Modifier.clickable { nav.navigate(Routes.playlist(p.id)) }
            )
        }
        item { Spacer(Modifier.height(96.dp)) }
    }

    if (showCreate) {
        AlertDialog(
            onDismissRequest = { showCreate = false },
            title = { Text("Новый плейлист") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.createPlaylist(newName.ifBlank { "Плейлист" })
                    newName = ""
                    showCreate = false
                }) { Text("Создать") }
            },
            dismissButton = { TextButton(onClick = { showCreate = false }) { Text("Отмена") } }
        )
    }
}

@Composable
private fun AlbumsTab(nav: NavHostController, vm: MainViewModel) {
    val albums by vm.albums.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize()) {
        items(albums, key = { it }) { a ->
            ListItem(
                headlineContent = { Text(a) },
                leadingContent = { Icon(Icons.Default.Album, null) },
                modifier = Modifier.clickable {
                    nav.navigate(Routes.ALBUM_DETAIL + "?name=" + android.net.Uri.encode(a))
                }
            )
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun ArtistsTab(nav: NavHostController, vm: MainViewModel) {
    val artists by vm.artists.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize()) {
        items(artists, key = { it }) { a ->
            ListItem(
                headlineContent = { Text(a) },
                leadingContent = { Icon(Icons.Default.Person, null) },
                modifier = Modifier.clickable {
                    nav.navigate(Routes.ARTIST_DETAIL + "?name=" + android.net.Uri.encode(a))
                }
            )
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}

@Composable
private fun SearchTab(nav: NavHostController, vm: MainViewModel) {
    var q by remember { mutableStateOf("") }
    val results by remember(q) { derivedStateOf { q } }
        .let { flowOf(it) }
        .let { _ -> vm.search(q) }
        .collectAsStateWithLifecycle(initialValue = emptyList())

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = q,
            onValueChange = { q = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp),
            placeholder = { Text("Название, исполнитель, альбом") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true
        )
        LazyColumn(Modifier.fillMaxSize()) {
            items(results, key = { it.id }) { t ->
                TrackRow(
                    track = t,
                    onClick = {
                        val list = results
                        vm.playerController.setQueue(list, list.indexOf(t), play = true)
                        nav.navigate(Routes.PLAYER)
                    },
                    onLongClick = {}
                )
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.MusicNote,
                null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text("Медиатека пуста", style = MaterialTheme.typography.titleMedium)
            Text(
                "Добавьте музыку на устройство и выполните сканирование",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
