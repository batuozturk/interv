package com.batuhan.interviewself.presentation.settings.exportquestions

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuhan.interviewself.R
import com.batuhan.interviewself.data.model.LanguageType
import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.domain.question.GetAllQuestionsAsList
import com.batuhan.interviewself.util.DialogAction
import com.batuhan.interviewself.util.DialogData
import com.batuhan.interviewself.util.DialogType
import com.batuhan.interviewself.util.Result
import com.batuhan.interviewself.util.ViewModelEventHandler
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExportQuestionsViewModel
    @Inject
    constructor(private val getAllQuestionsAsList: GetAllQuestionsAsList) :
    ViewModel(),
        ExportQuestionsEventHandler,
        ViewModelEventHandler<ExportQuestionsEvent, ExportQuestionsError> {
        private val _uiState = MutableStateFlow(ExportQuestionsUiState())
        val uiState = _uiState.asStateFlow()

        private val _event = Channel<ExportQuestionsEvent> { Channel.BUFFERED }
        val event = _event.receiveAsFlow()

        @OptIn(ExperimentalStdlibApi::class)
        override fun exportQuestions(
            contentResolver: ContentResolver,
            uri: Uri,
            isSharing: Boolean
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val result =
                    getAllQuestionsAsList.invoke(GetAllQuestionsAsList.Params(uiState.value.selectedLanguageCode))
                when (result) {
                    is Result.Success -> {
                        result.data?.let {
                            val stream = contentResolver.openOutputStream(uri)
                            val moshi =
                                Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                                    .adapter<List<Question>>()
                            val stringBuilder = StringBuilder()
                            stringBuilder.append(moshi.toJson(it))
                            stream?.write(stringBuilder.toString().toByteArray())
                            stream?.close()
                            if (isSharing) {
                                sendEvent(ExportQuestionsEvent.ShareQuestions(uri))
                            } else {
                                showDialog(
                                    DialogData(
                                        title = R.string.success_questions_exported,
                                        type = DialogType.SUCCESS_INFO,
                                        actions =
                                            listOf(
                                                DialogAction(R.string.dismiss, ::clearDialog),
                                            ),
                                    ),
                                )
                            }
                        }
                    }

                    is Result.Error -> {
                        showDialog(
                            DialogData(
                                title = R.string.error_export_questions,
                                type = DialogType.ERROR,
                                actions =
                                    listOf(
                                        DialogAction(R.string.retry) {
                                            retryOperation(ExportQuestionsError.ExportQuestions)
                                        },
                                    ),
                            ),
                        )
                    }
                }
            }
        }

        override fun updateSelectedLanguage(langCode: String) {
            _uiState.update {
                it.copy(selectedLanguageCode = langCode)
            }
        }

        override fun shareQuestions(success: Boolean) {
            if (success) {
                showDialog(
                    DialogData(
                        title = R.string.success_questions_shared,
                        type = DialogType.SUCCESS_INFO,
                        actions =
                            listOf(
                                DialogAction(R.string.dismiss, ::clearDialog),
                            ),
                    ),
                )
            } else {
                showDialog(
                    DialogData(
                        title = R.string.error_unknown,
                        type = DialogType.ERROR,
                        actions =
                        listOf(
                            DialogAction(R.string.dismiss, ::clearDialog),
                        ),
                    ),
                )
            }
        }

        override fun sendEvent(event: ExportQuestionsEvent) {
            viewModelScope.launch {
                _event.send(event)
            }
        }

        override fun showDialog(dialogData: DialogData) {
            viewModelScope.launch {
                if (uiState.value.dialogData != null) {
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
            sendEvent(ExportQuestionsEvent.ClearDialog)
        }

        override fun retryOperation(error: ExportQuestionsError) {
            when (error) {
                ExportQuestionsError.ExportQuestions -> {
                }
            }
        }
    }

data class ExportQuestionsUiState(
    val dialogData: DialogData? = null,
    val selectedLanguageCode: String = LanguageType.EN.code,
)

sealed class ExportQuestionsError {
    object ExportQuestions : ExportQuestionsError()
}

sealed class ExportQuestionsEvent {
    object Back : ExportQuestionsEvent()

    data class ExportQuestions(val isSharing: Boolean = false) : ExportQuestionsEvent()

    data class ShareQuestions(val uri: Uri) : ExportQuestionsEvent()

    object ClearDialog : ExportQuestionsEvent()
}
