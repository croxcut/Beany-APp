package com.example.myapplication.network

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @Multipart
    @POST("predict/")
    fun uploadImage(
        @Part file: MultipartBody.Part,
        @Part("min_score") minScore: Float
    ): Call<ResponseBody>

    @POST("/login")
    fun login(@Body credentials: Map<String, String>): Call<ResponseBody>
}
