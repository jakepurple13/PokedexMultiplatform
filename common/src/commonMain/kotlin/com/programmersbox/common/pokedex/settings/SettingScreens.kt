package com.programmersbox.common.pokedex.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.programmersbox.common.LocalNavController
import com.programmersbox.common.PokeballLoading
import com.programmersbox.common.PokedexRed
import com.programmersbox.common.SortingContainer
import com.programmersbox.common.pokedex.database.LocalPokedexDatabase
import com.programmersbox.common.pokedex.database.ThemeType
import com.programmersbox.common.pokedex.list.Animations
import moe.tlaster.precompose.viewmodel.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingScreen() {
    val db = LocalPokedexDatabase.current
    val navController = LocalNavController.current
    val vm = viewModel(SettingViewModel::class) { SettingViewModel(db) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier.background(PokedexRed)
            ) {
                Animations(Modifier.padding(TopAppBarDefaults.windowInsets.asPaddingValues()))
                TopAppBar(
                    title = {
                        Text("Settings")
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
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .padding(padding)
                .padding(vertical = 2.dp)
                .verticalScroll(rememberScrollState())
        ) {
            ClearListCache(
                onConfirm = vm::clearListCache,
                entryCount = vm.pokemonLists.listDb.size
            )

            ClearInfoCache(
                onConfirm = vm::clearInfoCache,
                entryCount = vm.pokemonLists.cachedInfo.size
            )

            ThemeOption(
                currentThemeType = vm.themeType,
                onThemeChange = vm::changeTheme
            )

            PokeballLoading(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeOption(
    currentThemeType: ThemeType,
    onThemeChange: (ThemeType) -> Unit
) {
    var showOptions by remember { mutableStateOf(false) }

    ElevatedCard(
        onClick = { showOptions = !showOptions }
    ) {
        ListItem(
            headlineContent = { Text("Change Theme") },
            overlineContent = { Text(currentThemeType.name) },
        )
        AnimatedVisibility(showOptions) {
            Column(modifier = Modifier.padding(start = 24.dp)) {
                ThemeType.values().forEach {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .clickable { onThemeChange(it) }
                            .padding(4.dp)
                    ) {
                        RadioButton(
                            selected = it == currentThemeType,
                            onClick = null
                        )
                        Text(it.name)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ClearListCache(
    onConfirm: () -> Unit,
    entryCount: Int
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        SortingContainer(
            onDismiss = { showDialog = false }
        ) {
            AlertDialogContent(
                buttons = {
                    FlowRow {
                        TextButton(
                            onClick = {
                                showDialog = false
                                onConfirm()
                            }
                        ) { Text("Confirm", color = MaterialTheme.colorScheme.error) }
                        TextButton(
                            onClick = { showDialog = false }
                        ) { Text("Cancel") }
                    }
                },
                title = { Text("Clear Cache?") },
                text = { Text("Do not do this too often. ONLY do this if there are new pokemon.") }
            )
        }
    }

    ElevatedCard(
        onClick = { showDialog = true }
    ) {
        ListItem(
            headlineContent = { Text("Clear Pokedex Cache") },
            trailingContent = { Text("$entryCount entries") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun ClearInfoCache(
    onConfirm: () -> Unit,
    entryCount: Int
) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        SortingContainer(
            onDismiss = { showDialog = false }
        ) {
            AlertDialogContent(
                buttons = {
                    FlowRow {
                        TextButton(
                            onClick = {
                                showDialog = false
                                onConfirm()
                            }
                        ) { Text("Confirm", color = MaterialTheme.colorScheme.error) }
                        TextButton(
                            onClick = { showDialog = false }
                        ) { Text("Cancel") }
                    }
                },
                title = { Text("Clear Detail Cache?") },
                text = { Text("Do not do this too often. ONLY do this if something goes wrong.") }
            )
        }
    }

    ElevatedCard(
        onClick = { showDialog = true }
    ) {
        ListItem(
            headlineContent = { Text("Clear Pokedex Detail Cache") },
            trailingContent = { Text("$entryCount entries") }
        )
    }
}

@Composable
internal fun AlertDialogContent(
    buttons: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    icon: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    tonalElevation: Dp = 6.dp,
    buttonContentColor: Color = MaterialTheme.colorScheme.primary,
    iconContentColor: Color = MaterialTheme.colorScheme.secondary,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    textContentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = modifier.sizeIn(minWidth = DialogMinWidth, maxWidth = DialogMaxWidth),
        propagateMinConstraints = true
    ) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = containerColor,
            tonalElevation = tonalElevation,
        ) {
            Column(
                modifier = Modifier.padding(DialogPadding)
            ) {
                icon?.let {
                    CompositionLocalProvider(LocalContentColor provides iconContentColor) {
                        Box(
                            Modifier
                                .padding(IconPadding)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            icon()
                        }
                    }
                }
                title?.let {
                    CompositionLocalProvider(LocalContentColor provides titleContentColor) {
                        val textStyle = MaterialTheme.typography.headlineSmall
                        ProvideTextStyle(textStyle) {
                            Box(
                                // Align the title to the center when an icon is present.
                                Modifier
                                    .padding(TitlePadding)
                                    .align(
                                        if (icon == null) {
                                            Alignment.Start
                                        } else {
                                            Alignment.CenterHorizontally
                                        }
                                    )
                            ) {
                                title()
                            }
                        }
                    }
                }
                text?.let {
                    CompositionLocalProvider(LocalContentColor provides textContentColor) {
                        val textStyle = MaterialTheme.typography.bodyMedium
                        ProvideTextStyle(textStyle) {
                            Box(
                                Modifier
                                    .weight(weight = 1f, fill = false)
                                    .padding(TextPadding)
                                    .align(Alignment.Start)
                            ) {
                                text()
                            }
                        }
                    }
                }
                Box(modifier = Modifier.align(Alignment.End)) {
                    CompositionLocalProvider(LocalContentColor provides buttonContentColor) {
                        val textStyle = MaterialTheme.typography.labelLarge
                        ProvideTextStyle(value = textStyle, content = buttons)
                    }
                }
            }
        }
    }
}

internal val DialogMinWidth = 280.dp
internal val DialogMaxWidth = 560.dp

// Paddings for each of the dialog's parts.
private val DialogPadding = PaddingValues(all = 24.dp)
private val IconPadding = PaddingValues(bottom = 16.dp)
private val TitlePadding = PaddingValues(bottom = 16.dp)
private val TextPadding = PaddingValues(bottom = 24.dp)