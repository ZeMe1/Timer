package kz.zeme.timer.core.theme.data

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kz.zeme.timer.core.activity.ActivityProvider
import kz.zeme.timer.core.activity.extensions.enableEdgeToEdge
import kz.zeme.timer.core.domain.Response
import kz.zeme.timer.core.theme.domain.AppThemeMode
import kz.zeme.timer.core.theme.domain.ThemeApplier
import kz.zeme.timer.core.theme.domain.ThemeOption
import kz.zeme.timer.core.theme.domain.ThemeRepository

class DefaultThemeRepository(
    private val context: Context,
    private val activityProvider: ActivityProvider,
    private val themeLocalDataSource: ThemeLocalDataSource,
    private val themeApplier: ThemeApplier,
) : ThemeRepository {

    private val _isDarkTheme = MutableStateFlow(resolveIsDark(getSelectedThemeMode()))

    override val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    override suspend fun getThemes(): Response<List<ThemeOption>> {
        val selectedMode = themeLocalDataSource.getSelectedThemeMode()

        return Response.Success(
            listOf(
                ThemeOption(
                    mode = AppThemeMode.Dark,
                    isSelected = selectedMode == AppThemeMode.Dark,
                ),
                ThemeOption(
                    mode = AppThemeMode.Light,
                    isSelected = selectedMode == AppThemeMode.Light,
                ),
                ThemeOption(
                    mode = AppThemeMode.System,
                    isSelected = selectedMode == AppThemeMode.System,
                ),
            ),
        )
    }

    override suspend fun saveSelectedTheme(mode: AppThemeMode): Response<Unit> {
        themeLocalDataSource.saveSelectedThemeMode(mode)
        applyMode(mode = mode, activity = activityProvider.currentActivity)

        return Response.Success(Unit)
    }

    override fun getSelectedThemeMode(): AppThemeMode =
        themeLocalDataSource.getSelectedThemeMode()

    override fun applyCurrentTheme(activity: Activity?) {
        val mode = themeLocalDataSource.getSelectedThemeMode()
        applyMode(
            mode = mode,
            activity = activity ?: activityProvider.currentActivity,
        )
    }

    private fun applyMode(
        mode: AppThemeMode,
        activity: Activity?,
    ) {
        val isDark = resolveIsDark(mode)
        _isDarkTheme.value = isDark
        themeApplier.applyTheme(isDark)
        activity?.enableEdgeToEdge(isDark)
    }

    private fun resolveIsDark(mode: AppThemeMode): Boolean = when (mode) {
        AppThemeMode.Dark -> true
        AppThemeMode.Light -> false
        AppThemeMode.System -> isSystemDarkTheme()
    }

    private fun isSystemDarkTheme(): Boolean {
        val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }
}
