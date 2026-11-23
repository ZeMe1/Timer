package kz.zeme.timer.core.timer.data

import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kz.zeme.timer.core.timer.domain.TimerRepository
import kz.zeme.timer.core.timer.domain.TimerState
import kz.zeme.timer.core.timer.domain.TimerStatus
import kz.zeme.timer.core.timer.domain.TimerTimeFormatter
import org.koin.android.ext.android.inject

private const val NOTIFICATION_ID = 1001
private const val NOTIFICATION_TICK_INTERVAL_MS = 1_000L

class TimerForegroundService : Service() {

    companion object {
        const val ACTION_PAUSE = "kz.zeme.timer.core.timer.ACTION_PAUSE"
        const val ACTION_RESUME = "kz.zeme.timer.core.timer.ACTION_RESUME"
        const val ACTION_RESET = "kz.zeme.timer.core.timer.ACTION_RESET"
        const val ACTION_REPEAT = "kz.zeme.timer.core.timer.ACTION_REPEAT"
        const val ACTION_DISMISS = "kz.zeme.timer.core.timer.ACTION_DISMISS"
    }

    private val timerRepository: TimerRepository by inject()
    private val timeFormatter: TimerTimeFormatter by inject()
    private val notificationFactory: TimerNotificationFactory by inject()

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var observeJob: Job? = null
    private var tickJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        notificationFactory.ensureChannels()
        startForegroundNotification()
        observeState()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        when (intent?.action) {
            ACTION_PAUSE -> timerRepository.pause()
            ACTION_RESUME -> timerRepository.resume()
            ACTION_RESET -> timerRepository.reset()
            ACTION_REPEAT -> timerRepository.repeat()
            ACTION_DISMISS -> timerRepository.dismissFinished()
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startForegroundNotification() {
        val state = timerRepository.state.value
        val notification = buildNotification(state.status)
        val serviceType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            else 0

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            serviceType,
        )
    }

    private fun observeState() {
        observeJob?.cancel()
        observeJob = serviceScope.launch {
            timerRepository.state.collect(::onStateChanged)
        }
    }

    private fun onStateChanged(state: TimerState) {
        if (state.status == TimerStatus.Idle) {
            stopTicking()
            stopSelf()
            return
        }

        notifyCurrentState(state.status)

        if (state.status == TimerStatus.Running) startTicking()
        else stopTicking()
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = serviceScope.launch {
            while (isActive) {
                delay(NOTIFICATION_TICK_INTERVAL_MS)
                notifyCurrentState(TimerStatus.Running)
            }
        }
    }

    private fun stopTicking() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun notifyCurrentState(status: TimerStatus) {
        val manager = getSystemService(NotificationManager::class.java) ?: return
        manager.notify(NOTIFICATION_ID, buildNotification(status))
    }

    private fun buildNotification(status: TimerStatus) = notificationFactory.build(
        status = status,
        formattedRemaining = timeFormatter.formatRemaining(timerRepository.getRemaining().remainingMs),
    )
}
