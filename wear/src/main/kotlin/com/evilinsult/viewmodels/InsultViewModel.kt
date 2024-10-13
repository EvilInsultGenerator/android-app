package com.evilinsult.viewmodels

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.math.roundToInt

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
        get() = "https://www.evilinsult.com/generate_insult.php?lang=$currentLanguageCode"

    private val insultBackupUrl: String
        get() = "https://slave.evilinsult.com/generate_insult.php?lang=$currentLanguageCode"

    fun generateInsult() {
        viewModelScope.launch {
            try {
                connectHttps(insultUrl)
            } catch (e: Exception) {
                e.printStackTrace()
                try {
                    connectHttps(insultBackupUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
                    insultData.postValue("")
                }
            }
        }
    }

    private fun getSSLFactory(): SSLSocketFactory {
        val trustAllCerts = arrayOf<TrustManager>(
            @SuppressLint("CustomX509TrustManager")
            object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                override fun checkClientTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                override fun checkServerTrusted(
                    chain: Array<X509Certificate?>?,
                    authType: String?
                ) {
                }

                override fun getAcceptedIssuers(): Array<X509Certificate?> = arrayOf()
            }
        )

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())
        return sslContext.socketFactory
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun connectHttps(url: String) = withContext(IO) {
        val doc: Document? = Jsoup.connect(url)
            .timeout((TIMEOUT_IN_SECONDS * 1000).roundToInt())
            .sslSocketFactory(getSSLFactory())
            .get()
        doc?.let {
            insultData.postValue(it.text().orEmpty().trim())
        } ?: run {
            throw Exception("Unexpected response exception")
        }
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
        insultData.observe(owner) { onUpdate(it) }
    }

    fun destroy(owner: LifecycleOwner) {
        insultData.removeObservers(owner)
    }

    companion object {
        private const val PREFERENCES_NAME = "evil_insult_generator_prefs"
        private const val LANGUAGE_PREF_KEY = "current_lang"
        private const val TIMEOUT_IN_SECONDS: Double = 10.0
    }
}
