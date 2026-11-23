package kz.zeme.timer.core.stopwatch.domain

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class StopwatchState(
    val status: StopwatchStatus = StopwatchStatus.Idle,
    val laps: ImmutableList<StopwatchLap> = persistentListOf(),
)
