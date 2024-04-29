package com.batuhan.interviewself.presentation.interview.create.addstep

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.batuhan.interviewself.R
import com.batuhan.interviewself.data.model.InterviewStep
import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.domain.interview.DeleteInterviewStep
import com.batuhan.interviewself.domain.interview.UpsertInterviewStep
import com.batuhan.interviewself.domain.question.GetAllQuestions
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
class AddStepViewModel @Inject constructor(
    private val upsertInterviewStep: UpsertInterviewStep,
    private val deleteInterviewStep: DeleteInterviewStep,
    getAllQuestions: GetAllQuestions,
    savedStateHandle: SavedStateHandle
) : ViewModel(), AddStepEventHandler, ViewModelEventHandler<AddStepEvent, AddStepError> {

    companion object {
        private const val KEY_INTERVIEW_ID = "interview_id"
    }

    private val _event = Channel<AddStepEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    private val _uiState = MutableStateFlow(AddStepUiState())
    val uiState = _uiState.asStateFlow()

    val questions = getAllQuestions.invoke().cachedIn(viewModelScope)

    val interviewId = savedStateHandle.get<Long>(KEY_INTERVIEW_ID)


    override fun sendEvent(event: AddStepEvent) {
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

    override fun retryOperation(error: AddStepError) {
        when(error){
            is AddStepError.AddStep -> addStep(error.question)
            is AddStepError.DeleteStep -> deleteStep(error.interviewStep)
        }
    }

    override fun addStep(question: Question) {
        viewModelScope.launch {
            val result = upsertInterviewStep.invoke(UpsertInterviewStep.Params(InterviewStep(question = question, interviewId = interviewId)))
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
                                    retryOperation(AddStepError.AddStep(question))
                                }
                            )
                        )
                    )
                }
            }
        }
    }



    override fun deleteStep(interviewStep: InterviewStep) {
        viewModelScope.launch {
            val result = deleteInterviewStep.invoke(DeleteInterviewStep.Params(interviewStep))
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
                                    retryOperation(AddStepError.DeleteStep(interviewStep))
                                }
                            )
                        )
                    )
                }
            }
        }
    }

}

data class AddStepUiState(
    private val dialogData: DialogData? = null,
)

sealed class AddStepError {
    data class AddStep(val question: Question): AddStepError()
    data class DeleteStep(val interviewStep: InterviewStep): AddStepError()
}

sealed class AddStepEvent {
    object Back : AddStepEvent()
    data class AddStep(val question: Question) : AddStepEvent()
    data class DeleteStep(val interviewStep: InterviewStep) : AddStepEvent()

}