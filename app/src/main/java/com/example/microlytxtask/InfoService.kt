package com.example.microlytxtask


import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

private const val BASE_URL = "http://ip-api.com/"

private fun logInterceptor(): HttpLoggingInterceptor {
    val logInterceptor = HttpLoggingInterceptor()
    logInterceptor.level = HttpLoggingInterceptor.Level.BODY
    return logInterceptor
}

private val client = OkHttpClient.Builder()
    .build()


private val retrofit = Retrofit.Builder()
    .addConverterFactory(GsonConverterFactory.create())
    .client(client)
    .baseUrl(BASE_URL)
    .build()

interface InfoService {
    @GET("json")
    suspend fun getInfo(): Info
}

object InfoApi {
    val infoService: InfoService by lazy {
        retrofit.create(InfoService::class.java)
    }
}