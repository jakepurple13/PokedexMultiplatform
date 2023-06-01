package com.programmersbox.common

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
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

@Composable
public actual fun SortingContainer(onDismiss: () -> Unit, block: @Composable () -> Unit) {
    Dialog(
        onCloseRequest = onDismiss,
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            block()
        }
    }
}

@Composable
public actual fun ScrollbarSupport(
    scrollState: LazyListState,
    modifier: Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        style = LocalScrollbarStyle.current.copy(
            hoverColor = MaterialTheme.colorScheme.onSurface,
            unhoverColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxHeight()
    )
}

@Composable
public actual fun ScrollbarSupport(
    scrollState: LazyGridState,
    modifier: Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        style = LocalScrollbarStyle.current.copy(
            hoverColor = MaterialTheme.colorScheme.onSurface,
            unhoverColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxHeight()
    )
}

@Composable
public actual fun ScrollbarSupport(
    scrollState: ScrollState,
    modifier: Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        style = LocalScrollbarStyle.current.copy(
            hoverColor = MaterialTheme.colorScheme.onSurface,
            unhoverColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier.fillMaxHeight()
    )
}