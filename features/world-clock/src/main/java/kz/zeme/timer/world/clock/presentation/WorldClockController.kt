package kz.zeme.timer.world.clock.presentation

interface WorldClockController {

    fun onStart()

    fun onRefresh()

    fun onRemoveCityClick(zoneId: String)
}
