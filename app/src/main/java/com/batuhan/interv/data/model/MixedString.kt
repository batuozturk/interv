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
    @StringRes val testString: Int? = null
)

fun MixedString.makeString(
    context: Context,
    langCode: String,
) = context.getLocaleStringResource(
    langCode,
    this.intString,
    quantity,
) + (string?.takeIf { it.isNotEmpty() } ?: "")

fun MixedString.makeTestString(
    context: Context,
    langCode: String,
): String {
    return context.getLocaleStringResource(
        langCode,
        this.intString,
        quantity,
    ) + (this.testString?.let {
        context.getLocaleStringResource(
            langCode,
            this.testString,
            null,
        )
    } ?: "")
}
