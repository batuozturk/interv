package com.batuhan.interv.domain.interview

import com.batuhan.interv.data.repository.InterviewRepository
import com.batuhan.interv.util.Result
import javax.inject.Inject

class DeleteInterviewSteps @Inject constructor(private val repository: InterviewRepository) {

    data class Params(val interviewId: Long)

    suspend operator fun invoke(params: Params): Result<Unit> {
        return runCatching {
            Result.Success(repository.deleteInterviewSteps(params.interviewId))
        }.getOrElse {
            Result.Error(it)
        }
    }
}