package com.batuhan.interviewself.presentation.interview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.batuhan.interviewself.R
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.domain.interview.DeleteInterview
import com.batuhan.interviewself.domain.interview.GetAllInterviews
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
class InterviewListViewModel @Inject constructor(
    private val deleteInterview: DeleteInterview,
    private val upsertInterview: UpsertInterview,
    getAllInterviews: GetAllInterviews
) : ViewModel(), InterviewListEventHandler, ViewModelEventHandler<InterviewListEvent, InterviewListError> {

    private val interviews = getAllInterviews.invoke().cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(InterviewListUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<InterviewListEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    override fun deleteInterview(interview: Interview) {
        viewModelScope.launch {
            val result = deleteInterview.invoke(
                DeleteInterview.Params(
                    interview
                )
            )
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(deletedInterview = interview)
                    }
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.INFO,
                            actions = listOf(
                                DialogAction(R.string.app_name){
                                    undoDeleteInterview()
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
                                    retryOperation(InterviewListError.DeleteInterview(interview))
                                }
                            )
                        )
                    )
                }
            }
        }
    }


    override fun undoDeleteInterview() {
        viewModelScope.launch {
            val result = upsertInterview.invoke(
                UpsertInterview.Params(
                    uiState.value.deletedInterview!!
                )
            )
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
                                    retryOperation(InterviewListError.UndoDeleteInterview)
                                }
                            )
                        )
                    )
                }
            }
        }
    }


    override fun sendEvent(event: InterviewListEvent) {
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

    override fun retryOperation(error: InterviewListError) {
        when(error){
            is InterviewListError.DeleteInterview -> deleteInterview(error.interview)
            InterviewListError.UndoDeleteInterview -> undoDeleteInterview()
        }
    }


}

data class InterviewListUiState(
    private val dialogData: DialogData? = null,
    internal val deletedInterview: Interview? = null,
)

sealed class InterviewListError {
    data class DeleteInterview(val interview: Interview): InterviewListError()
    object UndoDeleteInterview: InterviewListError()
}

sealed class InterviewListEvent {
    object Back : InterviewListEvent()
    object CreateInterview : InterviewListEvent()
    data class DeleteInterview(private val interview: Interview) : InterviewListEvent()
    data class Detail(private val interviewId: Long):InterviewListEvent()
}