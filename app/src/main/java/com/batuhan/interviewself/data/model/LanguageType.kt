package com.batuhan.interviewself.data.model

import androidx.annotation.StringRes
import com.batuhan.interviewself.R

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
}