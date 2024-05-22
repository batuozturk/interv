package com.batuhan.interv.domain.interview

import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.repository.InterviewRepository
import com.batuhan.interv.util.Result
import javax.inject.Inject

class DeleteInterview @Inject constructor(private val repository: InterviewRepository) {

    data class Params(val interview:Interview)

    suspend operator fun invoke(params: Params): Result<Unit> {
        return runCatching {
            Result.Success(repository.deleteInterview(params.interview))
        }.getOrElse {
            Result.Error(it)
        }
    }
}