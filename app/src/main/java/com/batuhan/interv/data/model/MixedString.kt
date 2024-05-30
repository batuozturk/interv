package com.batuhan.interv.data.model

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.StringRes
import com.batuhan.interv.util.getLocaleStringResource

@Keep
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
