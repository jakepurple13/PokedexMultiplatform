package com.programmersbox.common.pokedex.database

import androidx.compose.runtime.staticCompositionLocalOf
import com.programmersbox.common.pokedex.Pokemon
import io.realm.kotlin.MutableRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.asFlow
import io.realm.kotlin.migration.AutomaticSchemaMigration
import io.realm.kotlin.types.RealmObject
import kotlinx.coroutines.flow.mapNotNull

internal val LocalPokedexDatabase = staticCompositionLocalOf<PokedexDatabase> { error("Nothing here!") }

internal class PokedexDatabase(name: String = Realm.DEFAULT_FILE_NAME) {
    private val realm by lazy {
        Realm.open(
            RealmConfiguration.Builder(
                setOf(
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

    suspend fun getPokemonList() = realm.initDb { PokemonDbList() }
        .asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.listDb }

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

    suspend fun searchPokemon(searchQuery: String) = realm
        .initDb { PokemonDbList() }.asFlow()
        .mapNotNull { it.obj }
        .mapNotNull { it.listDb.filter { it.name.contains(searchQuery) } }
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