package com.batuhan.interv.presentation.settings.importquestions

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuhan.interv.R
import com.batuhan.interv.data.model.LanguageType
import com.batuhan.interv.data.model.Question
import com.batuhan.interv.domain.question.UpsertQuestions
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.DialogType
import com.batuhan.interv.util.Result
import com.batuhan.interv.util.ViewModelEventHandler
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
class ImportQuestionsViewModel
    @Inject
    constructor(private val upsertQuestions: UpsertQuestions) :
    ViewModel(),
        ImportQuestionsEventHandler,
        ViewModelEventHandler<ImportQuestionsEvent, ImportQuestionsError> {
        private val _uiState = MutableStateFlow(ImportQuestionsUiState())
        val uiState = _uiState.asStateFlow()

        private val _event = Channel<ImportQuestionsEvent> { Channel.BUFFERED }
        val event = _event.receiveAsFlow()

        @OptIn(ExperimentalStdlibApi::class)
        override fun importQuestions(
            contentResolver: ContentResolver,
            uri: Uri,
        ) {
            viewModelScope.launch(Dispatchers.IO) {
                val stream = contentResolver.openInputStream(uri)
                val byteArray = stream?.bufferedReader().use { it?.readText() } ?: return@launch
                val moshi =
                    Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                        .adapter<List<Question>>()
                val list =
                    runCatching {
                        moshi.fromJson(byteArray)?.map {
                            it.copy(questionId = null)
                        } ?: throw Throwable()
                    }.getOrElse {
                        showDialog(
                            DialogData(
                                title = R.string.error_unknown,
                                type = DialogType.ERROR,
                                actions =
                                    listOf(
                                        DialogAction(R.string.retry) {
                                            retryOperation(ImportQuestionsError.ImportQuestions)
                                        },
                                    ),
                            ),
                        )
                        return@launch
                    }
                val result = upsertQuestions.invoke(UpsertQuestions.Params(list))
                when (result) {
                    is Result.Success -> {
                        showDialog(
                            DialogData(
                                title = R.string.success_questions_imported,
                                type = DialogType.SUCCESS_INFO,
                                actions =
                                    listOf(
                                        DialogAction(R.string.dismiss, ::clearDialog),
                                    ),
                            ),
                        )
                    }

                    is Result.Error -> {
                        showDialog(
                            DialogData(
                                title = R.string.error_unknown,
                                type = DialogType.ERROR,
                                actions =
                                    listOf(
                                        DialogAction(R.string.retry) {
                                            retryOperation(ImportQuestionsError.ImportQuestions)
                                        },
                                    ),
                            ),
                        )
                    }
                }
            }
        }

        override fun sendEvent(event: ImportQuestionsEvent) {
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
            sendEvent(ImportQuestionsEvent.ClearDialog)
        }

        override fun retryOperation(error: ImportQuestionsError) {
            when (error) {
                ImportQuestionsError.ImportQuestions -> {
                    sendEvent(ImportQuestionsEvent.ImportQuestions)
                }
            }
        }
    }

data class ImportQuestionsUiState(
    val dialogData: DialogData? = null,
    val selectedLanguageCode: String = LanguageType.EN.code,
)

sealed class ImportQuestionsError {
    object ImportQuestions : ImportQuestionsError()
}

sealed class ImportQuestionsEvent {
    object Back : ImportQuestionsEvent()

    object ImportQuestions : ImportQuestionsEvent()

    object ClearDialog : ImportQuestionsEvent()
}
