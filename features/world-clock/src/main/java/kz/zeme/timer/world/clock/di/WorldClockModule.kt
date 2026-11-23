package kz.zeme.timer.world.clock.di

import kz.zeme.timer.world.clock.presentation.WorldClockVM
import kz.zeme.timer.world.clock.presentation.navigation.WorldClockRouter
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val worldClockModule = module {

    factory { WorldClockRouter() }

    viewModel {
        WorldClockVM(
            worldClockRepository = get(),
            navigationResult = get(),
        )
    }
}
