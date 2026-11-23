package kz.zeme.timer.timer.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.livedata.observeAsState
import kz.zeme.timer.core.fragment.BaseFragment
import kz.zeme.timer.core.fragment.extensions.composeView
import kz.zeme.timer.core.navigation.ExternalModulesNavigator
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class TimerFragment : BaseFragment() {

    private val timerVM: TimerVM by viewModel()
    private val externalModulesNavigator: ExternalModulesNavigator by inject()

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
            onUpdate = externalModulesNavigator::navigate,
        )
    }
}
