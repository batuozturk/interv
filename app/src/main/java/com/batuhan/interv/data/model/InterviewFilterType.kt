package com.batuhan.interv.data.model

import androidx.annotation.StringRes
import com.batuhan.interv.R

enum class InterviewFilterType(
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
    COMPLETED(
        R.string.filter_completed,
    ),
    NOT_COMPLETED(R.string.filter_not_completed),
}

fun InterviewFilterType.createQuery(searchText: String): String{
    var string = "SELECT * FROM interview "
    val stringIsValid = searchText.isNotEmpty() && searchText.isNotBlank()
    if(stringIsValid) string += "WHERE interviewName LIKE ? "
    string +=
    when(this){
        InterviewFilterType.NAME -> " ORDER BY interviewName ASC"
        InterviewFilterType.NAME_REVERSED -> " ORDER BY interviewName DESC"
        InterviewFilterType.LANG_EN -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'en-US'"
        InterviewFilterType.LANG_TR -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'tr-TR'"
        InterviewFilterType.LANG_FR -> (if(stringIsValid) "AND " else "WHERE ") + "langCode = 'fr-FR'"
        InterviewFilterType.DEFAULT -> ""
        InterviewFilterType.COMPLETED -> (if(stringIsValid) "AND " else "WHERE ") + "completed = true"
        InterviewFilterType.NOT_COMPLETED -> (if(stringIsValid) "AND " else "WHERE ") + "completed = false"
    }

    return string
}
