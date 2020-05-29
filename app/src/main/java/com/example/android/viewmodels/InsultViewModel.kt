package com.example.android.viewmodels

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import com.example.android.Language
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

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
        val url = URL(urlData)
        var urlConnection: HttpURLConnection? = null
        try {
            urlConnection = url
                .openConnection() as HttpURLConnection
            val `in`: InputStream = urlConnection.inputStream
            val isw = InputStreamReader(`in`)
            var data: Int = isw.read()
            var aux = ""
            while (data != -1) {
                val current = data.toChar()
                data = isw.read()
                aux += current
            }
            insultData.postValue(aux)
        } catch (e: Exception) {

        } finally {
            urlConnection?.disconnect()
        }
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