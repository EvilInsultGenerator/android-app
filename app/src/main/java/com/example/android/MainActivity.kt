package com.example.android


import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.android.viewmodels.InsultViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MainActivity : AppCompatActivity() {

    private val toolbar: Toolbar? by lazy { findViewById<Toolbar?>(R.id.toolbar) }

    private val insultViewModel: InsultViewModel by viewModels()

    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val btnGenerate: Button = findViewById(R.id.generate_btn)
        val textInsult: TextView = findViewById(R.id.insult_text)
        btnGenerate.setOnClickListener {
            btnGenerate.isEnabled = false
            insultViewModel.generateInsult()
        }
        insultViewModel.observe(this) {
            textInsult.text = insultViewModel.insult
            btnGenerate.isEnabled = true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_translate -> {
                /*   for (userEnum in Language.values()) {
                    Log.d("daniel", userEnum.name)
                }*/
                alertDialog = MaterialAlertDialogBuilder(this).setTitle("LANGUAGE")
                    .setItems(
                        //arrayOf(Language.values()).map { it[] }.toTypedArray(),
                        DialogInterface.OnClickListener { dialog, which ->

                        })
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        insultViewModel.destroy(this)
    }

}