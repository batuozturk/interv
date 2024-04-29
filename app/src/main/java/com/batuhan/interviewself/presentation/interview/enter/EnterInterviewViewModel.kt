package com.batuhan.interviewself.presentation.interview.enter

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuhan.interviewself.R
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewStep
import com.batuhan.interviewself.domain.interview.GetInterviewWithSteps
import com.batuhan.interviewself.domain.interview.UpsertInterviewStep
import com.batuhan.interviewself.presentation.interview.InterviewListError
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
class EnterInterviewViewModel @Inject constructor(
    private val upsertInterviewStep: UpsertInterviewStep,
    private val getInterviewWithSteps: GetInterviewWithSteps,
    savedStateHandle: SavedStateHandle
) : ViewModel(), EnterInterviewEventHandler, ViewModelEventHandler<EnterInterviewEvent, EnterInterviewError> {

    companion object {
        private const val KEY_INTERVIEW_ID = "interview_id"
    }

    private val _event = Channel<EnterInterviewEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    private val _uiState = MutableStateFlow(EnterInterviewUiState())
    val uiState = _uiState.asStateFlow()

    init {
        initalizeSteps()
    }

    val interviewId = savedStateHandle.get<Long>(KEY_INTERVIEW_ID)

    override fun sendEvent(event: EnterInterviewEvent) {
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

    override fun retryOperation(error: EnterInterviewError) {
        clearDialog()
        when(error){
            EnterInterviewError.Initialization -> initalizeSteps()
            is EnterInterviewError.UpsertInterviewStep -> upsertInterviewStep(error.answer)
        }
    }

    override fun updateCurrentStep(step: Int) {
            val currentStep = uiState.value.steps?.get(step - 1) ?: return
            _uiState.update {
                it.copy(currentStepInt = step, currentStep = currentStep)
            }
    }

    override fun initalizeSteps() {
        viewModelScope.launch {
            val result =
                getInterviewWithSteps.invoke(
                    GetInterviewWithSteps.Params(
                        interviewId ?: return@launch,
                    ),
                )
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            currentStepInt = 0, // caution, when user clicks to the next button, then increment by 1
                            currentStep = result.data.steps!![0],
                            steps = result.data.steps,
                        )
                    }
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
                                    retryOperation(EnterInterviewError.Initialization)
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun upsertInterviewStep(answer: String) {
        viewModelScope.launch {
            val interviewStep =
                uiState.value.currentStep?.copy(answer = answer) ?: run {
                    // showDialog
                    return@launch
                }
            val result = upsertInterviewStep.invoke(UpsertInterviewStep.Params(interviewStep))
            when (result) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.app_name,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.app_name) {
                                    updateCurrentStep(uiState.value.currentStepInt!!)
                                },
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
                                    retryOperation(EnterInterviewError.UpsertInterviewStep(answer))
                                }
                            )
                        )
                    )
                }
            }
        }
    }
}

data class EnterInterviewUiState(
    private val dialogData: DialogData? = null,
    private val totalSteps: Int? = null,
    internal val currentStepInt: Int? = null,
    internal val currentStep: InterviewStep? = null,
    internal val steps: List<InterviewStep>? = null,
)

sealed class EnterInterviewError {
    data class UpsertInterviewStep(val answer: String): EnterInterviewError()
    object Initialization: EnterInterviewError()
}

sealed class EnterInterviewEvent {
    object Back : EnterInterviewEvent()

    data class MicrophoneState(private val isEnabled: Boolean) : EnterInterviewEvent()

    data class VideoState(private val isEnabled: Boolean) : EnterInterviewEvent()
}
