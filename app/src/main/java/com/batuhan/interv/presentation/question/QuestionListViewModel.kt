package com.batuhan.interv.presentation.question

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.batuhan.interv.BuildConfig
import com.batuhan.interv.R
import com.batuhan.interv.data.model.FilterType
import com.batuhan.interv.data.model.LanguageType
import com.batuhan.interv.data.model.Question
import com.batuhan.interv.data.model.QuestionFilterType
import com.batuhan.interv.domain.question.DeleteQuestion
import com.batuhan.interv.domain.question.GetAllQuestions
import com.batuhan.interv.domain.question.GetAllQuestionsAsList
import com.batuhan.interv.domain.question.UpsertQuestion
import com.batuhan.interv.domain.question.UpsertQuestions
import com.batuhan.interv.presentation.interview.detail.InterviewDetailError
import com.batuhan.interv.util.DialogAction
import com.batuhan.interv.util.DialogData
import com.batuhan.interv.util.DialogType
import com.batuhan.interv.util.Result
import com.batuhan.interv.util.ViewModelEventHandler
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class QuestionListViewModel @Inject constructor(
    private val deleteQuestion: DeleteQuestion,
    private val upsertQuestion: UpsertQuestion,
    getAllQuestions: GetAllQuestions,
    private val getAllQuestionsAsList: GetAllQuestionsAsList,
    private val upsertQuestions: UpsertQuestions
) : ViewModel(), QuestionListEventHandler, ViewModelEventHandler<QuestionListEvent,QuestionListError> {

    val filterPair = MutableStateFlow(Pair("", QuestionFilterType.DEFAULT))

    @OptIn(ExperimentalCoroutinesApi::class)
    val questions = filterPair.flatMapLatest {
        getAllQuestions.invoke(GetAllQuestions.Params(it.first, it.second)).cachedIn(viewModelScope)
    }.cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(QuestionListUiState())
    val uiState = _uiState.asStateFlow()

    private val _event = Channel<QuestionListEvent> { Channel.BUFFERED }
    val event = _event.receiveAsFlow()

    override fun sendEvent(event: QuestionListEvent) {
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
        sendEvent(QuestionListEvent.ClearDialog)
    }

    override fun retryOperation(error: QuestionListError) {
        clearDialog()
        when(error){
            QuestionListError.CreateQuestion -> createQuestion()
            QuestionListError.QuestionEmpty,
            QuestionListError.LangCodeEmpty, QuestionListError.QuestionGenerateTextEmpty -> {  }
            is QuestionListError.DeleteQuestion -> deleteQuestion(error.question)
            is QuestionListError.UpdateQuestion -> updateQuestion(error.question)
            is QuestionListError.UndoDeleteQuestion -> undoDeleteQuestion()
            is QuestionListError.GenerateQuestions -> generateQuestions(error.apiKey)
        }
    }



    override fun updateQuestion(question: Question) {
        viewModelScope.launch {
            val result = upsertQuestion.invoke(UpsertQuestion.Params(question))
            when (result) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.update_question,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.dismiss,::clearDialog)
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
                                    retryOperation(QuestionListError.UpdateQuestion(question))
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            val result = deleteQuestion.invoke(DeleteQuestion.Params(question))
            when (result) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(deletedQuestion = question)
                    }
                    showDialog(
                        DialogData(
                            title = R.string.delete_question,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.undo){
                                   undoDeleteQuestion()
                                   clearDialog()
                                },
                                DialogAction(R.string.dismiss){
                                    _uiState.update {
                                        it.copy(deletedQuestion = null)
                                    }
                                    clearDialog()
                                },
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
                                    retryOperation(QuestionListError.DeleteQuestion(question))
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun createQuestion() {
        uiState.value.langCode.takeIf {
            it?.isNotEmpty() ?: false && it?.isNotBlank() ?: false
        } ?: run {
            _uiState.update {
                it.copy(langCode = LanguageType.EN.code)
            }
        }
        uiState.value.questionText.takeIf {
            it?.isNotEmpty() ?: false && it?.isNotBlank() ?: false
        } ?: run {
            showDialog(
                DialogData(
                    title = R.string.error_question_empty,
                    type = DialogType.ERROR,
                    actions = listOf(
                        DialogAction(R.string.dismiss) {
                            retryOperation(QuestionListError.QuestionEmpty)
                        }
                    )
                )
            )
            return
        }
        viewModelScope.launch {
            val result = upsertQuestion.invoke(
                UpsertQuestion.Params(
                    Question(
                        question = uiState.value.questionText,
                        langCode = uiState.value.langCode)
                )
            )
            when (result) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.success_question_saved,
                            type = DialogType.SUCCESS_INFO,
                            actions = listOf(
                                DialogAction(R.string.dismiss,::clearDialog),
                            )
                        )
                    )
                    setQuestionEditing(false, isSuccess = true)
                }

                is Result.Error -> {
                    showDialog(
                        DialogData(
                            title = R.string.error_unknown,
                            type = DialogType.ERROR,
                            actions = listOf(
                                DialogAction(R.string.retry) {
                                    retryOperation(QuestionListError.CreateQuestion)
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    override fun updateQuestionText(string: String) {
        _uiState.update {
            it.copy(questionText = string)
        }
    }

    override fun updateLangCode(langCode: String) {
        _uiState.update {
            it.copy(langCode = langCode)
        }
    }
    override fun setQuestionEditing(isEditing: Boolean, isSuccess: Boolean){
        if(!isEditing && !isSuccess) clearDialog()
        _uiState.update {
            if(!isEditing) it.copy(isEditing = false, questionText = null, langCode = null)
            else it.copy(isEditing = true)

        }
    }

    override fun filterByText(filterText: String) {
        filterPair.update {
            it.copy(filterText, it.second)
        }
    }

    override fun filter(filterType: QuestionFilterType) {
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
            it.copy(filterType = FilterType.Question)
        }
    }

    fun clearFilterType(){
        _uiState.update {
            it.copy(filterType = null)
        }
    }

    override fun undoDeleteQuestion() {
        viewModelScope.launch {
            val result = upsertQuestion.invoke(
                UpsertQuestion.Params(
                    uiState.value.deletedQuestion!!
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
                                    retryOperation(QuestionListError.UndoDeleteQuestion)
                                }
                            )
                        )
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun generateQuestions(apiKey: String) {
        val openAI = OpenAI(token = BuildConfig.openaiApiKey, logging = LoggingConfig(LogLevel.All))
        val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
            showDialog(
                DialogData(
                    title = R.string.error_unknown,
                    type = DialogType.ERROR,
                    actions = listOf(
                        DialogAction(R.string.dismiss) {
                            clearDialog()
                        },
                        DialogAction(R.string.retry) {
                            retryOperation(QuestionListError.GenerateQuestions(apiKey))
                        }
                    )
                )
            )
        }
        viewModelScope.launch(handler) {
            val locale = uiState.value.langCode ?: "en-US"
            val result =
                getAllQuestionsAsList.invoke(GetAllQuestionsAsList.Params(locale))
            if(result is Result.Error) return@launch
            val questionList = (result as Result.Success).data?.map {
                it.question
            }?.joinToString(",")

            val generateQuestionText = uiState.value.generateQuestionText ?: run {
                showDialog(
                    DialogData(
                        title = R.string.error_question_empty,
                        type = DialogType.ERROR,
                        actions = listOf(
                            DialogAction(R.string.dismiss) {
                                retryOperation(QuestionListError.QuestionGenerateTextEmpty)
                            }
                        )
                    )
                )
                return@launch
            }
            // state loading

            val chatCompletionRequest = ChatCompletionRequest(
                model = ModelId("gpt-4o-mini"),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.User,
                        content = "create 20 questions with locale $locale as JSON array and by following this rule: " + generateQuestionText +
                                ". Only respond with code as plain text without code block syntax around it." +
                                " Also any question that you generated is not included in this list : " + questionList
                    )
                )
            )
            setGenerating(false)
            val completion: ChatCompletion = openAI.chatCompletion(chatCompletionRequest)

            val response = completion.choices[0].message.content
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter<List<String>>()
            val list = moshi.fromJson(response ?: "")
            val questionsList = list?.map {
                Question(question = it, langCode = locale)
            } ?: return@launch
            val upsertResult = upsertQuestions.invoke(
                UpsertQuestions.Params(
                    questionsList
                )
            )
            when (upsertResult) {
                is Result.Success -> {
                    showDialog(
                        DialogData(
                            title = R.string.success_questions_saved, // TODO string
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
                                    retryOperation(QuestionListError.GenerateQuestions(apiKey))
                                }
                            )
                        )
                    )
                }
            }

        }
    }

    override fun updateGenerateQuestionText(text: String) {
        _uiState.update {
            it.copy(generateQuestionText = text)
        }
    }

    override fun setGenerating(isGenerating: Boolean) {
        _uiState.update {
            if(!isGenerating) it.copy(isGenerating = false, generateQuestionText = null, langCode = null)
            else it.copy(isGenerating = true)
        }
    }


}

