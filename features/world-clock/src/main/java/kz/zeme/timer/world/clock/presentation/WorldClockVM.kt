package kz.zeme.timer.world.clock.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kz.zeme.timer.core.domain.RequestCode.RC_CITY_PICKER
import kz.zeme.timer.core.domain.Response
import kz.zeme.timer.core.navigation.BaseNavigation.ShowToast
import kz.zeme.timer.core.navigation.NavigationResultLiveData
import kz.zeme.timer.core.navigation.NavigationResultLiveData.NavigationResult
import kz.zeme.timer.core.navigation.NavigationResultLiveData.NavigationResult.DialogResult
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.core.observable.Event
import kz.zeme.timer.core.observable.EventFlow
import kz.zeme.timer.core.observable.MutableEventFlow
import kz.zeme.timer.core.observable.ViewState
import kz.zeme.timer.core.observable.extensions.getLoadingState
import kz.zeme.timer.core.observable.extensions.getViewState
import kz.zeme.timer.core.world.clock.domain.WorldCity
import kz.zeme.timer.core.world.clock.domain.WorldClockRepository

private const val MILLIS_PER_SECOND = 1_000L

open class WorldClockVM(
    private val worldClockRepository: WorldClockRepository,
    private val navigationResult: NavigationResultLiveData,
) : ViewModel(), WorldClockController {

    private val _navigation = MutableEventFlow<Navigator.Navigation>(viewModelScope)
    private val _state = MutableLiveData<ViewState<WorldClockState>>()

    private var _loadCitiesJob: Job? = null
    private var _tickClockJob: Job? = null
    private var _removeCityJob: Job? = null
    private var _savedCities: List<WorldCity> = emptyList()

    val navigation: EventFlow<Navigator.Navigation> = _navigation.asEventFlow()
    val state: LiveData<ViewState<WorldClockState>> = _state

    init {
        _navigation.addSource(navigationResult, ::onNavigationResult)
    }

    override fun onStart() = onLoadCities()

    override fun onRefresh() = onLoadCities()

    override fun onRemoveCityClick(zoneId: String) = onRemoveCity(zoneId)

    private fun onNavigationResult(result: Event<NavigationResult>) {
        if (result.peekContent().requestCode != RC_CITY_PICKER) return
        if (result.getContentIfNotHandled() !is DialogResult.Ok) return

        onLoadCities()
    }

    private fun onLoadCities() {
        _loadCitiesJob?.cancel()
        _loadCitiesJob = loadCities()
    }

    private fun loadCities() = viewModelScope.launch {
        if (_state.value == null || _state.value is ViewState.Error) _state.value = _state.getLoadingState()

        val response = worldClockRepository.getSavedCities()

        _state.value = _state.getViewState(
            response = response,
            transform = { cities ->
                _savedCities = cities
                buildState(cities)
            },
        )

        if (response is Response.Success) onStartClockTick()
        else onStopClockTick()
    }

    private fun onRemoveCity(zoneId: String) {
        _removeCityJob?.cancel()
        _removeCityJob = removeCity(zoneId)
    }

    private fun removeCity(zoneId: String) = viewModelScope.launch {
        when (val response = worldClockRepository.removeCity(zoneId)) {
            is Response.Failure -> _navigation.value = ShowToast.MessageErr(response.data)
            is Response.Success -> onLoadCities()
        }
    }

    private fun onStartClockTick() {
        _tickClockJob?.cancel()
        _tickClockJob = startClockTick()
    }

    private fun startClockTick() = viewModelScope.launch {
        while (isActive) {
            val delayMs = MILLIS_PER_SECOND - (System.currentTimeMillis() % MILLIS_PER_SECOND)
            delay(delayMs)
            updateClock()
        }
    }

    private fun onStopClockTick() {
        _tickClockJob?.cancel()
        _tickClockJob = null
    }

    private fun updateClock() {
        if (_state.value !is ViewState.Data) return
        _state.value = ViewState.Data(buildState(_savedCities))
    }

    private fun buildState(cities: List<WorldCity>): WorldClockState {
        val localClock = worldClockRepository.buildLocalClock()
        val localDateUtc =
            if (localClock.dateText.isNotBlank()) "${localClock.dateText} · ${localClock.utcOffsetText}"
            else localClock.utcOffsetText
        val cityTimes = worldClockRepository.buildCityTimes(cities).toPersistentList()

        return WorldClockState(
            localTime = localClock.formattedTime,
            localDateUtc = localDateUtc,
            cities = cityTimes,
            canRemoveCities = cityTimes.size > 1,
        )
    }
}
