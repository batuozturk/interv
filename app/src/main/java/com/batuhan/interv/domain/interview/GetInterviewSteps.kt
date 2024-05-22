package com.batuhan.interv.domain.interview

import com.batuhan.interv.data.repository.InterviewRepository
import javax.inject.Inject

class GetInterviewSteps @Inject constructor(private val repository: InterviewRepository) {

    data class Params(val interviewId: Long)

    operator fun invoke(params: Params) = repository.getInterviewSteps(params.interviewId)
}