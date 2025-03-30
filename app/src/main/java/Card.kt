package com.example.listsqre_revamped

import androidx.room.Entity
import androidx.room.PrimaryKey

// Card.kt
@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val isSelected: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// for dynamic table
data class CardField(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fieldTitle: String,
    val fieldDescription: String,
    val isSelected: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)