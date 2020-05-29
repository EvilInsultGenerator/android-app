package com.example.android


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.android.viewmodels.InsultViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import openLink
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    private val toolbar: Toolbar? by lazy { findViewById<Toolbar?>(R.id.toolbar) }

    private val insultViewModel: InsultViewModel by viewModels()

    private var alertDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initListeners()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btn_translate -> {
                showAlertDialogLanguage()
                true
            }
            R.id.legal_item -> {
                openLink(LEGAL)
                true
            }
            R.id.twitter_item -> {
                openLink(TWITTER)
                true
            }
            R.id.proposal_item -> {
                openLink(
                    "mailto:marvin@evilinsult.com" +
                            "?subject=" + URLEncoder.encode(
                        "Evil\bInsult\bGenerator\bProposal",
                        StandardCharsets.UTF_8.toString()
                    ) +
                            "&body=" + URLEncoder.encode(
                        "Hej\bfuckers,\n\n" +
                                "please\badd\bthis\bbeauty:\n\n" +
                                "insult:...\n" +
                                "language:...\n" +
                                "comment\b(optional):...\n\n..."
                        , StandardCharsets.UTF_8.toString()
                    )
                )
                true
            }
            R.id.support_item -> {
                openLink(
                    "mailto:marvin@evilinsult.com" +
                            "?subject=" + URLEncoder.encode(
                        "Evil\bInsult\bGenerator\bContact",
                        StandardCharsets.UTF_8.toString()
                    ) +
                            "&body=" + URLEncoder.encode(
                        "Marvin,\bfuck\byou!",
                        StandardCharsets.UTF_8.toString()
                    )
                )
                true
            }
            R.id.website_item -> {
                openLink(WEBSITE)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        insultViewModel.destroy(this)
        dismissDialog()
    }

    private fun initListeners() {
        val btnGenerate: Button = findViewById(R.id.generate_btn)
        val textInsult: TextView = findViewById(R.id.insult_text)
        val btnShareInsult: Button = findViewById(R.id.share_btn)
        insultViewModel.observe(this) {
            textInsult.text = insultViewModel.insult
            btnGenerate.isEnabled = true
        }
        btnGenerate.setOnClickListener {
            btnGenerate.isEnabled = false
            insultViewModel.generateInsult()
        }
        /*
        *  Intent share=new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            String shareBody=txtSubject.getText().toString()+"\n\nhttps://evilinsult.com/";
            String shareSubject=txtSubject.getText().toString();
            share.putExtra(Intent.EXTRA_SUBJECT,shareSubject);
            share.putExtra(Intent.EXTRA_TEXT,shareBody);
            startActivity(Intent.createChooser(share,"Share using"));
        * */
        btnShareInsult.setOnClickListener {
            if (!insultViewModel.insult.isNullOrEmpty()) {
                val share: Intent = Intent(Intent.ACTION_SEND)
                share.type = "text/plain"
                share.putExtra(
                    Intent.EXTRA_SUBJECT,
                    insultViewModel.insult + "\n\nhttps://evilinsult.com/"
                );
                share.putExtra(Intent.EXTRA_TEXT, insultViewModel.insult)
                startActivity(Intent.createChooser(share, "Share using"))
            } else
                Toast.makeText(this, "Please, you generate a insult", Toast.LENGTH_LONG).show()
        }
    }

    private fun showAlertDialogLanguage() {
        dismissDialog()
        alertDialog = MaterialAlertDialogBuilder(this).setTitle("LANGUAGE")
            .setSingleChoiceItems(
                Language.values().map { getString(it.languageId) }.toTypedArray(),
                Language.values().indexOfFirst { it ->
                    it.languageCode == insultViewModel.currentLanguageCode
                },
                DialogInterface.OnClickListener { dialog, which -> })
            .setPositiveButton(R.string.ok) { dialog, which ->
                val lw: ListView =
                    (dialog as AlertDialog).listView
                if (lw.checkedItemCount > 0) {
                    insultViewModel.setPreference(lw.checkedItemPosition)
                }
            }
            .setNeutralButton(R.string.cancel) { dialog, which ->
            }
            .show()
    }

    private fun dismissDialog() {
        try {
            alertDialog?.dismiss()
        } catch (e: Exception) {
        }
        alertDialog = null
    }

    companion object {
        private const val TWITTER = "https://twitter.com/__E__I__G__"
        private const val WEBSITE = "https://evilinsult.com/"
        private const val LEGAL = "https://evilinsult.com/legal.html"
    }

}