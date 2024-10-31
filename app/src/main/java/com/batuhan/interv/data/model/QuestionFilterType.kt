package com.batuhan.interv.data.model

import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.batuhan.interv.R

@Keep
enum class QuestionFilterType(
    @StringRes val title: Int,
) {
    DEFAULT(R.string.filter_default),
    NAME(R.string.filter_name),
    NAME_REVERSED(R.string.filter_name_reversed),
    LANG_EN(R.string.filter_english),
    LANG_TR(
        R.string.filter_turkish,
    ),
    LANG_FR(R.string.filter_french),
    LANG_DE(R.string.filter_german),
    LANG_ES(R.string.filter_spanish),
    LANG_PL(R.string.filter_polish),
    LANG_AR(R.string.filter_arabic),
    LANG_IT(R.string.filter_italian),
    LANG_NO(R.string.filter_norwegian),
    LANG_DA(R.string.filter_danish),
    LANG_SV(R.string.filter_swedish),
    LANG_NL(R.string.filter_dutch),
}

fun QuestionFilterType.createQuery(searchText: String): String{
    var string = "SELECT * FROM question "
    val stringIsValid = searchText.isNotEmpty() && searchText.isNotBlank()
    if(stringIsValid) string += "WHERE question LIKE ? "
    string +=
        when(this){
            QuestionFilterType.NAME -> "ORDER BY question ASC"
            QuestionFilterType.NAME_REVERSED -> "ORDER BY question DESC"
            QuestionFilterType.LANG_EN -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'en-US'"
            QuestionFilterType.LANG_TR -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'tr-TR'"
            QuestionFilterType.LANG_FR -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'fr-FR'"
            QuestionFilterType.LANG_DE -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'de-DE'"
            QuestionFilterType.LANG_ES -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'es-ES'"
            QuestionFilterType.LANG_PL -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'pl-PL'"
            QuestionFilterType.LANG_AR -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'ar-AR'"
            QuestionFilterType.LANG_IT -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'it-IT'"
            QuestionFilterType.LANG_NO -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'no-NO'"
            QuestionFilterType.LANG_DA -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'da-DK'"
            QuestionFilterType.LANG_SV -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'sv-SE'"
            QuestionFilterType.LANG_NL -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'nl-NL'"
            QuestionFilterType.DEFAULT -> ""
        }

    return string
}

fun findLanguageFilterType(language: String?): QuestionFilterType{
    return when(language){
        LanguageType.EN.code -> QuestionFilterType.LANG_EN
        LanguageType.TR.code -> QuestionFilterType.LANG_TR
        LanguageType.FR.code -> QuestionFilterType.LANG_FR
        LanguageType.DE.code -> QuestionFilterType.LANG_DE
        LanguageType.ES.code -> QuestionFilterType.LANG_ES
        LanguageType.PL.code -> QuestionFilterType.LANG_PL
        LanguageType.AR.code -> QuestionFilterType.LANG_AR
        LanguageType.IT.code -> QuestionFilterType.LANG_IT
        LanguageType.NO.code -> QuestionFilterType.LANG_NO
        LanguageType.DA.code -> QuestionFilterType.LANG_DA
        LanguageType.SV.code -> QuestionFilterType.LANG_SV
        LanguageType.NL.code -> QuestionFilterType.LANG_NL
        else -> QuestionFilterType.DEFAULT
    }
}