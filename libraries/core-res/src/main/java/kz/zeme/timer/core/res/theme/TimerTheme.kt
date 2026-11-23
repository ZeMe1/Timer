package kz.zeme.timer.core.res.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf

object TimerTheme {

    private val _colors = mutableStateOf(DarkTimerColors)
    private val _isDarkTheme = mutableStateOf(true)

    val colors: TimerColors
        @Composable
        get() = _colors.value

    val isDarkTheme: Boolean
        @Composable
        get() = _isDarkTheme.value

    val typography: TimerTypography
        @Composable
        get() = LocalStaticTypography.current

    fun applyTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
        _colors.value = if (isDark) DarkTimerColors else LightTimerColors
    }
}
