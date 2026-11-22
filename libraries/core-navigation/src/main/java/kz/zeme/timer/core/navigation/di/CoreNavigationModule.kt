package kz.zeme.timer.core.navigation.di

import kz.zeme.timer.core.navigation.NavigationResultLiveData
import org.koin.dsl.module

val coreNavigationModule = module {
    single { NavigationResultLiveData() }
}
