package kz.zeme.timer.timer.presentation

interface TimerController {

    fun onStart()

    fun onRefresh()

    fun onHoursDelta(delta: Int)

    fun onMinutesDelta(delta: Int)

    fun onSecondsDelta(delta: Int)

    fun onStartClick()

    fun onPauseClick()

    fun onResumeClick()

    fun onResetClick()

    fun onRepeatClick()

    fun onDoneClick()
}
