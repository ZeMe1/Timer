package kz.zeme.timer.core.stopwatch.di

import kz.zeme.timer.core.di.extensions.binds
import kz.zeme.timer.core.stopwatch.data.DefaultStopwatchRepository
import kz.zeme.timer.core.stopwatch.domain.StopwatchRepository
import org.koin.dsl.module

val coreStopwatchModule = module {

    single {
        DefaultStopwatchRepository()
    }.binds(
        StopwatchRepository::class,
    )
}
