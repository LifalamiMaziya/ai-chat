package com.example.data.network

import com.example.data.network.model.SendMessageRequest
import com.squareup.moshi.Moshi
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume

/**
 * Consumes the backend's SSE chat stream (POST api/chat/stream) and reports
 * deltas through [onDelta]. Resolves with the full accumulated text.
 */
class StreamingChatClient(
    private val serverConfig: ServerConfig,
    private val moshi: Moshi,
    private val client: OkHttpClient
) {
    suspend fun stream(
        request: SendMessageRequest,
        onDelta: (String) -> Unit
    ): Result<String> = suspendCancellableCoroutine { cont ->
        val jsonBody = moshi.adapter(SendMessageRequest::class.java).toJson(request)
        val body = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

        val builder = Request.Builder()
            .url(serverConfig.baseUrl + "api/chat/stream")
            .header("Accept", "text/event-stream")
            .header("Content-Type", "application/json")
            .post(body)

        serverConfig.authToken?.let { token ->
            if (token.isNotBlank()) builder.header("Authorization", "Bearer $token")
        }

        val accumulated = StringBuilder()
        var completed = false

        val es = EventSources.createFactory(client).newEventSource(
            builder.build(),
            object : EventSourceListener() {
                override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                    if (completed) return
                    try {
                        val event = JSONObject(data)
                        when (event.optString("type")) {
                            "delta" -> {
                                val text = event.optString("content")
                                if (text.isNotEmpty()) {
                                    accumulated.append(text)
                                    onDelta(text)
                                }
                            }
                            "done" -> {
                                completed = true
                                eventSource.cancel()
                                if (cont.isActive) {
                                    cont.resume(Result.success(accumulated.toString()))
                                }
                            }
                            "error" -> {
                                completed = true
                                eventSource.cancel()
                                val message = event.optString("message", "Stream error")
                                if (cont.isActive) {
                                    // Hand back whatever arrived before the failure.
                                    if (accumulated.isNotEmpty()) {
                                        cont.resume(Result.success(accumulated.toString()))
                                    } else {
                                        cont.resume(Result.failure(IOException(message)))
                                    }
                                }
                            }
                        }
                    } catch (_: Exception) {
                        // Malformed event line; ignore and keep the stream open.
                    }
                }

                override fun onClosed(eventSource: EventSource) {
                    if (!completed) {
                        completed = true
                        if (cont.isActive) {
                            if (accumulated.isNotEmpty()) {
                                cont.resume(Result.success(accumulated.toString()))
                            } else {
                                cont.resume(Result.failure(IOException("Connection closed before any response")))
                            }
                        }
                    }
                }

                override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                    if (!completed) {
                        completed = true
                        if (cont.isActive) {
                            val error = t ?: IOException(
                                "SSE connection failed" + (response?.code?.let { " (HTTP $it)" } ?: "")
                            )
                            cont.resume(Result.failure(error))
                        }
                    }
                }
            }
        )

        cont.invokeOnCancellation { es.cancel() }
    }
}
