package kz.zeme.timer.core.locale.view.presentation.navigation

import kz.zeme.timer.core.fragment.BaseBottomSheetDialog
import kz.zeme.timer.core.locale.view.presentation.LanguagePickerFragment

class LanguagePickerRouter {
    companion object {
        const val TAG_LANGUAGE_PICKER = "TAG_LANGUAGE_PICKER"
    }

    fun getLanguagePickerFragment(): BaseBottomSheetDialog =
        LanguagePickerFragment()
}
