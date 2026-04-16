package com.evilinsult.activities

import android.Manifest
import android.app.AlarmManager
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.evilinsult.R
import com.evilinsult.extensions.AlarmScheduler
import com.evilinsult.extensions.isNetworkAvailable
import com.evilinsult.extensions.openLink
import com.evilinsult.extensions.tintMenu
import com.evilinsult.viewmodels.Language
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Calendar

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
    private var isScrollable: Boolean = false
    private val insultViewModel: InsultViewModel by viewModels()

    private val toolbar: Toolbar? by lazy { findViewById<Toolbar?>(R.id.toolbar) }
    private val scrollView: ScrollView? by lazy { findViewById<ScrollView?>(R.id.scroll_view) }
    private val frameLayout: FrameLayout? by lazy { findViewById<FrameLayout?>(R.id.frame_layout) }
    private val progressBar: ProgressBar? by lazy { findViewById<ProgressBar?>(R.id.progress_bar) }
    private val insultEditText: EditText? by lazy { findViewById<EditText?>(R.id.insult_text_view) }
    private val generateBtn: Button? by lazy { findViewById<Button?>(R.id.generate_btn) }
    private val shareBtn: Button? by lazy { findViewById<Button?>(R.id.share_btn) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.root_coordinator)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
        setSupportActionBar(toolbar)
        initListeners()
        generateInsult(true)
    }




    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showDailyInsultDialog()   // permission granted — now open dialog
        } else {
            Toast.makeText(
                this,
                getString(R.string.notification_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
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
            R.id.notification -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(
                        this, Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    showDailyInsultDialog()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        // Re-schedule if user just granted exact alarm permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (alarmManager.canScheduleExactAlarms()) {
                val prefs = getSharedPreferences("DailyInsultPrefs", Context.MODE_PRIVATE)
                val enabled = prefs.getBoolean("enabled", false)
                if (enabled) {
                    val rhythm     = prefs.getString("rhythm", "daily") ?: "daily"
                    val hour       = prefs.getInt("hour", 0)
                    val minute     = prefs.getInt("minute", 0)
                    val dayOfWeek  = prefs.getInt("dayOfWeek", Calendar.MONDAY)
                    val dayOfMonth = prefs.getInt("dayOfMonth", 1)
                    val langCode   = prefs.getString("languageCode", "") ?: ""
                    AlarmScheduler.schedule(this, rhythm, hour, minute, dayOfWeek, dayOfMonth, langCode)
                }
            }
        }
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
        checkScrollable()
        insultEditText?.isVisible = insult.isNotEmpty()
        progressBar?.isVisible = false
        shareBtn?.isEnabled = insult.isNotEmpty()
        generateBtn?.isEnabled = true
        saveInsultToLocalStorage(insult) // Save insult to local storage
        updateWidgetWithNewInsult() // Update the widget with the new insult
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

    private fun saveInsultToLocalStorage(insult: String) {
        val sharedPreferences = getSharedPreferences("InsultPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("lastInsult", insult)
        editor.apply() // Save the insult to local storage
    }

    private fun saveDailyInsultPrefs(
        enabled: Boolean,
        rhythm: String,
        hour: Int,
        minute: Int,
        dayOfWeek: Int,
        dayOfMonth: Int,
        languageCode: String
    ) {
        getSharedPreferences("DailyInsultPrefs", Context.MODE_PRIVATE).edit().apply {
            putBoolean("enabled", enabled)
            putString("rhythm", rhythm)
            putInt("hour", hour)
            putInt("minute", minute)
            putInt("dayOfWeek", dayOfWeek)
            putInt("dayOfMonth", dayOfMonth)
            putString("languageCode", languageCode)
            apply()
        }
    }

    private fun loadDailyInsultPrefs(): Bundle {
        val prefs = getSharedPreferences("DailyInsultPrefs", Context.MODE_PRIVATE)
        return Bundle().apply {
            putBoolean("enabled", prefs.getBoolean("enabled", false))
            putString("rhythm", prefs.getString("rhythm", "daily"))
            putInt("hour", prefs.getInt("hour", 0))
            putInt("minute", prefs.getInt("minute", 0))
            putInt("dayOfWeek", prefs.getInt("dayOfWeek", 2))
            putInt("dayOfMonth", prefs.getInt("dayOfMonth", 1))
            putString("languageCode", prefs.getString("languageCode", ""))
        }
    }

    private fun showDailyInsultDialog() {
        dismissDialog()
        val prefs = loadDailyInsultPrefs()

        var isEnabled = prefs.getBoolean("enabled", false)
        var rhythm = prefs.getString("rhythm", "daily") ?: "daily"
        var hour = prefs.getInt("hour", 0)
        var minute = prefs.getInt("minute", 0)
        var dayOfWeek = prefs.getInt("dayOfWeek", Calendar.MONDAY)
        var dayOfMonth = prefs.getInt("dayOfMonth", 1)
        var languageCode = prefs.getString("languageCode", "") ?: ""

        val view = layoutInflater.inflate(R.layout.dialog_daily_insult, null)

        val statusSwitch = view.findViewById<SwitchMaterial>(R.id.switch_status)
        val rhythmGroup = view.findViewById<LinearLayout>(R.id.group_rhythm)
        val languageGroup = view.findViewById<LinearLayout>(R.id.group_language)

        statusSwitch.isChecked = isEnabled
        rhythmGroup.isVisible = isEnabled
        languageGroup.isVisible = isEnabled

        statusSwitch.setOnCheckedChangeListener { _, checked ->
            isEnabled = checked
            rhythmGroup.isVisible = checked
            languageGroup.isVisible = checked
        }

        // ── Rhythm ───────────────────────────────────────────────
        val radioGroup = view.findViewById<RadioGroup>(R.id.radio_rhythm)
        val weeklyOptions = view.findViewById<LinearLayout>(R.id.layout_weekly_options)
        val monthlyOptions = view.findViewById<LinearLayout>(R.id.layout_monthly_options)
        val btnTimePicker = view.findViewById<Button>(R.id.btn_time)
        val btnWeekDay = view.findViewById<Button>(R.id.btn_weekday)
        val btnMonthDay = view.findViewById<Button>(R.id.btn_monthday)

        fun updateTimeButton() {
            btnTimePicker.text = String.format("%02d:%02d", hour, minute)
        }
        updateTimeButton()

        when (rhythm) {
            "daily" -> radioGroup.check(R.id.radio_daily)
            "weekly" -> radioGroup.check(R.id.radio_weekly)
            "monthly" -> radioGroup.check(R.id.radio_monthly)
        }
        weeklyOptions.isVisible = rhythm == "weekly"
        monthlyOptions.isVisible = rhythm == "monthly"

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            rhythm = when (checkedId) {
                R.id.radio_daily -> "daily"
                R.id.radio_weekly -> "weekly"
                R.id.radio_monthly -> "monthly"
                else -> "daily"
            }
            weeklyOptions.isVisible = rhythm == "weekly"
            monthlyOptions.isVisible = rhythm == "monthly"
        }

        btnTimePicker.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                hour = h; minute = m
                updateTimeButton()
            }, hour, minute, true).show()
        }

        val daysOfWeek = arrayOf(
            getString(R.string.mon), getString(R.string.tue), getString(R.string.wed),
            getString(R.string.thu), getString(R.string.fri), getString(R.string.sat),
            getString(R.string.sun)
        )
        fun calendarDayToIndex(cal: Int): Int = when (cal) {
            Calendar.MONDAY -> 0; Calendar.TUESDAY -> 1; Calendar.WEDNESDAY -> 2
            Calendar.THURSDAY -> 3; Calendar.FRIDAY -> 4; Calendar.SATURDAY -> 5
            else -> 6 // SUNDAY
        }
        fun indexToCalendarDay(index: Int): Int = when (index) {
            0 -> Calendar.MONDAY; 1 -> Calendar.TUESDAY; 2 -> Calendar.WEDNESDAY
            3 -> Calendar.THURSDAY; 4 -> Calendar.FRIDAY; 5 -> Calendar.SATURDAY
            else -> Calendar.SUNDAY
        }

        btnWeekDay.text = daysOfWeek[calendarDayToIndex(dayOfWeek)]
        btnWeekDay.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_day))
                .setItems(daysOfWeek) { _, which ->
                    dayOfWeek = indexToCalendarDay(which)
                    btnWeekDay.text = daysOfWeek[which]
                }.show()
        }

        // Day of month
        val daysOfMonth = (1..31).map { it.toString() }.toTypedArray()
        btnMonthDay.text = dayOfMonth.toString()
        btnMonthDay.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.select_day_of_month))
                .setItems(daysOfMonth) { _, which ->
                    dayOfMonth = which + 1
                    btnMonthDay.text = dayOfMonth.toString()
                }.show()
        }

        // ── Language ─────────────────────────────────────────────
        val languageSpinner = view.findViewById<Spinner>(R.id.spinner_language)
        val languageItems = listOf(getString(R.string.app_language)) +
                Language.entries.map { getString(it.languageId) }
        val languageCodes = listOf("") +
                Language.entries.map { it.languageCode }

        languageSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            languageItems
        )
        languageSpinner.setSelection(
            languageCodes.indexOf(languageCode).takeIf { it >= 0 } ?: 0
        )
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                languageCode = languageCodes[pos]
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }

        // ── Build dialog ─────────────────────────────────────────
        alertDialog = MaterialAlertDialogBuilder(this)
            .setTitle(R.string.daily_insult)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                saveDailyInsultPrefs(
                    isEnabled, rhythm, hour, minute,
                    dayOfWeek, dayOfMonth, languageCode
                )
                if (isEnabled) scheduleDailyInsult(
                    rhythm, hour, minute, dayOfWeek, dayOfMonth, languageCode
                ) else cancelDailyInsult()
                dismissDialog()
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismissDialog() }
            .create()
        alertDialog?.show()
    }


    private fun scheduleDailyInsult(
        rhythm: String,
        hour: Int,
        minute: Int,
        dayOfWeek: Int,
        dayOfMonth: Int,
        languageCode: String
    ) {
        val resolvedLang = languageCode.ifEmpty {
            insultViewModel.currentLanguageCode
        }
        AlarmScheduler.schedule(this, rhythm, hour, minute, dayOfWeek, dayOfMonth, resolvedLang)
    }

    private fun cancelDailyInsult() {
        AlarmScheduler.cancel(this)
    }
    private fun updateWidgetWithNewInsult() {
        val intent = Intent(this, InsultWidgetProvider::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(application, InsultWidgetProvider::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent) // Notify the widget to update
    }
        private fun checkScrollable() {
            val listener = object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    val height = scrollView?.height ?: return
                    val childHeight = scrollView?.getChildAt(0)?.height ?: return

                    val isScrollable = height < childHeight
                    val layoutParams = frameLayout?.layoutParams as? FrameLayout.LayoutParams

                    if (!isScrollable) {
                        layoutParams?.gravity = Gravity.CENTER
                    } else {
                        layoutParams?.gravity = Gravity.NO_GRAVITY
                    }
                    frameLayout?.layoutParams = layoutParams

                    scrollView?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
                }
            }

            scrollView?.viewTreeObserver?.addOnGlobalLayoutListener(listener)

        }
    companion object {
        private const val WEBSITE_URL = "https://evilinsult.com/"
        private const val LEGAL_URL = "https://evilinsult.com/legal.html"
        private const val TWITTER_URL = "https://twitter.com/__E__I__G__"
    }
}

