package kz.zeme.timer.core.world.clock.view.presentation

interface CityPickerController {

    fun onStart()

    fun onQueryChange(query: String)

    fun onCityClick(item: CityPickerItem)

    fun onClose()
}
