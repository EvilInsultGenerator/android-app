package com.evilinsult.android.app.activities

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import com.evilinsult.android.app.R
import com.evilinsult.android.app.extensions.openLink
import com.evilinsult.android.app.viewmodels.InsultViewModel
import com.evilinsult.android.app.viewmodels.Language
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : AppCompatActivity() {

    private val toolbar: Toolbar? by lazy { findViewById(R.id.toolbar) }
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
        when (item.itemId) {
            R.id.translate -> showAlertDialogLanguage()
            R.id.website -> openLink(WEBSITE_URL)
            R.id.legal -> openLink(LEGAL_URL)
            R.id.twitter -> openLink(TWITTER_URL)
            R.id.proposal -> {
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
            }
            R.id.support -> {
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
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        insultViewModel.destroy(this)
        dismissDialog()
    }

    private fun initListeners() {
        val btnGenerate: Button = findViewById(R.id.generate_btn)
        val insultEditText: EditText = findViewById(R.id.insult_text)
        val btnShareInsult: Button = findViewById(R.id.share_btn)
        val insultProgress: ProgressBar = findViewById(R.id.insult_progress)
        insultEditText.keyListener = null
        btnShareInsult.isEnabled = true
        insultViewModel.observe(this) {
            insultEditText.setText(insultViewModel.insult)
            btnGenerate.isEnabled = true
            insultProgress.visibility = ProgressBar.GONE
            insultEditText.isVisible = true
        }
        btnGenerate.setOnClickListener {
            btnGenerate.isEnabled = false
            btnShareInsult.isEnabled = false
            insultEditText.isVisible = false
            insultProgress.visibility = ProgressBar.VISIBLE
            insultViewModel.generateInsult()
        }
        btnShareInsult.setOnClickListener {
            shareListener()
        }
    }

    private fun shareListener() {
        if (insultViewModel.insult.isEmpty()) return
        val share = Intent(Intent.ACTION_SEND)
        share.type = "text/plain"
        share.putExtra(
            Intent.EXTRA_SUBJECT,
            insultViewModel.insult + "\n\nhttps://evilinsult.com/"
        )
        share.putExtra(Intent.EXTRA_TEXT, insultViewModel.insult)
        startActivity(Intent.createChooser(share, "Share using"))
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
            .setPositiveButton(android.R.string.ok) { dialog, which ->
                val lw: ListView =
                    (dialog as AlertDialog).listView
                if (lw.checkedItemCount > 0) {
                    insultViewModel.setPreference(lw.checkedItemPosition)
                }
            }
            .setNeutralButton(android.R.string.cancel) { dialog, which ->
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
        private const val WEBSITE_URL = "https://evilinsult.com/"
        private const val LEGAL_URL = "https://evilinsult.com/legal.html"
        private const val TWITTER_URL = "https://twitter.com/__E__I__G__"
    }

}