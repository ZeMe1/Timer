package kz.zeme.timer.core.stopwatch.data

import android.os.SystemClock
import java.util.concurrent.atomic.AtomicReference
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

private const val MAX_LAPS = 999

class DefaultStopwatchRepository(
    private val elapsedRealtimeProvider: () -> Long = SystemClock::elapsedRealtime,
) : StopwatchRepository {

    private data class EngineState(
        val status: StopwatchStatus = StopwatchStatus.Idle,
        val accumulatedElapsedMs: Long = 0L,
        val startedAtRealtimeMs: Long = 0L,
        val lastLapTotalMs: Long = 0L,
        val laps: PersistentList<StopwatchLap> = persistentListOf(),
    )

    private val engineState = AtomicReference(EngineState())
    private val _state = MutableStateFlow(StopwatchState())
    private val _isRunning = MutableStateFlow(false)

    override val state: StateFlow<StopwatchState> = _state.asStateFlow()
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    override fun getElapsed(): StopwatchElapsed {
        val current = engineState.get()
        val elapsedMs = calculateElapsedMs(current, elapsedRealtimeProvider())
        val currentLapElapsedMs = (elapsedMs - current.lastLapTotalMs).coerceAtLeast(0L)

        return StopwatchElapsed(
            elapsedMs = elapsedMs,
            currentLapElapsedMs = currentLapElapsedMs,
        )
    }

    override fun start() {
        val now = elapsedRealtimeProvider()
        val updated = engineState.updateAndGet { current ->
            if (current.status == StopwatchStatus.Running) current
            else current.copy(
                status = StopwatchStatus.Running,
                startedAtRealtimeMs = now,
            )
        }

        publish(updated)
    }

    override fun pause() {
        val now = elapsedRealtimeProvider()
        val updated = engineState.updateAndGet { current ->
            if (current.status != StopwatchStatus.Running) current
            else current.copy(
                status = StopwatchStatus.Paused,
                accumulatedElapsedMs = calculateElapsedMs(current, now),
            )
        }

        publish(updated)
    }

    override fun reset() {
        val updated = EngineState()
        engineState.set(updated)
        publish(updated)
    }

    override fun addLap() {
        val now = elapsedRealtimeProvider()
        val updated = engineState.updateAndGet { current ->
            if (current.status != StopwatchStatus.Running || current.laps.size >= MAX_LAPS) current
            else {
                val totalMs = calculateElapsedMs(current, now)
                val deltaMs = (totalMs - current.lastLapTotalMs).coerceAtLeast(0L)
                val newLap = StopwatchLap(
                    number = current.laps.size + 1,
                    deltaMs = deltaMs,
                    totalMs = totalMs,
                )

                current.copy(
                    lastLapTotalMs = totalMs,
                    laps = current.laps.add(newLap),
                )
            }
        }

        publish(updated)
    }

    private fun publish(engine: EngineState) {
        _state.value = StopwatchState(
            status = engine.status,
            laps = engine.laps,
        )
        _isRunning.value = engine.status == StopwatchStatus.Running
    }

    private fun calculateElapsedMs(
        engine: EngineState,
        nowRealtimeMs: Long,
    ): Long {
        if (engine.status != StopwatchStatus.Running) return engine.accumulatedElapsedMs

        val delta = (nowRealtimeMs - engine.startedAtRealtimeMs).coerceAtLeast(0L)
        return engine.accumulatedElapsedMs + delta
    }
}
