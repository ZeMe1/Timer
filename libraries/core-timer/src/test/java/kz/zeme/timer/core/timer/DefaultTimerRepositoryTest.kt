package kz.zeme.timer.core.timer

import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kz.zeme.timer.core.timer.data.DefaultTimerRepository
import kz.zeme.timer.core.timer.data.TimerCountdownEngine
import kz.zeme.timer.core.timer.domain.TimerAlertManager
import kz.zeme.timer.core.timer.domain.TimerServiceController
import kz.zeme.timer.core.timer.domain.TimerStatus
import kz.zeme.timer.core.timer.domain.TimerTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultTimerRepositoryTest {

    @Test
    fun startPauseResumeAndResetManageCountdownAndService() = runTest {
        var nowMs = 1_000L
        var epochMs = 1_700_000_000_000L
        val alertManager = FakeTimerAlertManager()
        val serviceController = FakeTimerServiceController()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = DefaultTimerRepository(
            engine = TimerCountdownEngine(
                elapsedRealtimeProvider = { nowMs },
                epochMillisProvider = { epochMs },
            ),
            alertManager = alertManager,
            serviceController = serviceController,
            dispatcher = dispatcher,
        )

        repository.updateDuration(hours = 0, minutes = 1, seconds = 30)
        assertEquals(90_000L, repository.state.value.configuredDuration.totalMs)

        repository.start()
        assertEquals(TimerStatus.Running, repository.state.value.status)
        assertTrue(repository.isRunning.value)
        assertEquals(epochMs + 90_000L, repository.state.value.endEpochMs)
        assertEquals(1, serviceController.startCalls)

        nowMs += 30_000L
        assertEquals(60_000L, repository.getRemaining().remainingMs)

        repository.pause()
        assertEquals(TimerStatus.Paused, repository.state.value.status)
        assertFalse(repository.isRunning.value)
        assertNull(repository.state.value.endEpochMs)

        nowMs += 20_000L
        assertEquals(60_000L, repository.getRemaining().remainingMs)

        epochMs += 50_000L
        repository.resume()
        assertEquals(TimerStatus.Running, repository.state.value.status)
        assertEquals(epochMs + 60_000L, repository.state.value.endEpochMs)
        assertEquals(2, serviceController.startCalls)

        repository.reset()
        assertEquals(TimerStatus.Idle, repository.state.value.status)
        assertEquals(1, serviceController.stopCalls)
        assertEquals(1, alertManager.stopCalls)
    }

    @Test
    fun countdownCompletionTriggersAlertAndSupportsRepeatAndDismiss() = runTest {
        var nowMs = 0L
        val alertManager = FakeTimerAlertManager()
        val serviceController = FakeTimerServiceController()
        val dispatcher = StandardTestDispatcher(testScheduler)
        val repository = DefaultTimerRepository(
            engine = TimerCountdownEngine(
                elapsedRealtimeProvider = { nowMs },
                epochMillisProvider = { 10_000L },
            ),
            alertManager = alertManager,
            serviceController = serviceController,
            dispatcher = dispatcher,
        )

        repository.updateDuration(hours = 0, minutes = 0, seconds = 5)
        repository.start()

        nowMs = 5_000L
        advanceTimeBy(5_000L)
        runCurrent()

        assertEquals(TimerStatus.Finished, repository.state.value.status)
        assertFalse(repository.isRunning.value)
        assertEquals(0L, repository.getRemaining().remainingMs)
        assertEquals(1, alertManager.startCalls)

        repository.repeat()
        assertEquals(TimerStatus.Running, repository.state.value.status)
        assertEquals(1, alertManager.stopCalls)
        assertEquals(5_000L, repository.getRemaining().remainingMs)

        repository.dismissFinished()
        assertEquals(TimerStatus.Idle, repository.state.value.status)
        assertEquals(2, alertManager.stopCalls)
        assertEquals(1, serviceController.stopCalls)
    }

    @Test
    fun timeFormatterFormatsRemainingAndEndClock() {
        val formatter = TimerTimeFormatter(zoneProvider = { ZoneOffset.UTC })

        assertEquals("00:00", formatter.formatRemaining(0L))
        assertEquals("00:01", formatter.formatRemaining(1L))
        assertEquals("03:18", formatter.formatRemaining(198_000L))
        assertEquals("01:01:01", formatter.formatRemaining(3_661_000L))
        assertEquals("05", formatter.formatUnit(5))

        val endEpoch = Instant.parse("2026-11-23T22:05:00Z").toEpochMilli()
        assertEquals("22:05", formatter.formatEndClock(endEpoch))
        assertEquals("", formatter.formatEndClock(null))
        assertEquals(24, formatter.hoursWheelItems.size)
        assertEquals("00", formatter.hoursWheelItems.first())
        assertEquals("23", formatter.hoursWheelItems.last())
        assertEquals(60, formatter.minutesWheelItems.size)
        assertEquals("59", formatter.minutesWheelItems.last())
        assertEquals(60, formatter.secondsWheelItems.size)
    }

    private class FakeTimerAlertManager : TimerAlertManager {
        var startCalls: Int = 0
        var stopCalls: Int = 0

        override fun startAlert() {
            startCalls++
        }

        override fun stopAlert() {
            stopCalls++
        }
    }

    private class FakeTimerServiceController : TimerServiceController {
        var startCalls: Int = 0
        var stopCalls: Int = 0

        override fun start() {
            startCalls++
        }

        override fun stop() {
            stopCalls++
        }
    }
}
