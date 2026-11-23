package kz.zeme.timer.core.stopwatch.di

import kz.zeme.timer.core.di.extensions.binds
import kz.zeme.timer.core.stopwatch.data.DefaultStopwatchRepository
import kz.zeme.timer.core.stopwatch.domain.StopwatchRepository
import kz.zeme.timer.core.stopwatch.domain.StopwatchTimeFormatter
import org.koin.dsl.module

val coreStopwatchModule = module {

    factory { StopwatchTimeFormatter() }

    single {
        DefaultStopwatchRepository()
    }.binds(
        StopwatchRepository::class,
    )
}
