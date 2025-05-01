package com.example.listsqre_revamped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CardItemViewModel(
    private val cardItemDao: CardItemDao
) : ViewModel() {
    private val _cardId = MutableStateFlow<Long?>(null)
    private val _isLoading = MutableStateFlow(true)
    val isLoadingForItems: StateFlow<Boolean> = _isLoading

    @OptIn(ExperimentalCoroutinesApi::class)
    val itemsForCard: StateFlow<List<CardItem>> = _cardId
        .filterNotNull()
        .distinctUntilChanged()
        .flatMapLatest { cardId ->
            cardItemDao.getItemsForCard(cardId)
                .onStart { _isLoading.value = true }
                .onEach { _isLoading.value = false }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setCardId(id: Long) {
        _cardId.value = id
    }

    fun insertCardItem(item: CardItem) = viewModelScope.launch {
        cardItemDao.insert(item)
    }

    fun updateCardItem(item: CardItem) = viewModelScope.launch {
        cardItemDao.update(item)
    }

    fun deleteCardItem(item: CardItem) = viewModelScope.launch {
        cardItemDao.delete(item)
    }

    fun deleteItemsByIds(cardId: Long, ids: List<Long>) = viewModelScope.launch {
        cardItemDao.deleteItemsByIds(cardId, ids)
    }

    fun setPinnedForItems(cardId: Long, ids: List<Long>, pin: Boolean) = viewModelScope.launch {
        cardItemDao.setPinnedForItems(cardId, ids, pin)
    }
}

