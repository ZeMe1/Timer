package kz.zeme.timer.core.theme.data

import android.content.SharedPreferences
import androidx.core.content.edit
import kz.zeme.timer.core.theme.domain.AppThemeMode

class ThemeLocalDataSource(
    private val sharedPreferences: SharedPreferences,
) {
    companion object {
        private const val THEME_MODE_KEY = "THEME_MODE_KEY"
    }

    fun getSelectedThemeMode(defaultMode: AppThemeMode = AppThemeMode.Dark): AppThemeMode =
        sharedPreferences.getString(THEME_MODE_KEY, defaultMode.key)
            ?.let { AppThemeMode.find(key = it, default = defaultMode) }
            ?: defaultMode

    fun saveSelectedThemeMode(mode: AppThemeMode) {
        sharedPreferences.edit(commit = true) {
            putString(THEME_MODE_KEY, mode.key)
        }
    }
}
