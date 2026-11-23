package kz.zeme.timer.stopwatch.presentation

interface StopwatchController {

    fun onStart()

    fun onRefresh()

    fun onStartClick()

    fun onPauseClick()

    fun onResumeClick()

    fun onResetClick()

    fun onLapClick()
}
