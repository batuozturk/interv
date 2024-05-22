package com.batuhan.interv.domain.interview

import com.batuhan.interv.data.model.InterviewStep
import com.batuhan.interv.data.repository.InterviewRepository
import com.batuhan.interv.util.Result
import javax.inject.Inject

class UpsertInterviewSteps @Inject constructor(private val repository: InterviewRepository) {

    data class Params(val steps: List<InterviewStep>)

    suspend operator fun invoke(params: Params):Result<Unit>{
        return runCatching {
            Result.Success(repository.upsertInterviewSteps(params.steps))
        }.getOrElse {
            Result.Error(it)
        }
    }
}