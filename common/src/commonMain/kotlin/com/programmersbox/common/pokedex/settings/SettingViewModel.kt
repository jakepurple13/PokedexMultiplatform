package com.programmersbox.common.pokedex.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.programmersbox.common.pokedex.database.PokedexDatabase
import com.programmersbox.common.pokedex.database.PokemonDbList
import com.programmersbox.common.pokedex.database.ThemeType
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

internal class SettingViewModel(
    private val pokedexDatabase: PokedexDatabase
) : ViewModel() {

    var pokemonLists by mutableStateOf(PokemonDbList())

    var themeType by mutableStateOf(ThemeType.Default)

    init {
        viewModelScope.launch {
            pokedexDatabase.getPokemonLists()
                .onEach { pokemonLists = it }
                .launchIn(this)
        }

        viewModelScope.launch {
            pokedexDatabase.getSettings()
                .onEach { themeType = it.themeType }
                .launchIn(this)
        }
    }

    fun clearListCache() {
        viewModelScope.launch {
            pokedexDatabase.clearPokemonCache()
            pokedexDatabase.setCacheState(false)
        }
    }

    fun clearInfoCache() {
        viewModelScope.launch { pokedexDatabase.clearPokemonInfoCache() }
    }

    fun changeTheme(themeType: ThemeType) {
        viewModelScope.launch { pokedexDatabase.setTheme(themeType) }
    }
}