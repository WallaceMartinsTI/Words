package com.wcsm.words.api

import com.wcsm.words.utils.EnvironmentVariables
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor : Interceptor {

    private val xRapidApiKey = EnvironmentVariables.X_RAPID_API_KEY
    private val xRapidApiHost = EnvironmentVariables.X_RAPID_API_HOST

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        val request = requestBuilder.addHeader(
            "X-RapidAPI-Key", xRapidApiKey
        ).addHeader(
            "X-RapidAPI-Host", xRapidApiHost
        ).build()

        return chain.proceed(request)
    }
}