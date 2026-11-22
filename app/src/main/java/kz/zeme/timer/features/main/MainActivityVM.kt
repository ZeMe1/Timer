package kz.zeme.timer.features.main

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kz.zeme.timer.core.navigation.BaseNavigation
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.core.observable.EventFlow
import kz.zeme.timer.core.observable.MutableEventFlow
import kz.zeme.timer.features.navigation.deeplink.AppRedirectParser

class MainActivityVM(
    private val appRedirectParser: AppRedirectParser,
) : ViewModel() {

    private val _navigation = MutableEventFlow<Navigator.Navigation>(viewModelScope)
    val navigation: EventFlow<Navigator.Navigation> = _navigation.asEventFlow()

    fun onCreate(
        extras: Bundle?,
        withSplash: Boolean = false,
    ) {
        val redirect = appRedirectParser.getRedirectOrNull(extras)
        _navigation.value = BaseNavigation.ShowMain(
            restart = false,
            redirect = redirect,
            withSplash = withSplash,
        )
    }

    fun onRedirect(intent: Intent?) {
        val redirect = appRedirectParser.getRedirectOrNull(intent) ?: return
        _navigation.value = redirect
    }
}
