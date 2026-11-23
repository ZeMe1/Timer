package kz.zeme.timer.core.theme.view.presentation.navigation

import kotlinx.parcelize.Parcelize
import kz.zeme.timer.core.navigation.Navigator

@Parcelize
sealed class ThemePickerNavigation : Navigator.Navigation {
    data object OpenThemePicker : ThemePickerNavigation()
}
