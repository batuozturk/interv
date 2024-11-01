package com.batuhan.interv.presentation.interview.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuhan.interv.R
import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.model.InterviewType
import com.batuhan.interv.data.model.LanguageType
import com.batuhan.interv.domain.interview.DeleteInterview
import com.batuhan.interv.domain.interview.DeleteInterviewSteps
import com.batuhan.interv.domain.interview.GetInterviewWithSteps
import com.batuhan.interv.domain.interview.UpsertInterview
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.DialogType
import com.batuhan.interv.util.Result
import com.batuhan.interv.util.ViewModelEventHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateInterviewViewModel @Inject constructor(
    private val upsertInterview: UpsertInterview,
    private val deleteInterview: DeleteInterview,
    private val deleteInterviewSteps: DeleteInterviewSteps,
    private val getInterviewWithSteps: GetInterviewWithSteps,
) : ViewModel(), CreateInterviewEventHandler, ViewModelEventHandler<CreateInterviewEvent,CreateInterviewError> {

    private val _event = Channel<CreateInterviewEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    private val _uiState = MutableStateFlow(CreateInterviewUiState())
    val uiState = _uiState.asStateFlow()

    override fun initializeInterview() {
        if (uiState.value.currentInterview.interviewId == null)
            {
            viewModelScope.launch {
                val result = upsertInterview.invoke(UpsertInterview.Params(uiState.value.currentInterview))
                when (result) {
                    is Result.Success -> {
                        _uiState.update {
                            val interview = it.currentInterview.copy(interviewId = result.data, interviewType = InterviewType.VIDEO, langCode = LanguageType.EN.code)
                            it.copy(currentInterview = interview)
                        }
                    }

                    is Result.Error -> {
                        showDialog(
                            DialogData(
                                title = R.string.error_unknown,
                                type = DialogType.ERROR,
                                actions =
                                    listOf(
                                        DialogAction(R.string.retry) {
                                            retryOperation(CreateInterviewError.CreateInterview)
                                        },
                                    ),
                            ),
                        )
                    }
                }
            }
        }
    }

    override fun sendEvent(event: CreateInterviewEvent) {
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
        sendEvent(CreateInterviewEvent.ClearDialog)
    }

    override fun retryOperation(error: CreateInterviewError) {
        when (error) {
            is CreateInterviewError.CancelInterview -> {
                deleteInterviewJob(error.interview)
                deleteInterviewStepsJob(error.interview.interviewId!!)
            }
            CreateInterviewError.DurationNotValid,
            CreateInterviewError.LangCodeEmpty,
            CreateInterviewError.NameEmpty,
            CreateInterviewError.TypeNotSelected,
            -> {}
            CreateInterviewError.CreateInterview -> createInterview()
            is CreateInterviewError.DeleteInterviewSteps -> deleteInterviewStepsJob(error.interviewId)
        }
    }

    override fun createInterview() {
        val interview = uiState.value.currentInterview
        interview.interviewName.takeIf {
            it?.isNotEmpty() ?: false && it?.isNotBlank() ?: false
        } ?: run {
            showDialog(
                DialogData(
                    title = R.string.error_interview_empty,
                    type = DialogType.ERROR,
                    actions =
                        listOf(
                            DialogAction(R.string.dismiss, ::clearDialog),
                        ),
                ),
            )
            return
        }
        interview.interviewType ?: run {
            showDialog(
                DialogData(
                    title = R.string.error_interview_type_not_selected,
                    type = DialogType.ERROR,
                    actions =
                        listOf(
                            DialogAction(R.string.dismiss, ::clearDialog),
                        ),
                ),
            )
            return
        }
        interview.langCode.takeIf {
            it?.isNotEmpty() ?: false && it?.isNotBlank() ?: false
        } ?: run {
            showDialog(
                DialogData(
                    title = R.string.error_interview_lang_code_empty,
                    type = DialogType.ERROR,
                    actions =
                        listOf(
                            DialogAction(R.string.dismiss, ::clearDialog),
                        ),
                ),
            )
            return
        }


        viewModelScope.launch {
            val resultSteps = getInterviewWithSteps.invoke(GetInterviewWithSteps.Params(interviewId = interview.interviewId!!))
            when(resultSteps){
                is Result.Success -> {
                    if((resultSteps.data.steps?.size ?: 0) == 0){
                        showDialog(
                            DialogData(
                                title = R.string.interview_steps_not_added,
                                type = DialogType.ERROR,
                                actions =
                                listOf(
                                    DialogAction(R.string.dismiss, ::clearDialog) ,
                                ),
                            ),
                        )
                        return@launch
                    }
                }
                else -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions =
                            listOf(
                                DialogAction(R.string.dismiss, ::clearDialog) ,
                            ),
                        ),
                    )
                    return@launch
                }
            }
            val result = upsertInterview.invoke(UpsertInterview.Params(interview))
            when (result) {
                is Result.Success -> {
                    sendEvent(CreateInterviewEvent.Back(true))
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions =
                                listOf(
                                    DialogAction(R.string.retry) {
                                        retryOperation(CreateInterviewError.CreateInterview)
                                    },
                                ),
                        ),
                    )
                }
            }
        }
    }

    override fun updateCurrentSetup(interviewField: InterviewField) {
        _uiState.update {
            val interview =
                when (interviewField) {
                    is InterviewField.Name -> {
                        it.currentInterview.copy(interviewName = interviewField.name)
                    }

                    is InterviewField.Type -> {
                        it.currentInterview.copy(interviewType = interviewField.type)
                    }

                    is InterviewField.Language -> {
                        val prevLang = it.currentInterview.langCode
                        if(prevLang != interviewField.langCode){
                            viewModelScope.launch {
                                deleteInterviewSteps.invoke(DeleteInterviewSteps.Params(it.currentInterview.interviewId!!))
                            }
                        }
                        it.currentInterview.copy(langCode = interviewField.langCode)
                    }
                }
            it.copy(currentInterview = interview)
        }
    }

    override fun setInterviewAsInitial() {
        _uiState.update {
            it.copy(currentInterview = Interview())
        }
    }

    private fun deleteInterviewJob(interview: Interview) =
        viewModelScope.launch {
            val result = deleteInterview.invoke(DeleteInterview.Params(interview))
            when (result) {
                is Result.Success -> {
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions =
                                listOf(
                                    DialogAction(R.string.retry) {
                                        retryOperation(CreateInterviewError.CancelInterview(interview))
                                    },
                                ),
                        ),
                    )
                    cancel()
                }
            }
        }

    private fun deleteInterviewStepsJob(interviewId: Long) =
        viewModelScope.launch {
            val result =
                deleteInterviewSteps.invoke(
                    DeleteInterviewSteps.Params(interviewId),
                )
            when (result) {
                is Result.Success -> {
                    setInterviewAsInitial()
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions =
                                listOf(
                                    DialogAction(R.string.retry) {
                                        retryOperation(CreateInterviewError.DeleteInterviewSteps(interviewId))
                                    },
                                ),
                        ),
                    )
                    cancel()
                }
            }
        }

    override fun cancelInterview(interview: Interview) {
        viewModelScope.launch {
            joinAll(deleteInterviewJob(interview), deleteInterviewStepsJob(interview.interviewId!!))
            clearDialog()
            sendEvent(CreateInterviewEvent.Back())
        }
    }
}

data class CreateInterviewUiState(
    internal val dialogData: DialogData? = null,
    internal val currentInterview: Interview = Interview(),
)

sealed class InterviewField {
    data class Name(val name: String) : InterviewField()

    data class Type(val type: InterviewType) : InterviewField()

    data class Language(val langCode: String) : InterviewField()
}

sealed class CreateInterviewError {
    object CreateInterview : CreateInterviewError()

    object NameEmpty : CreateInterviewError()

    object DurationNotValid : CreateInterviewError()

    object TypeNotSelected : CreateInterviewError()

    object LangCodeEmpty : CreateInterviewError()

    data class CancelInterview(val interview: Interview) : CreateInterviewError()

    data class DeleteInterviewSteps(val interviewId: Long) : CreateInterviewError()
}

sealed class CreateInterviewEvent {
    data class Back(val isSuccess: Boolean = false) : CreateInterviewEvent()

    data class AddStep(val interviewId: Long)

    object ClearDialog : CreateInterviewEvent()
}
