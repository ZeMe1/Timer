package kz.zeme.timer.features.navigation

import android.content.Intent
import androidx.core.net.toUri
import kz.zeme.timer.MainActivity
import kz.zeme.timer.core.activity.ActivityProvider
import kz.zeme.timer.core.activity.extensions.makeToast
import kz.zeme.timer.core.fragment.extensions.isShown
import kz.zeme.timer.core.locale.view.presentation.navigation.LanguagePickerNavigation
import kz.zeme.timer.core.locale.view.presentation.navigation.LanguagePickerRouter
import kz.zeme.timer.core.navigation.BaseNavigation
import kz.zeme.timer.core.navigation.ExternalModulesNavigator
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.core.navigation.extensions.putRedirect

class AppGlobalNavigator(
    private val activityProvider: ActivityProvider,
    private val languagePickerRouter: LanguagePickerRouter,
) : ExternalModulesNavigator {

    override fun navigate(navigation: Navigator.Navigation) = when (navigation) {
        is BaseNavigation.ShowMain -> showMain(navigation)
        is LanguagePickerNavigation.OpenLanguagePicker -> openLanguagePicker()
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

        navigation.redirect?.let(::navigate)
    }

    private fun openLanguagePicker() {
        val fm = activityProvider.currentActivity?.supportFragmentManager ?: return
        val tag = LanguagePickerRouter.TAG_LANGUAGE_PICKER

        if (fm.isShown(tag)) return

        languagePickerRouter
            .getLanguagePickerFragment()
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
