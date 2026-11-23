package kz.zeme.timer.timer.presentation

import kz.zeme.timer.core.timer.domain.TimerStatus

data class TimerState(
    val status: TimerStatus = TimerStatus.Idle,
    val hoursWheel: WheelColumn = WheelColumn(
        prev2 = "22",
        prev1 = "23",
        current = "00",
        next1 = "01",
        next2 = "02",
    ),
    val minutesWheel: WheelColumn = WheelColumn(
        prev2 = "03",
        prev1 = "04",
        current = "05",
        next1 = "06",
        next2 = "07",
    ),
    val secondsWheel: WheelColumn = WheelColumn(
        prev2 = "58",
        prev1 = "59",
        current = "00",
        next1 = "01",
        next2 = "02",
    ),
    val canStart: Boolean = true,
    val formattedRemaining: String = "05:00",
    val progress: Float = 1f,
    val endsAtClock: String = "",
) {
    data class WheelColumn(
        val prev2: String,
        val prev1: String,
        val current: String,
        val next1: String,
        val next2: String,
    )
}
