package com.example.listsqre_revamped

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards ORDER BY isPinned DESC, id DESC")
    fun getAllCards(): Flow<List<Card>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card)

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)

    @Query("DELETE FROM cards WHERE isSelected = 1")
    suspend fun deleteSelectedCards()

    @Query("UPDATE cards SET isPinned = 1 WHERE isSelected = 1")
    suspend fun pinSelectedCards()

    @Query("UPDATE cards SET isSelected = :isSelected WHERE id = :cardId")
    suspend fun updateCardSelection(cardId: Int, isSelected: Boolean)
}