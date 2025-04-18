package com.example.listsqre_revamped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CardItemViewModel(
    private val cardItemDao: CardItemDao
) : ViewModel() {
    // 1. Get items for a specific card
    fun getItemsForCard(cardId: Long): Flow<List<CardItem>> {
        return cardItemDao.getItemsForCard(cardId)
    }

    // 2. Insert a card item
    fun insertCardItem(item: CardItem) = viewModelScope.launch {
        cardItemDao.insert(item)
    }

    // 3. Update a card item
    fun updateCardItem(item: CardItem) = viewModelScope.launch {
        cardItemDao.update(item)
    }

    // 4. Delete a card item
    fun deleteCardItem(item: CardItem) = viewModelScope.launch {
        cardItemDao.delete(item)
    }

    // 5. Delete items by ids
    fun deleteItemsByIds(cardId: Long, ids: List<Long>) = viewModelScope.launch {
        cardItemDao.deleteItemsByIds(cardId, ids)
    }

    // 6. Set pinned status for items by ids
    fun setPinnedForItems(cardId: Long, ids: List<Long>, pin: Boolean) = viewModelScope.launch {
        cardItemDao.setPinnedForItems(cardId, ids, pin)
    }
}
