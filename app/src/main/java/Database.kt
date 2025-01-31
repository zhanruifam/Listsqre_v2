package com.example.listsqre_revamped

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase

@Entity(tableName = "cards")
data class CardItem(
    @PrimaryKey @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "isChecked") val isChecked: Boolean
)

@Dao
interface CardDao {
    @Query("SELECT * FROM cards")
    fun getAllCards(): List<CardItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCards(cards: List<CardItem>)

    @Query("DELETE FROM cards")
    fun deleteAllCards()
}

@Database(entities = [CardItem::class], version = 1)
abstract class CardDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao

    companion object {
        @Volatile
        private var INSTANCE: CardDatabase? = null

        fun getDatabase(context: Context): CardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CardDatabase::class.java,
                    "card_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
