package com.example

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.json.JSONObject
import java.io.InputStream

object TranslationManager {
    private const val PREFS_NAME = "counter_tracker_prefs"
    private const val KEY_LANG = "selected_language"

    var currentLanguage by mutableStateOf("en")
        private set

    private var translationsJson: JSONObject? = null

    // Supported languages list: Pair of code to Display Name
    val supportedLanguages = listOf(
        "en" to "English",
        "es" to "Español",
        "de" to "Deutsch",
        "ru" to "Русский"
    )

    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedLang = prefs.getString(KEY_LANG, "en") ?: "en"
        setLanguage(context, savedLang)
    }

    fun setLanguage(context: Context, langCode: String) {
        currentLanguage = langCode
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, langCode)
            .apply()

        // Load the translation JSON
        try {
            val fileName = "$langCode.json"
            val inputStream: InputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            val jsonStr = String(buffer, Charsets.UTF_8)
            translationsJson = JSONObject(jsonStr)
        } catch (e: Exception) {
            Log.e("TranslationManager", "Error loading translation: $langCode", e)
            translationsJson = null
        }
    }

    fun getString(key: String, fallback: String): String {
        return try {
            val value = translationsJson?.optString(key, fallback) ?: fallback
            if (value.isEmpty() || value == "null") fallback else value
        } catch (e: Exception) {
            fallback
        }
    }
}
