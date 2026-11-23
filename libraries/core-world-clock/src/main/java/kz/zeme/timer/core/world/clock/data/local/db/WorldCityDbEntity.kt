package kz.zeme.timer.core.world.clock.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "world_cities")
data class WorldCityDbEntity(
    @PrimaryKey
    @ColumnInfo(name = "zone_id")
    val zoneId: String,
    @ColumnInfo(name = "sort_order")
    val sortOrder: Int,
)
