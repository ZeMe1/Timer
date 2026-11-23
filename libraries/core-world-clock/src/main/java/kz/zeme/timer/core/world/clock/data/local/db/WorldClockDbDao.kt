package kz.zeme.timer.core.world.clock.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface WorldClockDbDao {

    @Query("SELECT * FROM world_cities ORDER BY sort_order ASC")
    suspend fun getAllCities(): List<WorldCityDbEntity>

    @Query("SELECT MAX(sort_order) FROM world_cities")
    suspend fun getMaxCitySortOrder(): Int?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCity(city: WorldCityDbEntity)

    @Query("DELETE FROM world_cities WHERE zone_id = :zoneId")
    suspend fun deleteCityByZoneId(zoneId: String)
}
