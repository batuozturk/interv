package com.batuhan.interv.domain.question

import com.batuhan.interv.data.model.Question
import com.batuhan.interv.data.repository.QuestionRepository
import com.batuhan.interv.util.Result
import javax.inject.Inject

class UpsertQuestions @Inject constructor(private val repository: QuestionRepository) {

    data class Params(val list: List<Question>)

    suspend operator fun invoke(params: Params): Result<Unit>{
        return runCatching {
            Result.Success(repository.upsertQuestions(params.list))
        }.getOrElse {
            Result.Error(it)
        }
    }
}