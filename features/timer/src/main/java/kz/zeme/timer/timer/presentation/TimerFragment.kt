package kz.zeme.timer.timer.presentation

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.livedata.observeAsState
import kz.zeme.timer.core.activity.extensions.isPermissionGranted
import kz.zeme.timer.core.fragment.BaseFragment
import kz.zeme.timer.core.fragment.extensions.composeView
import kz.zeme.timer.core.navigation.ExternalModulesNavigator
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.timer.presentation.navigation.TimerNavigation
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TimerFragment : BaseFragment() {

    private val timerVM: TimerVM by viewModel()
    private val externalModulesNavigator: ExternalModulesNavigator by inject()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = composeView {
        TimerScreen(
            state = timerVM.state.observeAsState(),
            controller = timerVM,
        )
    }.also {
        timerVM.onStart()
    }

    override fun startObserving() {
        timerVM.navigation.observeEvent(
            owner = viewLifecycleOwner,
            onUpdate = ::navigate,
        )
    }

    private fun navigate(navigation: Navigator.Navigation) = when (navigation) {
        is TimerNavigation.RequestNotificationPermission -> requestNotificationPermission()
        else -> externalModulesNavigator.navigate(navigation)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val currentActivity = activity ?: return
        if (currentActivity.isPermissionGranted(Manifest.permission.POST_NOTIFICATIONS)) return

        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
