@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common.pokedex.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.programmersbox.common.LocalNavController
import com.programmersbox.common.PokedexRed
import com.programmersbox.common.firstCharCapital
import com.programmersbox.common.navigateToDetail
import com.programmersbox.common.pokedex.database.LocalPokedexDatabase
import com.programmersbox.common.pokedex.list.Animations
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.viewmodel.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SearchScreen() {
    val navController = LocalNavController.current
    val db = LocalPokedexDatabase.current
    val vm = viewModel(SearchViewModel::class) { SearchViewModel(db) }

    val pokemonList by vm.searchList.collectAsStateWithLifecycle(emptyList())

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.background(PokedexRed)
            ) {
                Animations(Modifier.padding(TopAppBarDefaults.windowInsets.asPaddingValues()))
                TopAppBar(
                    title = {
                        TextField(
                            value = vm.searchQuery,
                            onValueChange = { it: String -> vm.searchQuery = it },
                            placeholder = { Text("Search") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { vm.searchQuery = "" }) {
                                    Icon(Icons.Default.Cancel, null)
                                }
                            },
                            shape = CircleShape,
                            maxLines = 1,
                            singleLine = true,
                            colors = TextFieldDefaults.textFieldColors(
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    windowInsets = WindowInsets(0.dp),
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = PokedexRed
                    ),
                )
            }
        }
    ) { padding ->
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            itemsIndexed(pokemonList) { index, pokemon ->
                ListItem(
                    headlineText = { Text(pokemon.name.firstCharCapital()) },
                    leadingContent = {
                        KamelImage(
                            resource = asyncPainterResource(pokemon.imageUrl),
                            contentDescription = pokemon.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(100.dp)
                        )
                    },
                    modifier = Modifier.clickable {
                        navController.navigateToDetail(pokemon.name)
                    }
                )
                if (index != pokemonList.lastIndex) {
                    Divider()
                }
            }
        }
    }
}
