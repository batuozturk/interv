package com.batuhan.interv.domain.interview

import com.batuhan.interv.data.model.InterviewStep
import com.batuhan.interv.data.repository.InterviewRepository
import com.batuhan.interv.util.Result
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