package kz.zeme.timer.core.timer.data

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import kz.zeme.timer.core.timer.domain.TimerServiceController

class DefaultTimerServiceController(
    private val context: Context,
) : TimerServiceController {

    override fun start() {
        val intent = Intent(context, TimerForegroundService::class.java)
        ContextCompat.startForegroundService(context, intent)
    }

    override fun stop() {
        context.stopService(Intent(context, TimerForegroundService::class.java))
    }
}
