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
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class NotificationReceiver : BroadcastReceiver() {
    companion object {
        private const val CHANNEL_ID = "time_picker_notification"
        private const val CHANNEL_NAME = "Time Picker Alerts"
        private const val REQUEST_CODE = 1000
        const val PERMISSION = "android.permission.POST_NOTIFICATIONS"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent?) {
        if (ContextCompat.checkSelfPermission(context, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val rawDescription = intent?.getStringExtra("DESCRIPTION") ?: "Check for pending items"
        val notificationId = intent?.getIntExtra("NID", System.currentTimeMillis().toInt()) ?: 1001

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel(notificationManager)

        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.listsqrev2)
            .setContentTitle("Scheduled reminder:")
            .setContentText(rawDescription)
            .setStyle(NotificationCompat.BigTextStyle().bigText(rawDescription)) // for expanded view
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)

        /* Reschedule notification, reusing the same notification Id */
        rescheduleNotification(context, notificationId, rawDescription)
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
fun scheduleNotification(context: Context, hour: Int, minute: Int, desc: String = ""): Int {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()

    val alarmIntent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("DESCRIPTION", desc)
        putExtra("NID", notificationId)
    }

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        notificationId, // unique request code per alarm
        alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (before(Calendar.getInstance())) {
            add(Calendar.DAY_OF_MONTH, 1)
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
        // Handle or log
    }

    return notificationId
}

@RequiresApi(Build.VERSION_CODES.S)
fun rescheduleNotification(context: Context, uniqueId: Int, desc: String = "") {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    CoroutineScope(Dispatchers.IO).launch {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "card-database"
        ).build()
        val notification = db.notificationDao().getNotificationById(uniqueId)
        notification?.let {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = it.notificationTime
                add(Calendar.DAY_OF_MONTH, 1)
            }

            val updatedNotification = it.copy(notificationTime = calendar.timeInMillis)
            db.notificationDao().insert(updatedNotification)

            val alarmIntent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("DESCRIPTION", desc)
                putExtra("NID", uniqueId)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                uniqueId, // unique request code per alarm
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

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
                // Handle or log
            }
        }
    }
}

fun cancelNotification(context: Context, notificationId: Int, desc: String = "") {
    val alarmIntent = Intent(context, NotificationReceiver::class.java).apply {
        putExtra("DESCRIPTION", desc)
        putExtra("NID", notificationId)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        notificationId,
        alarmIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancel(pendingIntent)
}
