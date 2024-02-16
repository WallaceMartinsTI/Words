package com.wcsm.words

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.wcsm.words.api.RetrofitService
import com.wcsm.words.api.WordsAPI
import com.wcsm.words.databinding.ActivityMainBinding
import com.wcsm.words.model.WordResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import com.wcsm.words.api.GoogleTranslationService
import kotlinx.coroutines.runBlocking
import android.speech.tts.TextToSpeech
import com.wcsm.words.utils.ViewsVisibilityOptions

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private val binding by lazy {ActivityMainBinding.inflate(layoutInflater)}
    private val translationAPI by lazy {GoogleTranslationService}
    private val wordsAPI by lazy {
        RetrofitService.getApiData(
            WordsAPI::class.java,
            RetrofitService.WORDS_BASE_URL
        )
    }

    private var getWordDataJob: Job? = null
    private var translationJob: Job? = null

    private var enSyllablesNumber = 0

    private lateinit var tts: TextToSpeech

    private lateinit var enWord: String
    private lateinit var ptWord: String
    private lateinit var enDefinition: String
    private lateinit var ptDefinition: String
    private lateinit var enSyllables: String
    private lateinit var pronunciation: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        tts = TextToSpeech(this, this)
        enWord = ""

        binding.btnSearch.setOnClickListener {
            showHideLoading(true)
            showHideViewItens(ViewsVisibilityOptions.HIDE)
            binding.btnSearch.isEnabled = false
            val inputtedWord = binding.wordEditText.text.toString()
            if(validateWord(inputtedWord.lowercase())) {
                hideKeyboard()
                getWordData(inputtedWord)
            } else {
                showHideLoading(false)
                binding.btnSearch.isEnabled = true
            }
        }
        binding.fabPlayPronunciation.setOnClickListener {
            if(enWord.isNotEmpty()) {
                wordToSpeak(enWord)
            }
        }
    }

    override fun onInit(status: Int) {}

    override fun onDestroy() {
        super.onDestroy()
        translationJob?.cancel()
        getWordDataJob?.cancel()

        if(tts.isSpeaking) {
            tts.stop()
        }
        tts.shutdown()
    }

    private fun validateWord(word: String): Boolean {
        binding.wordInputLayout.error = null
        if(word.isEmpty()) {
            binding.wordInputLayout.error = "Você deve informar uma palavra."
            return false
        }
        return true
    }

    private fun getWordData(word: String) {
        binding.wordInputLayout.error = null
        getWordDataJob = CoroutineScope(Dispatchers.IO).launch {
            var response: Response<WordResponse>? = null

            try {
                response = wordsAPI.getWordDefinition(word)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val isValidResponse = response != null && response.isSuccessful
            if(isValidResponse) {
                val result = response?.body()
                if(result != null) {
                    withContext(Dispatchers.Main) {
                        populateScreenWithWordData(result)
                    }
                }

            } else {
                withContext(Dispatchers.Main) {
                    binding.wordInputLayout.errorIconDrawable = null
                    binding.wordInputLayout.error = "Palavra não encontrada."
                    showHideLoading(false)
                    binding.btnSearch.isEnabled = true
                }
            }
        }
    }

    private fun fillDetailsVariables(result: WordResponse) {
        enWord = result.word
        ptWord = translateWord(enWord, "pt")

        enDefinition = result.results[0].definition
        ptDefinition = translateWord(enDefinition, "pt")

        enSyllablesNumber = result.syllables.count

        enSyllables = getWordSyllables(result.syllables.list)

        pronunciation = result.pronunciation.all
    }

    private fun translateWord(sentence: String, lang: String): String {
        var translatedWord = ""
        runBlocking {
            translationJob = CoroutineScope(Dispatchers.IO).launch {
                translatedWord = translationAPI.translate(sentence, lang)
            }
            translationJob?.join()
        }

        return translatedWord
    }

    private fun getWordSyllables(wordSyllables: List<String>): String {
        var syllables = ""
        wordSyllables.forEach {
            syllables += " $it,"
        }

        return syllables.dropLast(1)
    }

    private fun fillWordSection(enWord: String, ptWord: String) {
        binding.tvWordEn.text = "Word: $enWord"
        binding.tvWordPt.text = "Palavra: $ptWord"
    }

    private fun fillDefinitionSection(enDefinition: String, ptDefinition: String) {
        binding.tvDefinitionEn.text = "Definition: $enDefinition."
        binding.tvDefinitionPt.text = "Definição: $ptDefinition."
    }

    private fun fillSyllablesSection() {
        binding.tvSyllablesNumber.text = "Syllables Nº: $enSyllablesNumber"
        binding.tvSyllables.text = "Syllables:$enSyllables"
    }

    private fun fillPronunciationSection(pronunciationWord: String) {
        binding.tvPronunciation.text = pronunciationWord
    }

    private fun populateScreenWithWordData(result: WordResponse) {
        fillDetailsVariables(result)

        fillWordSection(result.word, translateWord(result.word, "pt"))
        fillDefinitionSection(result.results[0].definition, translateWord(result.results[0].definition, "pt"))
        fillSyllablesSection()
        fillPronunciationSection(result.pronunciation.all)

        showHideViewItens(ViewsVisibilityOptions.SHOW)
        showHideLoading(false)
        binding.btnSearch.isEnabled = true
    }

    private fun wordToSpeak(text: String) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "")
    }

    private fun showHideViewItens(option: ViewsVisibilityOptions) {
        when(option) {
            ViewsVisibilityOptions.SHOW -> {
                with(binding) {
                    tvWordEn.visibility = View.VISIBLE
                    tvWordPt.visibility = View.VISIBLE
                    tvDefinitionEn.visibility = View.VISIBLE
                    tvDefinitionPt.visibility = View.VISIBLE
                    tvSyllablesNumber.visibility = View.VISIBLE
                    tvSyllables.visibility = View.VISIBLE
                    tvPronunciationTitle.visibility = View.VISIBLE
                    tvPronunciation.visibility = View.VISIBLE
                    view2.visibility = View.VISIBLE
                    view3.visibility = View.VISIBLE
                    view4.visibility = View.VISIBLE
                    fabPlayPronunciation.visibility = View.VISIBLE
                }
            }
            ViewsVisibilityOptions.HIDE -> {
                with(binding) {
                    tvWordEn.visibility = View.INVISIBLE
                    tvWordPt.visibility = View.INVISIBLE
                    tvDefinitionEn.visibility = View.INVISIBLE
                    tvDefinitionPt.visibility = View.INVISIBLE
                    tvSyllablesNumber.visibility = View.INVISIBLE
                    tvSyllables.visibility = View.INVISIBLE
                    tvPronunciationTitle.visibility = View.INVISIBLE
                    tvPronunciation.visibility = View.INVISIBLE
                    view2.visibility = View.INVISIBLE
                    view3.visibility = View.INVISIBLE
                    view4.visibility = View.INVISIBLE
                    fabPlayPronunciation.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun showHideLoading(isLoading: Boolean) {
        val loadingView = binding.loading
        if(isLoading) {
            loadingView.visibility = View.VISIBLE
        } else {
            loadingView.visibility = View.INVISIBLE
        }
    }

    private fun hideKeyboard() {
        val view = currentFocus
        if(view != null) {
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // IF WANT TO KNOW AND DO MORE WITH TTS
    /*override fun onInit(status: Int) {
        if(status == TextToSpeech.SUCCESS) {
            val result = tts.setLanguage(Locale.US)
            if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Language not supported
                // Handle the situation according to your application needs
            }
        } else {
            // TTS initialization failure
            // Handle the situation according to your application needs
        }
    }*/
}