package kz.zeme.timer.core.theme.domain

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow
import kz.zeme.timer.core.domain.Response

interface ThemeRepository {

    val isDarkTheme: StateFlow<Boolean>

    suspend fun getThemes(): Response<List<ThemeOption>>

    suspend fun saveSelectedTheme(mode: AppThemeMode): Response<Unit>

    fun getSelectedThemeMode(): AppThemeMode

    fun applyCurrentTheme(activity: Activity? = null)
}
