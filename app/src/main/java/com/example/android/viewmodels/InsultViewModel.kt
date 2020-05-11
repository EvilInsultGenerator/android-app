package com.example.android.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class InsultViewModel : ViewModel() {

    val insult: MutableLiveData<String> = MutableLiveData()


    fun insultGenerator() {
        val url: URL
        var urlConnection: HttpURLConnection? = null
        try {
            url = URL("https://evilinsult.com/generate_insult.php?lang=es")
            urlConnection = url
                .openConnection() as HttpURLConnection
            val `in`: InputStream = urlConnection.inputStream
            val isw = InputStreamReader(`in`)
            var data: Int = isw.read()
            while (data != -1) {
                val current = data.toChar()
                data = isw.read()
                Log.d("daniel", "Current$current")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("daniel", "catch")
        } finally {
            urlConnection?.disconnect()
            Log.d("daniel", "Disconnect")
        }
    }

//cancion minuto 25, faltando 17 para terminar
}