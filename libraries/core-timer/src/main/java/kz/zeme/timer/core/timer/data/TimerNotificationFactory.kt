package kz.zeme.timer.core.timer.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import kz.zeme.timer.core.res.R
import kz.zeme.timer.core.timer.domain.TimerStatus

private const val RUNNING_CHANNEL_ID = "timer_running_channel"
private const val FINISHED_CHANNEL_ID = "timer_finished_channel"
private const val CONTENT_REQUEST_CODE = 100
private const val PAUSE_REQUEST_CODE = 101
private const val RESUME_REQUEST_CODE = 102
private const val RESET_REQUEST_CODE = 103
private const val REPEAT_REQUEST_CODE = 104
private const val DISMISS_REQUEST_CODE = 105

class TimerNotificationFactory(
    private val context: Context,
) {

    fun ensureChannels() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channelName = context.getString(R.string.timer_notification_channel)

        val runningChannel = NotificationChannel(
            RUNNING_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            setShowBadge(false)
        }

        val finishedChannel = NotificationChannel(
            FINISHED_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_HIGH,
        )

        manager.createNotificationChannel(runningChannel)
        manager.createNotificationChannel(finishedChannel)
    }

    fun build(
        status: TimerStatus,
        formattedRemaining: String,
    ): Notification = when (status) {
        TimerStatus.Running -> buildRunningNotification(formattedRemaining)
        TimerStatus.Paused -> buildPausedNotification(formattedRemaining)
        TimerStatus.Finished -> buildFinishedNotification()
        TimerStatus.Idle -> buildBaseBuilder(RUNNING_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.timer_notification_channel))
            .build()
    }

    private fun buildRunningNotification(formattedRemaining: String): Notification =
        buildBaseBuilder(RUNNING_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.timer_notification_channel))
            .setContentText(context.getString(R.string.timer_notification_running, formattedRemaining))
            .setOnlyAlertOnce(true)
            .addAction(
                0,
                context.getString(R.string.timer_pause),
                createActionPendingIntent(TimerForegroundService.ACTION_PAUSE, PAUSE_REQUEST_CODE),
            )
            .addAction(
                0,
                context.getString(R.string.timer_reset),
                createActionPendingIntent(TimerForegroundService.ACTION_RESET, RESET_REQUEST_CODE),
            )
            .build()

    private fun buildPausedNotification(formattedRemaining: String): Notification =
        buildBaseBuilder(RUNNING_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.timer_paused))
            .setContentText(context.getString(R.string.timer_notification_running, formattedRemaining))
            .setOnlyAlertOnce(true)
            .addAction(
                0,
                context.getString(R.string.timer_resume),
                createActionPendingIntent(TimerForegroundService.ACTION_RESUME, RESUME_REQUEST_CODE),
            )
            .addAction(
                0,
                context.getString(R.string.timer_reset),
                createActionPendingIntent(TimerForegroundService.ACTION_RESET, RESET_REQUEST_CODE),
            )
            .build()

    private fun buildFinishedNotification(): Notification =
        buildBaseBuilder(FINISHED_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.timer_notification_channel))
            .setContentText(context.getString(R.string.timer_notification_finished))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .addAction(
                0,
                context.getString(R.string.timer_repeat),
                createActionPendingIntent(TimerForegroundService.ACTION_REPEAT, REPEAT_REQUEST_CODE),
            )
            .addAction(
                0,
                context.getString(R.string.timer_done),
                createActionPendingIntent(TimerForegroundService.ACTION_DISMISS, DISMISS_REQUEST_CODE),
            )
            .build()

    private fun buildBaseBuilder(channelId: String): NotificationCompat.Builder {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_timer)
            .setOngoing(true)
            .setSilent(channelId == RUNNING_CHANNEL_ID)

        createContentPendingIntent()?.let(builder::setContentIntent)
        return builder
    }

    private fun createContentPendingIntent(): PendingIntent? {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?: return null

        return PendingIntent.getActivity(
            context,
            CONTENT_REQUEST_CODE,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun createActionPendingIntent(
        action: String,
        requestCode: Int,
    ): PendingIntent {
        val intent = Intent(context, TimerForegroundService::class.java).apply {
            this.action = action
        }

        return PendingIntent.getService(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
