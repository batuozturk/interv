package com.batuhan.interviewself.presentation.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.batuhan.interviewself.R
import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.domain.question.DeleteQuestion
import com.batuhan.interviewself.domain.question.GetAllQuestions
import com.batuhan.interviewself.domain.question.UpsertQuestion
import com.batuhan.interviewself.util.DialogAction
import com.batuhan.interviewself.util.DialogData
import com.batuhan.interviewself.util.DialogType
import com.batuhan.interviewself.util.Result
import com.batuhan.interviewself.util.ViewModelEventHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionListViewModel @Inject constructor(
    private val deleteQuestion: DeleteQuestion,
    private val upsertQuestion: UpsertQuestion,
    getAllQuestions: GetAllQuestions
) : ViewModel(), QuestionListEventHandler, ViewModelEventHandler<QuestionListEvent,QuestionListError> {

    private val questions = getAllQuestions.invoke().cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(QuestionListUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<QuestionListEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    override fun sendEvent(event: QuestionListEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    override fun showDialog(dialogData: DialogData) {
        _uiState.update {
            it.copy(dialogData = dialogData)
        }
    }

    override fun clearDialog() {
        _uiState.update {
            it.copy(dialogData = null)
        }
    }

    override fun retryOperation(error: QuestionListError) {
        clearDialog()
        when(error){
            QuestionListError.CreateQuestion -> createQuestion()
            QuestionListError.QuestionEmpty,
            QuestionListError.LangCodeEmpty -> {  }
            is QuestionListError.DeleteQuestion -> deleteQuestion(error.question)
            is QuestionListError.UpdateQuestion -> updateQuestion(error.question)
            is QuestionListError.UndoDeleteQuestion -> undoDeleteQuestion()
        }
    }



    override fun updateQuestion(question: Question) {
        viewModelScope.launch {
            val result = upsertQuestion.invoke(UpsertQuestion.Params(question))
            when (result) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.app_name,::clearDialog)
                            )
                        )
                    )
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.app_name) {
                                    retryOperation(QuestionListError.UpdateQuestion(question))
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            val result = deleteQuestion.invoke(DeleteQuestion.Params(question))
            when (result) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.app_name){
                                   undoDeleteQuestion()
                                   clearDialog()
                                },
                                DialogAction(R.string.app_name,::clearDialog),
                            )
                        )
                    )
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.app_name) {
                                    retryOperation(QuestionListError.DeleteQuestion(question))
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun createQuestion() {
        uiState.value.langCode ?: run {
            showDialog(
                DialogData(
                    title = R.string.app_name,
                    type = DialogType.ERROR,
                    actions = listOf(
                        DialogAction(R.string.app_name) {
                            retryOperation(QuestionListError.LangCodeEmpty)
                        }
                    )
                )
            )
            return
        }
        uiState.value.questionText ?: run {
            showDialog(
                DialogData(
                    title = R.string.app_name,
                    type = DialogType.ERROR,
                    actions = listOf(
                        DialogAction(R.string.app_name) {
                            retryOperation(QuestionListError.QuestionEmpty)
                        }
                    )
                )
            )
            return
        }
        viewModelScope.launch {
            val result = upsertQuestion.invoke(
                UpsertQuestion.Params(
                    Question(
                        question = uiState.value.questionText,
                        langCode = uiState.value.langCode)
                )
            )
            when (result) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.app_name){
                                    undoDeleteQuestion()
                                    clearDialog()
                                },
                                DialogAction(R.string.app_name,::clearDialog),
                            )
                        )
                    )
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.app_name) {
                                    retryOperation(QuestionListError.CreateQuestion)
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun updateQuestionText(string: String) {
        _uiState.update {
            it.copy(questionText = string)
        }
    }

    override fun updateLangCode(langCode: String) {
        _uiState.update {
            it.copy(langCode = langCode)
        }
    }

    override fun undoDeleteQuestion() {
        viewModelScope.launch {
            val result = upsertQuestion.invoke(
                UpsertQuestion.Params(
                    uiState.value.deletedQuestion!!
                )
            )
            when (result) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.INFO,
                            actions = listOf(
                                DialogAction(R.string.app_name){
                                    undoDeleteQuestion()
                                    clearDialog()
                                },
                                DialogAction(R.string.app_name,::clearDialog),
                            )
                        )
                    )
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.app_name) {
                                    retryOperation(QuestionListError.UndoDeleteQuestion)
                                }
                            )
                        )
                    )
                }
            }
        }
    }


}

data class QuestionListUiState(
    private val dialogData: DialogData? = null,
    internal val deletedQuestion: Question? = null,
    internal val questionText: String? = null,
    internal val langCode: String? = null
)

sealed class QuestionListError {
    object CreateQuestion: QuestionListError()
    object QuestionEmpty: QuestionListError()
    object LangCodeEmpty: QuestionListError()
    data class DeleteQuestion(val question: Question): QuestionListError()
    data class UpdateQuestion(val question: Question): QuestionListError()
    object UndoDeleteQuestion: QuestionListError()
}

sealed class QuestionListEvent {
    object Back : QuestionListEvent()
    object CreateQuestion : QuestionListEvent()
    data class DeleteQuestion(private val interview: Question) : QuestionListEvent()
}