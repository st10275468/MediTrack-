package com.example.meditrack

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import java.util.Locale
/**
 * LocaleHelper.kt
 *
 * This activity handles the logic for changing between 2 languages
 *
 * Reference:
 * OpenAI, 2025. ChatGPT [Computer program]. Version GPT-5 mini. Available at: https://chat.openai.com
 */

object LocaleHelper {
    //Storing selected language
    private const val prefs_Name = "app_prefs"
    private const val key_Language = "app_language"

    //Loads the saved language
    fun applyLocale(context: Context): Context {
        val prefs = context.getSharedPreferences(prefs_Name, Context.MODE_PRIVATE)
        val language = prefs.getString(key_Language, "en") ?: "en"
        return setLocale(context, language)
    }

    //Saves the new language and updates the app resources
    fun setLocale(context: Context, languageCode: String): Context {
        val prefs = context.getSharedPreferences(prefs_Name, Context.MODE_PRIVATE)
        prefs.edit().putString(key_Language, languageCode).apply()

        return updateResources(context, languageCode)
    }

    //Applies the new language to the app
    private fun updateResources(context: Context, language: String) : Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(prefs_Name, Context.MODE_PRIVATE)
        return prefs.getString(key_Language, "en") ?: "en"
    }

    //Reloads the activities
    fun refreshActivity(activity : Activity){
        activity.recreate()
    }

}