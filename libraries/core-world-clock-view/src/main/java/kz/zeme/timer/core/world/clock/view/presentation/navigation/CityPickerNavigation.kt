package kz.zeme.timer.core.world.clock.view.presentation.navigation

import kotlinx.parcelize.Parcelize
import kz.zeme.timer.core.navigation.Navigator

@Parcelize
sealed class CityPickerNavigation : Navigator.Navigation {
    data object OpenCityPicker : CityPickerNavigation()
}
