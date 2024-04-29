package com.batuhan.interviewself.domain.interview

import com.batuhan.interviewself.data.model.InterviewWithSteps
import com.batuhan.interviewself.data.repository.InterviewRepository
import com.batuhan.interviewself.util.Result
import javax.inject.Inject

class GetInterviewWithSteps @Inject constructor(private val repository: InterviewRepository) {

    data class Params(val interviewId: Long)

    suspend operator fun invoke(params: Params): Result<InterviewWithSteps> {
        return runCatching {
            Result.Success(repository.getInterviewWithSteps(params.interviewId))
        }.getOrElse {
            Result.Error(it)
        }
    }
}