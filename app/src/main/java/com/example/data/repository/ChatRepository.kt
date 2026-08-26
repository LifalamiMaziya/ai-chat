package com.example.data.repository

import android.content.Context
import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.UUID

class ChatRepository(
    private val chatDao: ChatDao,
    private val apiClient: ApiClient,
    private val appContext: Context
) {
    val allConversations: Flow<List<ConversationEntity>> = chatDao.getAllConversations()
    val pinnedConversations: Flow<List<ConversationEntity>> = chatDao.getPinnedConversations()

    private val streamingClient by lazy { apiClient.streaming }

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

    /**
     * Uploads an attachment to the backend so the model can access it.
     * Returns a server URL or null when the type is not uploadable / it failed.
     */
    private suspend fun uploadIfNeeded(att: AttachmentItem): String? = withContext(Dispatchers.IO) {
        val uploadable = att.isImage || att.mimeType.equals("application/pdf", ignoreCase = true)
        if (!uploadable) return@withContext null
        try {
            val uri = Uri.parse(att.uriString)
            val bytes = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: return@withContext null
            val mediaType = att.mimeType.ifBlank { "application/octet-stream" }.toMediaTypeOrNull()
            val part = MultipartBody.Part.createFormData(
                "file",
                att.name.ifBlank { "attachment" },
                bytes.toRequestBody(mediaType)
            )
            val response = apiClient.getApiService().uploadAttachment(part)
            if (response.isSuccessful && response.body() != null) response.body()!!.url else null
        } catch (_: Exception) {
            null
        }
    }

    private fun extractCode(text: String): Pair<String?, String?> {
        val match = CODE_FENCE.find(text) ?: return null to null
        val language = match.groupValues[1].trim().ifBlank { null }
        val snippet = match.groupValues[2].trimEnd()
        return snippet to language
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

        // 2. Upload attachments that carry content the model can use
        val attachmentDtos = attachments.map { att ->
            AttachmentDto(
                name = att.name,
                mimeType = att.mimeType,
                size = att.size,
                uriString = att.uriString,
                isImage = att.isImage,
                url = uploadIfNeeded(att)
            )
        }

        // 3. Placeholder AI bubble so the UI shows activity while streaming
        val aiMessageId = UUID.randomUUID().toString()
        val placeholder = ChatMessageEntity(
            id = aiMessageId,
            conversationId = conversationId,
            sender = "ai",
            textContent = "",
            timestamp = System.currentTimeMillis(),
            model = modelName,
            isStreaming = true
        )
        chatDao.insertMessage(placeholder)

        val request = SendMessageRequest(
            conversationId = conversationId,
            prompt = userText,
            model = modelName,
            attachments = attachmentDtos
        )

        // 4. Stream tokens; persist into Room on a light throttle
        var lastWrite = 0L
        val buffer = StringBuilder()

        val streamed: Result<String>? = try {
            streamingClient.stream(request) { delta ->
                synchronized(buffer) { buffer.append(delta) }
                val now = System.currentTimeMillis()
                if (now - lastWrite > THROTTLE_MS) {
                    lastWrite = now
                    val snapshot = synchronized(buffer) { buffer.toString() }
                    kotlinx.coroutines.runBlocking {
                        chatDao.updateMessageContent(aiMessageId, snapshot)
                    }
                }
            }
        } catch (_: Exception) {
            null
        }

        val fullText = streamed?.getOrNull()

        if (fullText == null) {
            // The stream never produced anything; fall back to a plain request.
            try {
                val response = apiClient.getApiService().sendMessage(request)
                if (!response.isSuccessful || response.body() == null) {
                    throw IOException("Server error (${response.code()})")
                }
                val dto = response.body()!!
                val fallback = placeholder.copy(
                    textContent = dto.textContent,
                    codeSnippet = dto.codeSnippet,
                    codeLanguage = dto.codeLanguage,
                    codeExplanation = dto.codeExplanation,
                    timestamp = dto.timestamp,
                    model = dto.model ?: modelName,
                    isStreaming = false
                )
                chatDao.updateMessage(fallback)
                updateConversationAfterReply(conversationId, fallback.textContent)
                return fallback
            } catch (e: Exception) {
                chatDao.deleteMessage(aiMessageId)
                throw e
            }
        }

        val text = fullText ?: ""
        val (snippet, language) = extractCode(text)
        val finalMessage = placeholder.copy(
            textContent = text,
            codeSnippet = snippet,
            codeLanguage = language,
            isStreaming = false
        )
        chatDao.updateMessage(finalMessage)
        updateConversationAfterReply(conversationId, text)
        return finalMessage
    }

    private suspend fun updateConversationAfterReply(conversationId: String, replyText: String) {
        val conv = chatDao.getConversationById(conversationId) ?: return
        chatDao.updateConversation(
            conv.copy(
                updatedAt = System.currentTimeMillis(),
                previewMessage = if (replyText.isNotBlank()) {
                    replyText.replace(Regex("\\s+"), " ").trim().take(60)
                } else conv.previewMessage
            )
        )
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

    companion object {
        private const val THROTTLE_MS = 250L
        private val CODE_FENCE = Regex("```([a-zA-Z0-9_+#.-]*)[ \\t]*\\r?\\n([\\s\\S]*?)```")
    }
}
