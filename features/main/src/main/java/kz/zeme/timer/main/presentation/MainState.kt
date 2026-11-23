package kz.zeme.timer.main.presentation

data class MainState(
    val selectedTab: MainTab = MainTab.Timer,
    val isStopwatchRunning: Boolean = false,
    val isTimerRunning: Boolean = false,
    val isDarkTheme: Boolean = true,
    val showAddCityAction: Boolean = false,
)
