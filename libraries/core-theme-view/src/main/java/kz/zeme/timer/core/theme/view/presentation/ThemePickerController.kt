package kz.zeme.timer.core.theme.view.presentation

import kz.zeme.timer.core.theme.domain.ThemeOption

interface ThemePickerController {
    fun onThemeClick(option: ThemeOption)
    fun onClose()
    fun onStart()
}
