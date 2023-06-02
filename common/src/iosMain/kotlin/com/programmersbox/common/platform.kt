package com.programmersbox.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import moe.tlaster.precompose.PreComposeApplication
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.dataWithContentsOfURL
import platform.UIKit.UIViewController

public actual fun getPlatformName(): String {
    return "iOS"
}

@Composable
private fun UIShow() {
    App()
}

public fun MainViewController(): UIViewController = PreComposeApplication("Pokedex") {
    PokedexTheme(
        colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                Spacer(Modifier.height(30.dp))
                UIShow()
            }
        }
    }
}

public actual suspend fun playAudio(url: String) {
    val audioUrl = NSURL.URLWithString(url)
    val data = audioUrl?.let { NSData.dataWithContentsOfURL(it) }
    val player = data?.let { AVAudioPlayer(it, null, null) }
    player?.play()
}

@Composable
public actual fun SortingContainer(onDismiss: () -> Unit, block: @Composable () -> Unit) {

}

@Composable
public actual fun ScrollbarSupport(
    scrollState: LazyListState,
    modifier: Modifier
) {

}

@Composable
public actual fun ScrollbarSupport(
    scrollState: LazyGridState,
    modifier: Modifier
) {

}

@Composable
public actual fun ScrollbarSupport(
    scrollState: ScrollState,
    modifier: Modifier
) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public actual fun DrawerContainer(
    drawerState: DrawerState,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet { drawerContent() }
        },
        content = content
    )
}

public actual val AllImageSize: Pair<Dp, Dp> = 100.dp to 160.dp