package com.programmersbox.common.pokedex.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.programmersbox.common.pokedex.database.PokedexDatabase
import com.programmersbox.common.pokedex.database.PokemonDbList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

internal class SettingViewModel(
    private val pokedexDatabase: PokedexDatabase
) : ViewModel() {

    var pokemonLists by mutableStateOf(PokemonDbList())

    init {
        viewModelScope.launch {
            pokedexDatabase.getPokemonLists()
                .onEach { pokemonLists = it }
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
}