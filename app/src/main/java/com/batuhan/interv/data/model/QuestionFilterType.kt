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
    LANG_DE(R.string.filter_german)
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
        else -> QuestionFilterType.DEFAULT
    }
}