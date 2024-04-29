package com.batuhan.interviewself.presentation.interview.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuhan.interviewself.R
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.domain.interview.DeleteInterview
import com.batuhan.interviewself.domain.interview.GetInterviewWithSteps
import com.batuhan.interviewself.domain.interview.UpsertInterview
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
class InterviewDetailViewModel @Inject constructor(
    private val deleteInterview: DeleteInterview,
    private val upsertInterview: UpsertInterview,
    private val getInterviewWithSteps: GetInterviewWithSteps
) : ViewModel(), InterviewDetailEventHandler, ViewModelEventHandler<InterviewDetailEvent, InterviewDetailError> {

    private val _event = Channel<InterviewDetailEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    private val _uiState = MutableStateFlow(InterviewDetailUiState())
    val uiState = _uiState.asStateFlow()

    override fun shareInterview(interview: Interview) {
        TODO("Not yet implemented")
    }

    override fun deleteInterview(interview: Interview) {
        viewModelScope.launch {
            val result = deleteInterview.invoke(
                DeleteInterview.Params(
                    interview
                )
            )
            when (result) {
                is Result.Success -> {
                    sendEvent(InterviewDetailEvent.Back)
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.app_name) {
                                    retryOperation(InterviewDetailError.DeleteInterview(interview))
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun retryInterview(interview: Interview) {
        val newInterview = interview.copy(interviewId = null)
        viewModelScope.launch {
            val result = upsertInterview.invoke(UpsertInterview.Params(newInterview))
            when (result) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.app_name, ::clearDialog),
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
                                    retryOperation(InterviewDetailError.RetryInterview(interview))
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

    override fun retryOperation(error: InterviewDetailError) {
        when(error){
            is InterviewDetailError.DeleteInterview -> deleteInterview(error.interview)
            is InterviewDetailError.ShareInterview -> shareInterview(error.interview)
            is InterviewDetailError.RetryInterview-> retryInterview(error.interview)
        }
    }

}

data class InterviewDetailUiState(
    private val dialogData: DialogData? = null,
    private val interviewWithSteps: GetInterviewWithSteps? = null
)

sealed class InterviewDetailError {
    data class DeleteInterview(val interview: Interview): InterviewDetailError()
    data class ShareInterview(val interview: Interview): InterviewDetailError()
    data class RetryInterview(val interview: Interview): InterviewDetailError()
}

sealed class InterviewDetailEvent {
    object Back : InterviewDetailEvent()
    data class DeleteInterview(private val interview: Interview) : InterviewDetailEvent()
    data class ShareInterview(private val interview: Interview) : InterviewDetailEvent()
    data class RetryInterview(private val interview: Interview): InterviewDetailEvent()
}