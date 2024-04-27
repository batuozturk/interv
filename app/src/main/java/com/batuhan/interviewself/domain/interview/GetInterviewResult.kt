package com.batuhan.interviewself.domain.interview

import com.batuhan.interviewself.data.model.InterviewResult
import com.batuhan.interviewself.data.repository.InterviewRepository
import com.batuhan.interviewself.util.Result
import javax.inject.Inject

class GetInterviewResult @Inject constructor(private val repository: InterviewRepository) {

    data class Params(val interviewId: Long)

    suspend operator fun invoke(params: Params): Result<InterviewResult> {
        return runCatching {
            Result.Success(repository.getInterviewResult(params.interviewId))
        }.getOrElse {
            Result.Error(it)
        }
    }
}