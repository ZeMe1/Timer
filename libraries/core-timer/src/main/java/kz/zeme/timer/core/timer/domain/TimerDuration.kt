package kz.zeme.timer.core.timer.domain

data class TimerDuration(
    val hours: Int = DEFAULT_HOURS,
    val minutes: Int = DEFAULT_MINUTES,
    val seconds: Int = DEFAULT_SECONDS,
) {

    val totalMs: Long
        get() = (
            (hours.toLong() * SECONDS_PER_HOUR) +
                (minutes.toLong() * SECONDS_PER_MINUTE) +
                seconds.toLong()
            ).coerceAtLeast(0L) * MILLIS_PER_SECOND

    val isZero: Boolean
        get() = totalMs <= 0L

    companion object {
        const val MIN_VALUE = 0
        const val MAX_HOURS = 23
        const val MAX_MINUTES = 59
        const val MAX_SECONDS = 59

        private const val DEFAULT_HOURS = 0
        private const val DEFAULT_MINUTES = 5
        private const val DEFAULT_SECONDS = 0
        private const val SECONDS_PER_MINUTE = 60L
        private const val SECONDS_PER_HOUR = 3600L
        private const val MILLIS_PER_SECOND = 1000L

        fun normalized(
            hours: Int,
            minutes: Int,
            seconds: Int,
        ): TimerDuration = TimerDuration(
            hours = hours.coerceIn(MIN_VALUE, MAX_HOURS),
            minutes = minutes.coerceIn(MIN_VALUE, MAX_MINUTES),
            seconds = seconds.coerceIn(MIN_VALUE, MAX_SECONDS),
        )
    }
}
