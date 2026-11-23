package kz.zeme.timer.di

import android.app.Application
import kz.zeme.timer.core.activity.ActivityProvider
import kz.zeme.timer.core.di.extensions.binds
import kz.zeme.timer.core.navigation.ExternalModulesNavigator
import kz.zeme.timer.core.theme.domain.ThemeApplier
import kz.zeme.timer.features.AppGlobalActivityProvider
import kz.zeme.timer.features.AppGlobalThemeApplier
import kz.zeme.timer.features.main.MainActivityVM
import kz.zeme.timer.features.navigation.AppGlobalNavigator
import kz.zeme.timer.features.navigation.deeplink.AppGlobalRedirectParser
import kz.zeme.timer.features.navigation.deeplink.AppRedirectParser
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single {
        AppGlobalActivityProvider()
    }.binds(
        ActivityProvider::class,
        Application.ActivityLifecycleCallbacks::class,
    )

    single {
        AppGlobalThemeApplier()
    }.binds(
        ThemeApplier::class,
    )

    factory {
        AppGlobalRedirectParser()
    }.binds(
        AppRedirectParser::class,
    )

    viewModel {
        MainActivityVM(
            appRedirectParser = get(),
        )
    }

    single {
        AppGlobalNavigator(
            activityProvider = get(),
            mainRouter = get(),
            stopwatchRouter = get(),
            timerRouter = get(),
            worldClockRouter = get(),
            languagePickerRouter = get(),
            themePickerRouter = get(),
            cityPickerRouter = get(),
        )
    }.binds(
        ExternalModulesNavigator::class,
    )
}
