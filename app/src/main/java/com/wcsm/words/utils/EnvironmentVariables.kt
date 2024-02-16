package com.wcsm.words.utils

import io.github.cdimascio.dotenv.dotenv

object EnvironmentVariables {

    private val dotenv = dotenv {
        directory = "/assets"
        filename = "env"
    }

    val GOOGLE_API_KEY: String = dotenv["GOOGLE_API_KEY"]
    val X_RAPID_API_KEY: String = dotenv["X_RAPID_API_KEY"]
    val X_RAPID_API_HOST: String = dotenv["X_RAPID_API_HOST"]
}