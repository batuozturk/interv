package com.batuhan.interv.presentation.interview.enter

import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.batuhan.interv.R
import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.model.InterviewStep
import com.batuhan.interv.data.model.MixedString
import com.batuhan.interv.domain.interview.GetInterviewWithSteps
import com.batuhan.interv.domain.interview.UpsertInterview
import com.batuhan.interv.domain.interview.UpsertInterviewStep
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.DialogType
import com.batuhan.interv.util.Result
import com.batuhan.interv.util.ViewModelEventHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterviewViewModel
    @Inject
    constructor(
        private val upsertInterviewStep: UpsertInterviewStep,
        private val getInterviewWithSteps: GetInterviewWithSteps,
        private val upsertInterview: UpsertInterview,
        savedStateHandle: SavedStateHandle,
    ) : ViewModel(),
        InterviewEventHandler,
        ViewModelEventHandler<InterviewEvent, InterviewError>,
        TextToSpeech.OnInitListener, RecognitionListener {
    companion object {
        private const val KEY_INTERVIEW_ID = "interviewId"
    }

    private val _event = Channel<InterviewEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    private val _uiState = MutableStateFlow(InterviewUiState())
    val uiState = _uiState.asStateFlow()

    val interviewId = savedStateHandle.get<String>(KEY_INTERVIEW_ID)?.toLong()

    val currentStepFlow = MutableStateFlow(-2)

    val myTurnStepFlow = MutableStateFlow(-2)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentStepToTalk =
        currentStepFlow.filter { it != -2 }.flatMapLatest {
            val mixedString =
                if (it == -1) {
                    MixedString(R.string.interview_start)
                }else if(it == uiState.value.steps?.size){
                    MixedString(R.string.interview_ended)
                } else if (it == 0) {
                    MixedString(R.string.first_question_talk, null, uiState.value.currentStep?.question?.question)
                } else {
                    MixedString(R.string.interview_question_talk, it + 1, uiState.value.currentStep?.question?.question)
                }
            flowOf(mixedString)
        }

    fun incrementMyTurnStep(){
        myTurnStepFlow.value += 1
    }

    override fun sendEvent(event: InterviewEvent) {
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

    override fun retryOperation(error: InterviewError) {
        clearDialog()
        when (error) {
            InterviewError.Initialization -> initalizeSteps()
            is InterviewError.UpsertInterviewStep -> upsertInterviewStep(error.answer)
        }
    }

    override fun updateCurrentStep(step: Int) {
        currentStepFlow.value = step
        if(step == uiState.value.steps?.size){
            return
        }

        val currentStep = uiState.value.steps?.get(step) ?: return
        _uiState.update {
            it.copy(currentStepInt = step, currentStep = currentStep)
        }
    }

    fun setCompleted(){
        viewModelScope.launch {
            val interview = uiState.value.interview?.copy(completed = true) ?: return@launch
            upsertInterview.invoke(UpsertInterview.Params(interview))
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
                            steps = result.data.steps,
                            interview = result.data.interview,
                            currentStep = result.data.steps?.get(0)
                        )
                    }
                    currentStepFlow.value = 0
                    setCompleted()
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions =
                                listOf(
                                    DialogAction(R.string.retry) {
                                        retryOperation(InterviewError.Initialization)
                                    },
                                ),
                        ),
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
                    updateCurrentStep(uiState.value.currentStepInt!! + 1)
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions =
                                listOf(
                                    DialogAction(R.string.retry) {
                                        retryOperation(InterviewError.UpsertInterviewStep(answer))
                                    },
                                ),
                        ),
                    )
                }
            }
        }
    }

    override fun configureCall(event: InterviewEvent) {
        if (event == InterviewEvent.Back) return
        _uiState.update {
            it.copy(
                isMicrophoneEnabled = if (event is InterviewEvent.MicrophoneState) event.isEnabled else uiState.value.isMicrophoneEnabled,
                isVideoEnabled = if (event is InterviewEvent.VideoState) event.isEnabled else uiState.value.isVideoEnabled,
            )
        }
    }

    fun resetStep() {
        currentStepFlow.value = -2
    }


    override fun onInit(p0: Int) {
        // no-op for now
        if (p0 == TextToSpeech.SUCCESS) {
            currentStepFlow.value = -1
            initalizeSteps()
        }
    }

    override fun onReadyForSpeech(p0: Bundle?) {
        Log.d("ready for speech", "true")
    }

    override fun onBeginningOfSpeech() {
        Log.d("speech start", "true")
    }

    override fun onRmsChanged(p0: Float) {
        // no-op for now
    }

    override fun onBufferReceived(p0: ByteArray?) {
        // no-op for now
    }

    override fun onEndOfSpeech() {
        // no-op
    }

    override fun onError(p0: Int) {
        if(p0 == SpeechRecognizer.ERROR_LANGUAGE_UNAVAILABLE){
            showDialog(
                DialogData(
                    title = R.string.speech_recognition_requires_internet,
                    type = DialogType.ERROR,
                    actions =
                    listOf(
                        DialogAction(R.string.retry) {
                            clearDialog()
                            sendEvent(InterviewEvent.ReinitializeSpeechRecognition)
                        },
                    ),
                ),
            )
        }
    }

    override fun onResults(p0: Bundle?) {
        val matches = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val answer = matches?.get(0) ?: return
        upsertInterviewStep(answer)
    }

    override fun onPartialResults(p0: Bundle?) {
        // no-op
    }

    override fun onEvent(p0: Int, p1: Bundle?) {
        // no-op
    }
}

data class InterviewUiState(
    internal val dialogData: DialogData? = null,
    private val totalSteps: Int? = null,
    internal val currentStepInt: Int? = null,
    internal val currentStep: InterviewStep? = null,
    internal val steps: List<InterviewStep>? = null,
    internal val isMicrophoneEnabled: Boolean = false,
    internal val isVideoEnabled: Boolean = false,
    internal val interview: Interview? = null
)

sealed class InterviewError {
    data class UpsertInterviewStep(val answer: String) : InterviewError()

    object Initialization : InterviewError()
}

sealed class InterviewEvent {
    object Back : InterviewEvent()

    data class MicrophoneState(internal val isEnabled: Boolean) : InterviewEvent()

    data class VideoState(internal val isEnabled: Boolean) : InterviewEvent()

    object ReinitializeSpeechRecognition: InterviewEvent()
}
