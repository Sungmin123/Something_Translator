package com.example.somethingtranslator

import android.annotation.SuppressLint
import android.util.Log
import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.*
import com.example.somethingtranslator.databinding.ActivityHomeBinding
import com.example.somethingtranslator.databinding.ActivityMainBinding
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.*
import org.w3c.dom.Text
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.log

class MainActivity : AppCompatActivity()  {

    private var currentSource = "No language was selected"
    private var currentLanguage = "No language was selected"
    private lateinit var sourceLanguage: String
    private lateinit var targetLanguage: String
    val modelManager = RemoteModelManager.getInstance()
    private lateinit var currentTranslator: Translator
    lateinit var binding: ActivityMainBinding
    lateinit var languageSource: Spinner
    lateinit var languageOptions: Spinner
    lateinit var speakVoice: TextToSpeech

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        speakVoice = TextToSpeech(this, TextToSpeech.OnInitListener { status ->
            if (status != TextToSpeech.ERROR) {
                speakVoice.language = Locale.ENGLISH
            }
        })

        val homeButton: Button = findViewById(R.id.home_button)
        homeButton.setOnClickListener {
            startActivity(Intent(this, ActivityHomeBinding::class.java))
        }

        binding.switchButton.setOnClickListener() {
            startActivity(Intent(this, MainActivity2::class.java))
        }

        languageSource = binding.languageSource
        languageOptions = binding.languageOptions
        val languageList = arrayListOf("Select a Language", "Afrikaans", "Albanian", "Arabic",
                "Belarusian", "Bengali", "Bulgarian", "Catalan", "Chinese", "Croatian", "Czech",
                "Danish", "Dutch", "English", "Esperanto", "Estonian", "Finnish", "French", "Galician",
                "Georgian", "German", "Greek", "Gujarati", "Haitian", "Hebrew", "Hindi",
                "Hungarian", "Icelandic", "Indonesian", "Irish", "Italian", "Japanese", "Kannada",
                "Korean", "Latvian", "Lithuanian", "Macedonian", "Malay", "Maltese", "Marathi",
                "Norwegian", "Persian", "Polish", "Portuguese", "Romanian", "Russian", "Slovak",
                "Slovenian", "Spanish", "Swahili", "Swedish", "Tagalog", "Tamil", "Telugu", "Thai",
                "Turkish", "Ukrainian", "Urdu", "Vietnamese", "Welsh")

