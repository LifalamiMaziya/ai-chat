package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["conversationId"])]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val sender: String, // "user" or "ai"
    val textContent: String,
    val codeSnippet: String? = null,
    val codeLanguage: String? = null,
    val codeExplanation: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val model: String = "GPT-4 Turbo",
    val rating: Int = 0, // 0 = none, 1 = up, -1 = down
    val isStreaming: Boolean = false,
    val attachedFilesJson: String? = null
)
