package com.batuhan.interv.domain.question

import com.batuhan.interv.data.model.Question
import com.batuhan.interv.data.repository.QuestionRepository
import com.batuhan.interv.util.Result
import javax.inject.Inject

class GetAllQuestionsAsList @Inject constructor(private val repository: QuestionRepository) {

    data class Params(val langCode: String)

    suspend operator fun invoke(params: Params): Result<List<Question>?>{
        return runCatching {
            Result.Success(repository.getAllQuestionsAsList(params.langCode))
        }.getOrElse {
            Result.Error(it)
        }
    }
}