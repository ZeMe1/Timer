package kz.zeme.timer.world.clock.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kz.zeme.timer.core.world.clock.domain.WorldCityTime

data class WorldClockState(
    val localTime: String = "",
    val localDateUtc: String = "",
    val cities: ImmutableList<WorldCityTime> = persistentListOf(),
    val canRemoveCities: Boolean = false,
)
