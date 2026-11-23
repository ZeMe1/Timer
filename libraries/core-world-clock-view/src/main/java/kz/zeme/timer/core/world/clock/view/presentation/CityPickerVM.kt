package kz.zeme.timer.core.world.clock.view.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kz.zeme.timer.core.domain.RequestCode.RC_CITY_PICKER
import kz.zeme.timer.core.domain.Response
import kz.zeme.timer.core.navigation.BaseNavigation.Close
import kz.zeme.timer.core.navigation.BaseNavigation.ShowToast
import kz.zeme.timer.core.navigation.NavigationResultLiveData
import kz.zeme.timer.core.navigation.NavigationResultLiveData.NavigationResult.DialogResult
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.core.observable.EventFlow
import kz.zeme.timer.core.observable.MutableEventFlow
import kz.zeme.timer.core.observable.ViewState
import kz.zeme.timer.core.observable.extensions.getLoadingState
import kz.zeme.timer.core.observable.extensions.getViewState
import kz.zeme.timer.core.observable.extensions.update
import kz.zeme.timer.core.res.R
import kz.zeme.timer.core.world.clock.domain.WorldCity
import kz.zeme.timer.core.world.clock.domain.WorldClockRepository

private const val SEARCH_DEBOUNCE_MS = 250L

open class CityPickerVM(
    private val worldClockRepository: WorldClockRepository,
    private val navigationResult: NavigationResultLiveData,
) : ViewModel(), CityPickerController {

    private val _navigation = MutableEventFlow<Navigator.Navigation>(viewModelScope)
    private val _state = MutableLiveData<ViewState<CityPickerState>>()

    private var _searchJob: Job? = null
    private var _toggleCityJob: Job? = null
    private var _lastQuery: String = ""

    val navigation: EventFlow<Navigator.Navigation> = _navigation.asEventFlow()
    val state: LiveData<ViewState<CityPickerState>> = _state

    override fun onStart() = onSearch(
        query = _lastQuery,
        debounceMs = 0L,
    )

    override fun onQueryChange(query: String) {
        _lastQuery = query
        _state.update { copy(query = query) }

        val debounceMs = if (query.isBlank()) 0L else SEARCH_DEBOUNCE_MS
        onSearch(
            query = query,
            debounceMs = debounceMs,
        )
    }

    override fun onCityClick(item: CityPickerItem) = onToggleCity(item)

    override fun onClose() {
        _navigation.value = Close
    }

    private fun onSearch(
        query: String,
        debounceMs: Long,
    ) {
        _searchJob?.cancel()
        _searchJob = search(
            query = query,
            debounceMs = debounceMs,
        )
    }

    private fun search(
        query: String,
        debounceMs: Long,
    ) = viewModelScope.launch {
        if (debounceMs > 0L) delay(debounceMs)

        if (_state.value == null || _state.value is ViewState.Error) _state.value = _state.getLoadingState()

        val savedCities = when (val savedResponse = worldClockRepository.getSavedCities()) {
            is Response.Failure -> {
                _state.value = _state.getViewState(
                    response = savedResponse,
                    transform = { _state.value?.data ?: CityPickerState(query = _lastQuery) },
                )
                return@launch
            }

            is Response.Success -> savedResponse.data
        }

        val savedZoneIds = savedCities.mapTo(hashSetOf()) { it.zoneId }

        val availableResponse = worldClockRepository.getAvailableCities(query)

        _state.value = _state.getViewState(
            response = availableResponse,
            transform = { cities ->
                buildPickerState(
                    query = _lastQuery,
                    cities = cities,
                    savedZoneIds = savedZoneIds,
                )
            },
        )
    }

    private fun onToggleCity(item: CityPickerItem) {
        _toggleCityJob?.cancel()
        _toggleCityJob = toggleCity(item)
    }

    private fun toggleCity(item: CityPickerItem) = viewModelScope.launch {
        val response =
            if (item.isAdded) worldClockRepository.removeCity(item.zoneId)
            else worldClockRepository.addCity(item.zoneId)

        when (response) {
            is Response.Failure -> _navigation.value = ShowToast.MessageErr(response.data)

            is Response.Success -> {
                if (!item.isAdded) _navigation.value = ShowToast.MessageRes(R.string.world_clock_city_added)
                navigationResult.setResult(DialogResult.Ok(requestCode = RC_CITY_PICKER))
                _navigation.value = Close
            }
        }
    }

    private fun buildPickerState(
        query: String,
        cities: List<WorldCity>,
        savedZoneIds: Set<String>,
    ): CityPickerState {
        val cityTimesByZoneId = worldClockRepository
            .buildCityTimes(cities)
            .associateBy { it.zoneId }

        val items = cities.map { city ->
            CityPickerItem(
                zoneId = city.zoneId,
                name = city.name,
                formattedTime = cityTimesByZoneId[city.zoneId]?.formattedTime.orEmpty(),
                isAdded = city.zoneId in savedZoneIds,
            )
        }.toPersistentList()

        return CityPickerState(
            query = query,
            items = items,
        )
    }
}
