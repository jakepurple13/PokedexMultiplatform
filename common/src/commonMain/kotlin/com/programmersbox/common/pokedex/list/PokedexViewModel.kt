package com.programmersbox.common.pokedex.list

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.programmersbox.common.pokedex.Pokemon
import com.programmersbox.common.pokedex.database.PokedexDatabase
import com.programmersbox.common.pokedex.database.SavedPokemon
import com.programmersbox.common.pokedex.database.toPokemon
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

internal class PokedexViewModel(
    private val pokedexDatabase: PokedexDatabase,
) : ViewModel() {

    private val dao = pokedexDatabase

    var pokemonSort by mutableStateOf(PokemonSort.Index)
    var pokemonListType by mutableStateOf(PokemonListType.Grid)

    private val pokedexEntries = mutableStateListOf<Pokemon>()

    val pokedexEntriesSorted by derivedStateOf {
        when (pokemonSort) {
            PokemonSort.Index -> pokedexEntries
            PokemonSort.Alphabetical -> pokedexEntries.sortedBy { it.name }
        }
    }

    val savedPokemon = mutableStateListOf<SavedPokemon>()

    init {
        viewModelScope.launch {
            pokedexDatabase.getPokemonList()
                .map { it.map { it.toPokemon() } }
                .onEach {
                    pokedexEntries.clear()
                    pokedexEntries.addAll(it)
                }
                .launchIn(viewModelScope)
        }
        viewModelScope.launch {
            pokedexDatabase.getSavedPokemonList()
                .onEach {
                    savedPokemon.clear()
                    savedPokemon.addAll(it)
                }
                .launchIn(viewModelScope)
        }
        viewModelScope.launch {
            pokedexDatabase.getSettings()
                .onEach {
                    pokemonSort = it.sort
                    pokemonListType = it.listType
                }
                .launchIn(viewModelScope)
        }
        /*viewModelScope.launch {
            PokedexService.fetchPokemonList(0)
                .onSuccess {
                    dao.insertPokemon(it.results)
                }
        }*/
    }

    /*@OptIn(ExperimentalPagingApi::class, ExperimentalCoroutinesApi::class)
    val pager = snapshotFlow { pokemonSort }
        .flatMapLatest { sort ->
            Pager<Int, PokemonDbList>(
                config = PagingConfig(
                    pageSize = PokedexService.PAGE_SIZE + 1,
                    enablePlaceholders = true,
                    initialLoadSize = PokedexService.PAGE_SIZE
                ),
                pagingSourceFactory = {
                    when (sort) {
                        PokemonSort.Index -> dao.getPokemonList()
                        PokemonSort.Alphabetical -> dao.getPokemonList().map { it.sortedBy { it.name } }
                    }
                },
                initialKey = 0
            )
                .flow
                .map { it.map { p -> p.listDb.map { it.toPokemon() } } }
                .cachedIn(viewModelScope)
        }*/

    var searchQuery by mutableStateOf("")

    val searchList = snapshotFlow { searchQuery }
        .flatMapLatest { dao.searchPokemon(it) }

    fun toggleViewType() {
        viewModelScope.launch {
            pokedexDatabase.updateSettings(
                listType = when (pokemonListType) {
                    PokemonListType.Grid -> PokemonListType.List
                    PokemonListType.List -> PokemonListType.Grid
                }
            )
        }
    }
}

internal enum class PokemonSort(val icon: ImageVector) {
    Index(Icons.Default.Numbers),
    Alphabetical(Icons.Default.SortByAlpha)
}

internal enum class PokemonListType(val icon: ImageVector) {
    Grid(Icons.Default.GridView),
    List(Icons.Default.ViewList);
}