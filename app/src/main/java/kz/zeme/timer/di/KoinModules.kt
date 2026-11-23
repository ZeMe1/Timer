package kz.zeme.timer.di

import kz.zeme.timer.core.locale.di.coreLocaleModule
import kz.zeme.timer.core.locale.view.di.coreLocaleViewModule
import kz.zeme.timer.core.navigation.di.coreNavigationModule
import kz.zeme.timer.core.stopwatch.di.coreStopwatchModule
import kz.zeme.timer.core.theme.di.coreThemeModule
import kz.zeme.timer.core.theme.view.di.coreThemeViewModule
import kz.zeme.timer.core.timer.di.coreTimerModule
import kz.zeme.timer.core.ui.di.coreUiModule
import kz.zeme.timer.core.world.clock.di.coreWorldClockModule
import kz.zeme.timer.core.world.clock.view.di.coreWorldClockViewModule
import kz.zeme.timer.main.di.mainModule
import kz.zeme.timer.stopwatch.di.stopwatchModule
import kz.zeme.timer.timer.di.timerModule
import kz.zeme.timer.world.clock.di.worldClockModule
import org.koin.core.module.Module

open class KoinModules {

    open fun getModules(): List<Module> = listOf(
        coreNavigationModule,
        coreUiModule,
        coreLocaleModule,
        coreLocaleViewModule,
        coreThemeModule,
        coreThemeViewModule,
        coreStopwatchModule,
        coreTimerModule,
        coreWorldClockModule,
        coreWorldClockViewModule,
        mainModule,
        stopwatchModule,
        timerModule,
        worldClockModule,
        appModule,
    )
}
