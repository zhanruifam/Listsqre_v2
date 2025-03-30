package com.example.listsqre_revamped

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class CardWithFields(
    val card: Card,
    val fields: List<CardField> = emptyList()
)

class DynamicTableViewModel(
    private val db: AppDatabase
) : ViewModel() {
    fun createCardTable(cardId: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            db.query(
                """
                CREATE TABLE IF NOT EXISTS card_${cardId} (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    field_name TEXT NOT NULL,
                    field_value TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """.trimIndent(),
                null
            )
            onComplete()
        }
    }

    fun dropCardTable(cardId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            db.query(
                "DROP TABLE IF EXISTS card_${cardId}",
                null
            )
            onComplete()
        }
    }

    fun insertCardField(
        cardId: Int,
        fieldName: String,
        fieldValue: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            db.query(
                """
                INSERT INTO card_$cardId (field_name, field_value)
                VALUES (?, ?)
                """.trimIndent(),
                arrayOf(fieldName, fieldValue)
            )
            onComplete()
        }
    }

    fun getCardFields(
        cardId: Int,
        onResult: (List<CardField>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val cursor = db.query("SELECT * FROM card_$cardId", null)
            val fields = buildList {
                while (cursor.moveToNext()) {
                    add(CardField(
                        id = cursor.getInt(0),
                        fieldTitle = cursor.getString(1),
                        fieldDescription = cursor.getString(2),
                        isSelected = cursor.getInt(3) == 1,
                        isPinned = cursor.getInt(4) == 1,
                        createdAt = cursor.getLong(5)
                    ))
                }
            }
            cursor.close()
            onResult(fields)
        }
    }

    fun updateCardField(
        cardId: Int,
        fieldId: String,
        newValue: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            db.query(
                """
                UPDATE card_$cardId 
                SET field_value = ?
                WHERE id = ?
                """.trimIndent(),
                arrayOf<Any?>(newValue, fieldId)
            )
            onComplete()
        }
    }
}