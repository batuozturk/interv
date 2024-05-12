package com.batuhan.interviewself.presentation.settings

interface SettingsEventHandler {

    fun writeData(settingsType: SettingsType)

    fun readData(isDarkMode: Boolean, langCode: String)
}