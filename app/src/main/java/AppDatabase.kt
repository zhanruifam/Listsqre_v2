package com.example.listsqre_revamped

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Card::class], // Base card entity
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "card_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Base tables are created automatically
            }
        }
    }
}

/* optimize this part of the code */

// Minimal required entity (won't be used for dynamic tables)
@Entity(tableName = "dummy_table")
data class DummyEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0
)

// Minimal required DAO
@Dao
interface DummyDao {
    @Query("SELECT * FROM dummy_table LIMIT 1")
    suspend fun getOne(): DummyEntity?
}

@Database(
    entities = [DummyEntity::class], // Required by Room
    version = 1,
    exportSchema = false
)
abstract class AppDatabaseCardField : RoomDatabase() {
    abstract fun dummyDao(): DummyDao // Required by Room

    // Provides direct access to SQLite for dynamic tables
    fun getWritableSupportDb(): SupportSQLiteDatabase {
        return openHelper.writableDatabase
    }

    companion object {
        @Volatile
        private var instance: AppDatabaseCardField? = null

        fun getDatabase(context: Context): AppDatabaseCardField {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabaseCardField::class.java,
                    "cardfield_dynamic.db"  // Database file name
                )
                    .fallbackToDestructiveMigration() // Wipes database on version change
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            // Optional: Initialize with default tables if needed
                        }
                    })
                    .build()
                    .also { instance = it }
            }
        }
    }
}