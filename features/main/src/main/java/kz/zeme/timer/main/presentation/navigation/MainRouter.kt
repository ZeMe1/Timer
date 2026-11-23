package kz.zeme.timer.main.presentation.navigation

import kz.zeme.timer.core.fragment.BaseFragment
import kz.zeme.timer.main.presentation.MainFragment

class MainRouter {

    companion object {
        const val TAG_MAIN = "TAG_MAIN"
    }

    fun getMainFragment(): BaseFragment = MainFragment()
}
