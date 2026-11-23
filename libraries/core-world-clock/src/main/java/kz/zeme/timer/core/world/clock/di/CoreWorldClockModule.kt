package kz.zeme.timer.core.world.clock.di

import androidx.room.Room
import kz.zeme.timer.core.di.extensions.binds
import kz.zeme.timer.core.world.clock.data.DefaultWorldClockRepository
import kz.zeme.timer.core.world.clock.data.DefaultWorldClockTimeZoneDataSource
import kz.zeme.timer.core.world.clock.data.WorldClockDbEntityMapper
import kz.zeme.timer.core.world.clock.data.WorldClockFormatter
import kz.zeme.timer.core.world.clock.data.WorldClockTimeZoneDataSource
import kz.zeme.timer.core.world.clock.data.local.db.WorldClockDb
import kz.zeme.timer.core.world.clock.domain.WorldClockRepository
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

private const val WORLD_CLOCK_DB_NAME = "WorldClock.db"

val coreWorldClockModule = module {

    factory { WorldClockDbEntityMapper() }
    factory { WorldClockFormatter() }

    single {
        DefaultWorldClockTimeZoneDataSource()
    }.binds(
        WorldClockTimeZoneDataSource::class,
    )

    single<WorldClockDb> {
        Room.databaseBuilder(
            androidApplication(),
            WorldClockDb::class.java,
            WORLD_CLOCK_DB_NAME,
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration(true)
            .build()
    }

    single {
        DefaultWorldClockRepository(
            localDataSource = get<WorldClockDb>().worldClockDao(),
            timeZoneDataSource = get(),
            localeRepository = get(),
            dbEntityMapper = get(),
            clockFormatter = get(),
        )
    }.binds(
        WorldClockRepository::class,
    )
}
