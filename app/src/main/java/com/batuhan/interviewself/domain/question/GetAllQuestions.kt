package com.batuhan.interviewself.domain.question

import com.batuhan.interviewself.data.repository.QuestionRepository
import javax.inject.Inject

class GetAllQuestions @Inject constructor(private val repository: QuestionRepository) {

    operator fun invoke() = repository.getAllQuestions()
}