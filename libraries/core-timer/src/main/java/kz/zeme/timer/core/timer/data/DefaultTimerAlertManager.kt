package kz.zeme.timer.core.timer.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kz.zeme.timer.core.activity.extensions.HapticLevel
import kz.zeme.timer.core.activity.extensions.vibrate
import kz.zeme.timer.core.timer.domain.TimerAlertManager

private const val MAX_ALERT_DURATION_MS = 60_000L
private const val ALERT_PULSE_INTERVAL_MS = 1_200L

class DefaultTimerAlertManager(
    context: Context,
    dispatcher: CoroutineDispatcher = Dispatchers.IO.limitedParallelism(1),
) : TimerAlertManager {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + dispatcher)
    private val alertState = AtomicLong(0L)

    private var activeRingtone: Ringtone? = null
    private var activeFocusRequest: AudioFocusRequest? = null
    private var activeJob: Job? = null

    override fun startAlert() {
        val token = tryAcquireAlertToken() ?: return
        scope.launch { runAlert(token) }
    }

    override fun stopAlert() {
        if (!tryMarkStopped()) return
        scope.launch { releaseActiveResources() }
    }

    private fun tryAcquireAlertToken(): Long? {
        while (true) {
            val current = alertState.get()
            if (current > 0L) return null

            val nextToken = (-current) + 1L
            if (alertState.compareAndSet(current, nextToken)) return nextToken
        }
    }

    private fun tryMarkStopped(): Boolean {
        while (true) {
            val current = alertState.get()
            if (current <= 0L) return false

            if (alertState.compareAndSet(current, -current)) return true
        }
    }

    private suspend fun runAlert(token: Long) {
        if (!isTokenActive(token)) return
        if (!startPlayback(token)) return

        try {
            pulseUntilTimeout(token)
        } finally {
            if (alertState.compareAndSet(token, -token)) releaseActiveResources()
        }
    }

    private suspend fun startPlayback(token: Long): Boolean {
        activeJob = currentCoroutineContext()[Job]

        val audioAttributes = buildAlarmAudioAttributes()
        val focusRequest = requestAudioFocus(audioAttributes)
        val ringtone = createRingtone(audioAttributes)

        if (!isTokenActive(token)) {
            abandonAudioFocus(focusRequest)
            activeJob = null
            return false
        }

        activeFocusRequest = focusRequest
        activeRingtone = ringtone
        playRingtoneSafely(ringtone)
        return true
    }

    private suspend fun pulseUntilTimeout(token: Long) {
        val startedAtMs = SystemClock.elapsedRealtime()

        while (isTokenActive(token)) {
            vibrateOnceSafely()
            replayRingtoneIfNeeded(token)

            if (isAlertExpired(startedAtMs)) break

            delay(ALERT_PULSE_INTERVAL_MS)
        }
    }

    private fun replayRingtoneIfNeeded(token: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) return

        val ringtone = activeRingtone ?: return
        if (!isRingtonePlayingSafely(ringtone) && isTokenActive(token)) playRingtoneSafely(ringtone)
    }

    private fun releaseActiveResources() {
        val job = activeJob
        val ringtone = activeRingtone
        val focusRequest = activeFocusRequest

        activeJob = null
        activeRingtone = null
        activeFocusRequest = null

        job?.cancel()
        stopRingtoneSafely(ringtone)
        abandonAudioFocus(focusRequest)
        cancelVibrationSafely()
    }

    private fun isTokenActive(token: Long): Boolean = alertState.get() == token

    private fun isAlertExpired(startedAtMs: Long): Boolean =
        SystemClock.elapsedRealtime() - startedAtMs >= MAX_ALERT_DURATION_MS

    private fun buildAlarmAudioAttributes(): AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    private fun resolveAlarmUri(): Uri? =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

    private fun createRingtone(audioAttributes: AudioAttributes): Ringtone? = try {
        val uri = resolveAlarmUri()
        val ringtone = uri?.let { RingtoneManager.getRingtone(appContext, it) }

        ringtone?.audioAttributes = audioAttributes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ringtone?.isLooping = true

        ringtone
    } catch (_: Exception) {
        null
    }

    private fun playRingtoneSafely(ringtone: Ringtone?) {
        if (ringtone == null) return

        try {
            ringtone.play()
        } catch (_: Exception) {
        }
    }

    private fun stopRingtoneSafely(ringtone: Ringtone?) {
        if (ringtone == null) return

        try {
            ringtone.stop()
        } catch (_: Exception) {
        }
    }

    private fun isRingtonePlayingSafely(ringtone: Ringtone): Boolean = try {
        ringtone.isPlaying
    } catch (_: Exception) {
        false
    }

    private fun requestAudioFocus(audioAttributes: AudioAttributes): AudioFocusRequest? = try {
        val audioManager = ContextCompat.getSystemService(appContext, AudioManager::class.java)
            ?: return null
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener {}
            .build()

        audioManager.requestAudioFocus(request)
        request
    } catch (_: Exception) {
        null
    }

    private fun abandonAudioFocus(focusRequest: AudioFocusRequest?) {
        if (focusRequest == null) return

        try {
            val audioManager = ContextCompat.getSystemService(appContext, AudioManager::class.java)
            audioManager?.abandonAudioFocusRequest(focusRequest)
        } catch (_: Exception) {
        }
    }

    private fun vibrateOnceSafely() {
        try {
            appContext.vibrate(HapticLevel.Warning)
        } catch (_: Exception) {
        }
    }

    private fun cancelVibrationSafely() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = ContextCompat.getSystemService(appContext, VibratorManager::class.java)
                manager?.defaultVibrator?.cancel()
            } else {
                @Suppress("DEPRECATION")
                val vibrator = ContextCompat.getSystemService(appContext, Vibrator::class.java)
                vibrator?.cancel()
            }
        } catch (_: Exception) {
        }
    }
}
