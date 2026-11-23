package kz.zeme.timer.core.world.clock.domain

data class WorldCityTime(
    val zoneId: String,
    val cityName: String,
    val formattedTime: String,
    val dayOffset: DayOffset,
    val hourDiffText: String,
)
