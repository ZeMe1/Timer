package kz.zeme.timer.timer.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.core.observable.EventFlow
import kz.zeme.timer.core.observable.MutableEventFlow
import kz.zeme.timer.core.observable.ViewState
import kz.zeme.timer.core.observable.extensions.update
import kz.zeme.timer.core.timer.domain.TimerDuration
import kz.zeme.timer.core.timer.domain.TimerRepository
import kz.zeme.timer.core.timer.domain.TimerState as DomainTimerState
import kz.zeme.timer.core.timer.domain.TimerStatus
import kz.zeme.timer.core.timer.domain.TimerTimeFormatter
import kz.zeme.timer.timer.presentation.TimerState.WheelColumn
import kz.zeme.timer.timer.presentation.navigation.TimerNavigation

private const val TICK_INTERVAL_MS = 100L
private const val HOURS_COUNT = TimerDuration.MAX_HOURS + 1
private const val MINUTES_COUNT = TimerDuration.MAX_MINUTES + 1
private const val SECONDS_COUNT = TimerDuration.MAX_SECONDS + 1

open class TimerVM(
    private val timerRepository: TimerRepository,
    private val timeFormatter: TimerTimeFormatter,
) : ViewModel(), TimerController {

    private val _navigation = MutableEventFlow<Navigator.Navigation>(viewModelScope)
    private val _state = MutableLiveData<ViewState<TimerState>>()

    private var _observeTimerJob: Job? = null
    private var _tickJob: Job? = null

    val navigation: EventFlow<Navigator.Navigation> = _navigation.asEventFlow()
    val state: LiveData<ViewState<TimerState>> = _state

    override fun onStart() = onObserveTimer()

    override fun onRefresh() = onObserveTimer()

    override fun onHoursDelta(delta: Int) {
        val duration = timerRepository.state.value.configuredDuration
        timerRepository.updateDuration(
            hours = shiftUnit(duration.hours, delta, HOURS_COUNT),
            minutes = duration.minutes,
            seconds = duration.seconds,
        )
    }

    override fun onMinutesDelta(delta: Int) {
        val duration = timerRepository.state.value.configuredDuration
        timerRepository.updateDuration(
            hours = duration.hours,
            minutes = shiftUnit(duration.minutes, delta, MINUTES_COUNT),
            seconds = duration.seconds,
        )
    }

    override fun onSecondsDelta(delta: Int) {
        val duration = timerRepository.state.value.configuredDuration
        timerRepository.updateDuration(
            hours = duration.hours,
            minutes = duration.minutes,
            seconds = shiftUnit(duration.seconds, delta, SECONDS_COUNT),
        )
    }

    override fun onStartClick() {
        _navigation.value = TimerNavigation.RequestNotificationPermission
        timerRepository.start()
    }

    override fun onPauseClick() = timerRepository.pause()

    override fun onResumeClick() = timerRepository.resume()

    override fun onResetClick() = timerRepository.reset()

    override fun onRepeatClick() = timerRepository.repeat()

    override fun onDoneClick() = timerRepository.dismissFinished()

    private fun onObserveTimer() {
        _observeTimerJob?.cancel()
        _observeTimerJob = observeTimer()
    }

    private fun observeTimer() = viewModelScope.launch {
        timerRepository.state.collect(::applyDomainState)
    }

    private fun applyDomainState(domainState: DomainTimerState) {
        val remaining = timerRepository.getRemaining()
        val duration = domainState.configuredDuration
        val isFinished = domainState.status == TimerStatus.Finished

        _state.value = ViewState.Data(
            TimerState(
                status = domainState.status,
                hoursWheel = buildWheelColumn(duration.hours, HOURS_COUNT),
                minutesWheel = buildWheelColumn(duration.minutes, MINUTES_COUNT),
                secondsWheel = buildWheelColumn(duration.seconds, SECONDS_COUNT),
                canStart = !duration.isZero,
                formattedRemaining = if (isFinished) timeFormatter.formatRemaining(0L)
                                     else timeFormatter.formatRemaining(remaining.remainingMs),
                progress = if (isFinished) 1f else remaining.progress,
                endsAtClock = timeFormatter.formatEndClock(domainState.endEpochMs),
            ),
        )

        if (domainState.status == TimerStatus.Running) onStartTicking()
        else onStopTicking()
    }

    private fun onStartTicking() {
        _tickJob?.cancel()
        _tickJob = startTicking()
    }

    private fun startTicking() = viewModelScope.launch {
        while (isActive) {
            delay(TICK_INTERVAL_MS)
            updateRemaining()
        }
    }

    private fun onStopTicking() {
        _tickJob?.cancel()
        _tickJob = null
    }

    private fun updateRemaining() {
        val remaining = timerRepository.getRemaining()
        _state.update {
            copy(
                formattedRemaining = timeFormatter.formatRemaining(remaining.remainingMs),
                progress = remaining.progress,
            )
        }
    }

    private fun buildWheelColumn(
        value: Int,
        maxCount: Int,
    ): WheelColumn = WheelColumn(
        prev2 = timeFormatter.formatUnit(shiftUnit(value, -2, maxCount)),
        prev1 = timeFormatter.formatUnit(shiftUnit(value, -1, maxCount)),
        current = timeFormatter.formatUnit(value),
        next1 = timeFormatter.formatUnit(shiftUnit(value, 1, maxCount)),
        next2 = timeFormatter.formatUnit(shiftUnit(value, 2, maxCount)),
    )

    private fun shiftUnit(
        value: Int,
        delta: Int,
        maxCount: Int,
    ): Int = Math.floorMod(value + delta, maxCount)
}
