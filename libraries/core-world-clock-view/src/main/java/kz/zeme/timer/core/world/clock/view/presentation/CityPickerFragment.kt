package kz.zeme.timer.core.world.clock.view.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.compose.runtime.livedata.observeAsState
import kz.zeme.timer.core.fragment.BaseBottomSheetDialog
import kz.zeme.timer.core.fragment.extensions.composeView
import kz.zeme.timer.core.navigation.BaseNavigation
import kz.zeme.timer.core.navigation.ExternalModulesNavigator
import kz.zeme.timer.core.navigation.Navigator
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CityPickerFragment : BaseBottomSheetDialog() {

    private val cityPickerVM: CityPickerVM by viewModel()
    private val externalModulesNavigator: ExternalModulesNavigator by inject()

    override val isExpanded: Boolean = true
    override val skipHalfCollapse: Boolean = true
    @Suppress("DEPRECATION")
    override val softInputMode: Int = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = composeView {
        CityPickerScreen(
            state = cityPickerVM.state.observeAsState(),
            controller = cityPickerVM,
        )
    }.also {
        cityPickerVM.onStart()
    }

    override fun startObserving() {
        cityPickerVM.navigation.observeEvent(
            owner = viewLifecycleOwner,
            onUpdate = ::navigate,
        )
    }

    private fun navigate(navigation: Navigator.Navigation) = when (navigation) {
        is BaseNavigation.GoBack,
        is BaseNavigation.Close -> dismiss()
        else -> externalModulesNavigator.navigate(navigation)
    }
}
