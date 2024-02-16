package com.wcsm.words.model

data class WordResponse(
    val frequency: Double,
    val pronunciation: Pronunciation,
    val results: List<Result>,
    val syllables: Syllables,
    val word: String
)