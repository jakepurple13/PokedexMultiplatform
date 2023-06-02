package com.programmersbox.common.pokedex.colors

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private fun createLightColorScheme(first: Color, second: Color, background: Color, surface: Color) = lightColorScheme(
    primary = first,
    onPrimaryContainer = second,
    onPrimary = Color.White,
    secondary = first,
    secondaryContainer = surface,
    onSecondaryContainer = second,
    onSecondary = Color.White,
    background = background,
    onBackground = Color.Black,
    surface = surface,
    onSurface = Color.Black
)

private fun createDarkColorScheme(first: Color, second: Color, background: Color, surface: Color) = darkColorScheme(
    primary = first,
    onPrimaryContainer = second,
    onPrimary = Color.Black,
    secondary = first,
    secondaryContainer = surface,
    onSecondaryContainer = second,
    onSecondary = Color.Black,
    background = background,
    onBackground = Color.White,
    surface = surface,
    onSurface = Color.White
)

internal val BlueLightColors = createLightColorScheme(
    first = Color(0xff005db6),
    second = Color(0xff555f71),
    background = Color(0xffd6e3ff),
    surface = Color(0xffd9e3f9)
)

internal val BlueDarkColors = createDarkColorScheme(
    first = Color(0xffa9c7ff),
    second = Color(0xffbdc7dc),
    background = Color(0xff00468b),
    surface = Color(0xff3e4758)
)