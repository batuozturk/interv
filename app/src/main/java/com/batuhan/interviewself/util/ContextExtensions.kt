package com.batuhan.interviewself.util

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import java.util.Locale

fun Context.isTablet() =
    resources.configuration.screenWidthDp >= 600 && resources.configuration.screenWidthDp * 0.75 > resources.configuration.screenHeightDp

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

fun Context.getLocaleStringResource(
    langCode: String,
    resourceId: Int,
    quantity: Int?,
): String {
    val result: String
    val config =
        Configuration(resources.configuration)
    config.setLocale(Locale.forLanguageTag(langCode))
    result = createConfigurationContext(config).getString(resourceId, quantity)

    return result
}
