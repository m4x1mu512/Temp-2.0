package com.example.temp.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaScanner(private val ctx: Context) {

    suspend fun scan(folderFilter: String? = null): List<TrackEntity> = withContext(Dispatchers.IO) {
        val list = mutableListOf<TrackEntity>()
        val collection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        val selectionParts = mutableListOf<String>()
        val selectionArgs = mutableListOf<String>()
        selectionParts += "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        if (!folderFilter.isNullOrBlank()) {
            selectionParts += "${MediaStore.Audio.Media.DATA} LIKE ?"
            selectionArgs += "$folderFilter%"
        }
        val selection = selectionParts.joinToString(" AND ")

        runCatching {
            ctx.contentResolver.query(
                collection, projection, selection,
                selectionArgs.toTypedArray(),
                "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"
            )?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = c.getColumnIndex(MediaStore.Audio.Media.TITLE)
                val artistCol = c.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                val albumCol = c.getColumnIndex(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                val durCol = c.getColumnIndex(MediaStore.Audio.Media.DURATION)
                val sizeCol = c.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val dateCol = c.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val mimeCol = c.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val dataCol = c.getColumnIndex(MediaStore.Audio.Media.DATA)
                val nameCol = c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)

                while (c.moveToNext()) {
                    val id = c.getLong(idCol)
                    val uri = ContentUris.withAppendedId(collection, id).toString()
                    val rawTitle = c.getStringOrNull(titleCol)
                    val displayName = c.getStringOrNull(nameCol)
                    val title = rawTitle?.takeIf { it.isNotBlank() && it != "<unknown>" }
                        ?: displayName?.substringBeforeLast('.')
                        ?: "Неизвестный трек"

                    val artist = c.getStringOrNull(artistCol)
                        ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                    val album = c.getStringOrNull(albumCol)
                        ?.takeIf { it.isNotBlank() && it != "<unknown>" }
                    val albumId = if (albumIdCol >= 0) c.getLong(albumIdCol) else null
                    val duration = if (durCol >= 0) c.getLong(durCol) else 0L
                    val size = if (sizeCol >= 0) c.getLong(sizeCol) else 0L
                    val date = if (dateCol >= 0) c.getLong(dateCol) else 0L
                    val mime = c.getStringOrNull(mimeCol)
                    val path = c.getStringOrNull(dataCol)
                    val folder = path?.substringBeforeLast('/')

                    val artwork = albumId?.let {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"), it
                        ).toString()
                    }

                    list += TrackEntity(
                        uri = uri,
                        title = title,
                        artist = artist,
                        album = album,
                        albumId = albumId,
                        artistId = null,
                        folder = folder,
                        durationMs = duration,
                        sizeBytes = size,
                        dateAdded = date,
                        mimeType = mime,
                        artworkUri = artwork
                    )
                }
            }
        }
        list
    }

    private fun android.database.Cursor.getStringOrNull(idx: Int): String? =
        if (idx >= 0 && !isNull(idx)) getString(idx) else null
}
