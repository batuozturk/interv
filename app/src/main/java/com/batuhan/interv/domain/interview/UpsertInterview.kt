package com.batuhan.interv.domain.interview

import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.repository.InterviewRepository
import com.batuhan.interv.util.Result
import javax.inject.Inject

class UpsertInterview @Inject constructor(private val repository: InterviewRepository) {

    data class Params(val interview:Interview)

    suspend operator fun invoke(params: Params): Result<Long> {
        return runCatching {
            Result.Success(repository.upsertInterview(params.interview))
        }.getOrElse {
            Result.Error(it)
        }
    }
}