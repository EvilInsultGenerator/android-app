package com.evilinsult.android.app.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class InsultViewModel(application: Application) : AndroidViewModel(application) {

    private val insultData: MutableLiveData<String> by lazy { MutableLiveData<String>() }
    val insult: String?
        get() = insultData.value

    private val prefs: SharedPreferences by lazy {
        application.getSharedPreferences(
            LANGUAGE_KEY,
            Context.MODE_PRIVATE
        )
    }

    val currentLanguageCode: String
        get() = prefs.getString(LANGUAGE_KEY, "en") ?: "en"

    private val insultUrl: String
        get() = "https://evilinsult.com/generate_insult.php?lang=$currentLanguageCode"

    private val insultBackupUrl: String
        get() = "https://slave.evilinsult.com/generate_insult.php?lang=$currentLanguageCode"

    fun generateInsult() {
        viewModelScope.launch {
            try {
                connectHttps(insultUrl)
            } catch (e: Exception) {
                connectHttps(insultBackupUrl)
            }
        }
    }

    private suspend fun connectHttps(urlData: String) = withContext(IO) {
        val insult: String? = Jsoup.connect(urlData).get().text()
        insultData.postValue(insult.orEmpty())
    }

    fun setPreference(selectedOption: Int) {
        if (Language.values()[selectedOption].languageCode != currentLanguageCode) {
            with(prefs.edit()) {
                putString(LANGUAGE_KEY, Language.values()[selectedOption].languageCode)
                apply()
            }
            generateInsult()
        }
    }

    fun observe(owner: LifecycleOwner, onUpdate: (String) -> Unit) {
        insultData.observe(owner, Observer { onUpdate(it) })
    }

    fun destroy(owner: LifecycleOwner) {
        insultData.removeObservers(owner)
    }

    companion object {
        const val LANGUAGE_KEY = "LANGUAGES"
    }

}