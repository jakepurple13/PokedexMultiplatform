package com.programmersbox.common

import androidx.compose.ui.graphics.Color
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