package com.example.listsqre_revamped

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "card_items",
    foreignKeys = [ForeignKey(
        entity = Card::class,
        parentColumns = ["id"],
        childColumns = ["cardId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("cardId")]
)
data class CardItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val description: String,
    val isSelected: Boolean = false,
    val isPinned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)