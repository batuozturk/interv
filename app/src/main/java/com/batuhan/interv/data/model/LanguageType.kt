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
    DE(R.string.language_type_de, "de-DE"),
    ES(R.string.language_type_es, "es-ES"),
    PL(R.string.language_type_pl, "pl-PL"),
    AR(R.string.language_type_ar, "ar-AR")
}