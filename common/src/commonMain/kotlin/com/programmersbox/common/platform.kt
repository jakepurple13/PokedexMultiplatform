package com.programmersbox.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

public expect fun getPlatformName(): String

public expect suspend fun playAudio(url: String)

@Composable
public expect fun SortingContainer(onDismiss: () -> Unit, block: @Composable () -> Unit)

@Composable
public expect fun ScrollbarSupport(scrollState: LazyListState, modifier: Modifier)

@Composable
public expect fun ScrollbarSupport(scrollState: ScrollState, modifier: Modifier)

@Composable
public expect fun ScrollbarSupport(scrollState: LazyGridState, modifier: Modifier)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
public expect fun DrawerContainer(
    drawerState: DrawerState,
    drawerContent: @Composable () -> Unit,
    content: @Composable () -> Unit
)