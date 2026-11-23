package kz.zeme.timer.core.world.clock

import java.time.Instant
import java.time.ZoneId
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kz.zeme.timer.core.domain.Response
import kz.zeme.timer.core.locale.domain.LocaleRepository
import kz.zeme.timer.core.world.clock.data.DefaultWorldClockRepository
import kz.zeme.timer.core.world.clock.data.WorldClockDbEntityMapper
import kz.zeme.timer.core.world.clock.data.WorldClockFormatter
import kz.zeme.timer.core.world.clock.data.WorldClockTimeZoneDataSource
import kz.zeme.timer.core.world.clock.data.local.db.WorldCityDbEntity
import kz.zeme.timer.core.world.clock.data.local.db.WorldClockDbDao
import kz.zeme.timer.core.world.clock.domain.DayOffset
import kz.zeme.timer.core.world.clock.domain.WorldCity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultWorldClockRepositoryTest {

    @Test
    fun addRemoveAndSearchCitiesManageRoomDataCorrectly() = runTest {
        val dao = FakeWorldClockDbDao()
        val timeZoneDataSource = FakeWorldClockTimeZoneDataSource(
            cities = listOf(
                WorldCity(zoneId = "Europe/London", name = "London"),
                WorldCity(zoneId = "Asia/Tokyo", name = "Tokyo"),
                WorldCity(zoneId = "America/New_York", name = "New York"),
            ),
        )
        val repository = DefaultWorldClockRepository(
            localDataSource = dao,
            timeZoneDataSource = timeZoneDataSource,
            localeRepository = FakeLocaleRepository(Locale.US),
            dbEntityMapper = WorldClockDbEntityMapper(),
            clockFormatter = WorldClockFormatter(
                instantProvider = { Instant.parse("2026-11-22T17:30:00Z") },
                localZoneProvider = { ZoneId.of("Asia/Almaty") },
            ),
            ioDispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        repository.addCity("Europe/London")
        repository.addCity("Asia/Tokyo")

        val savedResponse = repository.getSavedCities() as Response.Success
        assertEquals(listOf("Europe/London", "Asia/Tokyo"), savedResponse.data.map { it.zoneId })
        assertEquals(listOf(0, 1), savedResponse.data.map { it.sortOrder })

        val filtered = repository.getAvailableCities("tok") as Response.Success
        assertEquals(listOf("Asia/Tokyo"), filtered.data.map { it.zoneId })

        repository.removeCity("Europe/London")
        val afterRemove = repository.getSavedCities() as Response.Success
        assertEquals(listOf("Asia/Tokyo"), afterRemove.data.map { it.zoneId })
    }

    @Test
    fun formatterCalculatesLocalClockDayOffsetsAndHourDiffs() {
        val formatter = WorldClockFormatter(
            instantProvider = { Instant.parse("2026-11-22T17:30:15Z") },
            localZoneProvider = { ZoneId.of("Asia/Almaty") },
        )

        val local = formatter.buildLocalClock(Locale.US)
        assertEquals("22:30:15", local.formattedTime)
        assertEquals("UTC+5", local.utcOffsetText)
        assertTrue(local.dateText.isNotBlank())

        val cityTimes = formatter.buildCityTimes(
            listOf(
                WorldCity(zoneId = "Europe/London", name = "London"),
                WorldCity(zoneId = "Asia/Tokyo", name = "Tokyo"),
                WorldCity(zoneId = "Pacific/Honolulu", name = "Honolulu"),
            ),
        )

        assertEquals("17:30", cityTimes[0].formattedTime)
        assertEquals(DayOffset.Today, cityTimes[0].dayOffset)
        assertEquals("-5", cityTimes[0].hourDiffText)

        assertEquals("02:30", cityTimes[1].formattedTime)
        assertEquals(DayOffset.Tomorrow, cityTimes[1].dayOffset)
        assertEquals("+4", cityTimes[1].hourDiffText)

        assertEquals("07:30", cityTimes[2].formattedTime)
        assertEquals(DayOffset.Today, cityTimes[2].dayOffset)
        assertEquals("-15", cityTimes[2].hourDiffText)
    }

    @Test
    fun formatterDetectsYesterdayOffset() {
        val formatter = WorldClockFormatter(
            instantProvider = { Instant.parse("2026-11-22T21:00:00Z") },
            localZoneProvider = { ZoneId.of("Asia/Almaty") },
        )

        val cityTimes = formatter.buildCityTimes(
            listOf(WorldCity(zoneId = "Europe/London", name = "London")),
        )

        assertEquals("21:00", cityTimes[0].formattedTime)
        assertEquals(DayOffset.Yesterday, cityTimes[0].dayOffset)
        assertEquals("-5", cityTimes[0].hourDiffText)
    }

    private class FakeWorldClockDbDao : WorldClockDbDao {
        private val items = linkedMapOf<String, WorldCityDbEntity>()

        override suspend fun getAllCities(): List<WorldCityDbEntity> =
            items.values.sortedBy { it.sortOrder }

        override suspend fun getMaxCitySortOrder(): Int? =
            items.values.maxOfOrNull { it.sortOrder }

        override suspend fun insertCity(city: WorldCityDbEntity) {
            items[city.zoneId] = city
        }

        override suspend fun deleteCityByZoneId(zoneId: String) {
            items.remove(zoneId)
        }
    }

    private class FakeWorldClockTimeZoneDataSource(
        private val cities: List<WorldCity>,
    ) : WorldClockTimeZoneDataSource {

        override fun getCities(
            query: String,
            locale: Locale,
        ): List<WorldCity> {
            val trimmed = query.trim()
            if (trimmed.isEmpty()) return cities
            return cities.filter { it.name.contains(trimmed, ignoreCase = true) }
        }

        override fun getCityName(
            zoneId: String,
            locale: Locale,
        ): String = cities.firstOrNull { it.zoneId == zoneId }?.name ?: zoneId
    }

    private class FakeLocaleRepository(
        private val locale: Locale,
    ) : LocaleRepository {
        override fun getSelectedLocale(): Locale = locale
    }
}
