package com.example.listsqre_revamped

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardItemDao {
    @Query("SELECT * FROM card_items WHERE cardId = :cardId ORDER BY isPinned DESC, id ASC")
    fun getItemsForCard(cardId: Long): Flow<List<CardItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CardItem): Long

    @Update
    suspend fun update(item: CardItem)

    @Delete
    suspend fun delete(item: CardItem)

    @Query("DELETE FROM card_items WHERE id IN (:ids) AND cardId = :cardId")
    suspend fun deleteItemsByIds(cardId: Long, ids: List<Long>)

    @Query("UPDATE card_items SET isPinned = :pin WHERE id IN (:ids) AND cardId = :cardId")
    suspend fun setPinnedForItems(cardId: Long, ids: List<Long>, pin: Boolean)
}