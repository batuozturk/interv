package com.batuhan.interv.presentation.interview

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import com.batuhan.interv.R
import com.batuhan.interv.data.model.FilterType
import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.model.InterviewFilterType
import com.batuhan.interv.data.model.InterviewType
import com.batuhan.interv.data.model.Question
import com.batuhan.interv.domain.interview.DeleteInterview
import com.batuhan.interv.domain.interview.DeleteInterviewSteps
import com.batuhan.interv.domain.interview.GetAllInterviews
import com.batuhan.interv.domain.interview.UpsertInterview
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.DialogType
import com.batuhan.interv.util.Result
import com.batuhan.interv.util.ViewModelEventHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InterviewListViewModel @Inject constructor(
    private val deleteInterview: DeleteInterview,
    private val upsertInterview: UpsertInterview,
    getAllInterviews: GetAllInterviews,
    private val deleteInterviewSteps: DeleteInterviewSteps
) : ViewModel(), InterviewListEventHandler, ViewModelEventHandler<InterviewListEvent, InterviewListError> {

    companion object {
        internal val KEY_PREFERENCES_LANGUAGE = stringPreferencesKey("preferences_language")
    }

    val filterPair = MutableStateFlow(Pair("", InterviewFilterType.DEFAULT))

    @OptIn(ExperimentalCoroutinesApi::class)
    val interviews = filterPair.flatMapLatest { pair ->
        getAllInterviews.invoke(GetAllInterviews.Params(pair.first, pair.second)).cachedIn(viewModelScope)
    }.cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(InterviewListUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<InterviewListEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    private fun PagingData<Interview>.filterPagingData(filterType: InterviewFilterType, searchText: String): PagingData<Interview> {
        return this.filter {
            val condition1 = if(searchText.isNotEmpty() && searchText.isNotBlank()) it.interviewName!!.startsWith(searchText) else true
            val condition2 = when(filterType){
                        InterviewFilterType.LANG_EN -> it.langCode == "en-US"
                        InterviewFilterType.LANG_TR -> it.langCode == "tr-TR"
                        InterviewFilterType.LANG_FR -> it.langCode == "fr-FR"
                        InterviewFilterType.LANG_DE -> it.langCode == "de-DE"
                        InterviewFilterType.LANG_ES -> it.langCode == "es-ES"
                        InterviewFilterType.LANG_PL -> it.langCode == "pl-PL"
                        InterviewFilterType.LANG_AR -> it.langCode == "ar-AR"
                        InterviewFilterType.LANG_IT -> it.langCode == "it-IT"
                        InterviewFilterType.LANG_NO -> it.langCode == "no-NO"
                        InterviewFilterType.LANG_DA -> it.langCode == "da-DK"
                        InterviewFilterType.LANG_SV -> it.langCode == "sv-SE"
                        InterviewFilterType.COMPLETED -> it.completed == true
                        InterviewFilterType.NOT_COMPLETED -> it.completed == false
                        else -> true
                    }
            condition1 && condition2
        }

    }

    private val testInterviewVideo = Interview(
        interviewId = 0L,
        interviewName = "test phone interview",
        langCode = "en-US",
        interviewType = InterviewType.PHONE_CALL,
    )

    private val testInterviewPhone = Interview(
        interviewId = 1L,
        interviewName = "test video interview",
        langCode = "en-US",
        interviewType = InterviewType.VIDEO,
    )

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
                                    retryOperation(InterviewListError.DeleteInterviewSteps(interviewId))
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
                            title = R.string.delete_interview,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.undo){
                                    undoDeleteInterview()
                                    clearDialog()
                                },
                                DialogAction(R.string.dismiss){
                                    deleteInterviewStepsJob(interview.interviewId!!)
                                    clearDialog()
                                }
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
                    clearDialog()
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.retry) {
                                    retryOperation(InterviewListError.UndoDeleteInterview)
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun filterByText(filterText: String) {
        filterPair.update {
            it.copy(filterText, it.second)
        }
    }

    override fun filter(filterType: InterviewFilterType) {
        filterPair.update {
            it.copy(it.first, filterType)
        }
        _uiState.update {
            it.copy(selectedFilter = filterType)
        }
        clearFilterType()
    }

    fun setFilterType() {
        _uiState.update {
            it.copy(filterType = FilterType.Interview)
        }
    }

    fun clearFilterType(){
        _uiState.update {
            it.copy(filterType = null)
        }
    }


    override fun sendEvent(event: InterviewListEvent) {
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
        sendEvent(InterviewListEvent.ClearDialog)
    }

    override fun retryOperation(error: InterviewListError) {
        when(error){
            is InterviewListError.DeleteInterview -> {
                deleteInterview(error.interview)
            }
            InterviewListError.UndoDeleteInterview -> undoDeleteInterview()
            is InterviewListError.DeleteInterviewSteps -> deleteInterviewStepsJob(error.interviewId)
        }
    }

    suspend fun initializeTestInterviews(langCode: String){
        upsertInterview.invoke(UpsertInterview.Params(testInterviewPhone.copy(langCode = langCode)))
        upsertInterview.invoke(UpsertInterview.Params(testInterviewVideo.copy(langCode = langCode)))
    }


}

data class InterviewListUiState(
    internal val dialogData: DialogData? = null,
    internal val deletedInterview: Interview? = null,
    internal val filterType: FilterType.Interview? = null,
    internal val selectedFilter: InterviewFilterType = InterviewFilterType.DEFAULT
)

sealed class InterviewListError {
    data class DeleteInterview(val interview: Interview): InterviewListError()

    data class DeleteInterviewSteps(val interviewId: Long): InterviewListError()
    object UndoDeleteInterview: InterviewListError()
}

sealed class InterviewListEvent {
    object CreateInterview : InterviewListEvent()
    data class DeleteInterview(val interview: Interview) : InterviewListEvent()
    data class Detail(val interviewId: Long):InterviewListEvent()

    data class EnterInterview(val interviewId: Long, val interviewType: InterviewType, val langCode: String, val isTest: Boolean): InterviewListEvent()

    object OpenFilter: InterviewListEvent()

    object ClearDialog: InterviewListEvent()
}