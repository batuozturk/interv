package com.batuhan.interviewself.data.repository

import androidx.paging.PagingData
import com.batuhan.interviewself.data.model.Question
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    fun getAllQuestions(): Flow<PagingData<Question>>

    suspend fun deleteQuestion(question: Question)

    suspend fun upsertQuestion(question: Question)
}