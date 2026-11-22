package kz.zeme.timer.di

import kz.zeme.timer.core.locale.di.coreLocaleModule
import kz.zeme.timer.core.locale.view.di.coreLocaleViewModule
import kz.zeme.timer.core.navigation.di.coreNavigationModule
import kz.zeme.timer.core.ui.di.coreUiModule
import org.koin.core.module.Module

open class KoinModules {

    open fun getModules(): List<Module> = listOf(
        coreNavigationModule,
        coreUiModule,
        coreLocaleModule,
        coreLocaleViewModule,
        appModule,
    )
}
