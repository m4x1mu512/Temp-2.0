package com.example.temp.playback

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class MediaButtonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Media3 MediaSessionService автоматически обрабатывает медиа-кнопки.
    }
}
