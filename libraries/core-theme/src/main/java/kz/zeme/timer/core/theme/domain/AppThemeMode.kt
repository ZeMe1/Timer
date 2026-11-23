package kz.zeme.timer.core.theme.domain

enum class AppThemeMode(
    val key: String,
) {
    Dark("dark"),
    Light("light"),
    System("system");

    companion object {

        fun find(
            key: String?,
            default: AppThemeMode = Dark,
        ): AppThemeMode = entries
            .find { it.key.contentEquals(key) }
            ?: default
    }
}
