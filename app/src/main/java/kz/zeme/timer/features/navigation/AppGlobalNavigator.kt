package kz.zeme.timer.features.navigation

import android.content.Intent
import androidx.core.net.toUri
import androidx.fragment.app.commit
import kz.zeme.timer.MainActivity
import kz.zeme.timer.core.activity.ActivityProvider
import kz.zeme.timer.core.activity.extensions.makeToast
import kz.zeme.timer.core.fragment.BaseFragment
import kz.zeme.timer.core.fragment.extensions.isShown
import kz.zeme.timer.core.locale.view.presentation.navigation.LanguagePickerNavigation
import kz.zeme.timer.core.locale.view.presentation.navigation.LanguagePickerRouter
import kz.zeme.timer.core.navigation.BaseNavigation
import kz.zeme.timer.core.navigation.ExternalModulesNavigator
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.core.navigation.extensions.putRedirect
import kz.zeme.timer.core.res.R as CoreResR
import kz.zeme.timer.core.theme.view.presentation.navigation.ThemePickerNavigation
import kz.zeme.timer.core.theme.view.presentation.navigation.ThemePickerRouter
import kz.zeme.timer.core.world.clock.view.presentation.navigation.CityPickerNavigation
import kz.zeme.timer.core.world.clock.view.presentation.navigation.CityPickerRouter
import kz.zeme.timer.main.presentation.MainTab
import kz.zeme.timer.main.presentation.navigation.MainExternalNavigation
import kz.zeme.timer.main.presentation.navigation.MainRouter
import kz.zeme.timer.splash.SplashExternalNavigation
import kz.zeme.timer.splash.SplashFragment
import kz.zeme.timer.stopwatch.presentation.navigation.StopwatchRouter
import kz.zeme.timer.timer.presentation.navigation.TimerRouter
import kz.zeme.timer.world.clock.presentation.navigation.WorldClockRouter

