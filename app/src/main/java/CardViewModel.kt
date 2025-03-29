package com.example.listsqre_revamped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

class CardViewModel : ViewModel() {
    private val _state = MutableStateFlow(CardState())
    val state: StateFlow<CardState> = _state.asStateFlow()

    private var nextId = 1

    init {
        // Initialize with some sample data
        _state.value = CardState(
            cards = listOf(
                Card(0, "Welcome", "This is your first card", false, true),
                Card(1, "Getting Started", "Try adding more cards", false, false)
            )
        )
        nextId = 2
    }

    fun updateCard(id: Int, title: String, description: String, isPinned: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                cards = _state.value.cards.map { card ->
                    if (card.id == id) {
                        card.copy(
                            title = title,
                            description = description,
                            isPinned = isPinned
                        )
                    } else {
                        card
                    }
                }
            )
            // Update database here in a real app
        }
    }

    fun addCard(title: String, description: String, isPinned: Boolean = false) {
        viewModelScope.launch {
            val newCard = Card(
                id = nextId++,
                title = title,
                description = description,
                isPinned = isPinned
            )
            _state.value = _state.value.copy(
                cards = _state.value.cards + newCard
            )
            // Update database here in a real app
        }
    }

    fun updateCardSelection(cardId: Int, isSelected: Boolean) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                cards = _state.value.cards.map { card ->
                    if (card.id == cardId) card.copy(isSelected = isSelected) else card
                }
            )
        }
    }

    fun pinSelectedCards() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                cards = _state.value.cards.map { card ->
                    if (card.isSelected) card.copy(isPinned = true) else card
                }
            )
            // Here you would also update your database
        }
    }

    fun deleteSelectedCards() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                cards = _state.value.cards.filterNot { it.isSelected }
            )
            // Here you would also update your database
        }
    }
}