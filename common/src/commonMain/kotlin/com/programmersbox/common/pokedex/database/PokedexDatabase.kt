package com.programmersbox.common.pokedex.database

import androidx.compose.runtime.staticCompositionLocalOf
import com.programmersbox.common.pokedex.Pokemon
import com.programmersbox.common.pokedex.PokemonInfo
import com.programmersbox.common.pokedex.list.PokemonListType
import com.programmersbox.common.pokedex.list.PokemonSort
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

internal val LocalPokedexDatabase = staticCompositionLocalOf<PokedexDatabase> { error("Nothing here!") }

internal class PokedexDatabase(name: String = Realm.DEFAULT_FILE_NAME) {
    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(
                setOf(
                    PokedexSettingsDb::class,
                    PokemonDb::class,
                    PokemonInfoDb::class,
                    SavedPokemon::class,
                    PokemonDbList::class
                )
            )
                .schemaVersion(22)
                .name(name)
                .migration(AutomaticSchemaMigration { })
                .deleteRealmIfMigrationNeeded()
                .build()
        )
    }

    suspend fun getSettings() = realm.initDb { PokedexSettingsDb() }
        .asFlow()
        .mapNotNull { it.obj }
        .map {
            PokedexSettings(
                sort = PokemonSort.valueOf(it.sort),
                listType = PokemonListType.valueOf(it.listType),
                hasCache = it.hasCache
            )
        }

    suspend fun getPokemonLists() = realm.initDb { PokemonDbList() }
        .asFlow()
        .mapNotNull { it.obj }

    suspend fun getPokemonList() = realm.initDb { PokemonDbList() }
        .asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.listDb.map { p -> p.toPokemon() } }

    suspend fun getSavedPokemonList() = realm.initDb { PokemonDbList() }
        .asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.savedList }

    suspend fun insertPokemon(pokemon: Pokemon) {
        realm.updateInfo<PokemonDbList> {
            it?.listDb?.add(pokemon.toPokemonDb())
        }
    }

    suspend fun insertPokemon(pokemon: List<Pokemon>) {
        realm.updateInfo<PokemonDbList> {
            it?.listDb?.addAll(pokemon.map { p -> p.toPokemonDb() })
        }
    }

    suspend fun getPokemonInfo(name: String) = realm.initDb { PokemonDbList() }
        .cachedInfo
        .find { it.name == name }
        ?.toPokemonInfo()

    suspend fun insertPokemonInfo(pokemonInfo: PokemonInfo) {
        realm.updateInfo<PokemonDbList> {
            it?.cachedInfo?.add(pokemonInfo.toPokemonInfoDb())
        }
    }

    /*suspend fun insertPokemon(pokemon: List<PokemonDb>) {
        realm.updateInfo<PokemonDbList> {
            it?.listDb?.addAll(pokemon)
        }
    }*/

    suspend fun clearPokemonCache() {
        realm.updateInfo<PokemonDbList> {
            it?.listDb?.clear()
        }
    }

    suspend fun clearPokemonInfoCache() {
        realm.updateInfo<PokemonDbList> {
            it?.cachedInfo?.clear()
        }
    }

    suspend fun setCacheState(state: Boolean) {
        realm.updateInfo<PokedexSettingsDb> { it?.hasCache = state }
    }

    suspend fun getSinglePokemon(name: String) = realm
        .initDb { PokemonDbList() }
        .listDb
        .find { it.name == name }

    suspend fun searchPokemon(searchQuery: String) = realm
        .initDb { PokemonDbList() }.asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.listDb.filter { it.name.contains(searchQuery, true) } }

    suspend fun saved(name: String) = realm
        .initDb { PokemonDbList() }
        .asFlow()
        .mapNotNull { it.obj }
        .map { it.savedList.find { it.name == name } }

    suspend fun save(savedPokemon: SavedPokemon) {
        realm.updateInfo<PokemonDbList> {
            it?.savedList?.add(savedPokemon)
        }
    }

    suspend fun remove(savedPokemon: SavedPokemon) {
        realm.updateInfo<PokemonDbList> {
            it?.savedList?.removeAll { s -> s.url == savedPokemon.url }
        }
    }

    suspend fun updateSettings(
        sort: PokemonSort? = null,
        listType: PokemonListType? = null
    ) {
        realm.updateInfo<PokedexSettingsDb> { s ->
            sort?.let { s?.sort = it.name }
            listType?.let { s?.listType = it.name }
        }
    }
}

private suspend inline fun <reified T : RealmObject> Realm.updateInfo(crossinline block: MutableRealm.(T?) -> Unit) {
    query(T::class).first().find()?.also { info ->
        write { block(findLatest(info)) }
    }
}

private suspend inline fun <reified T : RealmObject> Realm.initDb(crossinline default: () -> T): T {
    val f = query(T::class).first().find()
    return f ?: write { copyToRealm(default()) }
}