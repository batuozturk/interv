package com.batuhan.interviewself.domain.question

import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.data.repository.QuestionRepository
import com.batuhan.interviewself.util.Result
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