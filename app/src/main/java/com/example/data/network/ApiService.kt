package com.example.data.network

import com.example.data.network.model.AuthRequest
import com.example.data.network.model.AuthResponse
import com.example.data.network.model.BillingInvoiceDto
import com.example.data.network.model.ChatResponseDto
import com.example.data.network.model.ConversationDto
import com.example.data.network.model.CreateConversationRequest
import com.example.data.network.model.EnhancePromptRequest
import com.example.data.network.model.EnhancePromptResponse
import com.example.data.network.model.ExportRequestDto
import com.example.data.network.model.ExportResponseDto
import com.example.data.network.model.SendMessageRequest
import com.example.data.network.model.ServerHealthResponse
import com.example.data.network.model.UserProfileDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @GET("api/health")
    suspend fun checkHealth(): Response<ServerHealthResponse>

    @POST("api/chat/send")
    suspend fun sendMessage(
        @Body request: SendMessageRequest
    ): Response<ChatResponseDto>

    @POST("api/chat/enhance-prompt")
    suspend fun enhancePrompt(
        @Body request: EnhancePromptRequest
    ): Response<EnhancePromptResponse>

    @GET("api/conversations")
    suspend fun getConversations(): Response<List<ConversationDto>>

    @POST("api/conversations")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): Response<ConversationDto>

    @DELETE("api/conversations/{id}")
    suspend fun deleteConversation(
        @Path("id") conversationId: String
    ): Response<Unit>

    @POST("api/conversations/{id}/pin")
    suspend fun togglePin(
        @Path("id") conversationId: String
    ): Response<Unit>

    @GET("api/user/profile")
    suspend fun getUserProfile(): Response<UserProfileDto>

    @GET("api/user/invoices")
    suspend fun getBillingHistory(): Response<List<BillingInvoiceDto>>

    @POST("api/exports/generate")
    suspend fun generateExport(
        @Body request: ExportRequestDto
    ): Response<ExportResponseDto>

    @GET("api/exports")
    suspend fun getExports(): Response<List<ExportResponseDto>>
}
