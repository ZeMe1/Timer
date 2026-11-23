package kz.zeme.timer.core.world.clock.view.di

import kz.zeme.timer.core.world.clock.view.presentation.CityPickerVM
import kz.zeme.timer.core.world.clock.view.presentation.navigation.CityPickerRouter
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val coreWorldClockViewModule = module {

    factory { CityPickerRouter() }

    viewModel {
        CityPickerVM(
            worldClockRepository = get(),
            navigationResult = get(),
        )
    }
}
