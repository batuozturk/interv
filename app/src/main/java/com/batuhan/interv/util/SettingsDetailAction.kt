package com.batuhan.interv.util

import androidx.annotation.StringRes
import com.batuhan.interv.R

sealed class SettingsDetailAction(
    @StringRes val title: Int,
    open val actions: List<DialogAction>,
) {
    data class Language(override val actions: List<DialogAction>) : SettingsDetailAction(
        R.string.settings_language_title,
        actions
    )

    data class Style(override val actions: List<DialogAction>) : SettingsDetailAction(
        R.string.settings_style_title,
        actions
    )
}
