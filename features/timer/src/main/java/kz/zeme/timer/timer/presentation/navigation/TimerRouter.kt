package kz.zeme.timer.timer.presentation.navigation

import kz.zeme.timer.core.fragment.BaseFragment
import kz.zeme.timer.timer.presentation.TimerFragment

class TimerRouter {

    companion object {
        const val TAG_TIMER = "TAG_TIMER"
    }

    fun getTimerFragment(): BaseFragment = TimerFragment()
}
