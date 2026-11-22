package kz.zeme.timer.core.res.theme

import androidx.compose.runtime.Composable

object TimerTheme {

    val colors: TimerColors
        @Composable
        get() = LocalStaticColors.current

    val typography: TimerTypography
        @Composable
        get() = LocalStaticTypography.current
}
