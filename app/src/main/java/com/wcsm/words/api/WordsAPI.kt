package com.wcsm.words.api

import com.wcsm.words.model.WordResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface WordsAPI {

    @GET("{word}")
    suspend fun getWordDefinition(
        @Path("word") word: String
    ) : Response<WordResponse>
}