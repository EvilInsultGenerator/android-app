package com.example.android.viewmodels

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Button
import androidx.lifecycle.*
import com.example.android.MainActivity
import com.example.android.R
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class InsultViewModel : ViewModel() {

    private val insultData: MutableLiveData<String> by lazy { MutableLiveData<String>() }

    val insult: String?
        get() = insultData.value

    fun generateInsult() {
        viewModelScope.launch { connectHttps() }
    }

    private suspend fun connectHttps() = withContext(IO) {
        val url: URL
        var urlConnection: HttpURLConnection? = null
        try {
            url = URL("https://evilinsult.com/generate_insult.php?lang=es")
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
            e.printStackTrace()
            Log.d("daniel", "catch")
        } finally {
            urlConnection?.disconnect()
        }
    }

    fun observe(owner: LifecycleOwner, onUpdate: (String) -> Unit) {
        insultData.observe(owner, Observer { onUpdate(it) })
    }

    fun destroy(owner: LifecycleOwner) {
        insultData.removeObservers(owner)
    }

    override fun onCleared() {
        super.onCleared()
    }

}