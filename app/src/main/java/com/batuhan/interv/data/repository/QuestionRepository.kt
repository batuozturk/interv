package com.batuhan.interv.data.repository

import androidx.paging.PagingData
import com.batuhan.interv.data.model.Question
import com.batuhan.interv.data.model.QuestionFilterType
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    fun getAllQuestions(searchText: String, filterType: QuestionFilterType): Flow<PagingData<Question>>

    suspend fun deleteQuestion(question: Question)

    suspend fun upsertQuestion(question: Question)

    suspend fun getAllQuestionsAsList(langCode: String) : List<Question>?

    suspend fun upsertQuestions(list: List<Question>)
}