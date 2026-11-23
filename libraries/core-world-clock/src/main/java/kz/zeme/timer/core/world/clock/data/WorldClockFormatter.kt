package kz.zeme.timer.core.world.clock.data

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.abs
import kz.zeme.timer.core.world.clock.domain.DayOffset
import kz.zeme.timer.core.world.clock.domain.LocalClockSnapshot
import kz.zeme.timer.core.world.clock.domain.WorldCity
import kz.zeme.timer.core.world.clock.domain.WorldCityTime

private const val SECONDS_PER_MINUTE = 60L
private const val MINUTES_PER_HOUR = 60L

class WorldClockFormatter(
    private val instantProvider: () -> Instant = Instant::now,
    private val localZoneProvider: () -> ZoneId = ZoneId::systemDefault,
) {

    private val localTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val cityTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    fun buildLocalClock(locale: Locale): LocalClockSnapshot {
        val localDateTime = instantProvider().atZone(localZoneProvider())
        val offsetMinutes = localDateTime.offset.totalSeconds / SECONDS_PER_MINUTE

        return LocalClockSnapshot(
            formattedTime = localDateTime.format(localTimeFormatter),
            dateText = formatDate(localDateTime, locale),
            utcOffsetText = "UTC${formatSignedOffset(offsetMinutes)}",
        )
    }

    fun buildCityTimes(cities: List<WorldCity>): List<WorldCityTime> {
        val now = instantProvider()
        val localDateTime = now.atZone(localZoneProvider())

        return cities.map { city ->
            val cityDateTime = now.atZone(ZoneId.of(city.zoneId))
            val diffMinutes = (cityDateTime.offset.totalSeconds - localDateTime.offset.totalSeconds) / SECONDS_PER_MINUTE

            WorldCityTime(
                zoneId = city.zoneId,
                cityName = city.name,
                formattedTime = cityDateTime.format(cityTimeFormatter),
                dayOffset = resolveDayOffset(localDateTime, cityDateTime),
                hourDiffText = formatSignedOffset(diffMinutes),
            )
        }
    }

    private fun resolveDayOffset(
        localDateTime: ZonedDateTime,
        cityDateTime: ZonedDateTime,
    ): DayOffset {
        val dayDiff = ChronoUnit.DAYS.between(localDateTime.toLocalDate(), cityDateTime.toLocalDate())
        return when {
            dayDiff > 0L -> DayOffset.Tomorrow
            dayDiff < 0L -> DayOffset.Yesterday
            else -> DayOffset.Today
        }
    }

    private fun formatDate(
        dateTime: ZonedDateTime,
        locale: Locale,
    ): String = dateTime
        .format(DateTimeFormatter.ofPattern("EEE, d MMM", locale))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }

    private fun formatSignedOffset(totalMinutes: Long): String {
        val sign = if (totalMinutes >= 0L) "+" else "-"
        val absMinutes = abs(totalMinutes)
        val hours = absMinutes / MINUTES_PER_HOUR
        val minutes = absMinutes % MINUTES_PER_HOUR

        return if (minutes == 0L) "$sign$hours"
        else "$sign$hours:${minutes.toString().padStart(2, '0')}"
    }
}
