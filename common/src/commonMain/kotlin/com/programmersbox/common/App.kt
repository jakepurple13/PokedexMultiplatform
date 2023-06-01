package com.programmersbox.common

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.programmersbox.common.pokedex.database.LocalPokedexDatabase
import com.programmersbox.common.pokedex.database.PokedexDatabase
import com.programmersbox.common.pokedex.detail.PokedexDetailScreen
import com.programmersbox.common.pokedex.list.PokedexScreen
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
internal fun App() {
    val navigator = rememberNavigator()
    CompositionLocalProvider(
        LocalPokedexDatabase provides remember { PokedexDatabase() },
        LocalNavController provides navigator
    ) {
        Surface {
            NavHost(
                navigator = navigator,
                initialRoute = PokedexScreens.Pokedex.route
            ) {
                scene(PokedexScreens.Pokedex.route) { PokedexScreen() }
                scene(PokedexScreens.Detail.route) { PokedexDetailScreen(it) }
                //this.dialog()
                //this.floating()
            }
        }
    }
}

internal val LocalNavController = staticCompositionLocalOf<Navigator> { error("Nope") }

internal enum class PokedexScreens(val route: String) {
    Pokedex("pokedex"),
    Detail("detail/{name}")
}

internal fun Navigator.navigateToDetail(name: String) =
    navigate(PokedexScreens.Detail.route.replace("{name}", name))