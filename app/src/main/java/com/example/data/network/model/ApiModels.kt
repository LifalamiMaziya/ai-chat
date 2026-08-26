package com.example.data.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class AuthResponse(
    @Json(name = "token") val token: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "refreshToken") val refreshToken: String? = null
)

@JsonClass(generateAdapter = true)
data class RefreshRequest(
    @Json(name = "refreshToken") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class AttachmentDto(
    @Json(name = "name") val name: String,
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "size") val size: String,
    @Json(name = "uriString") val uriString: String,
    @Json(name = "isImage") val isImage: Boolean = false,
    @Json(name = "url") val url: String? = null
)

@JsonClass(generateAdapter = true)
data class SavedUploadDto(
    @Json(name = "id") val id: String,
    @Json(name = "url") val url: String,
    @Json(name = "name") val name: String,
    @Json(name = "mimeType") val mimeType: String,
    @Json(name = "sizeBytes") val sizeBytes: Long = 0,
    @Json(name = "isImage") val isImage: Boolean = false
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "prompt") val prompt: String,
    @Json(name = "model") val model: String,
    @Json(name = "attachments") val attachments: List<AttachmentDto> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ChatResponseDto(
    @Json(name = "id") val id: String? = null,
    @Json(name = "conversationId") val conversationId: String? = null,
    @Json(name = "textContent") val textContent: String,
    @Json(name = "codeSnippet") val codeSnippet: String? = null,
    @Json(name = "codeLanguage") val codeLanguage: String? = null,
    @Json(name = "codeExplanation") val codeExplanation: String? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "timestamp") val timestamp: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class EnhancePromptRequest(
    @Json(name = "prompt") val prompt: String
)

@JsonClass(generateAdapter = true)
data class EnhancePromptResponse(
    @Json(name = "enhancedPrompt") val enhancedPrompt: String
)

@JsonClass(generateAdapter = true)
data class CreateConversationRequest(
    @Json(name = "title") val title: String = "New Conversation",
    @Json(name = "model") val model: String = "Default"
)

@JsonClass(generateAdapter = true)
data class ConversationDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "updatedAt") val updatedAt: Long,
    @Json(name = "isPinned") val isPinned: Boolean = false,
    @Json(name = "modelUsed") val modelUsed: String = "Default",
    @Json(name = "previewMessage") val previewMessage: String = ""
)

@JsonClass(generateAdapter = true)
data class UserProfileDto(
    @Json(name = "name") val name: String,
    @Json(name = "email") val email: String,
    @Json(name = "planName") val planName: String,
    @Json(name = "planPrice") val planPrice: String,
    @Json(name = "renewalDate") val renewalDate: String,
    @Json(name = "paymentCard") val paymentCard: String,
    @Json(name = "cardExpiry") val cardExpiry: String,
    @Json(name = "contextWindow") val contextWindow: String,
    @Json(name = "multimodal") val multimodal: String,
    @Json(name = "priorityAccess") val priorityAccess: String
)

@JsonClass(generateAdapter = true)
data class BillingInvoiceDto(
    @Json(name = "id") val id: String,
    @Json(name = "date") val date: String,
    @Json(name = "amount") val amount: String,
    @Json(name = "status") val status: String
)

@JsonClass(generateAdapter = true)
data class ExportRequestDto(
    @Json(name = "title") val title: String,
    @Json(name = "format") val format: String,
    @Json(name = "scope") val scope: String,
    @Json(name = "dateRangeText") val dateRangeText: String? = null
)

@JsonClass(generateAdapter = true)
data class ExportResponseDto(
    @Json(name = "id") val id: String,
    @Json(name = "title") val title: String,
    @Json(name = "timestamp") val timestamp: Long,
    @Json(name = "format") val format: String,
    @Json(name = "fileSize") val fileSize: String,
    @Json(name = "status") val status: String,
    @Json(name = "scope") val scope: String,
    @Json(name = "dateRangeText") val dateRangeText: String? = null,
    @Json(name = "downloadUrl") val downloadUrl: String? = null,
    @Json(name = "filePayload") val filePayload: String? = null
)

@JsonClass(generateAdapter = true)
data class PlanChangeRequest(
    @Json(name = "planId") val planId: String
)

@JsonClass(generateAdapter = true)
data class PlanChangeResponse(
    @Json(name = "planName") val planName: String,
    @Json(name = "status") val status: String,
    @Json(name = "currentPeriodEnd") val currentPeriodEnd: Long,
    @Json(name = "invoice") val invoice: BillingInvoiceDto? = null
)

@JsonClass(generateAdapter = true)
data class ServerHealthResponse(
    @Json(name = "status") val status: String,
    @Json(name = "version") val version: String = "1.0",
    @Json(name = "serverName") val serverName: String = "Server",
    @Json(name = "modelsAvailable") val modelsAvailable: List<String> = emptyList()
)
