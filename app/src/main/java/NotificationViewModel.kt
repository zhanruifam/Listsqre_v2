package com.example.listsqre_revamped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotiState(
    val cards: List<NotificationEntity> = emptyList()
)

class NotificationViewModel(
    private val notificationDao: NotificationDao
) : ViewModel() {
    private val _isLoading = MutableStateFlow(true)
    val isLoadingForNoti: StateFlow<Boolean> = _isLoading

    val notifications: StateFlow<NotiState> = notificationDao.getAllNotifications()
        .onStart { _isLoading.value = true }
        .map { cards -> NotiState(cards) }
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NotiState()
        )

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
