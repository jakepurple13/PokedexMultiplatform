package com.programmersbox.common

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.programmersbox.common.pokedex.Pokemon
import com.programmersbox.common.pokedex.database.LocalPokedexDatabase
import com.programmersbox.common.pokedex.database.PokedexDatabase
import com.programmersbox.common.pokedex.detail.PokedexDetailScreen
import com.programmersbox.common.pokedex.list.PokedexScreen
import com.programmersbox.common.pokedex.search.SearchScreen
import com.programmersbox.common.pokedex.settings.SettingScreen
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.transition.NavTransition
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

@Composable
internal fun App() {
    val db = LocalPokedexDatabase.current
    val vm = viewModel(AppViewModel::class) { AppViewModel(db) }
    Surface {
        NavHost(
            navigator = LocalNavController.current,
            initialRoute = PokedexScreens.Pokedex.route,
            navTransition = NavTransition(
                createTransition = slideInHorizontally { it },
                destroyTransition = slideOutHorizontally { it },
                resumeTransition = slideInHorizontally { -it },
                pauseTransition = slideOutHorizontally { -it },
            )
        ) {
            scene(PokedexScreens.Pokedex.route) { PokedexScreen() }
            scene(PokedexScreens.Detail.route) { PokedexDetailScreen(it, vm.pokemonList) }
            scene(PokedexScreens.Search.route) { SearchScreen() }
            scene(PokedexScreens.Settings.route) { SettingScreen() }
        }
    }
}

private class AppViewModel(
    pokedexDatabase: PokedexDatabase
) : ViewModel() {
    val pokemonList = mutableStateListOf<Pokemon>()

    init {
        viewModelScope.launch {
            pokedexDatabase.getPokemonList()
                .onEach {
                    pokemonList.clear()
                    pokemonList.addAll(it)
                }
                .launchIn(this)
        }
    }

}

internal val LocalNavController = staticCompositionLocalOf<Navigator> { error("Nope") }

internal enum class PokedexScreens(val route: String) {
    Pokedex("pokedex"),
    Detail("detail/{name}"),
    Search("search"),
    Settings("settings")
}

internal fun Navigator.navigateToDetail(name: String) =
    navigate(PokedexScreens.Detail.route.replace("{name}", name))

internal fun Navigator.navigate(
    screen: PokedexScreens,
    options: NavOptions? = null
) = navigate(screen.route, options)