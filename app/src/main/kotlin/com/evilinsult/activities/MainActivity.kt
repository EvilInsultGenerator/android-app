package com.evilinsult.activities

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
import com.evilinsult.R
import com.evilinsult.extensions.isNetworkAvailable
import com.evilinsult.extensions.openLink
import com.evilinsult.extensions.tintMenu
import com.evilinsult.viewmodels.InsultViewModel
import com.evilinsult.viewmodels.Language
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Suppress("RemoveExplicitTypeArguments")
class MainActivity : AppCompatActivity() {

    private val proposalUrl: String by lazy {
        "mailto:marvin@evilinsult.com?subject=" +
                URLEncoder.encode(
                    "Evil\bInsult\bGenerator\bProposal",
                    StandardCharsets.UTF_8.toString()
                ) +
                "&body=" +
                URLEncoder.encode(
                    "Hej\bfuckers,\n\n" +
                            "please\badd\bthis\bbeauty:\n\n" +
                            "Insult: ...\n" +
                            "Language: ...\n" +
                            "Comment\b(optional):...\n\n...",
                    StandardCharsets.UTF_8.toString()
                )
    }

    private val supportUrl: String by lazy {
        "mailto:marvin@evilinsult.com?subject=" +
                URLEncoder.encode(
                    "Evil\bInsult\bGenerator\bContact",
                    StandardCharsets.UTF_8.toString()
                ) +
                "&body=" +
                URLEncoder.encode("Marvin,\bfuck\byou!", StandardCharsets.UTF_8.toString())
    }

    private var alertDialog: AlertDialog? = null
    private val insultViewModel: InsultViewModel by viewModels()

    private val toolbar: Toolbar? by lazy { findViewById<Toolbar?>(R.id.toolbar) }
    private val progressBar: ProgressBar? by lazy { findViewById<ProgressBar?>(R.id.progress_bar) }
    private val insultEditText: EditText? by lazy { findViewById<EditText?>(R.id.insult_text_view) }
    private val generateBtn: Button? by lazy { findViewById<Button?>(R.id.generate_btn) }
    private val shareBtn: Button? by lazy { findViewById<Button?>(R.id.share_btn) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        initListeners()
        generateInsult(true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        toolbar?.tintMenu()
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.translate -> showLanguagePicker()
            R.id.website -> openLink(WEBSITE_URL)
            R.id.legal -> openLink(LEGAL_URL)
            R.id.twitter -> openLink(TWITTER_URL)
            R.id.proposal -> openLink(proposalUrl)
            R.id.support -> openLink(supportUrl)
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        insultViewModel.destroy(this)
        dismissDialog()
    }

    private fun initListeners() {
        insultEditText?.keyListener = null
        shareBtn?.isEnabled = false
        insultViewModel.observe(this, ::showInsult)
        generateBtn?.setOnClickListener { generateInsult() }
        shareBtn?.setOnClickListener { shareInsult() }
    }

    private fun showInsult(insult: String) {
        insultEditText?.setText(insult)
        insultEditText?.isVisible = insult.isNotEmpty()
        progressBar?.isVisible = false
        shareBtn?.isEnabled = insult.isNotEmpty()
        generateBtn?.isEnabled = true
    }

    private fun generateInsult(force: Boolean = false) {
        if (generateBtn?.isEnabled == false && !force) return
        if (!isNetworkAvailable) {
            showInsult(insultViewModel.insult)
            showNetworkErrorDialog()
            return
        }
        generateBtn?.isEnabled = false
        shareBtn?.isEnabled = false
        insultEditText?.isVisible = false
        progressBar?.isVisible = true
        insultViewModel.generateInsult()
    }

    private fun shareInsult() {
        if (shareBtn?.isEnabled == false) return
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

    private fun showLanguagePicker() {
        dismissDialog()
        alertDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.language)
            .setSingleChoiceItems(
                Language.values().map { getString(it.languageId) }.toTypedArray(),
                Language.values().indexOfFirst {
                    it.languageCode == insultViewModel.currentLanguageCode
                }) { _, _ -> }
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                val lw: ListView? = (dialog as? AlertDialog)?.listView
                if ((lw?.checkedItemCount ?: 0) > 0) {
                    val languageSet = insultViewModel.setLanguageCode(lw?.checkedItemPosition ?: -1)
                    if (languageSet) generateInsult(true)
                }
                dismissDialog()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismissDialog() }
            .create()
        alertDialog?.show()
    }

    private fun showNetworkErrorDialog() {
        dismissDialog()
        alertDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.error)
            .setMessage(
                getString(R.string.network_connection_required, getString(R.string.app_name))
            )
            .setPositiveButton(android.R.string.ok) { _, _ -> dismissDialog() }
            .create()
        alertDialog?.show()
    }

    private fun dismissDialog() {
        try {
            alertDialog?.dismiss()
        } catch (_: Exception) {
        }
        alertDialog = null
    }

    companion object {
        private const val WEBSITE_URL = "https://evilinsult.com/"
        private const val LEGAL_URL = "https://evilinsult.com/legal.html"
        private const val TWITTER_URL = "https://twitter.com/__E__I__G__"
    }
}
