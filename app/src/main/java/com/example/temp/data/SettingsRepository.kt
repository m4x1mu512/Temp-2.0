package com.example.temp.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "temp_settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class VisualizerType { SPECTRUM, WAVE, CIRCLE }

data class AppSettingsData(
    val theme: ThemeMode = ThemeMode.LIGHT,
    val visualizerEnabled: Boolean = true,
    val visualizerType: VisualizerType = VisualizerType.SPECTRUM,
    val visualizerBars: Int = 32,
    val visualizerSensitivity: Float = 1f,
    val sleepTimerMinutes: Int = 0, // 0 = off, -1 = end of track
    val lastTrackId: Long = -1L,
    val lastPositionMs: Long = 0L,
    val lastQueueIds: List<Long> = emptyList(),
    val lastQueueIndex: Int = 0
)

class SettingsRepository(private val ctx: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val VIS_ENABLED = booleanPreferencesKey("vis_enabled")
        val VIS_TYPE = stringPreferencesKey("vis_type")
        val VIS_BARS = intPreferencesKey("vis_bars")
        val VIS_SENS = floatPreferencesKey("vis_sens")
        val SLEEP_MIN = intPreferencesKey("sleep_min")
        val LAST_TRACK = longPreferencesKey("last_track")
        val LAST_POS = longPreferencesKey("last_pos")
        val LAST_QUEUE = stringPreferencesKey("last_queue")
        val LAST_INDEX = intPreferencesKey("last_index")
    }

    val flow: Flow<AppSettingsData> = ctx.dataStore.data.map { p ->
        AppSettingsData(
            theme = runCatching { ThemeMode.valueOf(p[Keys.THEME] ?: "LIGHT") }
                .getOrDefault(ThemeMode.LIGHT),
            visualizerEnabled = p[Keys.VIS_ENABLED] ?: true,
            visualizerType = runCatching {
                VisualizerType.valueOf(p[Keys.VIS_TYPE] ?: "SPECTRUM")
            }.getOrDefault(VisualizerType.SPECTRUM),
            visualizerBars = p[Keys.VIS_BARS] ?: 32,
            visualizerSensitivity = p[Keys.VIS_SENS] ?: 1f,
            sleepTimerMinutes = p[Keys.SLEEP_MIN] ?: 0,
            lastTrackId = p[Keys.LAST_TRACK] ?: -1L,
            lastPositionMs = p[Keys.LAST_POS] ?: 0L,
            lastQueueIds = (p[Keys.LAST_QUEUE] ?: "")
                .split(',').mapNotNull { it.toLongOrNull() },
            lastQueueIndex = p[Keys.LAST_INDEX] ?: 0
        )
    }

    suspend fun setTheme(t: ThemeMode) = ctx.dataStore.edit { it[Keys.THEME] = t.name }
    suspend fun setVisualizerEnabled(v: Boolean) = ctx.dataStore.edit { it[Keys.VIS_ENABLED] = v }
    suspend fun setVisualizerType(v: VisualizerType) = ctx.dataStore.edit { it[Keys.VIS_TYPE] = v.name }
    suspend fun setVisualizerBars(v: Int) = ctx.dataStore.edit { it[Keys.VIS_BARS] = v }
    suspend fun setVisualizerSensitivity(v: Float) = ctx.dataStore.edit { it[Keys.VIS_SENS] = v }
    suspend fun setSleepTimerMinutes(v: Int) = ctx.dataStore.edit { it[Keys.SLEEP_MIN] = v }

    suspend fun savePlayback(trackId: Long, posMs: Long, queue: List<Long>, index: Int) {
        ctx.dataStore.edit {
            it[Keys.LAST_TRACK] = trackId
            it[Keys.LAST_POS] = posMs
            it[Keys.LAST_QUEUE] = queue.joinToString(",")
            it[Keys.LAST_INDEX] = index
        }
    }

    suspend fun reset() {
        ctx.dataStore.edit { it.clear() }
    }
}
