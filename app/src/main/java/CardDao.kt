package com.example.listsqre_revamped

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// CardDao.kt
@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY isPinned DESC, createdAt DESC")
    fun getAllCards(): Flow<List<Card>>

    @Insert
    suspend fun insertCard(card: Card)

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("DELETE FROM cards WHERE isSelected = 1")
    suspend fun deleteSelectedCards()

    @Query("UPDATE cards SET isPinned = :isPinned WHERE id = :id")
    suspend fun updatePinStatus(id: Int, isPinned: Boolean)
}