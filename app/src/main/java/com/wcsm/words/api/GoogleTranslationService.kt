package com.wcsm.words.api

import com.google.cloud.translate.Translate
import com.google.cloud.translate.TranslateOptions
import com.google.cloud.translate.Translation
import com.wcsm.words.utils.EnvironmentVariables

object GoogleTranslationService {

    private val API_KEY = EnvironmentVariables.GOOGLE_API_KEY

    fun translate(englishWord: String, targetLang: String): String {
        val sourceLang = "en"
        val translate: Translate = TranslateOptions.newBuilder().setApiKey(API_KEY).build().service
        val translation: Translation = translate.translate(
            englishWord,
            Translate.TranslateOption.targetLanguage(targetLang),
            Translate.TranslateOption.sourceLanguage(sourceLang)
        )
        return translation.translatedText
    }

}