package com.programmersbox.common.pokedex.colors

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.programmersbox.common.LocalNavController
import com.programmersbox.common.pokedex.database.LocalPokedexDatabase
import com.programmersbox.common.pokedex.database.PokedexDatabase
import com.programmersbox.common.pokedex.database.ThemeType
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
public fun PokedexTheme(
    colorScheme: ColorScheme,
    shapes: Shapes = MaterialTheme.shapes,
    content: @Composable () -> Unit
) {
    val navigator = rememberNavigator()
    CompositionLocalProvider(
        LocalPokedexDatabase provides remember { PokedexDatabase() },
        LocalNavController provides navigator
    ) {
        MaterialTheme(
            colorScheme = chosenColorScheme(colorScheme).value.animateToNewScheme(),
            shapes = shapes,
            content = content
        )
    }
}

@Composable
internal fun chosenColorScheme(
    default: ColorScheme,
    darkTheme: Boolean = isSystemInDarkTheme()
): State<ColorScheme> {
    val db = LocalPokedexDatabase.current
    return produceState(default, darkTheme) {
        db.getSettings()
            .map { it.themeType }
            .onEach {
                value = when (it) {
                    ThemeType.Default -> default
                    ThemeType.Pokedex -> if (darkTheme) DarkColors else LightColors
                    ThemeType.Blue -> if (darkTheme) BlueDarkColors else BlueLightColors
                }
            }
            .launchIn(this)
    }
}

@Composable
internal fun ColorScheme.animateToNewScheme(): ColorScheme = copy(
    primary = primary.animate().value,
    onPrimary = onPrimary.animate().value,
    primaryContainer = primaryContainer.animate().value,
    onPrimaryContainer = onPrimaryContainer.animate().value,
    inversePrimary = inversePrimary.animate().value,
    secondary = secondary.animate().value,
    onSecondary = onSecondary.animate().value,
    secondaryContainer = secondaryContainer.animate().value,
    onSecondaryContainer = onSecondaryContainer.animate().value,
    tertiary = tertiary.animate().value,
    onTertiary = onTertiary.animate().value,
    tertiaryContainer = tertiaryContainer.animate().value,
    onTertiaryContainer = onTertiaryContainer.animate().value,
    background = background.animate().value,
    onBackground = onBackground.animate().value,
    surface = surface.animate().value,
    onSurface = onSurface.animate().value,
    surfaceVariant = surfaceVariant.animate().value,
    onSurfaceVariant = onSurfaceVariant.animate().value,
    surfaceTint = surfaceTint.animate().value,
    inverseSurface = inverseSurface.animate().value,
    inverseOnSurface = inverseOnSurface.animate().value,
    error = error.animate().value,
    onError = onError.animate().value,
    errorContainer = errorContainer.animate().value,
    onErrorContainer = onErrorContainer.animate().value,
    outline = outline.animate().value,
    outlineVariant = outlineVariant.animate().value,
    scrim = scrim.animate().value
)

@Composable
internal fun Color.animate() = animateColorAsState(this)