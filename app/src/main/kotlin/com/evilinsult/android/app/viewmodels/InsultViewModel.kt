package com.evilinsult.android.app.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class InsultViewModel(application: Application) : AndroidViewModel(application) {

    private val insultData: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    private val prefs: SharedPreferences by lazy {
        application.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    val insult: String
        get() = insultData.value.orEmpty().trim()

    val currentLanguageCode: String
        get() = prefs.getString(LANGUAGE_PREF_KEY, "en") ?: "en"

    private val insultUrl: String
        get() = "https://evilinsult.com/generate_insult.php?lang=$currentLanguageCode"

    private val insultBackupUrl: String
        get() = "https://slave.evilinsult.com/generate_insult.php?lang=$currentLanguageCode"

    fun generateInsult() {
        viewModelScope.launch {
            try {
                connectHttps(insultUrl)
            } catch (e: Exception) {
                try {
                    connectHttps(insultBackupUrl)
                } catch (e: Exception) {
                }
            }
        }
    }

    private suspend fun connectHttps(url: String) = withContext(IO) {
        val insult: String? = Jsoup.connect(url).get().text()
        insultData.postValue(insult.orEmpty().trim())
    }

    fun setLanguageCode(selectedOption: Int): Boolean {
        val selectedLanguageCode = Language.values()[selectedOption].languageCode
        if (selectedLanguageCode != currentLanguageCode) {
            with(prefs.edit()) {
                putString(LANGUAGE_PREF_KEY, selectedLanguageCode)
                apply()
            }
            return true
        }
        return false
    }

    fun observe(owner: LifecycleOwner, onUpdate: (String) -> Unit) {
        insultData.observe(owner, Observer { onUpdate(it) })
    }

    fun destroy(owner: LifecycleOwner) {
        insultData.removeObservers(owner)
    }

    companion object {
        private const val PREFERENCES_NAME = "evil_insult_generator_prefs"
        private const val LANGUAGE_PREF_KEY = "current_lang"
    }
}