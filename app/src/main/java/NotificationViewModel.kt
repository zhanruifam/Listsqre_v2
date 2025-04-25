package com.example.listsqre_revamped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val notificationDao: NotificationDao
) : ViewModel() {
    val notifications = notificationDao.getAllNotifications()

    fun insert(notification: NotificationEntity) = viewModelScope.launch {
        notificationDao.insert(notification)
    }

    fun cancelNotification(notification: NotificationEntity) = viewModelScope.launch {
        notificationDao.delete(notification)
    }

    fun cancelNotificationById(id: Int) = viewModelScope.launch {
        notificationDao.deleteByUniqueId(id)
    }
}
