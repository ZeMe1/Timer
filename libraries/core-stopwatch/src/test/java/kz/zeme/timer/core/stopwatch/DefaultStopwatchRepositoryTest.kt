package kz.zeme.timer.core.stopwatch

import kz.zeme.timer.core.stopwatch.data.DefaultStopwatchRepository
import kz.zeme.timer.core.stopwatch.domain.FormattedStopwatchTime
import kz.zeme.timer.core.stopwatch.domain.StopwatchStatus
import kz.zeme.timer.core.stopwatch.domain.StopwatchTimeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultStopwatchRepositoryTest {

    @Test
    fun startPauseResumeAndResetTrackElapsedTimeAccurately() {
        var nowMs = 1_000L
        val repository = DefaultStopwatchRepository(elapsedRealtimeProvider = { nowMs })

        assertEquals(StopwatchStatus.Idle, repository.state.value.status)
        assertFalse(repository.isRunning.value)

        repository.start()
        assertEquals(StopwatchStatus.Running, repository.state.value.status)
        assertTrue(repository.isRunning.value)

        nowMs += 2_500L
        assertEquals(2_500L, repository.getElapsed().elapsedMs)

        repository.pause()
        assertEquals(StopwatchStatus.Paused, repository.state.value.status)
        assertFalse(repository.isRunning.value)

        nowMs += 10_000L
        assertEquals(2_500L, repository.getElapsed().elapsedMs)

        repository.start()
        nowMs += 1_250L
        assertEquals(3_750L, repository.getElapsed().elapsedMs)

        repository.reset()
        assertEquals(StopwatchStatus.Idle, repository.state.value.status)
        assertEquals(0L, repository.getElapsed().elapsedMs)
        assertTrue(repository.state.value.laps.isEmpty())
    }

    @Test
    fun addLapCalculatesDeltaAndTotalCorrectly() {
        var nowMs = 5_000L
        val repository = DefaultStopwatchRepository(elapsedRealtimeProvider = { nowMs })

        repository.addLap()
        assertTrue(repository.state.value.laps.isEmpty())

        repository.start()
        nowMs += 12_480L
        repository.addLap()

        nowMs += 9_120L
        repository.addLap()

        nowMs += 4_870L
        val elapsed = repository.getElapsed()
        val laps = repository.state.value.laps

        assertEquals(2, laps.size)
        assertEquals(1, laps[0].number)
        assertEquals(12_480L, laps[0].deltaMs)
        assertEquals(12_480L, laps[0].totalMs)
        assertEquals(2, laps[1].number)
        assertEquals(9_120L, laps[1].deltaMs)
        assertEquals(21_600L, laps[1].totalMs)
        assertEquals(26_470L, elapsed.elapsedMs)
        assertEquals(4_870L, elapsed.currentLapElapsedMs)
    }

    @Test
    fun timeFormatterFormatsPartsFullAndLapNumbers() {
        val formatter = StopwatchTimeFormatter()

        assertEquals(FormattedStopwatchTime("00:00", ".00"), formatter.formatParts(0L))
        assertEquals(FormattedStopwatchTime("00:42", ".31"), formatter.formatParts(42_319L))
        assertEquals("01:05.09", formatter.formatFull(65_090L))
        assertEquals("00:00.00", formatter.formatFull(-500L))
        assertEquals("01", formatter.formatLapNumber(1))
        assertEquals("12", formatter.formatLapNumber(12))
    }
}
