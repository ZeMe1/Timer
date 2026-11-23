package kz.zeme.timer.world.clock.presentation.navigation

import kz.zeme.timer.core.fragment.BaseFragment
import kz.zeme.timer.world.clock.presentation.WorldClockFragment

class WorldClockRouter {

    companion object {
        const val TAG_WORLD_CLOCK = "TAG_WORLD_CLOCK"
    }

    fun getWorldClockFragment(): BaseFragment = WorldClockFragment()
}
