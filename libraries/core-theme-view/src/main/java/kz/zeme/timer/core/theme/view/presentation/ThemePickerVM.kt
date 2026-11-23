package kz.zeme.timer.core.theme.view.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kz.zeme.timer.core.domain.Response
import kz.zeme.timer.core.navigation.BaseNavigation.Close
import kz.zeme.timer.core.navigation.BaseNavigation.ShowToast
import kz.zeme.timer.core.navigation.Navigator
import kz.zeme.timer.core.observable.EventFlow
import kz.zeme.timer.core.observable.MutableEventFlow
import kz.zeme.timer.core.observable.ViewState
import kz.zeme.timer.core.observable.extensions.getErrorState
import kz.zeme.timer.core.observable.extensions.getLoadingState
import kz.zeme.timer.core.observable.extensions.getViewState
import kz.zeme.timer.core.res.R
import kz.zeme.timer.core.theme.domain.ThemeOption
import kz.zeme.timer.core.theme.domain.ThemeRepository

open class ThemePickerVM(
    private val themeRepository: ThemeRepository,
) : ViewModel(), ThemePickerController {

    private val _navigation = MutableEventFlow<Navigator.Navigation>(viewModelScope)
    private val _themes = MutableLiveData<ViewState<List<ThemeOption>>>()

    private var _getThemesJob: Job? = null
    private var _saveThemeJob: Job? = null

    val navigation: EventFlow<Navigator.Navigation> = _navigation.asEventFlow()
    val themes: LiveData<ViewState<List<ThemeOption>>> = _themes

    override fun onThemeClick(option: ThemeOption) = onSaveSelectedTheme(option)

    override fun onClose() {
        _navigation.value = Close
    }

    override fun onStart() = onLoadThemes()

    private fun onLoadThemes() {
        _getThemesJob?.cancel()
        _getThemesJob = loadThemes()
    }

    private fun loadThemes() = viewModelScope.launch {
        _themes.value = _themes.getLoadingState()

        val response = themeRepository.getThemes()

        _themes.value = _themes.getViewState(response)
    }

    private fun onSaveSelectedTheme(option: ThemeOption) {
        _saveThemeJob?.cancel()
        _saveThemeJob = saveTheme(option)
    }

    private fun saveTheme(option: ThemeOption) = viewModelScope.launch {
        _themes.value = _themes.getLoadingState()

        when (val response = themeRepository.saveSelectedTheme(option.mode)) {
            is Response.Failure -> _themes.value = _themes.getErrorState(response)
            is Response.Success -> {
                _navigation.value = ShowToast.MessageRes(R.string.theme_picker_changed_message)
                _navigation.value = Close
            }
        }
    }
}
