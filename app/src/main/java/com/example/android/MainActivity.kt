package com.example.android

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.android.viewmodels.InsultViewModel

class MainActivity : AppCompatActivity() {

    private val toolbar: Toolbar? by lazy { findViewById<Toolbar?>(R.id.toolbar) }

    private val insultViewModel: InsultViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val btnGenerate: Button = findViewById(R.id.generate_btn)
        val textInsult: TextView = findViewById(R.id.insult_text)
        btnGenerate.setOnClickListener {
            btnGenerate.isVisible = false
            insultViewModel.generateInsult()
        }
        insultViewModel.observe(this) {
            textInsult.text = insultViewModel.insult
            btnGenerate.isVisible = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onDestroy() {
        super.onDestroy()
        insultViewModel.destroy(this)
    }

}