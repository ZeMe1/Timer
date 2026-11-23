package kz.zeme.timer.core.timer.domain

data class TimerRemaining(
    val remainingMs: Long = 0L,
    val totalMs: Long = 0L,
) {

    val progress: Float
        get() = if (totalMs <= 0L) 0f
        else (remainingMs.toFloat() / totalMs.toFloat()).coerceIn(0f, 1f)
}
