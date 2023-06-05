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
                .schemaVersion(23)
                .name(name)
                .migration(AutomaticSchemaMigration { })
                //.deleteRealmIfMigrationNeeded()
                .build()
        )
    }

    private val pokemonDbList: PokemonDbList = realm.initDbBlocking { PokemonDbList() }
    private val pokemonSettingsDb = realm.initDbBlocking { PokedexSettingsDb() }

    fun getSettings() = pokemonSettingsDb
        .asFlow()
        .mapNotNull { it.obj }
        .map {
            PokedexSettings(
                sort = PokemonSort.valueOf(it.sort),
                listType = PokemonListType.valueOf(it.listType),
                hasCache = it.hasCache,
                themeType = ThemeType.valueOf(it.themeType)
            )
        }

    fun getPokemonLists() = pokemonDbList
        .asFlow()
        .mapNotNull { it.obj }

    fun getPokemonList() = pokemonDbList
        .asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.listDb.map { p -> p.toPokemon() } }

    fun getSavedPokemonList() = pokemonDbList
        .asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.savedList }

    suspend fun insertPokemon(pokemon: List<Pokemon>) {
        realm.updateInfo<PokemonDbList> {
            it?.listDb?.addAll(pokemon.map { p -> p.toPokemonDb() })
        }
    }

    fun getPokemonInfo(name: String) = pokemonDbList
        .cachedInfo
        .find { it.name == name }
        ?.toPokemonInfo()

    suspend fun insertPokemonInfo(pokemonInfo: PokemonInfo) {
        realm.updateInfo<PokemonDbList> {
            it?.cachedInfo?.add(pokemonInfo.toPokemonInfoDb())
        }
    }

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

    fun getSinglePokemon(name: String) = pokemonDbList
        .listDb
        .find { it.name == name }

    fun searchPokemon(searchQuery: String) = pokemonDbList
        .asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.listDb.filter { l -> l.name.contains(searchQuery, true) } }

    fun saved(name: String) = pokemonDbList
        .asFlow()
        .mapNotNull { it.obj }
        .map { it.savedList.find { s -> s.name == name } }

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

    suspend fun setTheme(themeType: ThemeType) {
        realm.updateInfo<PokedexSettingsDb> { it?.themeType = themeType.name }
    }
}

private suspend inline fun <reified T : RealmObject> Realm.updateInfo(crossinline block: MutableRealm.(T?) -> Unit) {
    query(T::class).first().find()?.also { info ->
        write { block(findLatest(info)) }
    }
}

private inline fun <reified T : RealmObject> Realm.initDbBlocking(crossinline default: () -> T): T {
    val f = query(T::class).first().find()
    return f ?: writeBlocking { copyToRealm(default()) }
}