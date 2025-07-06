package com.example.listsqre_revamped

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Called when the system broadcasts BOOT_COMPLETED.
 * This function is triggered after the device has completed booting.
 *
 * Sequence of operations:
 * 1. Reschedule all notifications on boot.
 *
 * This ensures that certain data persist across device restarts.
 */
class BootReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleNotiOnBoot(context)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun rescheduleNotiOnBoot(context: Context) {
    val application = context.applicationContext as MyApplication
    val dao = application.database.notificationDao()

    // Start coroutine for DB access
    CoroutineScope(Dispatchers.IO).launch {
        dao.getAllNotifications()
            .first() // Collect the current value once from Flow
            .forEach { notification ->
                rescheduleNotification(
                    context,
                    notification.uniqueId,
                    notification.description
                )
            }
    }
}