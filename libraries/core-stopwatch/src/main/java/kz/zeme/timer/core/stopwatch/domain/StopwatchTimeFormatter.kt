package kz.zeme.timer.core.stopwatch.domain

private const val MS_PER_MINUTE = 60_000L
private const val MS_PER_SECOND = 1_000L
private const val MS_PER_CENTISECOND = 10L

class StopwatchTimeFormatter {

    fun formatParts(durationMs: Long): FormattedStopwatchTime {
        val safeMs = durationMs.coerceAtLeast(0L)
        val minutes = safeMs / MS_PER_MINUTE
        val seconds = (safeMs % MS_PER_MINUTE) / MS_PER_SECOND
        val centiseconds = (safeMs % MS_PER_SECOND) / MS_PER_CENTISECOND

        return FormattedStopwatchTime(
            mainTime = "${minutes.pad2()}:${seconds.pad2()}",
            centiseconds = ".${centiseconds.pad2()}",
        )
    }

    fun formatFull(durationMs: Long): String {
        val parts = formatParts(durationMs)
        return "${parts.mainTime}${parts.centiseconds}"
    }

    fun formatLapNumber(number: Int): String = number.toString().padStart(2, '0')

    private fun Long.pad2(): String = toString().padStart(2, '0')
}
