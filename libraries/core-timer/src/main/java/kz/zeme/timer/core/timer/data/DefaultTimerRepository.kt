package kz.zeme.timer.core.timer.data

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kz.zeme.timer.core.timer.domain.TimerAlertManager
import kz.zeme.timer.core.timer.domain.TimerRemaining
import kz.zeme.timer.core.timer.domain.TimerRepository
import kz.zeme.timer.core.timer.domain.TimerServiceController
import kz.zeme.timer.core.timer.domain.TimerState
import kz.zeme.timer.core.timer.domain.TimerStatus

class DefaultTimerRepository(
    private val engine: TimerCountdownEngine,
    private val alertManager: TimerAlertManager,
    private val serviceController: TimerServiceController,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : TimerRepository {

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val _state = MutableStateFlow(engine.toState())
    private val _isRunning = MutableStateFlow(false)

    private var completionJob: Job? = null

    override val state: StateFlow<TimerState> = _state.asStateFlow()
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    override fun getRemaining(): TimerRemaining = engine.getRemaining()

    override fun updateDuration(
        hours: Int,
        minutes: Int,
        seconds: Int,
    ) {
        engine.updateDuration(hours, minutes, seconds)
        publish()
    }

    override fun start() {
        val delayMs = engine.start() ?: return
        publish()
        serviceController.start()
        scheduleCompletion(delayMs)
    }

    override fun pause() {
        cancelCompletion()
        val remainingMs = engine.pause()
        if (remainingMs <= 0L) finishCountdown()
        else publish()
    }

    override fun resume() {
        val delayMs = engine.resume()
        if (delayMs <= 0L) finishCountdown()
        else {
            publish()
            serviceController.start()
            scheduleCompletion(delayMs)
        }
    }

    override fun reset() {
        cancelCompletion()
        alertManager.stopAlert()
        engine.reset()
        publish()
        serviceController.stop()
    }

    override fun repeat() {
        cancelCompletion()
        alertManager.stopAlert()
        val delayMs = engine.start()
        if (delayMs == null) {
            engine.reset()
            publish()
            serviceController.stop()
            return
        }

        publish()
        serviceController.start()
        scheduleCompletion(delayMs)
    }

    override fun dismissFinished() = reset()

    private fun scheduleCompletion(delayMs: Long) {
        completionJob?.cancel()
        completionJob = scope.launch {
            delay(delayMs)
            finishCountdown()
        }
    }

    private fun cancelCompletion() {
        completionJob?.cancel()
        completionJob = null
    }

    private fun finishCountdown() {
        cancelCompletion()
        engine.finish()
        publish()
        alertManager.startAlert()
    }

    private fun publish() {
        _state.value = engine.toState()
        _isRunning.value = engine.status == TimerStatus.Running
    }
}
