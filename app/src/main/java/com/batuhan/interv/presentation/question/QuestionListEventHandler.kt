package com.batuhan.interv.presentation.question

import com.batuhan.interv.data.model.Question
import com.batuhan.interv.data.model.QuestionFilterType

interface QuestionListEventHandler {
    fun updateQuestion(question: Question)

    fun deleteQuestion(question: Question)

    fun createQuestion()

    fun updateQuestionText(string: String)

    fun updateLangCode(langCode: String)

    fun undoDeleteQuestion()

    fun setQuestionEditing(
        isEditing: Boolean,
        isSuccess: Boolean = false,
    )

    fun filterByText(filterText: String)

    fun filter(filterType: QuestionFilterType)

    fun generateQuestions(apiKey: String)

    fun updateGenerateQuestionText(text: String)

    fun setGenerating(isGenerating: Boolean)
}
