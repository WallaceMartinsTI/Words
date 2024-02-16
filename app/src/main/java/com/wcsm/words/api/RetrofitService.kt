package com.wcsm.words.api


import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitService {

    const val WORDS_BASE_URL = "https://wordsapiv1.p.rapidapi.com/words/"

    fun <T> getApiData(apiClass: Class<T>, baseUrl: String) : T {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(AuthInterceptor())
                    .build()
            )
            .build()
            .create(apiClass)
    }
}