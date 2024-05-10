package com.batuhan.interviewself.domain.question

import com.batuhan.interviewself.data.model.InterviewFilterType
import com.batuhan.interviewself.data.model.QuestionFilterType
import com.batuhan.interviewself.data.repository.QuestionRepository
import javax.inject.Inject

class GetAllQuestions @Inject constructor(private val repository: QuestionRepository) {

    data class Params(val searchText: String, val filterType: QuestionFilterType)

    operator fun invoke(params: Params) = repository.getAllQuestions(params.searchText, params.filterType)
}