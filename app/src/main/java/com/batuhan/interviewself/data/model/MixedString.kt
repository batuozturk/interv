package com.batuhan.interviewself.data.model

import android.content.Context
import androidx.annotation.StringRes
import com.batuhan.interviewself.util.getLocaleStringResource

data class MixedString(
    @StringRes val intString: Int,
    val quantity: Int? = null,
    val string: String? = null,
)

fun MixedString.makeString(
    context: Context,
    langCode: String,
) = context.getLocaleStringResource(
    langCode,
    this.intString,
    quantity,
) + (string?.takeIf { it.isNotEmpty() } ?: "")
