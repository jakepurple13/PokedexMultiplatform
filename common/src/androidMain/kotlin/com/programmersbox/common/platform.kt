package com.programmersbox.common

import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.runtime.Composable

public actual fun getPlatformName(): String {
    return "Android"
}

@Composable
public fun UIShow() {
    App()
}

public actual suspend fun playAudio(url: String) {
    try {
        val media = MediaPlayer()
        media.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
        )
        media.setDataSource(url)
        media.prepareAsync()
        media.setOnCompletionListener { media.release() }
        media.setOnPreparedListener { it.start() }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
