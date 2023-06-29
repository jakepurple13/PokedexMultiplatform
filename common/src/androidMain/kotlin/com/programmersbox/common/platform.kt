@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.window.layout.DisplayFeature
import com.google.accompanist.adaptive.HorizontalTwoPaneStrategy
import com.google.accompanist.adaptive.TwoPane
import com.programmersbox.common.pokedex.Pokemon
import com.programmersbox.common.pokedex.database.LocalPokedexDatabase
import com.programmersbox.common.pokedex.database.PokedexDatabase
import com.programmersbox.common.pokedex.detail.PokedexDetailScreen
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlin.time.Duration.Companion.minutes

public actual fun getPlatformName(): String {
    return "Android"
}

@Composable
public fun UIShow() {
    var pokemonChosen by rememberSaveable { mutableStateOf("bulbasaur") }
    val navController = LocalNavController.current
    val size = LocalWindowClassSize.current
    CompositionLocalProvider(
        LocalCurrentPokemonChosen provides pokemonChosen
    ) {
        App(
            navController = navController,
            onDetailNavigation = {
                //FIXME: This isn't working on configuration changes
                when (size.widthSizeClass) {
                    WindowWidthSizeClass.Expanded -> pokemonChosen = it
                    else -> navController.navigateToDetail(it)
                }
            }
        )
    }
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

@Composable
public actual fun SortingContainer(onDismiss: () -> Unit, block: @Composable () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Surface(shape = MaterialTheme.shapes.extraLarge) {
            block()
        }
    }
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
    val features = LocalDisplayFeatures.current
    val size = LocalWindowClassSize.current
    when {
        size.widthSizeClass == WindowWidthSizeClass.Expanded && size.heightSizeClass != WindowHeightSizeClass.Compact -> {
            val db = LocalPokedexDatabase.current
            val vm = viewModel(BigViewModel::class) { BigViewModel(db) }
            TwoPane(
                first = {
                    DismissibleNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = { DismissibleDrawerSheet { drawerContent() } },
                        content = content
                    )
                },
                second = {
                    PokedexDetailScreen(
                        backStackEntry = null,
                        name = LocalCurrentPokemonChosen.current,
                        list = vm.pokemonList
                    )
                },
                displayFeatures = features,
                strategy = HorizontalTwoPaneStrategy(splitFraction = 0.5f)
            )

            val dockStatus by broadcastReceiver(
                defaultValue = false,
                intentFilter = IntentFilter(Intent.ACTION_DOCK_EVENT)
            ) { _, i ->
                val state = i.getIntExtra(Intent.EXTRA_DOCK_STATE, -1)
                state != Intent.EXTRA_DOCK_STATE_UNDOCKED
            }

            AnimatedVisibility(
                dockStatus,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                var newPokemon by remember { mutableStateOf(0) }
                val randomPokemon: Pokemon? by produceState<Pokemon?>(
                    initialValue = null,
                    key1 = dockStatus,
                    key2 = vm.pokemonList,
                    key3 = newPokemon
                ) {
                    while (dockStatus) {
                        value = vm.pokemonList.randomOrNull()
                        delay(1.minutes.inWholeMilliseconds)
                    }
                }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    onClick = { newPokemon++ }
                ) {
                    Crossfade(randomPokemon) { pokemon ->
                        pokemon?.let { PokemonImage(it) }
                    }
                }
            }
        }

        else -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = { ModalDrawerSheet { drawerContent() } },
                content = content
            )
        }
    }
}

private class BigViewModel(
    pokedexDatabase: PokedexDatabase
) : ViewModel() {
    val pokemonList = mutableStateListOf<Pokemon>()

    init {
        pokedexDatabase.getPokemonList()
            .onEach {
                pokemonList.clear()
                pokemonList.addAll(it)
            }
            .launchIn(viewModelScope)
    }
}

public actual val AllImageSize: Pair<Dp, Dp> = 100.dp to 160.dp

public val LocalWindowClassSize: ProvidableCompositionLocal<WindowSizeClass> =
    staticCompositionLocalOf { error("Nothing here") }
public val LocalDisplayFeatures: ProvidableCompositionLocal<List<DisplayFeature>> =
    staticCompositionLocalOf { error("Nothing here") }
public val LocalCurrentPokemonChosen: ProvidableCompositionLocal<String> =
    compositionLocalOf { error("Nothing here") }

@Composable
private fun <T : Any> broadcastReceiver(
    defaultValue: T,
    intentFilter: IntentFilter,
    tick: (context: Context, intent: Intent) -> T
): State<T> {
    val item: MutableState<T> = remember { mutableStateOf(defaultValue) }
    val context = LocalContext.current

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                item.value = tick(context, intent)
            }
        }
        context.registerReceiver(receiver, intentFilter)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return item
}

@Composable
private fun PokemonImage(pokemon: Pokemon) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        KamelImage(
            resource = asyncPainterResource(pokemon.imageUrl),
            contentDescription = pokemon.name,
            contentScale = ContentScale.FillWidth,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(3f) }),
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxWidth(.9f)
                .wrapContentHeight(Alignment.Top, true)
                .scale(1f, 1.8f)
                .blur(70.dp, BlurredEdgeTreatment.Unbounded)
                .alpha(.5f)
        )
        KamelImage(
            resource = asyncPainterResource(pokemon.imageUrl),
            contentDescription = pokemon.name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .fillMaxHeight()
        )
    }
}