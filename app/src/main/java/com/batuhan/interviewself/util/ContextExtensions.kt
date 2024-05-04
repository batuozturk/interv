package com.batuhan.interviewself.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

fun Context.isTablet() =
    resources.configuration.screenWidthDp >= 600 && resources.configuration.screenWidthDp * 0.75 > resources.configuration.screenHeightDp

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
