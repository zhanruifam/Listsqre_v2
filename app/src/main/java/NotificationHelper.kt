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
    override fun onReceive(context: Context, intent: Intent?) {
        val permission = "android.permission.POST_NOTIFICATIONS"
        val permissionState = ContextCompat.checkSelfPermission(context, permission)

        val channelId = "time_picker_notification"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Time Picker Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
//        if(permissionState == PackageManager.PERMISSION_GRANTED) {
//            val openAppIntent = Intent(context, MainActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            }
//            val pendingIntent = PendingIntent.getActivity(
//                context,
//                1000, // Unique request code
//                openAppIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//            val notification = NotificationCompat.Builder(context, channelId)
//                .setSmallIcon(R.mipmap.listsqrev2)
//                .setContentTitle("Scheduled Event")
//                .setContentText("It's time to check for pending items.")
//                .setPriority(NotificationCompat.PRIORITY_HIGH)
//                .setContentIntent(pendingIntent)  // Open app on click
//                .build()
//
//            notificationManager.notify(1001, notification)
//        } else {
//            // ActivityCompat.requestPermissions()
//        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun scheduleNotification(context: Context, hour: Int, minute: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val alarmIntent = Intent(context, NotificationReceiver::class.java)
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