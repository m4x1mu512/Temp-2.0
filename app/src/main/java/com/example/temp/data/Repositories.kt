package com.example.temp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrackRepository(
    private val trackDao: TrackDao,
    private val favoriteDao: FavoriteDao
) {
    fun observeAll(): Flow<List<TrackEntity>> = trackDao.observeAll()
    fun observeFavorites(): Flow<List<Long>> = favoriteDao.observeFavoriteIds()
    fun search(q: String): Flow<List<TrackEntity>> = trackDao.search(q)
    fun byArtist(a: String) = trackDao.byArtist(a)
    fun byAlbum(a: String) = trackDao.byAlbum(a)
    fun byFolder(f: String) = trackDao.byFolder(f)
    fun observeFolders() = trackDao.observeFolders()
    fun observeAlbums() = trackDao.observeAlbums()
    fun observeArtists() = trackDao.observeArtists()

    suspend fun getById(id: Long) = trackDao.getById(id)
    suspend fun getByUri(uri: String) = trackDao.getByUri(uri)

    suspend fun replaceAll(tracks: List<TrackEntity>) {
        trackDao.clear()
        trackDao.insertAll(tracks)
    }

    suspend fun toggleFavorite(id: Long) {
        if (favoriteDao.isFavorite(id)) favoriteDao.remove(id)
        else favoriteDao.add(FavoriteEntity(id))
    }
}

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val refDao: PlaylistTrackDao
) {
    fun observeAll(): Flow<List<PlaylistEntity>> = playlistDao.observeAll()
    fun tracks(playlistId: Long): Flow<List<TrackEntity>> = refDao.observeTracks(playlistId)

    suspend fun create(name: String): Long =
        playlistDao.insert(PlaylistEntity(name = name.trim().ifBlank { "Плейлист" }))

    suspend fun rename(id: Long, name: String) = playlistDao.rename(id, name)
    suspend fun delete(id: Long) = playlistDao.delete(id)
    suspend fun addTrack(playlistId: Long, trackId: Long) =
        refDao.add(PlaylistTrackCrossRef(playlistId, trackId))
    suspend fun removeTrack(playlistId: Long, trackId: Long) =
        refDao.remove(playlistId, trackId)
}
