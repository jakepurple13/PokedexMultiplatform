package com.programmersbox.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun PokeballLoading(
    modifier: Modifier = Modifier
) {
    val animation = rememberInfiniteTransition()
    val rotation by animation.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Restart
        )
    )

    Pokeball(modifier.rotate(rotation))
}

@Composable
internal fun Pokeball(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 200.dp
) {
    val sizePx = with(LocalDensity.current) { sizeDp.toPx() }

    val blackLineColor = Color.Black
    val strokeWidth = sizePx * .04f
    val outerBallPercentage = .25f
    val innerBallPercentage = .17f
    val centerBallPercentage = .10f

    Canvas(
        modifier = modifier.size(sizeDp)
    ) {
        drawArc(
            brush = Brush.linearGradient(listOf(Color.White, Color.White)),
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false
        )
        drawArc(
            brush = Brush.linearGradient(listOf(Color.Red, Color.Red)),
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false
        )
        drawArc(
            color = blackLineColor,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(2f)
        )

        drawLine(
            color = blackLineColor,
            start = Offset(
                x = 0f,
                y = (size.height / 2)
            ),
            end = Offset(
                x = size.width,
                y = (size.height / 2)
            ),
            strokeWidth = strokeWidth

        )

        drawCircle(
            color = Color.Black,
            radius = sizePx * outerBallPercentage / 2,
        )

        drawCircle(
            color = Color.White,
            radius = sizePx * innerBallPercentage / 2,
        )

        val centerBallSizePx = sizePx * centerBallPercentage
        val centerBallMarginPx = ((sizePx - centerBallSizePx) / 2)
        drawArc(
            color = Color.LightGray,
            startAngle = 0f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(
                x = centerBallMarginPx,
                y = centerBallMarginPx,
            ),
            size = Size(
                width = centerBallSizePx,
                height = centerBallSizePx,
            ),
            style = Stroke(4f)
        )
    }
}