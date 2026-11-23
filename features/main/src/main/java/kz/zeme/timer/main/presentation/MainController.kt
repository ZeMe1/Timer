package kz.zeme.timer.main.presentation

interface MainController {

    fun onStart()

    fun onRefresh()

    fun onTabSelected(tab: MainTab)

    fun onThemeClick()

    fun onLanguageClick()

    fun onAddCityClick()
}
