package kz.zeme.timer.core.activity.extensions

import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import kotlin.system.exitProcess

private const val DARK_WINDOW_BG = 0xFF010101.toInt()
private const val LIGHT_WINDOW_BG = 0xFFF6F6F8.toInt()

fun Activity.isPermissionGranted(
    vararg permission: String,
): Boolean {
    if (permission.isEmpty()) return false

    permission.forEach {
        val result = ContextCompat.checkSelfPermission(this, it)
        if (result == PackageManager.PERMISSION_GRANTED) Unit
        else return false
    }

    return true
}

fun Activity.closeApp() {
    finishAffinity()
    exitProcess(0)
}

fun Activity.enableEdgeToEdge(isDark: Boolean = true) {
    val systemBarStyle = when {
        !isTimer() -> SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
        isDark -> SystemBarStyle.dark(Color.TRANSPARENT)
        else -> SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    }

    (this as? AppCompatActivity)?.enableEdgeToEdge(
        statusBarStyle = systemBarStyle,
        navigationBarStyle = systemBarStyle,
    )

    val windowBackgroundColor = if (isDark) DARK_WINDOW_BG else LIGHT_WINDOW_BG
    window?.setBackgroundDrawable(windowBackgroundColor.toDrawable())
}

fun Activity.isTimer(): Boolean =
    componentName.className.startsWith("kz.zeme.timer")
