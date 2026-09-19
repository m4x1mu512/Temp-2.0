package com.example.temp

import android.app.Application
import com.example.temp.data.MusicDatabase
import com.example.temp.data.PlaylistRepository
import com.example.temp.data.SettingsRepository
import com.example.temp.data.TrackRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class TempApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { MusicDatabase.get(this) }
    val trackRepository by lazy { TrackRepository(database.trackDao(), database.favoriteDao()) }
    val playlistRepository by lazy {
        PlaylistRepository(database.playlistDao(), database.playlistTrackDao())
    }
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: TempApp
            private set
    }
}
