package kz.zeme.timer.core.timer.di

import kz.zeme.timer.core.di.extensions.binds
import kz.zeme.timer.core.timer.data.DefaultTimerAlertManager
import kz.zeme.timer.core.timer.data.DefaultTimerRepository
import kz.zeme.timer.core.timer.domain.TimerAlertManager
import kz.zeme.timer.core.timer.domain.TimerRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreTimerModule = module {

    single {
        DefaultTimerAlertManager(
            context = androidContext(),
        )
    }.binds(
        TimerAlertManager::class,
    )

    single {
        DefaultTimerRepository(
            alertManager = get(),
        )
    }.binds(
        TimerRepository::class,
    )
}
