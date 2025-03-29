package com.example.listsqre_revamped

import android.app.Application
import androidx.room.Room

class MyApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "card-database"
        ).build()
    }
}