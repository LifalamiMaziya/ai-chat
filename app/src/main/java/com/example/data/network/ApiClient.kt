package com.example.data.network

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ServerConfig(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("server_config", Context.MODE_PRIVATE)

    var baseUrl: String
        get() = prefs.getString("server_base_url", DEFAULT_URL) ?: DEFAULT_URL
        set(value) {
            val sanitized = if (value.endsWith("/")) value else "$value/"
            prefs.edit().putString("server_base_url", sanitized).apply()
        }

    var authToken: String?
        get() = prefs.getString("auth_token", null)
        set(value) {
            prefs.edit().putString("auth_token", value).apply()
        }

    var refreshToken: String?
        get() = prefs.getString("refresh_token", null)
        set(value) {
            prefs.edit().putString("refresh_token", value).apply()
        }

    fun clearCredentials() {
        prefs.edit().remove("auth_token").remove("refresh_token").apply()
    }

    companion object {
        const val DEFAULT_URL = "http://10.0.2.2:8080/"
    }
}

class ApiClient(private val serverConfig: ServerConfig) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val builder = original.newBuilder()
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")

        serverConfig.authToken?.let { token ->
            if (token.isNotBlank()) {
                builder.header("Authorization", "Bearer $token")
            }
        }

        chain.proceed(builder.build())
    }

    /**
     * Refreshes the auth tokens against api/auth/refresh using a bare client
     * (no authenticator, to avoid recursion). Returns the new bearer token or
     * null if the refresh failed.
     */
    private fun performTokenRefresh(): String? {
        val refresh = serverConfig.refreshToken ?: return null
        return try {
            val body = JSONObject().put("refreshToken", refresh).toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(serverConfig.baseUrl + "api/auth/refresh")
                .post(body)
                .build()
            bareClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val raw = resp.body?.string() ?: return null
                val json = JSONObject(raw)
                val newAccess = json.optString("token", "")
                val newRefresh = json.optString("refreshToken", "")
                if (newAccess.isBlank()) return null
                serverConfig.authToken = newAccess
                if (newRefresh.isNotBlank()) serverConfig.refreshToken = newRefresh
                newAccess
            }
        } catch (_: Exception) {
            null
        }
    }

    private val tokenAuthenticator = object : Authenticator {
        override fun authenticate(route: Route?, response: Response): Request? {
            if (responseCount(response) >= 2) return null
            synchronized(this@ApiClient) {
                val newToken = performTokenRefresh() ?: return null
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            }
        }

        private fun responseCount(response: Response): Int {
            var count = 1
            var prior = response.priorResponse
            while (prior != null) {
                count++
                prior = prior.priorResponse
            }
            return count
        }
    }

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(tokenAuthenticator)
        .retryOnConnectionFailure(true)
        .build()

    private val bareClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    val streaming by lazy {
        StreamingChatClient(serverConfig, moshi, okHttpClient)
    }

    fun getApiService(): ApiService {
        val url = serverConfig.baseUrl
        return Retrofit.Builder()
            .baseUrl(url)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(ApiService::class.java)
    }
}
