package kz.zeme.timer.timer.presentation

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kz.zeme.timer.core.timer.domain.TimerStatus

data class TimerState(
    val status: TimerStatus = TimerStatus.Idle,
    val hoursItems: ImmutableList<String> = persistentListOf(),
    val minutesItems: ImmutableList<String> = persistentListOf(),
    val secondsItems: ImmutableList<String> = persistentListOf(),
    val selectedHours: Int = 0,
    val selectedMinutes: Int = 5,
    val selectedSeconds: Int = 0,
    val canStart: Boolean = true,
    val formattedRemaining: String = "05:00",
    val progress: Float = 1f,
    val endsAtClock: String = "",
)
