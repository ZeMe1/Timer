package kz.zeme.timer.core.world.clock.data

import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kz.zeme.timer.core.domain.Response
import kz.zeme.timer.core.locale.domain.LocaleRepository
import kz.zeme.timer.core.world.clock.data.local.db.WorldClockDbDao
import kz.zeme.timer.core.world.clock.domain.LocalClockSnapshot
import kz.zeme.timer.core.world.clock.domain.WorldCity
import kz.zeme.timer.core.world.clock.domain.WorldCityTime
import kz.zeme.timer.core.world.clock.domain.WorldClockRepository

class DefaultWorldClockRepository(
    private val localDataSource: WorldClockDbDao,
    private val timeZoneDataSource: WorldClockTimeZoneDataSource,
    private val localeRepository: LocaleRepository,
    private val dbEntityMapper: WorldClockDbEntityMapper,
    private val clockFormatter: WorldClockFormatter,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WorldClockRepository {

    override suspend fun getSavedCities(): Response<List<WorldCity>> = withContext(ioDispatcher) {
        val locale = localeRepository.getSelectedLocale()
        val cities = localDataSource.getAllCities().map { entity ->
            dbEntityMapper.mapToDomain(
                entity = entity,
                cityName = timeZoneDataSource.getCityName(entity.zoneId, locale),
            )
        }

        Response.Success(cities)
    }

    override suspend fun getAvailableCities(query: String): Response<List<WorldCity>> = withContext(ioDispatcher) {
        val locale = localeRepository.getSelectedLocale()
        Response.Success(timeZoneDataSource.getCities(query, locale))
    }

    override suspend fun addCity(zoneId: String): Response<Unit> = withContext(ioDispatcher) {
        val nextOrder = (localDataSource.getMaxCitySortOrder() ?: -1) + 1
        localDataSource.insertCity(dbEntityMapper.mapToDbEntity(zoneId, nextOrder))
        Response.Success(Unit)
    }

    override suspend fun removeCity(zoneId: String): Response<Unit> = withContext(ioDispatcher) {
        localDataSource.deleteCityByZoneId(zoneId)
        Response.Success(Unit)
    }

    override fun buildLocalClock(locale: Locale?): LocalClockSnapshot =
        clockFormatter.buildLocalClock(locale ?: localeRepository.getSelectedLocale())

    override fun buildCityTimes(cities: List<WorldCity>): List<WorldCityTime> =
        clockFormatter.buildCityTimes(cities)
}
