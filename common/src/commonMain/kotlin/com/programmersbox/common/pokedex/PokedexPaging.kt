package com.programmersbox.common.pokedex

import app.cash.paging.*
import com.programmersbox.common.pokedex.database.PokedexDatabase
import com.programmersbox.common.pokedex.database.PokemonDb
import com.programmersbox.common.pokedex.database.toPokemonDb

/*
@OptIn(ExperimentalPagingApi::class)
internal class PokemonRemoteMediator(
    private val database: PokedexDatabase,
    private val networkService: PokedexService = PokedexService
) : RemoteMediator<Int, PokemonDb>() {
    private var page = 0

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PokemonDb>
    ): RemoteMediatorMediatorResult {
        return try {
            // The network load method takes an optional after=<user.id>
            // parameter. For every page after the first, pass the last user
            // ID to let it continue from where it left off. For REFRESH,
            // pass null to load the first page.
            val loadKey = when (loadType) {
                LoadType.REFRESH -> {
                    page = 0
                    0
                }
                // In this example, you never need to prepend, since REFRESH
                // will always load the first page in the list. Immediately
                // return, reporting end of pagination.
                LoadType.PREPEND -> return RemoteMediatorMediatorResultSuccess(endOfPaginationReached = true) as RemoteMediatorMediatorResult
                LoadType.APPEND -> {
                    */
/*val lastItem = state.lastItemOrNull() ?: return MediatorResult.Success(
                        endOfPaginationReached = true
                    )*//*


                    // You must explicitly check if the last item is null when
                    // appending, since passing null to networkService is only
                    // valid for initial load. If lastItem is null it means no
                    // items were loaded after the initial REFRESH and there are
                    // no more items to load.

                    //lastItem.page + 1
                    page
                }

                else -> page
            }

            // Suspending network load via Retrofit. This doesn't need to be
            // wrapped in a withContext(Dispatcher.IO) { ... } block since
            // Retrofit's Coroutine CallAdapter dispatches on a worker
            // thread.
            val response = networkService.fetchPokemonList(loadKey).getOrThrow()
            page++
            database.clearPokemonCache()
            database.insertPokemon(response.results.map { it.toPokemonDb(page) })

            RemoteMediatorMediatorResultSuccess(endOfPaginationReached = response.next == null)
        } catch (e: Exception) {
            e.printStackTrace()
            RemoteMediatorMediatorResultError(e)
        } as RemoteMediatorMediatorResult
    }
}*/
