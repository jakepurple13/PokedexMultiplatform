package com.programmersbox.common

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

internal fun String.firstCharCapital(): String =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

internal fun Int.toComposeColor(): Color = Color(this)

internal fun Float.roundToDecimals(decimals: Int): Float {
    var dotAt = 1
    repeat(decimals) { dotAt *= 10 }
    val roundedValue = (this * dotAt).roundToInt()
    return (roundedValue / dotAt) + (roundedValue % dotAt).toFloat() / dotAt
}

internal val MaleColor = Color(0xff448aff)
internal val FemaleColor = Color(0xfff06292)
internal val PokedexRed = Color(0xFFe74c3c)

internal class CustomAdaptive(private val minSize: Dp) : GridCells {
    init {
        require(minSize > 0.dp)
    }

    override fun Density.calculateCrossAxisCellSizes(
        availableSize: Int,
        spacing: Int
    ): List<Int> {
        val count = maxOf((availableSize + spacing) / (minSize.roundToPx() + spacing), 1) + 1
        return calculateCellsCrossAxisSizeImpl(availableSize, count, spacing)
    }

    override fun hashCode(): Int {
        return minSize.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other is CustomAdaptive && minSize == other.minSize
    }
}

private fun calculateCellsCrossAxisSizeImpl(
    gridSize: Int,
    slotCount: Int,
    spacing: Int
): List<Int> {
    val gridSizeWithoutSpacing = gridSize - spacing * (slotCount - 1)
    val slotSize = gridSizeWithoutSpacing / slotCount
    val remainingPixels = gridSizeWithoutSpacing % slotCount
    return List(slotCount) {
        slotSize + if (it < remainingPixels) 1 else 0
    }
}