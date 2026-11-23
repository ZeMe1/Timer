package kz.zeme.timer.core.stopwatch.domain

import kotlinx.coroutines.flow.StateFlow

interface StopwatchRepository {

    val state: StateFlow<StopwatchState>
    val isRunning: StateFlow<Boolean>

    fun getElapsed(): StopwatchElapsed
    fun start()
    fun pause()
    fun reset()
    fun addLap()
}
