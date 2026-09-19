package com.example.temp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun TopIconsRow(
    onQueue: () -> Unit,
    onFolders: () -> Unit,
    onPlaylists: () -> Unit,
    onAlbums: () -> Unit,
    onArtists: () -> Unit,
    onSearch: () -> Unit,
    onMore: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icons = listOf(
            Icons.Default.PlaylistPlay to onQueue,
            Icons.Default.Folder to onFolders,
            Icons.Default.QueueMusic to onPlaylists,
            Icons.Default.Album to onAlbums,
            Icons.Default.Person to onArtists,
            Icons.Default.Search to onSearch
        )
        icons.forEach { (icon, onClick) ->
            IconButton(
                onClick = onClick,
                modifier = Modifier.weight(1f)
            ) { Icon(icon, null) }
        }
        Spacer(Modifier.width(12.dp))
        IconButton(onClick = onMore) { Icon(Icons.Default.MoreVert, null) }
    }
}
