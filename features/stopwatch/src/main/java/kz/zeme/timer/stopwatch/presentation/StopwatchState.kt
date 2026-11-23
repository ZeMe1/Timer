package kz.zeme.timer.stopwatch.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kz.zeme.timer.core.stopwatch.domain.StopwatchStatus

data class StopwatchState(
    val status: StopwatchStatus = StopwatchStatus.Idle,
    val mainTime: String = "00:00",
    val centiseconds: String = ".00",
    val currentLapNumber: String? = null,
    val currentLapTime: String? = null,
    val laps: ImmutableList<StopwatchLapItem> = persistentListOf(),
) {
    data class StopwatchLapItem(
        val number: Int,
        val numberText: String,
        val deltaText: String,
        val totalText: String,
    )
}
