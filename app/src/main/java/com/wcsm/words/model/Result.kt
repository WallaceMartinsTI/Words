package com.wcsm.words.model

data class Result(
    val definition: String,
    val examples: List<String>,
    val synonyms: List<String>,
)