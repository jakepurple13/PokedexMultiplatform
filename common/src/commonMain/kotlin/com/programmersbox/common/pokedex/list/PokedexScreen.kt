@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common.pokedex.list

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListLayoutInfo
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.programmersbox.common.LocalNavController
import com.programmersbox.common.firstCharCapital
import com.programmersbox.common.navigateToDetail
import com.programmersbox.common.pokedex.Pokemon
import com.programmersbox.common.pokedex.database.LocalPokedexDatabase
import com.programmersbox.common.pokedex.database.PokemonDb
import com.programmersbox.common.pokedex.database.SavedPokemon
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.tlaster.precompose.viewmodel.viewModel
import kotlin.math.absoluteValue
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun PokedexScreen() {
    val pokedexDatabase = LocalPokedexDatabase.current
    val vm = viewModel(PokedexViewModel::class) { PokedexViewModel(pokedexDatabase) }

    val entries = vm.pokedexEntriesSorted//vm.pager.collectAsLazyPagingItems()

    val saved = vm.savedPokemon// by pokedexDatabase.collectAsStateWithLifecycle(initialValue = emptyList())

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController = LocalNavController.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    EnterFullScreen()

    var showSearch by remember { mutableStateOf(false) }

    /*if (showSearch) {
        SearchPokemon(
            query = vm.searchQuery,
            onQueryChange = { vm.searchQuery = it },
            pokemonList = vm.searchList
                .collectAsStateWithLifecycle(initialValue = emptyList())
                .value,
            onQueryClick = { navController.navigateToPokemonDetail(it.name) },
            onDismiss = { showSearch = false }
        )
    }*/

    var showSort by remember { mutableStateOf(false) }

    if (showSort) {
        /*SortPokemon(
            pokemonSort = vm.pokemonSort,
            onSortChange = { vm.pokemonSort = it },
            onDismiss = { showSort = false }
        )*/
    }

    /*ModalNavigationDrawer(
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(navController = navController, saved = saved)
            }
        },
        drawerState = drawerState
    ) {*/
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokedex") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                        Icon(Icons.Default.Menu, null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = vm::toggleViewType
                    ) { Icon(vm.pokemonListType.icon, null) }

                    IconButton(
                        onClick = { showSort = true }
                    ) { Icon(vm.pokemonSort.icon, null) }

                    IconButton(
                        onClick = { showSearch = true }
                    ) { Icon(Icons.Default.Search, null) }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = Color(0xFFe74c3c)
                ),
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            /*AnimatedVisibility(
                visible = entries.loadState.append is LoadState.Error,
                enter = slideInVertically { it / 2 },
                exit = slideOutVertically { it / 2 }
            ) {
                BottomAppBar(
                    containerColor = Color(0xFFe74c3c)
                ) {
                    FilledTonalButton(
                        onClick = { entries.retry() },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Error. Please Try Again") }
                }
            }*/
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding ->
        val onClick: (Pokemon) -> Unit = {
            it.name.let(navController::navigateToDetail)
        }
        Crossfade(targetState = vm.pokemonListType) { target ->
            when (target) {
                PokemonListType.Grid -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = padding,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .fillMaxSize()
                    ) {
                        items(entries) {
                            PokedexEntry(
                                pokemon = it,
                                saved = saved,
                                onClick = { onClick(it) },
                                modifier = Modifier.animateItemPlacement()
                            )
                        }
                    }
                }

                PokemonListType.List -> {
                    val state = rememberLazyListState()
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        contentPadding = padding,
                        state = state,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .fillMaxSize()
                    ) {
                        items(entries) { pokemon ->
                            val change = pokemon.let { p ->
                                state.layoutInfo.normalizedItemPosition(p.url)
                            }
                            PokedexEntryList(
                                pokemon = pokemon,
                                saved = saved,
                                onClick = { onClick(pokemon) },
                                modifier = Modifier
                                    .animateItemPlacement()
                                    .graphicsLayer {
                                        change.let { c ->
                                            translationX = c.absoluteValue * 50
                                            translationY = -c
                                        }
                                    }
                            )
                        }
                    }
                }
            }
        }
        //}
    }

    Animations()
}

