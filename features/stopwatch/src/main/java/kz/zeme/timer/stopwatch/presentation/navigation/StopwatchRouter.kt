package kz.zeme.timer.stopwatch.presentation.navigation

import kz.zeme.timer.core.fragment.BaseFragment
import kz.zeme.timer.stopwatch.presentation.StopwatchFragment

class StopwatchRouter {

    companion object {
        const val TAG_STOPWATCH = "TAG_STOPWATCH"
    }

    fun getStopwatchFragment(): BaseFragment = StopwatchFragment()
}
