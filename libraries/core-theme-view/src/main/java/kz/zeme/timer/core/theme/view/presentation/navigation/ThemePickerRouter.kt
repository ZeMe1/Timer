package kz.zeme.timer.core.theme.view.presentation.navigation

import kz.zeme.timer.core.fragment.BaseBottomSheetDialog
import kz.zeme.timer.core.theme.view.presentation.ThemePickerFragment

class ThemePickerRouter {
    companion object {
        const val TAG_THEME_PICKER = "TAG_THEME_PICKER"
    }

    fun getThemePickerFragment(): BaseBottomSheetDialog =
        ThemePickerFragment()
}
