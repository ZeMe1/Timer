package kz.zeme.timer.core.timer.di

import kz.zeme.timer.core.di.extensions.binds
import kz.zeme.timer.core.timer.data.DefaultTimerAlertManager
import kz.zeme.timer.core.timer.data.DefaultTimerRepository
import kz.zeme.timer.core.timer.data.DefaultTimerServiceController
import kz.zeme.timer.core.timer.data.TimerCountdownEngine
import kz.zeme.timer.core.timer.data.TimerNotificationFactory
import kz.zeme.timer.core.timer.domain.TimerAlertManager
import kz.zeme.timer.core.timer.domain.TimerRepository
import kz.zeme.timer.core.timer.domain.TimerServiceController
import kz.zeme.timer.core.timer.domain.TimerTimeFormatter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreTimerModule = module {

    factory { TimerCountdownEngine() }
    factory { TimerTimeFormatter() }
    factory { TimerNotificationFactory(context = androidContext()) }

    single {
        DefaultTimerAlertManager(
            context = androidContext(),
        )
    }.binds(
        TimerAlertManager::class,
    )

    single {
        DefaultTimerServiceController(
            context = androidContext(),
        )
    }.binds(
        TimerServiceController::class,
    )

    single {
        DefaultTimerRepository(
            engine = get(),
            alertManager = get(),
            serviceController = get(),
        )
    }.binds(
        TimerRepository::class,
    )
}
