package kz.zeme.timer.core.timer.data

import android.os.SystemClock
import kz.zeme.timer.core.timer.domain.TimerDuration
import kz.zeme.timer.core.timer.domain.TimerRemaining
import kz.zeme.timer.core.timer.domain.TimerState
import kz.zeme.timer.core.timer.domain.TimerStatus

class TimerCountdownEngine(
    private val elapsedRealtimeProvider: () -> Long = SystemClock::elapsedRealtime,
    private val epochMillisProvider: () -> Long = System::currentTimeMillis,
) {

    var status: TimerStatus = TimerStatus.Idle
        private set

    private var configuredDuration: TimerDuration = TimerDuration()
    private var totalMs: Long = configuredDuration.totalMs
    private var pausedRemainingMs: Long = configuredDuration.totalMs
    private var endRealtimeMs: Long = 0L
    private var endEpochMs: Long? = null

    fun toState(): TimerState = TimerState(
        status = status,
        configuredDuration = configuredDuration,
        totalMs = totalMs,
        endEpochMs = endEpochMs,
    )

    fun getRemaining(): TimerRemaining {
        val remainingMs = when (status) {
            TimerStatus.Idle -> configuredDuration.totalMs
            TimerStatus.Running -> (endRealtimeMs - elapsedRealtimeProvider()).coerceIn(0L, totalMs)
            TimerStatus.Paused -> pausedRemainingMs
            TimerStatus.Finished -> 0L
        }

        return TimerRemaining(
            remainingMs = remainingMs,
            totalMs = totalMs,
        )
    }

    fun updateDuration(
        hours: Int,
        minutes: Int,
        seconds: Int,
    ) {
        configuredDuration = TimerDuration.normalized(hours, minutes, seconds)
        totalMs = configuredDuration.totalMs
        pausedRemainingMs = totalMs
    }

    fun start(): Long? {
        val durationMs = configuredDuration.totalMs
        if (durationMs <= 0L) return null

        startWith(totalMs = durationMs, remainingMs = durationMs)
        return durationMs
    }

    fun pause(): Long {
        val remainingMs = (endRealtimeMs - elapsedRealtimeProvider()).coerceIn(0L, totalMs)
        status = TimerStatus.Paused
        pausedRemainingMs = remainingMs
        endRealtimeMs = 0L
        endEpochMs = null
        return remainingMs
    }

    fun resume(): Long {
        if (pausedRemainingMs <= 0L) return 0L

        startWith(totalMs = totalMs, remainingMs = pausedRemainingMs)
        return pausedRemainingMs
    }

    fun reset() {
        status = TimerStatus.Idle
        totalMs = configuredDuration.totalMs
        pausedRemainingMs = totalMs
        endRealtimeMs = 0L
        endEpochMs = null
    }

    fun finish() {
        status = TimerStatus.Finished
        pausedRemainingMs = 0L
        endRealtimeMs = 0L
        endEpochMs = null
    }

    private fun startWith(
        totalMs: Long,
        remainingMs: Long,
    ) {
        this.status = TimerStatus.Running
        this.totalMs = totalMs
        this.pausedRemainingMs = remainingMs
        this.endRealtimeMs = elapsedRealtimeProvider() + remainingMs
        this.endEpochMs = epochMillisProvider() + remainingMs
    }
}
