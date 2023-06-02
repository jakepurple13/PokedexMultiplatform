package com.programmersbox.common.pokedex.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.programmersbox.common.LocalNavController
import com.programmersbox.common.PokedexRed
import com.programmersbox.common.SortingContainer
import com.programmersbox.common.pokedex.database.LocalPokedexDatabase
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
        ) {
            ClearListCache(
                onConfirm = vm::clearListCache,
                entryCount = vm.pokemonLists.listDb.size
            )

            ClearInfoCache(
                onConfirm = vm::clearInfoCache,
                entryCount = vm.pokemonLists.cachedInfo.size
            )
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

    Card(
        onClick = { showDialog = true }
    ) {
        ListItem(
            headlineText = { Text("Clear Pokedex Cache") },
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

    Card(
        onClick = { showDialog = true }
    ) {
        ListItem(
            headlineText = { Text("Clear Pokedex Detail Cache") },
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