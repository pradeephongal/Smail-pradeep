package com.example.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AlertSoundHelper {

  fun playSpamWarningAlert(context: Context) {
    CoroutineScope(Dispatchers.Default).launch {
      // 1. Play audible sharp warning tone
      try {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 300)
        delay(350)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
      } catch (_: Exception) {
        // Fallback to system notification ringtone if tone generator is unavailable
        try {
          val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
          val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
          ringtone.play()
        } catch (_: Exception) {}
      }

      // 2. Vibrate device with alert pulse
      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
          val vibrator = vibratorManager?.defaultVibrator
          vibrator?.vibrate(
            VibrationEffect.createWaveform(
              longArrayOf(0, 150, 100, 250, 100, 300),
              -1
            )
          )
        } else {
          @Suppress("DEPRECATION")
          val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
              VibrationEffect.createWaveform(
                longArrayOf(0, 150, 100, 250, 100, 300),
                -1
              )
            )
          } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 150, 100, 250, 100, 300), -1)
          }
        }
      } catch (_: Exception) {}
    }
  }
}
