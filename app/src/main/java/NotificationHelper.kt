package com.example.listsqre_revamped

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "time_picker_notification"
        private const val CHANNEL_NAME = "Time Picker Alerts"
        private const val REQUEST_CODE = 1000
        private const val NOTIFICATION_ID = 1001
        const val PERMISSION = "android.permission.POST_NOTIFICATIONS"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // Check notification permission
        if (ContextCompat.checkSelfPermission(context, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            return // Permission not granted
        }
        val description = intent?.getStringExtra("DESCRIPTION") ?: "Check for pending items"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        // Prepare intent to launch activity when notification is clicked
        // val openAppIntent = Intent(context, SpotlightActivity::class.java).apply {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build and show the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.listsqrev2)
            .setContentTitle("Scheduled Event")
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun scheduleNotification(context: Context, hour: Int, minute: Int, desc: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmIntent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("DESCRIPTION", desc)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        1001, // Unique request code
        alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    // Get the selected time in milliseconds
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_MONTH, 1) // If time has passed, schedule for next day
        }
    }

    try {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    } catch (_: SecurityException) {
        // do nothing
    }
}