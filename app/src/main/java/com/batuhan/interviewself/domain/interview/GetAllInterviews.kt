package com.batuhan.interviewself.domain.interview

import com.batuhan.interviewself.data.model.InterviewFilterType
import com.batuhan.interviewself.data.repository.InterviewRepository
import javax.inject.Inject

class GetAllInterviews @Inject constructor(private val repository: InterviewRepository) {

    data class Params(val searchText: String, val filterType: InterviewFilterType)

    operator fun invoke(params: Params) = repository.getAllInterviews(params.searchText, params.filterType)
}