package kz.zeme.timer.core.stopwatch.data

import android.os.SystemClock
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kz.zeme.timer.core.stopwatch.domain.StopwatchElapsed
import kz.zeme.timer.core.stopwatch.domain.StopwatchLap
import kz.zeme.timer.core.stopwatch.domain.StopwatchRepository
import kz.zeme.timer.core.stopwatch.domain.StopwatchState
import kz.zeme.timer.core.stopwatch.domain.StopwatchStatus

class DefaultStopwatchRepository(
    private val elapsedRealtimeProvider: () -> Long = SystemClock::elapsedRealtime,
) : StopwatchRepository {

    private var status: StopwatchStatus = StopwatchStatus.Idle
    private var accumulatedElapsedMs: Long = 0L
    private var startedAtRealtimeMs: Long = 0L
    private var lastLapTotalMs: Long = 0L
    private var laps: PersistentList<StopwatchLap> = persistentListOf()

    private val _state = MutableStateFlow(StopwatchState())
    private val _isRunning = MutableStateFlow(false)

    override val state: StateFlow<StopwatchState> = _state.asStateFlow()
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    override fun getElapsed(): StopwatchElapsed {
        val elapsedMs = currentElapsedMs()

        return StopwatchElapsed(
            elapsedMs = elapsedMs,
            currentLapElapsedMs = (elapsedMs - lastLapTotalMs).coerceAtLeast(0L),
        )
    }

    override fun start() {
        if (status == StopwatchStatus.Running) return

        status = StopwatchStatus.Running
        startedAtRealtimeMs = elapsedRealtimeProvider()
        publish()
    }

    override fun pause() {
        if (status != StopwatchStatus.Running) return

        accumulatedElapsedMs = currentElapsedMs()
        status = StopwatchStatus.Paused
        publish()
    }

    override fun reset() {
        status = StopwatchStatus.Idle
        accumulatedElapsedMs = 0L
        startedAtRealtimeMs = 0L
        lastLapTotalMs = 0L
        laps = persistentListOf()
        publish()
    }

    override fun addLap() {
        if (status != StopwatchStatus.Running) return

        val totalMs = currentElapsedMs()
        val lap = StopwatchLap(
            number = laps.size + 1,
            deltaMs = (totalMs - lastLapTotalMs).coerceAtLeast(0L),
            totalMs = totalMs,
        )

        lastLapTotalMs = totalMs
        laps = laps.add(lap)
        publish()
    }

    private fun currentElapsedMs(): Long =
        if (status != StopwatchStatus.Running) accumulatedElapsedMs
        else accumulatedElapsedMs + (elapsedRealtimeProvider() - startedAtRealtimeMs).coerceAtLeast(0L)

    private fun publish() {
        _state.value = StopwatchState(status = status, laps = laps)
        _isRunning.value = status == StopwatchStatus.Running
    }
}
