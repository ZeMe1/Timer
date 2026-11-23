package kz.zeme.timer.core.timer.domain

data class TimerState(
    val status: TimerStatus = TimerStatus.Idle,
    val configuredDuration: TimerDuration = TimerDuration(),
    val totalMs: Long = configuredDuration.totalMs,
    val endEpochMs: Long? = null,
)
