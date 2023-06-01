@file:Suppress("INLINE_FROM_HIGHER_PLATFORM")

package com.programmersbox.common.pokedex.detail

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import androidx.compose.ui.unit.dp
import com.programmersbox.common.LocalNavController
import com.programmersbox.common.ScrollbarSupport
import com.programmersbox.common.firstCharCapital
import com.programmersbox.common.pokedex.PokemonInfo
import com.programmersbox.common.pokedex.database.LocalPokedexDatabase
import io.kamel.image.KamelImage
import io.kamel.image.lazyPainterResource
import moe.tlaster.precompose.navigation.BackStackEntry
import moe.tlaster.precompose.navigation.path
import moe.tlaster.precompose.viewmodel.viewModel
import kotlin.math.roundToInt

@Composable
internal fun PokedexDetailScreen(backStackEntry: BackStackEntry) {
    val pokedexDatabase = LocalPokedexDatabase.current
    val vm = viewModel(PokedexDetailViewModel::class) {
        PokedexDetailViewModel(
            backStackEntry.path("name"),
            pokedexDatabase
        )
    }

    Crossfade(targetState = vm.pokemonInfo, label = "") { target ->
        when (target) {
            DetailState.Error -> ErrorState(
                onTryAgain = {}
            )

            DetailState.Loading -> Box(modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            is DetailState.Success -> ContentScreen(
                pokemon = target.pokemonInfo,
                isSaved = vm.savedPokemon != null,
                onSave = vm::save,
                onDelete = vm::remove,
                onPlayCry = vm::playCry
            )
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
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
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
    ) { padding -> ContentBody(pokemon = pokemon, paddingValues = padding) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentBody(
    pokemon: PokemonInfo,
    paddingValues: PaddingValues,
) {
    val scrollState = rememberScrollState()
    Box {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                KamelImage(
                    resource = lazyPainterResource(pokemon.imageUrl),
                    contentDescription = pokemon.name,
                    contentScale = ContentScale.FillWidth,
                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(3f) }),
                    modifier = Modifier
                        .size(300.dp)
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
                        .size(240.dp)
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .fillMaxHeight()
                )
            }

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
                Crossfade(targetState = isSaved, label = "") { target ->
                    Icon(
                        if (target) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        null,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.smallTopAppBarColors(),
        scrollBehavior = scrollBehavior
    )
}