        languageSource.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languageList)
        languageSource.onItemSelectedListener = object :

                AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //binding.languageText.text = "Select a Language"
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //binding.languageText.text = languageList[position]
                if (languageList[position].equals("Select a Language")) {
                    currentSource = "No language was selected"
                } else {
                    currentSource = languageList[position]
                }
            }
        }

        languageOptions.adapter = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languageList)
        languageOptions.onItemSelectedListener = object :

                AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //binding.languageText.text = "Select a Language"
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //binding.languageText.text = languageList[position]
                if (languageList[position].equals("Select a Language")) {
                    currentLanguage = "No language was selected"
                } else {
                    currentLanguage = languageList[position]
                }
            }
        }

        val speakButton: Button = findViewById(R.id.speak_button)
        val outputText: TextView = findViewById(R.id.output_text)
        speakButton.setOnClickListener() {
            val toSpeak = outputText.text
            Toast.makeText(this, toSpeak.toString(), Toast.LENGTH_SHORT).show()
            speakVoice.speak(toSpeak.toString(), TextToSpeech.QUEUE_FLUSH, null)
        }

        binding.voiceButton.setOnClickListener() {
            speech()
        }

        binding.translateButton.setOnClickListener() {
            if (currentLanguage != "No language was selected" && currentSource !=
                    "No language was selected") {
                sourceLanguage = getLanguage(currentSource)
                targetLanguage = getLanguage(currentLanguage)

                currentTranslator = prepareModel(sourceLanguage, targetLanguage)
                currentTranslator.downloadModelIfNeeded()
                        .addOnSuccessListener {
                            currentTranslator.translate(binding.inputText.text as String)
                                    .addOnSuccessListener { translatedText ->
                                        binding.outputText.text = translatedText
                                    }
                                    .addOnFailureListener {
                                        binding.outputText.text = "Model not translated"
                                    }
                        }
                        .addOnFailureListener { exception ->
                            binding.outputText.text = exception.toString()
                        }
            }
        }

        binding.clearButton.setOnClickListener() {
            binding.inputText.text = ""
            binding.outputText.text = ""
        }
    }

    private fun getLocale(language: String): Locale {
        if (language == "Korean") {
            return Locale.KOREAN
        } else if (language == "Chinese") {
            return Locale.CHINESE
        } else if (language == "English") {
            return Locale.ENGLISH
        } else if (language == "French") {
            return Locale.FRENCH
        } else if (language == "German") {
            return Locale.GERMAN
        } else if (language == "Italian") {
            return Locale.ITALIAN
        } else if (language == "Japanese") {
            return Locale.JAPANESE
        } else {
            Toast.makeText(this, "Unsupported language", Toast.LENGTH_SHORT).show()
            return Locale.getDefault()
        }
    }

    private fun speech() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLocale(currentSource))

        try {
            startActivityForResult(intent, 10)

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 10) {
            if (resultCode == RESULT_OK && data != null) {
                val list = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (list != null) {
                    binding.inputText.text = list.get(0)
                }
            }
        }

    }

    private fun getLanguage(language: String): String {
        if (language == "Afrikaans") {
            return TranslateLanguage.AFRIKAANS
        } else if (language == "Arabic") {
            return TranslateLanguage.ARABIC
        } else if (language == "Belarusian") {
            return TranslateLanguage.BELARUSIAN
        } else if (language == "Bulgarian") {
            return TranslateLanguage.BULGARIAN
        } else if (language == "Bengali") {
            return TranslateLanguage.BENGALI
        } else if (language == "Catalan") {
            return TranslateLanguage.CATALAN
        } else if (language == "Czech") {
            return TranslateLanguage.CZECH
        } else if (language == "Welsh") {
            return TranslateLanguage.WELSH
        } else if (language == "Danish") {
            return TranslateLanguage.DANISH
        } else if (language == "German") {
            return TranslateLanguage.GERMAN
        } else if (language == "Greek") {
            return TranslateLanguage.GREEK
        } else if (language == "English") {
            return TranslateLanguage.ENGLISH
        } else if (language == "Esperanto") {
            return TranslateLanguage.ESPERANTO
        } else if (language == "Spanish") {
            return TranslateLanguage.SPANISH
        } else if (language == "Estonian") {
            return TranslateLanguage.ESTONIAN
        } else if (language == "Persian") {
            return TranslateLanguage.PERSIAN
        } else if (language == "Finnish") {
            return TranslateLanguage.FINNISH
        } else if (language == "French") {
            return TranslateLanguage.FRENCH
        } else if (language == "Irish") {
            return TranslateLanguage.IRISH
        } else if (language == "Galician") {
            return TranslateLanguage.GALICIAN
        } else if (language == "Gujarati") {
            return TranslateLanguage.GUJARATI
        } else if (language == "Hebrew") {
            return TranslateLanguage.HEBREW
        } else if (language == "Hindi") {
            return TranslateLanguage.HINDI
        } else if (language == "Croatian") {
            return TranslateLanguage.CROATIAN
        } else if (language == "Haitian") {
            return TranslateLanguage.HAITIAN_CREOLE
        } else if (language == "Hungarian") {
            return TranslateLanguage.HUNGARIAN
        } else if (language == "Indonesian") {
            return TranslateLanguage.INDONESIAN
        } else if (language == "Icelandic") {
            return TranslateLanguage.ICELANDIC
        } else if (language == "Italian") {
            return TranslateLanguage.ITALIAN
        } else if (language == "Japanese") {
            return TranslateLanguage.JAPANESE
        } else if (language == "Georgian") {
            return TranslateLanguage.GEORGIAN
        } else if (language == "Kannada") {
            return TranslateLanguage.KANNADA
        } else if (language == "Korean") {
            return TranslateLanguage.KOREAN
        } else if (language == "Lithuanian") {
            return TranslateLanguage.LITHUANIAN
        } else if (language == "Latvian") {
            return TranslateLanguage.LATVIAN
        } else if (language == "Macedonian") {
            return TranslateLanguage.MACEDONIAN
        } else if (language == "Marathi") {
            return TranslateLanguage.MARATHI
        } else if (language == "Malay") {
            return TranslateLanguage.MALAY
        } else if (language == "Maltese") {
            return TranslateLanguage.MALTESE
        } else if (language == "Dutch") {
            return TranslateLanguage.DUTCH
        } else if (language == "Norwegian") {
            return TranslateLanguage.NORWEGIAN
        } else if (language == "Polish") {
            return TranslateLanguage.POLISH
        } else if (language == "Portuguese") {
            return TranslateLanguage.PORTUGUESE
        } else if (language == "Romanian") {
            return TranslateLanguage.ROMANIAN
        } else if (language == "Russian") {
            return TranslateLanguage.RUSSIAN
        } else if (language == "Slovak") {
            return TranslateLanguage.SLOVAK
        } else if (language == "Slovenian") {
            return TranslateLanguage.SLOVENIAN
        } else if (language == "Albanian") {
            return TranslateLanguage.ALBANIAN
        } else if (language == "Swedish") {
            return TranslateLanguage.SWEDISH
        } else if (language == "Swahili") {
            return TranslateLanguage.SWAHILI
        } else if (language == "Tamil") {
            return TranslateLanguage.TAMIL
        } else if (language == "Telugu") {
            return TranslateLanguage.TELUGU
        } else if (language == "Thai") {
            return TranslateLanguage.THAI
        } else if (language == "Tagalog") {
            return TranslateLanguage.TAGALOG
        } else if (language == "Turkish") {
            return TranslateLanguage.TURKISH
        } else if (language == "Ukrainian") {
            return TranslateLanguage.UKRAINIAN
        } else if (language == "Urdu") {
            return TranslateLanguage.URDU
        } else if (language == "Vietnamese") {
            return TranslateLanguage.VIETNAMESE
        } else {
            return TranslateLanguage.CHINESE
        }

    }

    private fun prepareModel(source: String, target: String): Translator {
        val options = TranslatorOptions.Builder()
                .setSourceLanguage(source)
                .setTargetLanguage(target)
                .build()

        return Translation.getClient(options)

    }

}

