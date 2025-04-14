package com.example.listsqre_revamped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/* --- not used in Card.kt ---
data class Card(
    val id: Int,
    val title: String,
    val description: String,
    val isSelected: Boolean = false,
    val isPinned: Boolean = false
)
*/

data class CardState(
    val cards: List<Card> = emptyList()
)

class CardViewModel(private val cardDao: CardDao) : ViewModel() {
    // State is now derived from the database flow
    val state: StateFlow<CardState> = cardDao.getAllCards()
        .map { cards -> CardState(cards) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CardState()
        )

    init {
        // Initialize with some sample data if the database is empty
        viewModelScope.launch {
            if (cardDao.getAllCards().first().isEmpty()) {
                cardDao.insertCard(Card(title = "Welcome", description = "This is your first card", isPinned = true))
                cardDao.insertCard(Card(title = "Getting Started", description = "Try adding more cards"))
            }
        }
    }

    fun updateCard(id: Int, title: String, description: String, isPinned: Boolean) {
        viewModelScope.launch {
            val card = Card(id, title, description, isPinned = isPinned)
            cardDao.updateCard(card)
        }
    }

    fun addCard(title: String, description: String, isPinned: Boolean = false) {
        viewModelScope.launch {
            val card = Card(title = title, description = description, isPinned = isPinned)
            cardDao.insertCard(card)
        }
    }

    fun updateCardSelection(cardId: Int, isSelected: Boolean) {
        viewModelScope.launch {
            // First get the current card
            val currentCards = cardDao.getAllCards().first()
            val cardToUpdate = currentCards.firstOrNull { it.id == cardId }

            cardToUpdate?.let { card ->
                val updatedCard = card.copy(isSelected = isSelected)
                cardDao.updateCard(updatedCard)
            }
        }
    }

    fun pinSelectedCards() {
        viewModelScope.launch {
            cardDao.pinSelectedCards()

            // Also clear selection after pinning
            val selectedCards = cardDao.getAllCards().first().filter { it.isSelected }
            selectedCards.forEach { card ->
                cardDao.updateCard(card.copy(isSelected = false))
            }
        }
    }

    fun deleteSelectedCards() {
        viewModelScope.launch {
            cardDao.deleteSelectedCards()
        }
    }

    suspend fun getSelectedCards(): List<Card> {
        return cardDao.getAllCards().first().filter { it.isSelected }
    }
}