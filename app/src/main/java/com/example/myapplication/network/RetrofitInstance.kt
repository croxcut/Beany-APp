package com.example.myapplication.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private const val MODEL_BASE_URL = "http://192.168.1.2:8000/"

    private const val LOGIN_BASE_URL = "http://192.168.1.2:3000/"

    private val okHttpClient = OkHttpClient.Builder().build()

    private val modelRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(MODEL_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val loginRetrofit by lazy {
        Retrofit.Builder()
            .baseUrl(LOGIN_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val modelApi: ApiService by lazy {
        modelRetrofit.create(ApiService::class.java)
    }

    val loginApi: ApiService by lazy {
        loginRetrofit.create(ApiService::class.java)
    }
}
