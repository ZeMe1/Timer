package kz.zeme.timer.core.theme

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.test.runTest
import kz.zeme.timer.core.activity.ActivityProvider
import kz.zeme.timer.core.domain.Response
import kz.zeme.timer.core.theme.data.DefaultThemeRepository
import kz.zeme.timer.core.theme.data.ThemeLocalDataSource
import kz.zeme.timer.core.theme.domain.AppThemeMode
import kz.zeme.timer.core.theme.domain.ThemeApplier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultThemeRepositoryTest {

    @Test
    fun saveAndApplyThemePersistsSelectionAndUpdatesState() = runTest {
        var systemDark = false
        val sharedPreferences = FakeSharedPreferences()
        val applier = FakeThemeApplier()
        val repository = DefaultThemeRepository(
            isSystemDarkProvider = { systemDark },
            activityProvider = FakeActivityProvider(),
            themeLocalDataSource = ThemeLocalDataSource(sharedPreferences),
            themeApplier = applier,
        )

        assertEquals(AppThemeMode.Dark, repository.getSelectedThemeMode())
        assertTrue(repository.isDarkTheme.value)

        repository.saveSelectedTheme(AppThemeMode.Light)
        assertEquals(AppThemeMode.Light, repository.getSelectedThemeMode())
        assertFalse(repository.isDarkTheme.value)
        assertEquals(listOf(false), applier.appliedValues)

        val optionsResponse = repository.getThemes() as Response.Success
        val selected = optionsResponse.data.first { it.isSelected }
        assertEquals(AppThemeMode.Light, selected.mode)

        repository.saveSelectedTheme(AppThemeMode.System)
        assertFalse(repository.isDarkTheme.value)

        systemDark = true
        repository.applyCurrentTheme()
        assertTrue(repository.isDarkTheme.value)
        assertEquals(listOf(false, false, true), applier.appliedValues)
    }

    private class FakeActivityProvider : ActivityProvider {
        override var currentActivity: AppCompatActivity? = null
    }

    private class FakeThemeApplier : ThemeApplier {
        val appliedValues = mutableListOf<Boolean>()

        override fun applyTheme(isDark: Boolean) {
            appliedValues.add(isDark)
        }
    }

    private class FakeSharedPreferences : SharedPreferences {
        private val values = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = values
        override fun getString(key: String?, defValue: String?): String? = values[key] as? String ?: defValue
        override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? = defValues
        override fun getInt(key: String?, defValue: Int): Int = values[key] as? Int ?: defValue
        override fun getLong(key: String?, defValue: Long): Long = values[key] as? Long ?: defValue
        override fun getFloat(key: String?, defValue: Float): Float = values[key] as? Float ?: defValue
        override fun getBoolean(key: String?, defValue: Boolean): Boolean = values[key] as? Boolean ?: defValue
        override fun contains(key: String?): Boolean = values.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

        override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private val removed = mutableSetOf<String>()
            private var clearAll = false

            override fun putString(key: String?, value: String?) = apply { if (key != null) pending[key] = value }
            override fun putStringSet(key: String?, values: MutableSet<String>?) = this
            override fun putInt(key: String?, value: Int) = apply { if (key != null) pending[key] = value }
            override fun putLong(key: String?, value: Long) = apply { if (key != null) pending[key] = value }
            override fun putFloat(key: String?, value: Float) = apply { if (key != null) pending[key] = value }
            override fun putBoolean(key: String?, value: Boolean) = apply { if (key != null) pending[key] = value }
            override fun remove(key: String?) = apply { if (key != null) removed.add(key) }
            override fun clear() = apply { clearAll = true }

            override fun commit(): Boolean {
                apply()
                return true
            }

            override fun apply() {
                if (clearAll) values.clear()
                removed.forEach(values::remove)
                values.putAll(pending)
            }
        }
    }
}
