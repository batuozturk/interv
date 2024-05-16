package com.batuhan.interviewself.presentation.settings

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuhan.interviewself.util.DialogData
import com.batuhan.interviewself.util.ViewModelEventHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor() : ViewModel(), SettingsEventHandler, ViewModelEventHandler<SettingsEvent, SettingsError> {
        companion object {
            internal val KEY_PREFERENCES_STYLE = booleanPreferencesKey("preferences_style")
            internal val KEY_PREFERENCES_LANGUAGE = stringPreferencesKey("preferences_language")
        }

        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState = _uiState.asStateFlow()

        private val _event = Channel<SettingsEvent> { Channel.BUFFERED }
        val event = _event.receiveAsFlow()

        override fun writeData(settingsType: SettingsType) {
            _uiState.update {
                when (settingsType) {
                    is SettingsType.Style -> it.copy(isDarkMode = settingsType.isDarkMode)
                    is SettingsType.LangCode -> it.copy(langCode = settingsType.langCode)
                }
            }
            when (settingsType) {
                is SettingsType.Style -> sendEvent(SettingsEvent.ChangeStyle(settingsType.isDarkMode))
                is SettingsType.LangCode -> sendEvent(SettingsEvent.RestartApplication)
            }
            clearDialog()
        }

        override fun readData(
            isDarkMode: Boolean,
            langCode: String,
        ) {
            _uiState.update {
                it.copy(isDarkMode = isDarkMode, langCode = langCode)
            }
        }

        override fun sendEvent(event: SettingsEvent) {
            viewModelScope.launch {
                _event.send(event)
            }
        }

        override fun showDialog(dialogData: DialogData) {
            viewModelScope.launch {
                if (uiState.value.dialogData != null)
                    {
                        clearDialog()
                        delay(1000L)
                    }
                _uiState.update {
                    it.copy(dialogData = dialogData)
                }
            }
        }

        override fun clearDialog() {
            _uiState.update {
                it.copy(dialogData = null)
            }
            sendEvent(SettingsEvent.ClearDialog)
        }

        override fun retryOperation(error: SettingsError) {
            // no-op
        }
    }

data class SettingsUiState(
    internal val dialogData: DialogData? = null,
    internal val isDarkMode: Boolean = false,
    internal val langCode: String = "",
)

sealed class SettingsType {
    data class Style(val isDarkMode: Boolean) : SettingsType()

    data class LangCode(val langCode: String) : SettingsType()
}

sealed class SettingsError {
    data class WriteData(val settingsType: SettingsType) : SettingsError()

    object ReadData : SettingsError()
}

sealed class SettingsEvent {
    object RestartApplication : SettingsEvent()

    data class ChangeStyle(val isDarkMode: Boolean) : SettingsEvent()

    object ClearDialog : SettingsEvent()

    object ExportQuestions: SettingsEvent()

    object ImportQuestions: SettingsEvent()
}
