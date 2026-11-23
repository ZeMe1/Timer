package kz.zeme.timer.core.timer.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kz.zeme.timer.core.activity.extensions.HapticLevel
import kz.zeme.timer.core.activity.extensions.vibrate
import kz.zeme.timer.core.timer.domain.TimerAlertManager

private const val MAX_ALERT_PULSES = 50
private const val ALERT_PULSE_INTERVAL_MS = 1_200L

class DefaultTimerAlertManager(
    context: Context,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : TimerAlertManager {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)

    private var alertJob: Job? = null
    private var ringtone: Ringtone? = null
    private var focusRequest: AudioFocusRequest? = null

    override fun startAlert() {
        if (alertJob?.isActive == true) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        focusRequest = requestAudioFocus(audioAttributes)
        ringtone = startRingtone(audioAttributes)

        alertJob = scope.launch {
            repeat(MAX_ALERT_PULSES) {
                appContext.vibrate(HapticLevel.Warning)
                delay(ALERT_PULSE_INTERVAL_MS)
            }
            stopAlert()
        }
    }

    override fun stopAlert() {
        alertJob?.cancel()
        alertJob = null

        ringtone?.stop()
        ringtone = null

        focusRequest?.let { request ->
            ContextCompat.getSystemService(appContext, AudioManager::class.java)
                ?.abandonAudioFocusRequest(request)
        }
        focusRequest = null

        cancelVibration()
    }

    private fun startRingtone(audioAttributes: AudioAttributes): Ringtone? {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: return null

        return RingtoneManager.getRingtone(appContext, uri)?.apply {
            this.audioAttributes = audioAttributes
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) isLooping = true
            play()
        }
    }

    private fun requestAudioFocus(audioAttributes: AudioAttributes): AudioFocusRequest? {
        val audioManager = ContextCompat.getSystemService(appContext, AudioManager::class.java)
            ?: return null
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener {}
            .build()

        audioManager.requestAudioFocus(request)
        return request
    }

    private fun cancelVibration() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.getSystemService(appContext, VibratorManager::class.java)
                ?.defaultVibrator
                ?.cancel()
        } else {
            @Suppress("DEPRECATION")
            ContextCompat.getSystemService(appContext, Vibrator::class.java)?.cancel()
        }
    }
}
