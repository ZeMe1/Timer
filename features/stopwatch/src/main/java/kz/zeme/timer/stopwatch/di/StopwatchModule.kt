package kz.zeme.timer.stopwatch.di

import kz.zeme.timer.stopwatch.presentation.StopwatchVM
import kz.zeme.timer.stopwatch.presentation.navigation.StopwatchRouter
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val stopwatchModule = module {

    factory { StopwatchRouter() }

    viewModel {
        StopwatchVM(
            stopwatchRepository = get(),
            timeFormatter = get(),
        )
    }
}
