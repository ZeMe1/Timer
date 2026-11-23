package kz.zeme.timer.core.timer.domain

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private const val MS_PER_SECOND = 1_000L
private const val SECONDS_PER_HOUR = 3_600L
private const val SECONDS_PER_MINUTE = 60L

class TimerTimeFormatter(
    private val zoneProvider: () -> ZoneId = ZoneId::systemDefault,
) {

    private val endClockFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun formatRemaining(remainingMs: Long): String {
        val safeMs = remainingMs.coerceAtLeast(0L)
        val totalSeconds = (safeMs + MS_PER_SECOND - 1L) / MS_PER_SECOND
        val hours = totalSeconds / SECONDS_PER_HOUR
        val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE

        return if (hours > 0L) "${hours.pad2()}:${minutes.pad2()}:${seconds.pad2()}"
        else "${minutes.pad2()}:${seconds.pad2()}"
    }

    fun formatEndClock(endEpochMs: Long?): String = endEpochMs?.let {
        Instant.ofEpochMilli(it).atZone(zoneProvider()).format(endClockFormatter)
    }.orEmpty()

    fun formatUnit(value: Int): String = value.toString().padStart(2, '0')

    private fun Long.pad2(): String = toString().padStart(2, '0')
}
