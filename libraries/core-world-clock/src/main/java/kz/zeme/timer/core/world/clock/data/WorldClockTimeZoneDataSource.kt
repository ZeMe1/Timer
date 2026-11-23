package kz.zeme.timer.core.world.clock.data

import java.util.Locale
import kz.zeme.timer.core.world.clock.domain.WorldCity

interface WorldClockTimeZoneDataSource {

    fun getCities(
        query: String,
        locale: Locale,
    ): List<WorldCity>

    fun getCityName(
        zoneId: String,
        locale: Locale,
    ): String
}
