package kz.zeme.timer.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module

object KoinController {

    fun onStart(
        application: Application,
        modules: List<Module>,
    ) {
        startKoin {
            allowOverride(true)
            androidContext(application)
            modules(modules = modules)
        }
    }
}
