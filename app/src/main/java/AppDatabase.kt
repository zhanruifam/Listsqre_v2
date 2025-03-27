package com.example.listsqre_revamped

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// AppDatabase.kt
@Database(entities = [Card::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "card_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}