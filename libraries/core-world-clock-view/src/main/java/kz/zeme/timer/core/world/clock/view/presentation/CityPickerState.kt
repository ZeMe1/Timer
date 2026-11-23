package kz.zeme.timer.core.world.clock.view.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class CityPickerState(
    val query: String = "",
    val items: ImmutableList<CityPickerItem> = persistentListOf(),
)

data class CityPickerItem(
    val zoneId: String,
    val name: String,
    val formattedTime: String,
    val isAdded: Boolean,
)
