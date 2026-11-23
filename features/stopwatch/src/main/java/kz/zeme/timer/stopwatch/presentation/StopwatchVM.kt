package kz.zeme.timer.stopwatch.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.core.observable.EventFlow
import kz.zeme.timer.core.observable.MutableEventFlow
import kz.zeme.timer.core.observable.ViewState
import kz.zeme.timer.core.observable.extensions.update
import kz.zeme.timer.core.stopwatch.domain.StopwatchLap
import kz.zeme.timer.core.stopwatch.domain.StopwatchRepository
import kz.zeme.timer.core.stopwatch.domain.StopwatchState as DomainStopwatchState
import kz.zeme.timer.core.stopwatch.domain.StopwatchStatus
import kz.zeme.timer.core.stopwatch.domain.StopwatchTimeFormatter
import kz.zeme.timer.stopwatch.presentation.StopwatchState.StopwatchLapItem

private const val TICK_INTERVAL_MS = 33L

open class StopwatchVM(
    private val stopwatchRepository: StopwatchRepository,
    private val timeFormatter: StopwatchTimeFormatter,
) : ViewModel(), StopwatchController {

    private val _navigation = MutableEventFlow<Navigator.Navigation>(viewModelScope)
    private val _state = MutableLiveData<ViewState<StopwatchState>>()

    private var _observeStopwatchJob: Job? = null
    private var _tickJob: Job? = null

    val navigation: EventFlow<Navigator.Navigation> = _navigation.asEventFlow()
    val state: LiveData<ViewState<StopwatchState>> = _state

    override fun onStart() = onObserveStopwatch()

    override fun onRefresh() = onObserveStopwatch()

    override fun onStartClick() = stopwatchRepository.start()

    override fun onPauseClick() = stopwatchRepository.pause()

    override fun onResumeClick() = stopwatchRepository.start()

    override fun onResetClick() = stopwatchRepository.reset()

    override fun onLapClick() = stopwatchRepository.addLap()

    private fun onObserveStopwatch() {
        _observeStopwatchJob?.cancel()
        _observeStopwatchJob = observeStopwatch()
    }

    private fun observeStopwatch() = viewModelScope.launch {
        stopwatchRepository.state.collect(::applyDomainState)
    }

    private fun applyDomainState(domainState: DomainStopwatchState) {
        val elapsed = stopwatchRepository.getElapsed()
        val parts = timeFormatter.formatParts(elapsed.elapsedMs)
        val hasLaps = domainState.laps.isNotEmpty()

        _state.value = ViewState.Data(
            StopwatchState(
                status = domainState.status,
                mainTime = parts.mainTime,
                centiseconds = parts.centiseconds,
                currentLapNumber = if (hasLaps) timeFormatter.formatLapNumber(domainState.laps.size + 1) else null,
                currentLapTime = if (hasLaps) timeFormatter.formatFull(elapsed.currentLapElapsedMs) else null,
                laps = formatLaps(domainState.laps),
            ),
        )

        if (domainState.status == StopwatchStatus.Running) onStartTicking()
        else onStopTicking()
    }

    private fun formatLaps(
        laps: ImmutableList<StopwatchLap>,
    ): ImmutableList<StopwatchLapItem> = laps
        .asReversed()
        .map { lap ->
            StopwatchLapItem(
                number = lap.number,
                numberText = timeFormatter.formatLapNumber(lap.number),
                deltaText = "+${timeFormatter.formatFull(lap.deltaMs)}",
                totalText = timeFormatter.formatFull(lap.totalMs),
            )
        }
        .toPersistentList()

    private fun onStartTicking() {
        _tickJob?.cancel()
        _tickJob = startTicking()
    }

    private fun startTicking() = viewModelScope.launch {
        while (isActive) {
            delay(TICK_INTERVAL_MS)
            updateElapsed()
        }
    }

    private fun onStopTicking() {
        _tickJob?.cancel()
        _tickJob = null
    }

    private fun updateElapsed() {
        val elapsed = stopwatchRepository.getElapsed()
        val parts = timeFormatter.formatParts(elapsed.elapsedMs)

        _state.update {
            copy(
                mainTime = parts.mainTime,
                centiseconds = parts.centiseconds,
                currentLapTime = currentLapNumber?.let { timeFormatter.formatFull(elapsed.currentLapElapsedMs) },
            )
        }
    }
}
