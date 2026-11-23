package kz.zeme.timer.core.timer.data

import android.os.SystemClock
import java.util.concurrent.atomic.AtomicReference
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
import kz.zeme.timer.core.timer.domain.TimerDuration
import kz.zeme.timer.core.timer.domain.TimerRemaining
import kz.zeme.timer.core.timer.domain.TimerRepository
import kz.zeme.timer.core.timer.domain.TimerState
import kz.zeme.timer.core.timer.domain.TimerStatus

private const val COMPLETION_CHECK_SLICE_MS = 60_000L

class DefaultTimerRepository(
    private val alertManager: TimerAlertManager,
    private val elapsedRealtimeProvider: () -> Long = SystemClock::elapsedRealtime,
    private val epochMillisProvider: () -> Long = System::currentTimeMillis,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : TimerRepository {

    private data class EngineState(
        val generation: Long = 0L,
        val status: TimerStatus = TimerStatus.Idle,
        val configuredDuration: TimerDuration = TimerDuration(),
        val totalMs: Long = configuredDuration.totalMs,
        val pausedRemainingMs: Long = configuredDuration.totalMs,
        val endRealtimeMs: Long = 0L,
        val endEpochMs: Long? = null,
    )

    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val transitionLock = Any()
    private val engineState = AtomicReference(EngineState())
    private val _state = MutableStateFlow(TimerState())
    private val _isRunning = MutableStateFlow(false)

    private var completionJob: Job? = null

    override val state: StateFlow<TimerState> = _state.asStateFlow()
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    override fun getRemaining(): TimerRemaining {
        val current = engineState.get()
        val remainingMs = when (current.status) {
            TimerStatus.Idle -> current.configuredDuration.totalMs
            TimerStatus.Running -> resolveRunningRemainingMs(current)
            TimerStatus.Paused -> current.pausedRemainingMs.coerceIn(0L, current.totalMs)
            TimerStatus.Finished -> 0L
        }

        return TimerRemaining(
            remainingMs = remainingMs,
            totalMs = current.totalMs,
        )
    }

    override fun updateDuration(
        hours: Int,
        minutes: Int,
        seconds: Int,
    ) {
        synchronized(transitionLock) {
            val current = engineState.get()
            if (current.status != TimerStatus.Idle) return

            val normalized = TimerDuration.normalized(
                hours = hours,
                minutes = minutes,
                seconds = seconds,
            )
            if (normalized == current.configuredDuration) return

            applyState(
                current.copy(
                    generation = current.generation + 1L,
                    configuredDuration = normalized,
                    totalMs = normalized.totalMs,
                    pausedRemainingMs = normalized.totalMs,
                ),
            )
        }
    }

    override fun start() {
        synchronized(transitionLock) {
            val current = engineState.get()
            if (current.status != TimerStatus.Idle) return

            val durationMs = current.configuredDuration.totalMs
            if (durationMs <= 0L) return

            startCountdownLocked(
                current = current,
                totalMs = durationMs,
                remainingMs = durationMs,
            )
        }
    }

    override fun pause() {
        synchronized(transitionLock) {
            val current = engineState.get()
            if (current.status != TimerStatus.Running) return

            val remainingMs = (current.endRealtimeMs - elapsedRealtimeProvider()).coerceIn(0L, current.totalMs)
            cancelCompletionLocked()

            if (remainingMs <= 0L) transitionToFinishedLocked(current)
            else transitionToPausedLocked(current, remainingMs)
        }
    }

    override fun resume() {
        synchronized(transitionLock) {
            val current = engineState.get()
            if (current.status != TimerStatus.Paused) return

            val remainingMs = current.pausedRemainingMs.coerceIn(0L, current.totalMs)
            if (remainingMs <= 0L) transitionToFinishedLocked(current)
            else startCountdownLocked(
                current = current,
                totalMs = current.totalMs,
                remainingMs = remainingMs,
            )
        }
    }

    override fun reset() {
        synchronized(transitionLock) {
            val current = engineState.get()
            if (current.status == TimerStatus.Idle) return

            cancelCompletionLocked()
            if (current.status == TimerStatus.Finished) alertManager.stopAlert()
            transitionToIdleLocked(current)
        }
    }

    override fun repeat() {
        synchronized(transitionLock) {
            val current = engineState.get()
            cancelCompletionLocked()
            if (current.status == TimerStatus.Finished) alertManager.stopAlert()

            val durationMs = current.configuredDuration.totalMs
            if (durationMs <= 0L) transitionToIdleLocked(current)
            else startCountdownLocked(
                current = current,
                totalMs = durationMs,
                remainingMs = durationMs,
            )
        }
    }

    override fun dismissFinished() {
        synchronized(transitionLock) {
            val current = engineState.get()
            if (current.status != TimerStatus.Finished) return

            cancelCompletionLocked()
            alertManager.stopAlert()
            transitionToIdleLocked(current)
        }
    }

    private fun resolveRunningRemainingMs(current: EngineState): Long {
        val leftMs = current.endRealtimeMs - elapsedRealtimeProvider()
        if (leftMs <= 0L) {
            finishIfMatching(current.generation)
            return 0L
        }

        return leftMs.coerceAtMost(current.totalMs)
    }

    private fun startCountdownLocked(
        current: EngineState,
        totalMs: Long,
        remainingMs: Long,
    ) {
        val nowRealtime = elapsedRealtimeProvider()
        val nowEpoch = epochMillisProvider()
        val updated = current.copy(
            generation = current.generation + 1L,
            status = TimerStatus.Running,
            totalMs = totalMs,
            pausedRemainingMs = remainingMs,
            endRealtimeMs = nowRealtime + remainingMs,
            endEpochMs = nowEpoch + remainingMs,
        )

        applyState(updated)
        scheduleCompletionLocked(updated.generation)
    }

    private fun transitionToPausedLocked(
        current: EngineState,
        remainingMs: Long,
    ) {
        applyState(
            current.copy(
                generation = current.generation + 1L,
                status = TimerStatus.Paused,
                pausedRemainingMs = remainingMs,
                endRealtimeMs = 0L,
                endEpochMs = null,
            ),
        )
    }

    private fun transitionToIdleLocked(current: EngineState) {
        val durationMs = current.configuredDuration.totalMs
        applyState(
            current.copy(
                generation = current.generation + 1L,
                status = TimerStatus.Idle,
                totalMs = durationMs,
                pausedRemainingMs = durationMs,
                endRealtimeMs = 0L,
                endEpochMs = null,
            ),
        )
    }

    private fun transitionToFinishedLocked(current: EngineState) {
        applyState(
            current.copy(
                generation = current.generation + 1L,
                status = TimerStatus.Finished,
                pausedRemainingMs = 0L,
                endRealtimeMs = 0L,
                endEpochMs = null,
            ),
        )
        alertManager.startAlert()
    }

    private fun applyState(updated: EngineState) {
        engineState.set(updated)
        _state.value = TimerState(
            status = updated.status,
            configuredDuration = updated.configuredDuration,
            totalMs = updated.totalMs,
            endEpochMs = updated.endEpochMs,
        )
        _isRunning.value = updated.status == TimerStatus.Running
    }

    private fun cancelCompletionLocked() {
        completionJob?.cancel()
        completionJob = null
    }

    private fun scheduleCompletionLocked(expectedGeneration: Long) {
        completionJob?.cancel()
        completionJob = scope.launch { awaitCompletion(expectedGeneration) }
    }

    private suspend fun awaitCompletion(expectedGeneration: Long) {
        while (true) {
            val current = engineState.get()
            if (current.generation != expectedGeneration || current.status != TimerStatus.Running) return

            val remainingMs = current.endRealtimeMs - elapsedRealtimeProvider()
            if (remainingMs <= 0L) {
                finishIfMatching(expectedGeneration)
                return
            }

            delay(remainingMs.coerceAtMost(COMPLETION_CHECK_SLICE_MS))
        }
    }

    private fun finishIfMatching(expectedGeneration: Long) {
        synchronized(transitionLock) {
            val current = engineState.get()
            if (current.generation != expectedGeneration || current.status != TimerStatus.Running) return

            cancelCompletionLocked()
            transitionToFinishedLocked(current)
        }
    }
}
