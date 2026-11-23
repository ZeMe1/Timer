package kz.zeme.timer.core.stopwatch.domain

data class StopwatchLap(
    val number: Int,
    val deltaMs: Long,
    val totalMs: Long,
)
