package com.batuhan.interviewself.presentation.question

import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.Question

interface QuestionListEventHandler {

    fun updateQuestion(question: Question)

    fun deleteQuestion(question: Question)

    fun createQuestion()

    fun updateQuestionText(string: String)

    fun updateLangCode(langCode: String)

    fun undoDeleteQuestion()

    fun setQuestionEditing(isEditing: Boolean, isSuccess: Boolean = false)
}