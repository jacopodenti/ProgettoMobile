package com.example.mangiabasta.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object  RetrofitClient {
    private const val BASE_URL = "https://develop.ewlab.di.unimi.it/mc/2425/"

    // Logging interceptor per debug
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Configurazione dell'HTTP Client
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    // Creazione dell'istanza di Retrofit
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(httpClient)
        .build()

    // Espone l'interfaccia ApiService
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
