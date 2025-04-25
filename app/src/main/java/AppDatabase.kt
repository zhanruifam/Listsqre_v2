package com.example.listsqre_revamped

import androidx.room.Database
import androidx.room.RoomDatabase

// AppDatabase.kt
@Database(
    entities = [Card::class, CardItem::class, NotificationEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun cardItemDao(): CardItemDao
    abstract fun notificationDao(): NotificationDao

    companion object /* no companion object */
}