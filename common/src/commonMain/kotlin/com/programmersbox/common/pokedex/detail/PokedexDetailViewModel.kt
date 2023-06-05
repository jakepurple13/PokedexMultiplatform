package com.programmersbox.common.pokedex.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.programmersbox.common.playAudio
import com.programmersbox.common.pokedex.PokedexService
import com.programmersbox.common.pokedex.PokemonInfo
import com.programmersbox.common.pokedex.database.PokedexDatabase
import com.programmersbox.common.pokedex.database.SavedPokemon
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class PokedexDetailViewModel(
    private val name: String?,
    private val pokedexDatabase: PokedexDatabase,
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    var pokemonInfo: DetailState by mutableStateOf(DetailState.Loading)

    var savedPokemon by mutableStateOf<SavedPokemon?>(null)

    init {
        load()
    }

    private fun load() {
        pokemonInfo = DetailState.Loading
        viewModelScope.launch {
            name?.let { n ->
                val fromDb = pokedexDatabase
                    .getPokemonInfo(n)
                    ?.let { DetailState.Success(it) }
                pokemonInfo = fromDb ?: PokedexService.fetchPokemon(n)
                    .onSuccess { pokedexDatabase.insertPokemonInfo(it) }
                    .fold(
                        onSuccess = { DetailState.Success(it) },
                        onFailure = { DetailState.Error }
                    )
                pokedexDatabase.saved(n)
                    .onEach { savedPokemon = it }
                    .launchIn(viewModelScope)
            }
        }
    }

    fun save() {
        when (val state = pokemonInfo) {
            is DetailState.Success -> {
                viewModelScope.launch {
                    pokedexDatabase.getSinglePokemon(state.pokemonInfo.name)?.url?.let { url ->
                        pokedexDatabase.save(
                            SavedPokemon().apply {
                                this.url = url
                                name = state.pokemonInfo.name
                                imageUrl = state.pokemonInfo.imageUrl
                                pokedexEntry = state.pokemonInfo.id
                            }
                        )
                    }
                }
            }

            else -> {}
        }
    }

    fun remove() {
        viewModelScope.launch {
            savedPokemon?.let { pokedexDatabase.remove(it) }
        }
    }

    fun playCry(url: String) {
        viewModelScope.launch {
            playAudio(url)
        }
    }
}

internal sealed class DetailState {
    class Success(val pokemonInfo: PokemonInfo) : DetailState()

    object Loading : DetailState()

    object Error : DetailState()
}