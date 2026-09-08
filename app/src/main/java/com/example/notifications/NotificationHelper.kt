package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.data.EmailEntity
import com.example.util.AlertSoundHelper

class NotificationHelper(private val context: Context) {

  companion object {
    const val SPAM_ALERT_CHANNEL_ID = "smail_spam_warning_channel"
    const val NORMAL_EMAIL_CHANNEL_ID = "smail_normal_email_channel"
  }

  init {
    createNotificationChannels()
  }

  private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

      // High-priority red warning channel for spam
      val spamChannel = NotificationChannel(
        SPAM_ALERT_CHANNEL_ID,
        "🚨 Smail Spam Threats (Warning Sound & Red Alert)",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "High-priority warning notifications when Naïve Bayes AI detects spam or phishing"
        enableLights(true)
        lightColor = Color.RED
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 200, 100, 300, 100, 300)
        val audioAttributes = AudioAttributes.Builder()
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .setUsage(AudioAttributes.USAGE_ALARM)
          .build()
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        setSound(alarmUri, audioAttributes)
      }

      // Normal polite channel for clean ham email
      val normalChannel = NotificationChannel(
        NORMAL_EMAIL_CHANNEL_ID,
        "✉️ Smail Normal Inbox Messages",
        NotificationManager.IMPORTANCE_DEFAULT
      ).apply {
        description = "Standard notifications for clean inbox email messages"
        enableLights(true)
        lightColor = Color.BLUE
      }

      notificationManager?.createNotificationChannel(spamChannel)
      notificationManager?.createNotificationChannel(normalChannel)
    }
  }

  fun showEmailNotification(email: EmailEntity) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("TARGET_FOLDER", email.folder)
      putExtra("EMAIL_ID", email.id)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      email.id.toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (email.isSpam) {
      // Trigger warning sound and haptic alert immediately
      AlertSoundHelper.playSpamWarningAlert(context)

      val riskPercent = (email.spamProbability * 100).toInt()
      val notification = NotificationCompat.Builder(context, SPAM_ALERT_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.stat_notify_error)
        .setColor(Color.RED)
        .setColorized(true)
        .setContentTitle("🚨 [SPAM ALERT] ${email.subject}")
        .setContentText("Naïve Bayes AI Warning: $riskPercent% Spam Risk from ${email.senderName}")
        .setStyle(
          NotificationCompat.BigTextStyle().bigText(
            "⚠️ WARNING: High-risk spam detected by Smail Naïve Bayes Classifier ($riskPercent% confidence).\n" +
                "Quarantined to Spam folder to protect your privacy.\n" +
                "Trigger Tokens: ${email.topTriggerWords.ifEmpty { "High-risk spam patterns" }}\n\n" +
                "Preview: ${email.body.take(120)}..."
          )
        )
        .setPriority(NotificationCompat.PRIORITY_MAX)
        .setCategory(NotificationCompat.CATEGORY_ALARM)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      try {
        NotificationManagerCompat.from(context).notify(email.id.toInt(), notification)
      } catch (_: SecurityException) {}
    } else {
      val notification = NotificationCompat.Builder(context, NORMAL_EMAIL_CHANNEL_ID)
        .setSmallIcon(android.R.drawable.sym_action_email)
        .setContentTitle("✉️ ${email.senderName}: ${email.subject}")
        .setContentText(email.body.take(80))
        .setStyle(
          NotificationCompat.BigTextStyle().bigText(
            "${email.subject}\n\n${email.body.take(200)}..."
          )
        )
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(pendingIntent)
        .build()

      try {
        NotificationManagerCompat.from(context).notify(email.id.toInt(), notification)
      } catch (_: SecurityException) {}
    }
  }

  fun showDispatchNotification(title: String, message: String, targetFolder: String = "SENT") {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      if (ContextCompat.checkSelfPermission(
          context,
          Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
      ) {
        return
      }
    }

    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
      putExtra("TARGET_FOLDER", targetFolder)
    }

    val pendingIntent = PendingIntent.getActivity(
      context,
      System.currentTimeMillis().toInt(),
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val notification = NotificationCompat.Builder(context, NORMAL_EMAIL_CHANNEL_ID)
      .setSmallIcon(android.R.drawable.sym_action_email)
      .setContentTitle(title)
      .setContentText(message)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    try {
      NotificationManagerCompat.from(context).notify((System.currentTimeMillis() % 10000).toInt(), notification)
    } catch (_: SecurityException) {}
  }
}
