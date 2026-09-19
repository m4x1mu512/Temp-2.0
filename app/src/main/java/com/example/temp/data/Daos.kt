package com.example.temp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY title COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE uri = :uri LIMIT 1")
    suspend fun getByUri(uri: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tracks: List<TrackEntity>)

    @Query("DELETE FROM tracks")
    suspend fun clear()

    @Query("DELETE FROM tracks WHERE uri NOT IN (:uris)")
    suspend fun deleteMissing(uris: List<String>)

    @Query("""
        SELECT * FROM tracks
        WHERE title LIKE '%' || :q || '%'
           OR artist LIKE '%' || :q || '%'
           OR album LIKE '%' || :q || '%'
        ORDER BY title COLLATE NOCASE ASC
    """)
    fun search(q: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY album, title")
    fun byArtist(artist: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY title")
    fun byAlbum(album: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE folder = :folder ORDER BY title")
    fun byFolder(folder: String): Flow<List<TrackEntity>>

    @Query("SELECT DISTINCT folder FROM tracks WHERE folder IS NOT NULL ORDER BY folder")
    fun observeFolders(): Flow<List<String>>

    @Query("SELECT DISTINCT album FROM tracks WHERE album IS NOT NULL ORDER BY album")
    fun observeAlbums(): Flow<List<String>>

    @Query("SELECT DISTINCT artist FROM tracks WHERE artist IS NOT NULL ORDER BY artist")
    fun observeArtists(): Flow<List<String>>
}

@Dao
interface FavoriteDao {
    @Query("SELECT trackId FROM favorites")
    fun observeFavoriteIds(): Flow<List<Long>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE trackId = :id")
    suspend fun remove(id: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE trackId = :id)")
    suspend fun isFavorite(id: Long): Boolean
}

@Dao
interface PlaylistDao {
    @Query("SELECT * FROM playlists ORDER BY name COLLATE NOCASE ASC")
    fun observeAll(): Flow<List<PlaylistEntity>>

    @Insert
    suspend fun insert(p: PlaylistEntity): Long

    @Query("UPDATE playlists SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM playlists WHERE id = :id")
    suspend fun delete(id: Long)
}

@Dao
interface PlaylistTrackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(ref: PlaylistTrackCrossRef)

    @Query("DELETE FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun remove(playlistId: Long, trackId: Long)

    @Query("""
        SELECT t.* FROM tracks t
        INNER JOIN playlist_tracks pt ON pt.trackId = t.id
        WHERE pt.playlistId = :playlistId
        ORDER BY pt.position ASC
    """)
    fun observeTracks(playlistId: Long): Flow<List<TrackEntity>>
}
