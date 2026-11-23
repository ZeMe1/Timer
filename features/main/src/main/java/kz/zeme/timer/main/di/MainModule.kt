package kz.zeme.timer.main.di

import kz.zeme.timer.main.presentation.MainVM
import kz.zeme.timer.main.presentation.navigation.MainRouter
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mainModule = module {

    factory { MainRouter() }

    viewModel {
        MainVM(
            stopwatchRepository = get(),
            timerRepository = get(),
            themeRepository = get(),
        )
    }
}
