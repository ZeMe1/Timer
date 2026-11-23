package kz.zeme.timer.features

import kz.zeme.timer.core.res.theme.TimerTheme
import kz.zeme.timer.core.theme.domain.ThemeApplier

class AppGlobalThemeApplier : ThemeApplier {

    override fun applyTheme(isDark: Boolean) {
        TimerTheme.applyTheme(isDark)
    }
}