@Composable
private fun Animations() {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(top = 4.dp)
            .fillMaxWidth(),
    ) {
        val animation = rememberInfiniteTransition()
        val color by animation.animateColor(
            initialValue = Color(0xff3b4cca),
            targetValue = Color(0xff1de9b6),
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        )
        Box(
            Modifier
                .size(60.dp)
                .background(color, CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )

        Spacer(Modifier.width(2.dp))
        Light(
            color = Color.Red,
            offColor = { it.copy(red = it.red * .8f) },
            changeChance = { Random.nextInt(1, 10) % 2 == 0 },
            delayAmount = { Random.nextLong(50, 250) }
        )
        Spacer(Modifier.width(2.dp))
        Light(
            color = Color.Yellow,
            offColor = {
                it.copy(
                    red = it.red * .8f,
                    green = it.green * .8f,
                    blue = it.blue * .8f
                )
            },
            changeChance = { Random.nextInt(1, 50) % 2 == 1 },
            delayAmount = { Random.nextLong(2500, 5000) }
        )
        Spacer(Modifier.width(2.dp))
        Light(
            color = Color.Green,
            offColor = { it.copy(green = it.green * .8f) },
            changeChance = { Random.nextInt(1, 100) == 25 },
            delayAmount = { 10000 },
        )
    }
}

