package com.batuhan.interviewself.domain.question

import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.data.repository.QuestionRepository
import com.batuhan.interviewself.util.Result
import javax.inject.Inject

class UpsertQuestion @Inject constructor(private val repository: QuestionRepository) {

    data class Params(val question:Question)

    suspend operator fun invoke(params: Params): Result<Unit> {
        return runCatching {
            Result.Success(repository.upsertQuestion(params.question))
        }.getOrElse {
            Result.Error(it)
        }
    }
}