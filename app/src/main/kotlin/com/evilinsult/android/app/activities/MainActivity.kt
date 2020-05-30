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
import com.evilinsult.android.app.viewmodels.InsultViewModel
import com.evilinsult.android.app.viewmodels.Language
import com.example.android.R
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
        if (insultViewModel.insult.isNullOrEmpty()) return
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