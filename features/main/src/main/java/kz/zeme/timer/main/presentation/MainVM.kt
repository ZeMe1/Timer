package kz.zeme.timer.main.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kz.zeme.timer.core.locale.view.presentation.navigation.LanguagePickerNavigation
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.core.observable.EventFlow
import kz.zeme.timer.core.observable.MutableEventFlow
import kz.zeme.timer.core.observable.ViewState
import kz.zeme.timer.core.observable.extensions.update
import kz.zeme.timer.core.stopwatch.domain.StopwatchRepository
import kz.zeme.timer.core.theme.domain.ThemeRepository
import kz.zeme.timer.core.theme.view.presentation.navigation.ThemePickerNavigation
import kz.zeme.timer.core.timer.domain.TimerRepository
import kz.zeme.timer.core.world.clock.view.presentation.navigation.CityPickerNavigation
import kz.zeme.timer.main.presentation.navigation.MainExternalNavigation

open class MainVM(
    private val stopwatchRepository: StopwatchRepository,
    private val timerRepository: TimerRepository,
    private val themeRepository: ThemeRepository,
) : ViewModel(), MainController {

    private val _navigation = MutableEventFlow<Navigator.Navigation>(viewModelScope)
    private val _state = MutableLiveData<ViewState<MainState>>()

    private var _observeStopwatchJob: Job? = null
    private var _observeTimerJob: Job? = null
    private var _observeThemeJob: Job? = null
    private var _selectedTab: MainTab = MainTab.Timer

    val navigation: EventFlow<Navigator.Navigation> = _navigation.asEventFlow()
    val state: LiveData<ViewState<MainState>> = _state

    override fun onStart() {
        _state.value = ViewState.Data(buildInitialState())
        onObserveStopwatch()
        onObserveTimer()
        onObserveTheme()
        _navigation.value = MainExternalNavigation.SelectTab(_selectedTab)
    }

    override fun onRefresh() = onStart()

    override fun onTabSelected(tab: MainTab) {
        if (tab == _selectedTab) return

        _selectedTab = tab
        _state.update {
            copy(
                selectedTab = tab,
                showAddCityAction = tab == MainTab.WorldClock,
            )
        }
        _navigation.value = MainExternalNavigation.SelectTab(tab)
    }

    override fun onThemeClick() {
        _navigation.value = ThemePickerNavigation.OpenThemePicker
    }

    override fun onLanguageClick() {
        _navigation.value = LanguagePickerNavigation.OpenLanguagePicker
    }

    override fun onAddCityClick() {
        _navigation.value = CityPickerNavigation.OpenCityPicker
    }

    fun onBackPressed(): Boolean {
        if (_selectedTab == MainTab.Timer) return false

        onTabSelected(MainTab.Timer)
        return true
    }

    private fun onObserveStopwatch() {
        _observeStopwatchJob?.cancel()
        _observeStopwatchJob = observeStopwatch()
    }

    private fun observeStopwatch() = viewModelScope.launch {
        stopwatchRepository.isRunning.collect { isRunning ->
            _state.update { copy(isStopwatchRunning = isRunning) }
        }
    }

    private fun onObserveTimer() {
        _observeTimerJob?.cancel()
        _observeTimerJob = observeTimer()
    }

    private fun observeTimer() = viewModelScope.launch {
        timerRepository.isRunning.collect { isRunning ->
            _state.update { copy(isTimerRunning = isRunning) }
        }
    }

    private fun onObserveTheme() {
        _observeThemeJob?.cancel()
        _observeThemeJob = observeTheme()
    }

    private fun observeTheme() = viewModelScope.launch {
        themeRepository.isDarkTheme.collect { isDark ->
            _state.update { copy(isDarkTheme = isDark) }
        }
    }

    private fun buildInitialState(): MainState = MainState(
        selectedTab = _selectedTab,
        isStopwatchRunning = stopwatchRepository.isRunning.value,
        isTimerRunning = timerRepository.isRunning.value,
        isDarkTheme = themeRepository.isDarkTheme.value,
        showAddCityAction = _selectedTab == MainTab.WorldClock,
    )
}
