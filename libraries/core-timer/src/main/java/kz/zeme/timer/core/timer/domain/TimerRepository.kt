package kz.zeme.timer.core.timer.domain

import kotlinx.coroutines.flow.StateFlow

interface TimerRepository {

    val state: StateFlow<TimerState>
    val isRunning: StateFlow<Boolean>

    fun getRemaining(): TimerRemaining
    fun updateDuration(hours: Int, minutes: Int, seconds: Int)
    fun start()
    fun pause()
    fun resume()
    fun reset()
    fun repeat()
    fun dismissFinished()
}
