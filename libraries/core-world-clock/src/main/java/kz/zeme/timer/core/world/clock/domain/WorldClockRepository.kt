package kz.zeme.timer.core.world.clock.domain

import java.util.Locale
import kz.zeme.timer.core.domain.Response

interface WorldClockRepository {

    suspend fun getSavedCities(): Response<List<WorldCity>>

    suspend fun getAvailableCities(query: String): Response<List<WorldCity>>

    suspend fun addCity(zoneId: String): Response<Unit>

    suspend fun removeCity(zoneId: String): Response<Unit>

    fun buildLocalClock(locale: Locale? = null): LocalClockSnapshot

    fun buildCityTimes(cities: List<WorldCity>): List<WorldCityTime>
}
