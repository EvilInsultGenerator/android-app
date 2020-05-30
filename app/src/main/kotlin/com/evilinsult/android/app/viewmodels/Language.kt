package com.evilinsult.android.app.viewmodels

import androidx.annotation.StringRes
import com.example.android.R

enum class Language(@StringRes val languageId: Int, val languageCode: String) {
    ENGLISH(R.string.english, "en"),
    SPANISH(R.string.spanish, "es"),
    CHINESE(R.string.chinese,"dv"),
    HINDI(R.string.hindi,"ik"),
    ARABIC(R.string.arabic,"ar"),
    PORTUGUESE(R.string.portuguese,"pt"),
    BENGALI(R.string.bengali,"bn"),
    RUSSIAN(R.string.russian,"ru"),
    JAPANESE(R.string.japanese,"ja"),
    JAVANESE(R.string.javanese,"jv"),
    SWAHILI(R.string.swahili,"sw"),
    GERMAN(R.string.german,"de"),
    KOREAN(R.string.korean,"ko"),
    FRENCH(R.string.french,"fr"),
    TELUGU(R.string.telugu,"te"),
    MARATHI(R.string.marathi,"mr"),
    TURKISH(R.string.turkish,"tr"),
    TAMIL(R.string.tamil,"ta"),
    VIETNAMESE(R.string.vietnamese,"vi"),
    URDU(R.string.urdu,"ur"),
    GREEK(R.string.greek,"el"),
    ITALIAN(R.string.italian,"it"),
    CZECH(R.string.czech,"cs"),
    LATIN(R.string.latin,"la"),
}