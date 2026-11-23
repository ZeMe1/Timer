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
import kz.zeme.timer.core.timer.domain.TimerRepository
import kz.zeme.timer.core.timer.domain.TimerState as DomainTimerState
import kz.zeme.timer.core.timer.domain.TimerStatus
import kz.zeme.timer.core.timer.domain.TimerTimeFormatter
import kz.zeme.timer.timer.presentation.navigation.TimerNavigation

private const val TICK_INTERVAL_MS = 100L

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

    override fun onHoursSelected(hours: Int) {
        val duration = timerRepository.state.value.configuredDuration
        if (duration.hours == hours) return

        timerRepository.updateDuration(
            hours = hours,
            minutes = duration.minutes,
            seconds = duration.seconds,
        )
    }

    override fun onMinutesSelected(minutes: Int) {
        val duration = timerRepository.state.value.configuredDuration
        if (duration.minutes == minutes) return

        timerRepository.updateDuration(
            hours = duration.hours,
            minutes = minutes,
            seconds = duration.seconds,
        )
    }

    override fun onSecondsSelected(seconds: Int) {
        val duration = timerRepository.state.value.configuredDuration
        if (duration.seconds == seconds) return

        timerRepository.updateDuration(
            hours = duration.hours,
            minutes = duration.minutes,
            seconds = seconds,
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
                hoursItems = timeFormatter.hoursWheelItems,
                minutesItems = timeFormatter.minutesWheelItems,
                secondsItems = timeFormatter.secondsWheelItems,
                selectedHours = duration.hours,
                selectedMinutes = duration.minutes,
                selectedSeconds = duration.seconds,
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
}
