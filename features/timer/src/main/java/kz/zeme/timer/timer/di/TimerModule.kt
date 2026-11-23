package kz.zeme.timer.timer.di

import kz.zeme.timer.timer.presentation.TimerVM
import kz.zeme.timer.timer.presentation.navigation.TimerRouter
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val timerModule = module {

    factory { TimerRouter() }

    viewModel {
        TimerVM(
            timerRepository = get(),
            timeFormatter = get(),
        )
    }
}
