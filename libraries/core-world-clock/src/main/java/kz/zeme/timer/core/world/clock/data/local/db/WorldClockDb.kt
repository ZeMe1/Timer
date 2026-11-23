package kz.zeme.timer.core.world.clock.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        WorldCityDbEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class WorldClockDb : RoomDatabase() {

    abstract fun worldClockDao(): WorldClockDbDao
}
