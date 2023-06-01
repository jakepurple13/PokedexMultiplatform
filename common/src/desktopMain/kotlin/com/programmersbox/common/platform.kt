package com.programmersbox.common

import androidx.compose.runtime.Composable
import javazoom.jl.player.Player
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL


public actual fun getPlatformName(): String {
    return "Desktop"
}

@Composable
public fun UIShow() {
    App()
}

@OptIn(DelicateCoroutinesApi::class)
public actual suspend fun playAudio(url: String) {
    GlobalScope.launch {
        Player(URL(url).openStream()).play()
    }
}