package kz.zeme.timer.main.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.FragmentContainerView
import kz.zeme.timer.core.fragment.BaseFragment
import kz.zeme.timer.core.fragment.extensions.composeView
import kz.zeme.timer.core.navigation.ExternalModulesNavigator
import kz.zeme.timer.core.res.R
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainFragment : BaseFragment() {

    private val mainVM: MainVM by viewModel()
    private val externalModulesNavigator: ExternalModulesNavigator by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        val tabContainerView = FragmentContainerView(requireContext()).apply {
            id = R.id.main_tab_container_view
        }

        return composeView {
            MainScreen(
                state = mainVM.state.observeAsState(),
                controller = mainVM,
                tabContainerView = tabContainerView,
            )
        }.also {
            mainVM.onStart()
        }
    }

    override fun startObserving() {
        mainVM.navigation.observeEvent(
            owner = viewLifecycleOwner,
            onUpdate = externalModulesNavigator::navigate,
        )
    }

    override fun handledOnBackPressed(): Boolean = mainVM.onBackPressed()
}
