package com.batuhan.interv.presentation.interview.create.addstep

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.batuhan.interv.R
import com.batuhan.interv.data.model.InterviewStep
import com.batuhan.interv.data.model.Question
import com.batuhan.interv.data.model.QuestionFilterType
import com.batuhan.interv.data.model.findLanguageFilterType
import com.batuhan.interv.domain.interview.DeleteInterviewStep
import com.batuhan.interv.domain.interview.GetInterviewSteps
import com.batuhan.interv.domain.interview.UpsertInterviewStep
import com.batuhan.interv.domain.question.GetAllQuestions
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.DialogType
import com.batuhan.interv.util.Result
import com.batuhan.interv.util.ViewModelEventHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddStepViewModel @Inject constructor(
    private val upsertInterviewStep: UpsertInterviewStep,
    private val deleteInterviewStep: DeleteInterviewStep,
    getAllQuestions: GetAllQuestions,
    getInterviewSteps: GetInterviewSteps,
    savedStateHandle: SavedStateHandle
) : ViewModel(), AddStepEventHandler, ViewModelEventHandler<AddStepEvent, AddStepError> {

    companion object {
        private const val KEY_INTERVIEW_ID = "interviewId"
        private const val KEY_LANG_CODE = "langCode"
    }

    private val _event = Channel<AddStepEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    private val _uiState = MutableStateFlow(AddStepUiState())
    val uiState = _uiState.asStateFlow()

    val interviewId = MutableStateFlow(savedStateHandle.get<String>(KEY_INTERVIEW_ID)?.toLong())

    val language = MutableStateFlow(savedStateHandle.get<String>(KEY_LANG_CODE))

    val searchText = MutableStateFlow("")

    val filterPair = MutableStateFlow(Pair(searchText.value, language.value))

    @OptIn(ExperimentalCoroutinesApi::class)
    val questions = filterPair.flatMapLatest {
        getAllQuestions.invoke(GetAllQuestions.Params(it.first, findLanguageFilterType(it.second))).cachedIn(viewModelScope)
    }.cachedIn(viewModelScope)


    val steps = interviewId.flatMapLatest {
        if(it != null){
            getInterviewSteps.invoke(GetInterviewSteps.Params(it)).cachedIn(viewModelScope)
        }
        else emptyFlow()
    }.cachedIn(viewModelScope)

    fun initInterviewId(interviewId: Long){
        this.interviewId.value = interviewId
    }

    fun initLanguage(language: String){
        this.language.value = language
        filterPair.value = Pair("", language)
    }


    override fun sendEvent(event: AddStepEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }

    override fun showDialog(dialogData: DialogData) {
        viewModelScope.launch {
            if(uiState.value.dialogData != null){
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
        sendEvent(AddStepEvent.ClearDialog)
    }

    override fun retryOperation(error: AddStepError) {
        when(error){
            is AddStepError.AddStep -> addStep(error.question)
            is AddStepError.DeleteStep -> deleteStep(error.interviewStep)
        }
    }

    override fun addStep(question: Question) {
        viewModelScope.launch {
            val result = upsertInterviewStep.invoke(UpsertInterviewStep.Params(InterviewStep(question = question, interviewId = interviewId.value)))
            when (result) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.add_step,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.dismiss,::clearDialog),
                            )
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
                            title = R.string.delete_step,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.dismiss,::clearDialog),
                            )
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
                                    retryOperation(AddStepError.DeleteStep(interviewStep))
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun search(searchText: String) {
        this.searchText.value = searchText
        val pair = this.filterPair.value
        filterPair.value = Pair(searchText, pair.second)
        _uiState.update {
            it.copy(searchText = searchText)
        }
    }

}

data class AddStepUiState(
    internal val dialogData: DialogData? = null,
    private val interviewSteps: List<InterviewStep>? = null,
    internal val searchText: String = ""
)

sealed class AddStepError {
    data class AddStep(val question: Question): AddStepError()
    data class DeleteStep(val interviewStep: InterviewStep): AddStepError()
}

sealed class AddStepEvent {
    object Back : AddStepEvent()
    data class AddStep(val question: Question) : AddStepEvent()
    data class DeleteStep(val interviewStep: InterviewStep) : AddStepEvent()

    data class Search(val searchText: String): AddStepEvent()

    object ClearDialog: AddStepEvent()

}