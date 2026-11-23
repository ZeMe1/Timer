package kz.zeme.timer.timer.presentation

interface TimerController {

    fun onStart()

    fun onRefresh()

    fun onHoursSelected(hours: Int)

    fun onMinutesSelected(minutes: Int)

    fun onSecondsSelected(seconds: Int)

    fun onStartClick()

    fun onPauseClick()

    fun onResumeClick()

    fun onResetClick()

    fun onRepeatClick()

    fun onDoneClick()
}
