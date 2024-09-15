package com.batuhan.interv.data.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.batuhan.interv.R

@Keep
enum class LanguageType(
    @StringRes val text: Int,
    val code: String,
) {
    EN(R.string.language_type_en, "en-US"),
    TR(
        R.string.language_type_tr,
        "tr-TR",
    ),
    FR(R.string.language_type_fr, "fr-FR"),
    DE(R.string.settings_lang_option_four, "de-DE")
}