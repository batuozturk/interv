package com.batuhan.interviewself.domain.interview

import com.batuhan.interviewself.data.model.InterviewStep
import com.batuhan.interviewself.data.repository.InterviewRepository
import com.batuhan.interviewself.util.Result
import javax.inject.Inject

class UpsertInterviewStep @Inject constructor(private val repository: InterviewRepository) {

    data class Params(val interviewStep: InterviewStep)

    suspend operator fun invoke(params: Params): Result<Unit> {
        return runCatching {
            Result.Success(repository.upsertInterviewStep(params.interviewStep))
        }.getOrElse {
            Result.Error(it)
        }
    }
}