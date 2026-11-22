package kz.zeme.timer.core.locale.view.presentation.navigation

import kotlinx.parcelize.Parcelize
import kz.zeme.timer.core.navigation.Navigator

@Parcelize
sealed class LanguagePickerNavigation : Navigator.Navigation {
    data object OpenLanguagePicker : LanguagePickerNavigation()
}
