package com.example.listsqre_revamped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CardItemViewModel(
    private val cardItemDao: CardItemDao
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoadingForItems: StateFlow<Boolean> = _isLoading

    fun getItemsForCard(cardId: Long): Flow<List<CardItem>> {
        return cardItemDao.getItemsForCard(cardId)
    }

    fun insertCardItem(item: CardItem) = launchWithLoading {
        cardItemDao.insert(item)
    }

    fun updateCardItem(item: CardItem) = launchWithLoading {
        cardItemDao.update(item)
    }

    fun deleteCardItem(item: CardItem) = launchWithLoading {
        cardItemDao.delete(item)
    }

    fun deleteItemsByIds(cardId: Long, ids: List<Long>) = launchWithLoading {
        cardItemDao.deleteItemsByIds(cardId, ids)
    }

    fun setPinnedForItems(cardId: Long, ids: List<Long>, pin: Boolean) = launchWithLoading {
        cardItemDao.setPinnedForItems(cardId, ids, pin)
    }

    /* Clear separation: all DAO actions go through launchWithLoading */
    private fun launchWithLoading(block: suspend () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            block()
            _isLoading.value = false
        }
    }
}

