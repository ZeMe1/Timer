package kz.zeme.timer.timer.presentation.navigation

import kotlinx.parcelize.Parcelize
import kz.zeme.timer.core.navigation.Navigator

@Parcelize
sealed class TimerNavigation : Navigator.Navigation {
    data object RequestNotificationPermission : TimerNavigation()
}
