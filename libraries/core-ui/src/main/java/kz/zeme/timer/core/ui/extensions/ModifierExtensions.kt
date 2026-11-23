package kz.zeme.timer.core.ui.extensions

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import kz.zeme.timer.core.res.theme.TimerTheme

fun Modifier.shimmer(shape: Shape = RectangleShape): Modifier = composed {
    shimmer(
        backgroundColor = TimerTheme.colors.mainShimmer,
        shimmerColor = if (TimerTheme.isDarkTheme) TimerTheme.colors.buttonSecondary else TimerTheme.colors.basicDark,
        shape = shape,
    )
}

fun Modifier.shimmer(
    backgroundColor: Color,
    shimmerColor: Color,
    shape: Shape = RectangleShape,
): Modifier = composed {
    background(
        brush = shimmerBrush(
            backgroundColor = backgroundColor,
            shimmerColor = shimmerColor,
        ),
        shape = shape,
    )
}

fun Modifier.applyIf(
    condition: Boolean,
    block: @Composable Modifier.() -> Modifier,
): Modifier = if (condition) composed { block(Modifier) } else this
