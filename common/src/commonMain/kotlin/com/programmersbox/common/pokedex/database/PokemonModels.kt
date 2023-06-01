package com.programmersbox.common.pokedex.database

import com.programmersbox.common.pokedex.Pokemon
import com.programmersbox.common.pokedex.PokemonDescription
import com.programmersbox.common.pokedex.PokemonInfo
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class PokemonDbList : RealmObject {
    var listDb = realmListOf<PokemonDb>()
    var savedList = realmListOf<SavedPokemon>()
}

internal class PokemonDb : RealmObject {
    @PrimaryKey
    var url: String = ""
    var name: String = ""
    var page: Int = 0
}

internal class PokemonInfoDb : RealmObject {
    @PrimaryKey
    var id: Int = 0
    var name: String = ""
    var height: Int = 0
    var weight: Int = 0
    var types: String = ""
    var stats: String = ""
    var description: String? = null
    var experience: Int = 0
}

internal class SavedPokemon : RealmObject {
    @PrimaryKey
    var url: String = ""
    var name: String = ""
    var imageUrl: String = ""
    var pokedexEntry: Int = 0
}

internal object PokemonConverters {

    private val json = Json { ignoreUnknownKeys = true }

    fun fromTypes(type: List<PokemonInfo.TypeResponse>) = json.encodeToString(type)

    fun toTypes(typeString: String) =
        json.decodeFromString<List<PokemonInfo.TypeResponse>>(typeString)

    fun fromStats(stats: List<PokemonInfo.Stats>) = json.encodeToString(stats)

    fun toStats(statsString: String) = json.decodeFromString<List<PokemonInfo.Stats>>(statsString)

    fun fromDescription(description: PokemonDescription) = json.encodeToString(description)

    fun toDescription(descriptionString: String) =
        json.decodeFromString<PokemonDescription>(descriptionString)
}

internal fun PokemonDb.toPokemon() = Pokemon(name = name, url = url, page = page)
internal fun Pokemon.toPokemonDb(newPage: Int = page) = PokemonDb().apply {
    name = this@toPokemonDb.name
    url = this@toPokemonDb.url
    page = newPage
}

internal fun PokemonInfoDb.toPokemonInfo() = PokemonInfo(
    id = id,
    name = name,
    height = height,
    weight = weight,
    types = PokemonConverters.toTypes(types),
    stats = PokemonConverters.toStats(stats),
    pokemonDescription = description?.let { PokemonConverters.toDescription(it) },
    experience = experience
)

internal fun PokemonInfo.toPokemonInfoDb() = PokemonInfoDb().apply {
    id = this@toPokemonInfoDb.id
    name = this@toPokemonInfoDb.name
    height = this@toPokemonInfoDb.height
    weight = this@toPokemonInfoDb.weight
    types = PokemonConverters.fromTypes(this@toPokemonInfoDb.types)
    stats = PokemonConverters.fromStats(this@toPokemonInfoDb.stats)
    description = pokemonDescription?.let { PokemonConverters.fromDescription(it) }
    experience = this@toPokemonInfoDb.experience
}
