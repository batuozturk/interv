package com.batuhan.interviewself.presentation.interview.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuhan.interviewself.R
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewType
import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.domain.interview.DeleteInterview
import com.batuhan.interviewself.domain.interview.DeleteInterviewSteps
import com.batuhan.interviewself.domain.interview.UpsertInterview
import com.batuhan.interviewself.presentation.interview.create.addstep.AddStepError
import com.batuhan.interviewself.util.DialogAction
import com.batuhan.interviewself.util.DialogData
import com.batuhan.interviewself.util.DialogType
import com.batuhan.interviewself.util.Result
import com.batuhan.interviewself.util.ViewModelEventHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
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
    private val deleteInterviewSteps: DeleteInterviewSteps
) : ViewModel(), CreateInterviewEventHandler, ViewModelEventHandler<CreateInterviewEvent,CreateInterviewError> {

    private val _event = Channel<CreateInterviewEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    private val _uiState = MutableStateFlow(CreateInterviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        initializeInterview()
    }

    override fun initializeInterview() {
        viewModelScope.launch {
            val result = upsertInterview.invoke(UpsertInterview.Params(uiState.value.currentInterview))
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        val interview = it.currentInterview.copy(interviewId = result.data)
                        it.copy(currentInterview = interview)
                    }
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.app_name) {
                                    retryOperation(CreateInterviewError.CreateInterview)
                                }
                            )
                        )
                    )
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
        _uiState.update {
            it.copy(dialogData = dialogData)
        }
    }

    override fun clearDialog() {
        _uiState.update {
            it.copy(dialogData = null)
        }
    }

    override fun retryOperation(error: CreateInterviewError) {
        when(error){
            is CreateInterviewError.CancelInterview -> {
                deleteInterviewJob(error.interview)
                deleteInterviewStepsJob(error.interview.interviewId!!)
            }
            CreateInterviewError.DurationNotValid,
            CreateInterviewError.LangCodeEmpty,
            CreateInterviewError.NameEmpty,
            CreateInterviewError.TypeNotSelected -> {}
            CreateInterviewError.CreateInterview -> createInterview()
            is CreateInterviewError.DeleteInterviewSteps -> deleteInterviewStepsJob(error.interviewId)
        }
    }

    override fun createInterview() {
        val interview = uiState.value.currentInterview
        interview.interviewName ?: run {
            showDialog(
                DialogData(
                    title = R.string.app_name,
                    type = DialogType.ERROR,
                    actions = listOf(
                        DialogAction(R.string.app_name) {
                            retryOperation(CreateInterviewError.NameEmpty)
                        }
                    )
                )
            )
            return
        }
        interview.interviewType ?: run {
            showDialog(
                DialogData(
                    title = R.string.app_name,
                    type = DialogType.ERROR,
                    actions = listOf(
                        DialogAction(R.string.app_name) {
                            retryOperation(CreateInterviewError.TypeNotSelected)
                        }
                    )
                )
            )
            return
        }
        interview.langCode ?: run {
            showDialog(
                DialogData(
                    title = R.string.app_name,
                    type = DialogType.ERROR,
                    actions = listOf(
                        DialogAction(R.string.app_name) {
                            retryOperation(CreateInterviewError.LangCodeEmpty)
                        }
                    )
                )
            )
            return
        }
        interview.questionDuration ?: run {
            showDialog(
                DialogData(
                    title = R.string.app_name,
                    type = DialogType.ERROR,
                    actions = listOf(
                        DialogAction(R.string.app_name) {
                            retryOperation(CreateInterviewError.DurationNotValid)
                        }
                    )
                )
            )
            return
        }
        viewModelScope.launch {
            val result = upsertInterview.invoke(UpsertInterview.Params(interview))
            when (result) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
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
                                        retryOperation(CreateInterviewError.CreateInterview)
                                    }
                                )
                            )
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

                    is InterviewField.Duration -> {
                        it.currentInterview.copy(questionDuration = interviewField.duration)
                    }

                    is InterviewField.Type -> {
                        it.currentInterview.copy(interviewType = interviewField.type)
                    }

                    is InterviewField.Language -> {
                        it.currentInterview.copy(langCode = interviewField.langCode)
                    }
                }
            it.copy(currentInterview = interview)
        }
    }

    private fun deleteInterviewJob(interview: Interview) = viewModelScope.launch {
        val result = deleteInterview.invoke(DeleteInterview.Params(interview))
        when (result) {
            is Result.Success -> {

            }

            is Result.Error -> {
                showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.app_name) {
                                    retryOperation(CreateInterviewError.CancelInterview(interview))
                                }
                            )
                        )
                )
                cancel()
            }
        }

    }

    private fun deleteInterviewStepsJob(interviewId: Long) = viewModelScope.launch {
        val result = deleteInterviewSteps.invoke(
            DeleteInterviewSteps.Params(interviewId)
        )
        when (result) {
            is Result.Success -> {

            }

            is Result.Error -> {
                showDialog(
                    DialogData(
                        title = R.string.app_name,
                        type = DialogType.ERROR,
                        actions = listOf(
                            DialogAction(R.string.app_name) {
                                retryOperation(CreateInterviewError.DeleteInterviewSteps(interviewId))
                            }
                        )
                    )
                )
                cancel()
            }
        }
    }

    override fun cancelInterview(interview: Interview) {
        viewModelScope.launch {
            joinAll(deleteInterviewJob(interview), deleteInterviewStepsJob(interview.interviewId!!))
            sendEvent(CreateInterviewEvent.Back)
        }
    }

}

data class CreateInterviewUiState(
    private val dialogData: DialogData? = null,
    internal val currentInterview: Interview = Interview()
)

sealed class InterviewField {
    data class Name(val name: String) : InterviewField()
    data class Duration(val duration: Int) : InterviewField()
    data class Type(val type: InterviewType) : InterviewField()
    data class Language(val langCode: String) : InterviewField()
}

sealed class CreateInterviewError {
    object CreateInterview: CreateInterviewError()
    object NameEmpty: CreateInterviewError()
    object DurationNotValid: CreateInterviewError()
    object TypeNotSelected: CreateInterviewError()
    object LangCodeEmpty: CreateInterviewError()
    data class CancelInterview(val interview: Interview): CreateInterviewError()
    data class DeleteInterviewSteps(val interviewId: Long): CreateInterviewError()
}

sealed class CreateInterviewEvent {
    object Back : CreateInterviewEvent()
}