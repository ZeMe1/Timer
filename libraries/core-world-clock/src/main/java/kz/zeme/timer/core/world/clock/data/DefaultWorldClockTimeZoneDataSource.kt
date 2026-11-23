package kz.zeme.timer.core.world.clock.data

import android.icu.text.TimeZoneNames
import android.icu.util.TimeZone
import java.text.Collator
import java.time.ZoneId
import java.util.Locale
import kz.zeme.timer.core.world.clock.domain.WorldCity

private val GEOGRAPHIC_REGIONS = setOf(
    "Africa",
    "America",
    "Antarctica",
    "Arctic",
    "Asia",
    "Atlantic",
    "Australia",
    "Europe",
    "Indian",
    "Pacific",
)

class DefaultWorldClockTimeZoneDataSource : WorldClockTimeZoneDataSource {

    private data class IndexedCity(
        val zoneId: String,
        val localizedName: String,
        val englishName: String,
    )

    private var cachedLocaleTag: String? = null
    private var cachedCities: List<IndexedCity> = emptyList()

    override fun getCities(
        query: String,
        locale: Locale,
    ): List<WorldCity> {
        val normalizedQuery = query.trim()
        val cities = getCatalog(locale)
        val filtered =
            if (normalizedQuery.isEmpty()) cities
            else cities.filter { it.matches(normalizedQuery) }

        return filtered.mapIndexed { index, city ->
            WorldCity(
                zoneId = city.zoneId,
                name = city.localizedName,
                sortOrder = index,
            )
        }
    }

    override fun getCityName(
        zoneId: String,
        locale: Locale,
    ): String = getExemplarNameOrNull(zoneId, locale)
        ?: zoneId.substringAfterLast('/').replace('_', ' ')

    private fun getCatalog(locale: Locale): List<IndexedCity> {
        val localeTag = locale.toLanguageTag()
        if (cachedLocaleTag == localeTag) return cachedCities

        val collator = Collator.getInstance(locale)
        val built = ZoneId.getAvailableZoneIds()
            .asSequence()
            .map { TimeZone.getCanonicalID(it) ?: it }
            .filter(::isGeographicZoneId)
            .distinct()
            .mapNotNull { buildIndexedCityOrNull(it, locale) }
            .sortedWith { first, second -> collator.compare(first.localizedName, second.localizedName) }
            .toList()

        cachedLocaleTag = localeTag
        cachedCities = built
        return built
    }

    private fun buildIndexedCityOrNull(
        zoneId: String,
        locale: Locale,
    ): IndexedCity? {
        val localizedName = getExemplarNameOrNull(zoneId, locale) ?: return null
        val englishName =
            if (locale.language == Locale.ENGLISH.language) localizedName
            else getExemplarNameOrNull(zoneId, Locale.ENGLISH).orEmpty()

        return IndexedCity(
            zoneId = zoneId,
            localizedName = localizedName,
            englishName = englishName,
        )
    }

    private fun getExemplarNameOrNull(
        zoneId: String,
        locale: Locale,
    ): String? = TimeZoneNames
        .getInstance(locale)
        .getExemplarLocationName(zoneId)
        ?.takeIf { it.isNotBlank() }

    private fun isGeographicZoneId(zoneId: String): Boolean =
        zoneId.contains('/') && zoneId.substringBefore('/') in GEOGRAPHIC_REGIONS

    private fun IndexedCity.matches(query: String): Boolean =
        localizedName.contains(query, ignoreCase = true) ||
            englishName.contains(query, ignoreCase = true)
}
