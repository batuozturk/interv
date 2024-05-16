package com.batuhan.interviewself.domain.question

import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.data.repository.QuestionRepository
import com.batuhan.interviewself.util.Result
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