class AppGlobalNavigator(
    private val activityProvider: ActivityProvider,
    private val mainRouter: MainRouter,
    private val stopwatchRouter: StopwatchRouter,
    private val timerRouter: TimerRouter,
    private val worldClockRouter: WorldClockRouter,
    private val languagePickerRouter: LanguagePickerRouter,
    private val themePickerRouter: ThemePickerRouter,
    private val cityPickerRouter: CityPickerRouter,
) : ExternalModulesNavigator {

    override fun navigate(navigation: Navigator.Navigation) = when (navigation) {
        is BaseNavigation.ShowMain -> showMain(navigation)
        is SplashExternalNavigation.ShowNextScreen -> showNextScreen(navigation)
        is MainExternalNavigation.SelectTab -> selectTab(navigation)
        is LanguagePickerNavigation.OpenLanguagePicker -> openLanguagePicker()
        is ThemePickerNavigation.OpenThemePicker -> openThemePicker()
        is CityPickerNavigation.OpenCityPicker -> openCityPicker()
        is BaseNavigation.ShowToast.Message -> showToast(navigation)
        is BaseNavigation.ShowToast.MessageErr -> showErrorToast(navigation)
        is BaseNavigation.ShowToast.MessageRes -> showResToast(navigation)
        is BaseNavigation.ShowDeeplink -> showDeeplink(navigation)
        else -> Unit
    }

    private fun showMain(navigation: BaseNavigation.ShowMain) {
        val activity = activityProvider.currentActivity ?: return

        if (navigation.restart) {
            val intent = Intent(activity, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                putRedirect(navigation.redirect)
            }
            activity.finish()
            activity.startActivity(intent)
            return
        }

        val fm = activity.supportFragmentManager

        if (navigation.withSplash) {
            val hasSplash = fm.fragments
                .filterIsInstance<SplashFragment>()
                .isNotEmpty()

            if (hasSplash) return

            fm.commit {
                setReorderingAllowed(true)
                replace(CoreResR.id.fragment_container_view, SplashFragment())
            }
            return
        }

        val tag = MainRouter.TAG_MAIN
        val existingFragment = fm.findFragmentByTag(tag)

        if (existingFragment == null) {
            fm.commit {
                setReorderingAllowed(true)
                replace(
                    CoreResR.id.fragment_container_view,
                    mainRouter.getMainFragment(),
                    tag,
                )
            }
        }

        navigation.redirect?.let(::navigate)
    }

    private fun showNextScreen(navigation: SplashExternalNavigation.ShowNextScreen) {
        val redirect = (navigation.redirect as? BaseNavigation.ShowMain)?.redirect ?: navigation.redirect
        showMain(
            BaseNavigation.ShowMain(
                restart = false,
                redirect = redirect,
                withSplash = false,
            ),
        )
    }

    private fun selectTab(navigation: MainExternalNavigation.SelectTab) {
        val mainFragment = activityProvider.currentActivity
            ?.supportFragmentManager
            ?.findFragmentByTag(MainRouter.TAG_MAIN)
            ?: return

        val childFm = mainFragment.childFragmentManager
        val targetTag = when (navigation.tab) {
            MainTab.Stopwatch -> StopwatchRouter.TAG_STOPWATCH
            MainTab.Timer -> TimerRouter.TAG_TIMER
            MainTab.WorldClock -> WorldClockRouter.TAG_WORLD_CLOCK
        }

        childFm.commit {
            setReorderingAllowed(true)
            childFm.fragments
                .filter { it.tag != targetTag }
                .forEach(::hide)

            val existing = childFm.findFragmentByTag(targetTag)
            if (existing != null) show(existing)
            else add(
                CoreResR.id.main_tab_container_view,
                createTabFragment(navigation.tab),
                targetTag,
            )
        }
    }

    private fun createTabFragment(tab: MainTab): BaseFragment = when (tab) {
        MainTab.Stopwatch -> stopwatchRouter.getStopwatchFragment()
        MainTab.Timer -> timerRouter.getTimerFragment()
        MainTab.WorldClock -> worldClockRouter.getWorldClockFragment()
    }

    private fun openLanguagePicker() {
        val fm = activityProvider.currentActivity?.supportFragmentManager ?: return
        val tag = LanguagePickerRouter.TAG_LANGUAGE_PICKER

        if (fm.isShown(tag)) return

        languagePickerRouter
            .getLanguagePickerFragment()
            .show(fm, tag)
    }

    private fun openThemePicker() {
        val fm = activityProvider.currentActivity?.supportFragmentManager ?: return
        val tag = ThemePickerRouter.TAG_THEME_PICKER

        if (fm.isShown(tag)) return

        themePickerRouter
            .getThemePickerFragment()
            .show(fm, tag)
    }

    private fun openCityPicker() {
        val fm = activityProvider.currentActivity?.supportFragmentManager ?: return
        val tag = CityPickerRouter.TAG_CITY_PICKER

        if (fm.isShown(tag)) return

        cityPickerRouter
            .getCityPickerFragment()
            .show(fm, tag)
    }

    private fun showToast(navigation: BaseNavigation.ShowToast.Message) {
        val context = activityProvider.currentActivity ?: return
        context.makeToast(navigation.msg)
    }

    private fun showErrorToast(navigation: BaseNavigation.ShowToast.MessageErr) {
        val context = activityProvider.currentActivity ?: return
        context.makeToast(navigation.error)
    }

    private fun showResToast(navigation: BaseNavigation.ShowToast.MessageRes) {
        val context = activityProvider.currentActivity ?: return
        context.makeToast(navigation.msgRes)
    }

    private fun showDeeplink(navigation: BaseNavigation.ShowDeeplink) {
        val context = activityProvider.currentActivity ?: return
        context.startActivity(Intent(Intent.ACTION_VIEW, navigation.url.toUri()))
    }
}
