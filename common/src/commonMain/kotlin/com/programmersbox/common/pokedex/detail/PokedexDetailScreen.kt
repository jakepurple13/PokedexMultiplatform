@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common.pokedex.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.programmersbox.common.*
import com.programmersbox.common.pokedex.Pokemon
import com.programmersbox.common.pokedex.PokemonInfo
import com.programmersbox.common.pokedex.SpriteType
import com.programmersbox.common.pokedex.database.LocalPokedexDatabase
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.path
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PokedexDetailScreen(backStackEntry: BackStackEntry, list: List<Pokemon>) {
    val pokedexDatabase = LocalPokedexDatabase.current
    val scope = rememberCoroutineScope()
    val name: String? = backStackEntry.path("name")
    val f = name
        ?.let { n -> list.indexOfFirst { it.name == n } }
        ?.coerceAtLeast(0) ?: 3
    val pagerState = rememberPagerState(initialPage = f)

    HorizontalPager(
        pageCount = list.size,
        state = pagerState,
        beyondBoundsPageCount = 1,
        pageSpacing = 2.dp,
        key = { it }
    ) { index ->
        val newVm = remember {
            PokedexDetailViewModel(list[index].name, pokedexDatabase)
        }
        Crossfade(
            targetState = newVm.pokemonInfo,
            label = "",
        ) { target ->
            when (target) {
                DetailState.Error -> ErrorState(
                    onTryAgain = {}
                )

                DetailState.Loading -> Box(modifier = Modifier.fillMaxSize()) {
                    PokeballLoading(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is DetailState.Success -> ContentScreen(
                    pokemon = target.pokemonInfo,
                    isSaved = newVm.savedPokemon != null,
                    onSave = newVm::save,
                    onDelete = newVm::remove,
                    onPlayCry = newVm::playCry,
                    showLeft = index > 0,
                    showRight = index < list.size,
                    leftPress = {
                        scope.launch {
                            pagerState.animateScrollToPage(index - 1)
                        }
                    },
                    rightPress = {
                        scope.launch {
                            pagerState.animateScrollToPage(index + 1)
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ErrorState(
    onTryAgain: () -> Unit,
) {
    val navController = LocalNavController.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pokedex") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
            )
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Something went wrong")
                OutlinedButton(onClick = onTryAgain) {
                    Text("Try Again")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentScreen(
    pokemon: PokemonInfo,
    isSaved: Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onPlayCry: (String) -> Unit,
    showLeft: Boolean,
    showRight: Boolean,
    leftPress: () -> Unit,
    rightPress: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            ContentHeader(
                pokemon = pokemon,
                isSaved = isSaved,
                onSave = onSave,
                onDelete = onDelete,
                onPlayCry = onPlayCry,
                scrollBehavior = scrollBehavior
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { padding -> ContentBody(pokemon = pokemon, paddingValues = padding, scrollState = scrollState) }
    val alphaScroll = remember { Animatable(0f) }

    LaunchedEffect(scrollState.value) {
        alphaScroll.animateTo(1f)
        delay(3000)
        alphaScroll.animateTo(0f)
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alphaScroll.value)
    ) {
        if (showLeft) {
            IconButton(
                onClick = leftPress,
                modifier = Modifier.align(Alignment.CenterStart)
            ) { Icon(Icons.Default.ArrowLeft, null) }
        }
        if (showRight) {
            IconButton(
                onClick = rightPress,
                modifier = Modifier.align(Alignment.CenterEnd)
            ) { Icon(Icons.Default.ArrowRight, null) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentBody(
    pokemon: PokemonInfo,
    paddingValues: PaddingValues,
    scrollState: ScrollState = rememberScrollState()
) {
    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            ImageWithBlurImage(
                url = pokemon.imageUrl,
                name = pokemon.name,
                modifier = Modifier
            )

            Text(
                pokemon.name.firstCharCapital(),
                style = MaterialTheme.typography.displayMedium
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                pokemon.types.forEach {
                    val typeColor = Color(it.getTypeColor())
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = typeColor,
                    ) {
                        Text(
                            it.type.name.firstCharCapital(),
                            color = if (typeColor.luminance() > .5)
                                MaterialTheme.colorScheme.surface
                            else
                                MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Scale, null)
                    Text(
                        pokemon.getWeightString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Height, null)
                    Text(
                        pokemon.getHeightString(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Base Stats",
                    style = MaterialTheme.typography.displaySmall
                )

                pokemon.stats.forEach {
                    StatInfoBar(
                        color = it.stat.statColor ?: MaterialTheme.colorScheme.primary,
                        statType = it.stat.shortenedName,
                        statAmount = "${it.baseStat}/300",
                        statCount = it.baseStat / 300f
                    )
                }
            }

            ShowImages(pokemon)

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                pokemon.pokemonDescription
                    ?.filtered
                    ?.forEach {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ListItem(
                                headlineText = { Text(it.version.name) },
                                supportingText = { Text(it.flavorText) }
                            )
                        }
                    }
            }
        }
        ScrollbarSupport(
            scrollState = scrollState,
            modifier = Modifier
                .padding(paddingValues)
                .padding(end = 4.dp)
                .align(Alignment.CenterEnd)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ShowImages(pokemon: PokemonInfo) {
    pokemon.sprites?.spriteMap?.let { spritesList ->
        var showMoreImages by remember { mutableStateOf(false) }

        ElevatedCard(
            onClick = { showMoreImages = !showMoreImages }
        ) {
            Text(
                "Show All Images",
                modifier = Modifier.padding(8.dp)
            )
        }

        AnimatedVisibility(showMoreImages) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                spritesList.forEach { sprite ->
                    val iconChoice: @Composable (Modifier) -> Unit = when (sprite.key) {
                        SpriteType.Default -> {
                            if (spritesList[SpriteType.Female]?.isEmpty() == true) {
                                {
                                    Row(it) {
                                        Icon(Icons.Default.Male, null, tint = MaleColor)
                                        Icon(Icons.Default.Female, null, tint = FemaleColor)
                                    }
                                }
                            } else {
                                {
                                    Icon(
                                        Icons.Default.Male,
                                        null,
                                        modifier = it,
                                        tint = MaleColor
                                    )
                                }
                            }
                        }

                        SpriteType.Female -> {
                            {
                                Icon(
                                    Icons.Default.Female,
                                    null,
                                    modifier = it,
                                    tint = FemaleColor
                                )
                            }
                        }
                    }
                    sprite.value.forEach {
                        ElevatedCard(
                            modifier = Modifier
                                .size(AllImageSize.second)
                                .padding(vertical = 2.dp)
                        ) {
                            ImageWithBlurImage(
                                url = it,
                                name = pokemon.name,
                                blurSize = AllImageSize.second,
                                imageSize = AllImageSize.first,
                            ) { iconChoice(Modifier.align(Alignment.TopEnd)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatInfoBar(
    color: Color,
    statType: String,
    statAmount: String,
    statCount: Float,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.width(24.dp))
        Text(
            statType,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(3f)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(3f)
        ) {
            val animationProgress = animateDelay(statCount)

            LinearProgressIndicator(
                progress = animationProgress.value,
                color = color,
                trackColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.height(16.dp),
            )
            Text(
                statAmount,
                color = MaterialTheme.colorScheme.surface
            )
        }
        Spacer(Modifier.width(24.dp))
    }
}

@Composable
private fun animateDelay(
    toValue: Float
): Animatable<Float, AnimationVector1D> {
    val animationProgress = remember { Animatable(initialValue = 0f) }

    LaunchedEffect(toValue) {
        animationProgress.animateTo(
            targetValue = toValue,
            animationSpec = tween(
                durationMillis = (8 * toValue * 100).roundToInt(),
                easing = LinearEasing
            )
        )
    }
    return animationProgress
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentHeader(
    pokemon: PokemonInfo,
    isSaved: Boolean,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onPlayCry: (String) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val navController = LocalNavController.current
    TopAppBar(
        title = { Text(pokemon.name.firstCharCapital()) },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null)
            }
        },
        actions = {
            Text("#${pokemon.id}")
            IconButton(
                onClick = { onPlayCry(pokemon.cryUrl) }
            ) { Icon(Icons.Default.VolumeUp, null) }
            IconButton(
                onClick = { if (isSaved) onDelete() else onSave() }
            ) {
                Pokeball(
                    sizeDp = 24.dp,
                    modifier = Modifier.alpha(animateFloatAsState(if (isSaved) 1f else .5f).value)
                )
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
        ),
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun ImageWithBlurImage(
    url: String,
    name: String,
    modifier: Modifier = Modifier,
    blurSize: Dp = 300.dp,
    imageSize: Dp = 240.dp,
    additionalContent: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        KamelImage(
            resource = asyncPainterResource(url),
            contentDescription = name,
            contentScale = ContentScale.FillWidth,
            colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(3f) }),
            modifier = Modifier
                .size(blurSize)
                .fillMaxWidth(.9f)
                .wrapContentHeight(Alignment.Top, true)
                .scale(1f, 1.8f)
                .blur(70.dp, BlurredEdgeTreatment.Unbounded)
                .alpha(.5f)
        )
        KamelImage(
            resource = asyncPainterResource(url),
            contentDescription = name,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(imageSize)
                .fillMaxWidth()
                .aspectRatio(1.2f)
                .fillMaxHeight()
        )
        additionalContent()
    }
}