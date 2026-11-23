package kz.zeme.timer.splash

import kotlinx.parcelize.Parcelize
import kz.zeme.timer.core.navigation.Navigator

@Parcelize
sealed class SplashExternalNavigation : Navigator.Navigation {

    data class ShowNextScreen(
        val redirect: Navigator.Navigation? = null,
    ) : SplashExternalNavigation()
}
