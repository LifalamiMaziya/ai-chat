package com.example.data.network

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
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

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

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
