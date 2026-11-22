package kz.zeme.timer.core.locale.view.presentation

import kz.zeme.timer.core.locale.domain.Language

interface LanguagePickerController {
    fun onLanguageClick(language: Language)
    fun onClose()
    fun onStart()
}