@Composable
private fun Light(
    color: Color,
    offColor: (Color) -> Color,
    delayAmount: () -> Long,
    changeChance: () -> Boolean,
) {
    val off = offColor(color)
    var newColor by remember { mutableStateOf(off) }

    LaunchedEffect(Unit) {
        while (true) {
            newColor = if (changeChance()) off else color
            delay(delayAmount())
        }
    }

    Box(
        Modifier
            .size(20.dp)
            .background(newColor, CircleShape)
            .border(1.dp, Color.Black, CircleShape)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokedexEntry(
    modifier: Modifier = Modifier,
    pokemon: Pokemon?,
    saved: List<SavedPokemon>,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.sizeIn(minHeight = 250.dp)
    ) {
        if (pokemon != null) {
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize()
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(pokemon.pokedexEntry)
                    if (saved.any { it.url == pokemon.url }) {
                        Icon(Icons.Default.Bookmark, null)
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    KamelImage(
                        resource = lazyPainterResource(pokemon.imageUrl),
                        contentDescription = pokemon.name,
                        contentScale = ContentScale.FillWidth,
                        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(3f) }),
                        modifier = Modifier
                            .widthIn(max = 800.dp)
                            .fillMaxWidth(.9f)
                            .wrapContentHeight(Alignment.Top, true)
                            .scale(1f, 1.8f)
                            .blur(70.dp, BlurredEdgeTreatment.Unbounded)
                            .alpha(.5f)
                    )
                    KamelImage(
                        resource = lazyPainterResource(pokemon.imageUrl),
                        contentDescription = pokemon.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .widthIn(max = 500.dp)
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                            .fillMaxHeight()
                    )
                }
                Text(
                    pokemon.name.firstCharCapital(),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize()
            ) { CircularProgressIndicator() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PokedexEntryList(
    modifier: Modifier = Modifier,
    pokemon: Pokemon?,
    saved: List<SavedPokemon>,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.sizeIn(minHeight = 100.dp)
    ) {
        if (pokemon != null) {
            ListItem(
                headlineText = { Text(pokemon.name.firstCharCapital()) },
                leadingContent = {
                    Box(contentAlignment = Alignment.Center) {
                        KamelImage(
                            resource = lazyPainterResource(pokemon.imageUrl),
                            contentDescription = pokemon.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(100.dp)
                        )
                    }
                },
                trailingContent = {
                    if (saved.any { it.url == pokemon.url }) {
                        Icon(Icons.Default.Bookmark, null)
                    }
                },
                overlineText = { Text("#${pokemon.pokedexEntry}") },
                modifier = Modifier.padding(4.dp)
            )
        } else {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxSize()
            ) { CircularProgressIndicator() }
        }
    }
}

/*@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun DrawerContent(
    navController: NavController,
    saved: List<SavedPokemon>,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        stickyHeader { TopAppBar(title = { Text("Saved Pokemon") }) }
        items(saved) {
            val surface = MaterialTheme.colorScheme.surface
            val defaultSwatch = SwatchInfo(
                rgb = surface,
                bodyColor = Color.Blue,
                titleColor = contentColorFor(surface)
            )
            var swatchInfo by remember { mutableStateOf(defaultSwatch) }
            Card(
                onClick = {
                    navController.navigate(
                        Screens.PokedexDetail
                            .route
                            .replace("{name}", it.name)
                    ) { launchSingleTop = true }
                },
                colors = CardDefaults.cardColors(
                    containerColor = swatchInfo.rgb,
                    contentColor = swatchInfo.titleColor
                )
            ) {
                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = swatchInfo.rgb,
                        headlineColor = swatchInfo.titleColor,
                        overlineColor = swatchInfo.titleColor
                    ),
                    headlineContent = { Text(it.name.firstCharCapital()) },
                    overlineContent = { Text("#${it.pokedexEntry}") },
                    leadingContent = {
                        val latestSwatch by rememberUpdatedState(newValue = swatchInfo)
                        GlideImage(
                            imageModel = { it.imageUrl },
                            component = rememberImageComponent {
                                +PalettePlugin { p ->
                                    if (latestSwatch == defaultSwatch) {
                                        p.dominantSwatch?.let { s ->
                                            swatchInfo = SwatchInfo(
                                                rgb = s.rgb.toComposeColor(),
                                                titleColor = s.titleTextColor.toComposeColor(),
                                                bodyColor = s.bodyTextColor.toComposeColor()
                                            )
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                        )
                    }
                )
            }
        }
    }
}*/

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortPokemon(
    pokemonSort: PokemonSort,
    onSortChange: (PokemonSort) -> Unit,
    onDismiss: () -> Unit,
) {
    /*ModalBottomSheet(onDismissRequest = onDismiss) {
        TopAppBar(title = { Text("Sort") })
        LazyColumn {
            val values = PokemonSort.values()
            itemsIndexed(values) { index, sort ->
                ListItem(
                    leadingContent = {
                        RadioButton(selected = sort == pokemonSort, onClick = null)
                    },
                    headlineText = { Text(sort.name) },
                    modifier = Modifier.clickable {
                        onSortChange(sort)
                        onDismiss()
                    }
                )

                if (index != values.lastIndex) Divider()
            }
        }
    }*/
}

//TODO: Search
// Show Saved Pokemon
// Dialog to choose sorting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchPokemon(
    query: String,
    onQueryChange: (String) -> Unit,
    pokemonList: List<PokemonDb>,
    onQueryClick: (PokemonDb) -> Unit,
    onDismiss: () -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var active by rememberSaveable { mutableStateOf(false) }

    fun closeSearchBar() {
        focusManager.clearFocus()
        active = false
        onDismiss()
    }

    LaunchedEffect(Unit) {
        delay(200)
        active = true
    }

    /*SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { closeSearchBar() },
        active = active,
        onActiveChange = {
            active = it
            if (!active) {
                focusManager.clearFocus()
                onDismiss()
            }
        },
        placeholder = { Text("Search") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(Icons.Default.Cancel, null)
            }
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            itemsIndexed(pokemonList) { index, pokemon ->
                ListItem(
                    headlineContent = { Text(pokemon.name.firstCharCapital()) },
                    leadingContent = { Icon(Icons.Filled.Search, contentDescription = null) },
                    modifier = Modifier.clickable {
                        closeSearchBar()
                        onQueryClick(pokemon)
                    }
                )
                if (index != pokemonList.lastIndex) {
                    Divider()
                }
            }
        }
    }*/
}

internal fun LazyListLayoutInfo.normalizedItemPosition(key: Any): Float = visibleItemsInfo
    .firstOrNull { it.key == key }
    ?.let {
        val center = (viewportEndOffset + viewportStartOffset - it.size) / 2F
        (it.offset.toFloat() - center) / center
    } ?: 0F

@Composable
private fun EnterFullScreen() {
    //TODO: This isn't working for some reason
    // it'll hide the status bar but it's like it loses the insets
    /*val uiController = rememberSystemUiController()
    LifecycleHandler(
        onStart = { uiController.isSystemBarsVisible = false },
        onDestroy = { uiController.isSystemBarsVisible = true }
    )*/
}

@Composable
private fun AnimationsPreview() {
    Animations()
}