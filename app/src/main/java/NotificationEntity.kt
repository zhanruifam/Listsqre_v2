package com.example.listsqre_revamped

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uniqueId: Int,
    val description: String,
    val notificationTime: Long // Store as epoch time in millis
)

val NotificationEntity.isToday: Boolean
    get() = notificationTime > System.currentTimeMillis()
