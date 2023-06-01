package com.programmersbox.common.pokedex.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.programmersbox.common.pokedex.database.PokedexDatabase
import com.programmersbox.common.pokedex.database.toPokemon
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import moe.tlaster.precompose.viewmodel.ViewModel

internal class SearchViewModel(
    pokedexDatabase: PokedexDatabase
) : ViewModel() {

    var searchQuery by mutableStateOf("")

    val searchList = snapshotFlow { searchQuery }
        .flatMapLatest { pokedexDatabase.searchPokemon(it).map { s -> s.map { p -> p.toPokemon() } } }


}