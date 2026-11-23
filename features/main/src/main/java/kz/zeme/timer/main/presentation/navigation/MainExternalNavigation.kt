package kz.zeme.timer.main.presentation.navigation

import kotlinx.parcelize.Parcelize
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.main.presentation.MainTab

@Parcelize
sealed class MainExternalNavigation : Navigator.Navigation {

    data class SelectTab(
        val tab: MainTab,
    ) : MainExternalNavigation()
}
