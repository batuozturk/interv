package com.batuhan.interv.presentation.interview.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuhan.interv.R
import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.model.InterviewStep
import com.batuhan.interv.data.model.InterviewType
import com.batuhan.interv.data.model.InterviewWithSteps
import com.batuhan.interv.domain.interview.DeleteInterview
import com.batuhan.interv.domain.interview.DeleteInterviewSteps
import com.batuhan.interv.domain.interview.GetInterviewWithSteps
import com.batuhan.interv.domain.interview.UpsertInterview
import com.batuhan.interv.domain.interview.UpsertInterviewSteps
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
class InterviewDetailViewModel @Inject constructor(
    private val deleteInterview: DeleteInterview,
    private val upsertInterview: UpsertInterview,
    private val getInterviewWithSteps: GetInterviewWithSteps,
    private val deleteInterviewSteps: DeleteInterviewSteps,
    private val upsertInterviewSteps: UpsertInterviewSteps,
) : ViewModel(), InterviewDetailEventHandler, ViewModelEventHandler<InterviewDetailEvent, InterviewDetailError> {

    private val _event = Channel<InterviewDetailEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    private val _uiState = MutableStateFlow(InterviewDetailUiState())
    val uiState = _uiState.asStateFlow()

    override fun shareInterview(interview: Interview) {
        TODO("Not yet implemented")
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
                            actions = listOf(
                                DialogAction(R.string.retry) {
                                    retryOperation(InterviewDetailError.DeleteInterview(interview))
                                }
                            )
                        )
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

                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions =
                            listOf(
                                DialogAction(R.string.retry) {
                                    retryOperation(InterviewDetailError.DeleteInterviewSteps(interviewId))
                                },
                            ),
                        ),
                    )
                    cancel()
                }
            }
        }

    override fun deleteInterview(interview: Interview) {
        viewModelScope.launch {
            joinAll(deleteInterviewJob(interview), deleteInterviewStepsJob(interview.interviewId!!))
            sendEvent(InterviewDetailEvent.Back)
        }
    }

    override fun retryInterview(interview: Interview, isTablet: Boolean) {
        val newInterview = interview.copy(interviewId = null, interviewName = interview.interviewName + " - copy", completed = false)
        viewModelScope.launch {
            val result = upsertInterview.invoke(UpsertInterview.Params(newInterview))
            when (result) {
                is Result.Success -> {
                    val newInterviewId = result.data
                    val steps = uiState.value.interviewWithSteps?.steps?.map {
                        it.copy(interviewStepId = null, interviewId = newInterviewId)
                    }
                    upsertInterviewSteps(newInterviewId, steps!!, isTablet)
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.retry) {
                                    retryOperation(InterviewDetailError.RetryInterview(interview, isTablet))
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun upsertInterviewSteps(interviewId: Long, steps: List<InterviewStep>, isTablet: Boolean) {
        viewModelScope.launch {
            val result = upsertInterviewSteps.invoke(UpsertInterviewSteps.Params(steps))
            when (result) {
                is Result.Success -> {
                    val interviewType = uiState.value.interviewWithSteps?.interview?.interviewType!!
                    val langCode = uiState.value.interviewWithSteps?.interview?.langCode!!
                    showDialog(
                        DialogData(
                            title = R.string.success_interview_saved,
                            type = DialogType.SUCCESS_INFO,
                            actions = if(!isTablet){
                                listOf(
                                    DialogAction(R.string.start_interview){
                                        clearDialog()
                                        sendEvent(InterviewDetailEvent.EnterInterview(interviewId, interviewType, langCode))
                                    },
                                    DialogAction(R.string.dismiss, ::clearDialog)
                                )
                            }
                            else listOf(DialogAction(R.string.dismiss, ::clearDialog))
                        )
                    )
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.retry) {
                                    retryOperation(InterviewDetailError.UpsertInterviewSteps(interviewId, steps, isTablet))
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun sendEvent(event: InterviewDetailEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    override fun getInterviewWithSteps(interviewId: Long) {
        viewModelScope.launch {
            val result = getInterviewWithSteps.invoke(GetInterviewWithSteps.Params(interviewId))
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(interviewWithSteps = result.data)
                    }
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.retry) {
                                    retryOperation(InterviewDetailError.GetInterviewWithSteps(interviewId))
                                }
                            )
                        )
                    )
                }
            }
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
        sendEvent(InterviewDetailEvent.ClearDialog)
    }

    override fun retryOperation(error: InterviewDetailError) {
        when(error){
            is InterviewDetailError.DeleteInterview -> {
                deleteInterviewJob(error.interview)
                deleteInterviewStepsJob(error.interview.interviewId!!)
            }
            is InterviewDetailError.ShareInterview -> shareInterview(error.interview)
            is InterviewDetailError.RetryInterview-> retryInterview(error.interview, error.isTablet)
            is InterviewDetailError.GetInterviewWithSteps -> getInterviewWithSteps(error.interviewId)
            is InterviewDetailError.DeleteInterviewSteps -> deleteInterviewStepsJob(error.interviewId)
            is InterviewDetailError.UpsertInterviewSteps -> upsertInterviewSteps(error.interviewId, error.steps, error.isTablet)
        }
    }

    override fun setInterviewWithStepsAsInitial() {
        _uiState.update {
            it.copy(interviewWithSteps = null)
        }
    }

}

data class InterviewDetailUiState(
    internal val dialogData: DialogData? = null,
    internal val interviewWithSteps: InterviewWithSteps? = null
)

sealed class InterviewDetailError {
    data class DeleteInterview(val interview: Interview): InterviewDetailError()
    data class ShareInterview(val interview: Interview): InterviewDetailError()
    data class RetryInterview(val interview: Interview, val isTablet: Boolean): InterviewDetailError()
    data class GetInterviewWithSteps(val interviewId: Long) : InterviewDetailError()

    data class DeleteInterviewSteps(val interviewId: Long): InterviewDetailError()
    data class UpsertInterviewSteps(val interviewId: Long, val steps: List<InterviewStep>, val isTablet: Boolean) : InterviewDetailError()
}

sealed class InterviewDetailEvent {
    object Back : InterviewDetailEvent()
    data class DeleteInterview(val interview: Interview) : InterviewDetailEvent()
    data class EnterInterview(val interviewId: Long, val interviewType: InterviewType, val languageCode: String) : InterviewDetailEvent()
    data class ShareInterview(val interview: Interview) : InterviewDetailEvent()
    data class RetryInterview(val interview: Interview, val isTablet: Boolean): InterviewDetailEvent()

    object ClearDialog: InterviewDetailEvent()
}