data class QuestionListUiState(
    val dialogData: DialogData? = null,
    internal val deletedQuestion: Question? = null,
    internal val questionText: String? = null,
    internal val langCode: String? = null,
    internal val isEditing: Boolean = false,
    internal val isGenerating: Boolean = false,
    internal val generateQuestionText: String? = null,
    internal val filterType: FilterType.Question? = null,
    internal val selectedFilter: QuestionFilterType = QuestionFilterType.DEFAULT
)

sealed class QuestionListError {
    object CreateQuestion: QuestionListError()
    object QuestionEmpty: QuestionListError()
    object LangCodeEmpty: QuestionListError()
    data class DeleteQuestion(val question: Question): QuestionListError()
    data class UpdateQuestion(val question: Question): QuestionListError()
    object UndoDeleteQuestion: QuestionListError()
    object QuestionGenerateTextEmpty: QuestionListError()

    data class GenerateQuestions(val apiKey: String): QuestionListError()
}

sealed class QuestionListEvent {
    object CreateQuestion : QuestionListEvent()

    object InitializeQuestion: QuestionListEvent()
    data class DeleteQuestion(val question: Question) : QuestionListEvent()

    object OpenFilter: QuestionListEvent()

    object ClearDialog: QuestionListEvent()

    object GenerateQuestions: QuestionListEvent()

    object InitializeGenerateQuestions: QuestionListEvent()
}