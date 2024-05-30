package com.batuhan.interv.domain.interview

import com.batuhan.interv.data.model.InterviewWithSteps
import com.batuhan.interv.data.repository.InterviewRepository
import com.batuhan.interv.util.Result
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