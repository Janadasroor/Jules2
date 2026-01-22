package com.jnd.jules.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://jules.googleapis.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()
            
            val apiKey = com.jnd.jules.util.PreferenceManager.getApiKey()
            if (!apiKey.isNullOrBlank()) {
                builder.header("X-Goog-Api-Key", apiKey)
            }
            
            chain.proceed(builder.build())
        }
        .build()

    val apiService: JulesApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(JulesApiService::class.java)
    }
}
