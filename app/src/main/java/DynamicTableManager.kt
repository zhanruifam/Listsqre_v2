package com.example.listsqre_revamped

import android.database.Cursor
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DynamicTableManager(
    private val db: AppDatabase,
    private val db_: AppDatabaseCardField
) : ViewModel() {

    /**
     * Creates a dynamic table for a specific card if it doesn't exist
     * @param cardId The ID of the card to create a table for
     * @param onComplete Callback when operation completes
     */
    fun createCardTable(cardId: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            db.query(
                """
                CREATE TABLE IF NOT EXISTS card_${cardId} (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fieldTitle TEXT NOT NULL,
                    fieldDescription TEXT,
                    isSelected INTEGER DEFAULT 0,
                    isPinned INTEGER DEFAULT 0,
                    createdAt INTEGER DEFAULT (strftime('%s','now') * 1000)
                )
                """.trimIndent(),
                null
            )
            withContext(Dispatchers.Main) { onComplete() }
            Log.d("DynamicTable", "Successfully created table card_$cardId")
        }
    }

    /**
     * Drops a card's dynamic table if it exists
     * @param cardId The ID of the card whose table should be dropped
     * @param onComplete Callback when operation completes
     */
    fun dropCardTable(cardId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            db.query("DROP TABLE IF EXISTS card_${cardId}", null)
            withContext(Dispatchers.Main) { onComplete() }
            Log.d("DynamicTable", "Successfully dropped table card_$cardId")
        }
    }

    /**
     * Safely gets column index or throws descriptive exception
     */
    private fun Cursor.getColumnIndexSafe(columnName: String): Int {
        val index = getColumnIndex(columnName)
        if (index == -1) {
            throw IllegalArgumentException("Column '$columnName' not found in cursor. Available columns: ${columnNames.joinToString()}")
        }
        return index
    }

    /**
     * Inserts a card field into the specified card table
     * @param cardId The ID of the card table
     * @param field The CardField to insert
     * @param onComplete Callback with the new row ID (-1 if failed)
     */
    fun insertField(cardId: Long, field: CardField, onComplete: (Long) -> Unit = { _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rowId = db_.query(
                    """
                    INSERT INTO card_${cardId} 
                    (fieldTitle, fieldDescription, isSelected, isPinned, createdAt) 
                    VALUES (?, ?, ?, ?, ?)
                    """.trimIndent(),
                    arrayOf<Any>(
                        field.fieldTitle,
                        field.fieldDescription,
                        if (field.isSelected) 1 else 0,
                        if (field.isPinned) 1 else 0,
                        field.createdAt
                    )
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getLong(cursor.getColumnIndexSafe("last_insert_rowid()"))
                    } else -1
                }
                withContext(Dispatchers.Main) { onComplete(rowId) }
            } catch (e: Exception) {
                Log.e("DynamicTableManager", "Insert failed", e)
                withContext(Dispatchers.Main) { onComplete(-1) }
            }
        }
    }

    /**
     * Updates a card field in the specified card table
     * @param cardId The ID of the card table
     * @param field The CardField with updated values
     * @param onComplete Callback when operation completes
     */
    fun updateField(cardId: Long, field: CardField, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            db_.query(
                """
                UPDATE card_${cardId} 
                SET 
                    fieldTitle = ?, 
                    fieldDescription = ?, 
                    isSelected = ?, 
                    isPinned = ?, 
                    createdAt = ? 
                WHERE id = ?
                """.trimIndent(),
                arrayOf(
                    field.fieldTitle,
                    field.fieldDescription,
                    if (field.isSelected) 1 else 0,
                    if (field.isPinned) 1 else 0,
                    field.createdAt,
                    field.id
                )
            )
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    /**
     * Deletes a card field from the specified card table
     * @param cardId The ID of the card table
     * @param fieldId The ID of the field to delete
     * @param onComplete Callback when operation completes
     */
    fun deleteField(cardId: Long, fieldId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            db_.query(
                "DELETE FROM card_${cardId} WHERE id = ?",
                arrayOf(fieldId)
            )
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    /**
     * Retrieves all card fields from the specified card table
     * @param cardId The ID of the card table
     * @param onComplete Callback with list of CardField objects
     */
    fun getAllFields(cardId: Long, onComplete: (List<CardField>) -> Unit = { _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fields = mutableListOf<CardField>()
                db_.query("SELECT * FROM card_${cardId}", null).use { cursor ->
                    val idIndex = cursor.getColumnIndexSafe("id")
                    val titleIndex = cursor.getColumnIndexSafe("fieldTitle")
                    val descIndex = cursor.getColumnIndexSafe("fieldDescription")
                    val selectedIndex = cursor.getColumnIndexSafe("isSelected")
                    val pinnedIndex = cursor.getColumnIndexSafe("isPinned")
                    val createdIndex = cursor.getColumnIndexSafe("createdAt")

                    while (cursor.moveToNext()) {
                        fields.add(
                            CardField(
                                id = cursor.getInt(idIndex),
                                fieldTitle = cursor.getString(titleIndex),
                                fieldDescription = cursor.getString(descIndex),
                                isSelected = cursor.getInt(selectedIndex) == 1,
                                isPinned = cursor.getInt(pinnedIndex) == 1,
                                createdAt = cursor.getLong(createdIndex)
                            )
                        )
                    }
                }
                withContext(Dispatchers.Main) { onComplete(fields) }
            } catch (e: Exception) {
                Log.e("DynamicTableManager", "Query failed", e)
                withContext(Dispatchers.Main) { onComplete(emptyList()) }
            }
        }
    }

    /**
     * Retrieves a single card field by ID
     * @param cardId The ID of the card table
     * @param fieldId The ID of the field to retrieve
     * @param onComplete Callback with CardField or null if not found
     */
    fun getFieldById(cardId: Long, fieldId: Int, onComplete: (CardField?) -> Unit = { _ -> }) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val field = db_.query(
                    "SELECT * FROM card_${cardId} WHERE id = ?",
                    arrayOf(fieldId.toString())
                ).use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndexSafe("id")
                        val titleIndex = cursor.getColumnIndexSafe("fieldTitle")
                        val descIndex = cursor.getColumnIndexSafe("fieldDescription")
                        val selectedIndex = cursor.getColumnIndexSafe("isSelected")
                        val pinnedIndex = cursor.getColumnIndexSafe("isPinned")
                        val createdIndex = cursor.getColumnIndexSafe("createdAt")

                        CardField(
                            id = cursor.getInt(idIndex),
                            fieldTitle = cursor.getString(titleIndex),
                            fieldDescription = cursor.getString(descIndex),
                            isSelected = cursor.getInt(selectedIndex) == 1,
                            isPinned = cursor.getInt(pinnedIndex) == 1,
                            createdAt = cursor.getLong(createdIndex)
                        )
                    } else null
                }
                withContext(Dispatchers.Main) { onComplete(field) }
            } catch (e: Exception) {
                Log.e("DynamicTableManager", "Query failed", e)
                withContext(Dispatchers.Main) { onComplete(null) }
            }
        }
    }

    /**
     * Toggles the selected state of a card field
     * @param cardId The ID of the card table
     * @param fieldId The ID of the field to toggle
     * @param onComplete Callback when operation completes
     */
    fun toggleFieldSelection(cardId: Int, fieldId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            db_.query(
                """
                UPDATE card_${cardId} 
                SET isSelected = CASE WHEN isSelected = 1 THEN 0 ELSE 1 END 
                WHERE id = ?
                """.trimIndent(),
                arrayOf(fieldId)
            )
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    /**
     * Toggles the pinned state of a card field
     * @param cardId The ID of the card table
     * @param fieldId The ID of the field to toggle
     * @param onComplete Callback when operation completes
     */
    fun toggleFieldPin(cardId: Long, fieldId: Int, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            db_.query(
                """
                UPDATE card_${cardId} 
                SET isPinned = CASE WHEN isPinned = 1 THEN 0 ELSE 1 END 
                WHERE id = ?
                """.trimIndent(),
                arrayOf(fieldId)
            )
            withContext(Dispatchers.Main) { onComplete() }
        }
    }
}