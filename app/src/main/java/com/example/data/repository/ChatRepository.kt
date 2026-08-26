package com.example.data.repository

import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.AttachmentItem
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ConversationEntity
import com.example.data.network.ApiClient
import com.example.data.network.model.AttachmentDto
import com.example.data.network.model.CreateConversationRequest
import com.example.data.network.model.EnhancePromptRequest
import com.example.data.network.model.SendMessageRequest
import com.example.data.network.model.ServerHealthResponse
import kotlinx.coroutines.flow.Flow
import java.io.IOException
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiClient: ApiClient
) {
    val allConversations: Flow<List<ConversationEntity>> = chatDao.getAllConversations()
    val pinnedConversations: Flow<List<ConversationEntity>> = chatDao.getPinnedConversations()

    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForConversation(conversationId)
    }

    suspend fun pingServer(): Result<ServerHealthResponse> {
        return try {
            val response = apiClient.getApiService().checkHealth()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(IOException("Server returned code ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createNewConversation(title: String = "New Conversation", model: String = "GPT-4 Turbo"): String {
        val id = "conv_" + UUID.randomUUID().toString().take(8)
        val conversation = ConversationEntity(
            id = id,
            title = title,
            updatedAt = System.currentTimeMillis(),
            modelUsed = model
        )
        chatDao.insertConversation(conversation)

        // Asynchronously notify server
        try {
            apiClient.getApiService().createConversation(
                CreateConversationRequest(title = title, model = model)
            )
        } catch (_: Exception) {
            // Optimistic local persistence succeeds even if server is offline
        }

        return id
    }

    suspend fun togglePin(conversationId: String, currentPinned: Boolean) {
        chatDao.setPinned(conversationId, !currentPinned)
        try {
            apiClient.getApiService().togglePin(conversationId)
        } catch (_: Exception) {
            // Local state preserved
        }
    }

    suspend fun deleteConversation(conversationId: String) {
        chatDao.deleteConversation(conversationId)
        try {
            apiClient.getApiService().deleteConversation(conversationId)
        } catch (_: Exception) {
            // Local state deleted
        }
    }

    suspend fun clearAllHistory() {
        chatDao.clearAllConversations()
    }

    suspend fun sendMessage(
        conversationId: String,
        userText: String,
        modelName: String,
        attachments: List<AttachmentItem> = emptyList()
    ): ChatMessageEntity {
        // 1. Insert user message in local cache immediately
        val userMessage = ChatMessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            sender = "user",
            textContent = userText,
            timestamp = System.currentTimeMillis(),
            model = modelName,
            attachedFilesJson = if (attachments.isNotEmpty()) AttachmentItem.listToJson(attachments) else null
        )
        chatDao.insertMessage(userMessage)

        // Update local conversation preview and title
        val conv = chatDao.getConversationById(conversationId)
        if (conv != null) {
            val newTitle = if (conv.title == "New Conversation" || conv.title.isBlank()) {
                val effectiveTitle = if (userText.isNotBlank()) userText else (attachments.firstOrNull()?.name ?: "Attached Content")
                effectiveTitle.take(28) + if (effectiveTitle.length > 28) "..." else ""
            } else conv.title
            val preview = if (userText.isNotBlank()) userText.take(60) else "[Attached ${attachments.size} file(s)]"
            chatDao.updateConversation(
                conv.copy(
                    title = newTitle,
                    updatedAt = System.currentTimeMillis(),
                    previewMessage = preview
                )
            )
        }

        // 2. Prepare request payload for backend server
        val attachmentDtos = attachments.map {
            AttachmentDto(
                name = it.name,
                mimeType = it.mimeType,
                size = it.size,
                uriString = it.uriString,
                isImage = it.isImage
            )
        }

        val request = SendMessageRequest(
            conversationId = conversationId,
            prompt = userText,
            model = modelName,
            attachments = attachmentDtos
        )

        // 3. Make HTTP request to backend server
        val response = apiClient.getApiService().sendMessage(request)

        if (!response.isSuccessful || response.body() == null) {
            val errorMsg = "Server error (${response.code()}): ${response.errorBody()?.string() ?: response.message()}"
            throw IOException(errorMsg)
        }

        val dto = response.body()!!

        // 4. Save server's response to local database
        val aiMessage = ChatMessageEntity(
            id = dto.id ?: UUID.randomUUID().toString(),
            conversationId = conversationId,
            sender = "ai",
            textContent = dto.textContent,
            codeSnippet = dto.codeSnippet,
            codeLanguage = dto.codeLanguage,
            codeExplanation = dto.codeExplanation,
            timestamp = dto.timestamp,
            model = dto.model ?: modelName
        )
        chatDao.insertMessage(aiMessage)
        return aiMessage
    }

    suspend fun enhancePrompt(prompt: String): String {
        val response = apiClient.getApiService().enhancePrompt(EnhancePromptRequest(prompt))
        if (response.isSuccessful && response.body() != null) {
            return response.body()!!.enhancedPrompt
        }
        throw IOException("Failed to enhance prompt from server: ${response.message()}")
    }

    suspend fun updateMessageRating(messageId: String, rating: Int) {
        chatDao.updateMessageRating(messageId, rating)
    }

    suspend fun getAllMessagesForExport(): List<ChatMessageEntity> {
        return chatDao.getAllMessages()
    }
}
