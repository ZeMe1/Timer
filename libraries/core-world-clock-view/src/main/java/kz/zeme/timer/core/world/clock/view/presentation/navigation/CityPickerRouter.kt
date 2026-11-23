package kz.zeme.timer.core.world.clock.view.presentation.navigation

import kz.zeme.timer.core.fragment.BaseBottomSheetDialog
import kz.zeme.timer.core.world.clock.view.presentation.CityPickerFragment

class CityPickerRouter {
    companion object {
        const val TAG_CITY_PICKER = "TAG_CITY_PICKER"
    }

    fun getCityPickerFragment(): BaseBottomSheetDialog =
        CityPickerFragment()
}
