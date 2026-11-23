package kz.zeme.timer.core.stopwatch.domain

data class StopwatchElapsed(
    val elapsedMs: Long = 0L,
    val currentLapElapsedMs: Long = 0L,
)
