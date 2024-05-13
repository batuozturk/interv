package com.batuhan.interviewself.domain.interview

import com.batuhan.interviewself.data.repository.InterviewRepository
import javax.inject.Inject

class GetInterviewSteps @Inject constructor(private val repository: InterviewRepository) {

    data class Params(val interviewId: Long)

    operator fun invoke(params: Params) = repository.getInterviewSteps(params.interviewId)
}