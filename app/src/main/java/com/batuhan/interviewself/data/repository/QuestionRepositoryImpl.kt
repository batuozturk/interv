package com.batuhan.interviewself.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.data.source.QuestionLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor(private val localDataSource: QuestionLocalDataSource) :
    QuestionRepository {

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun getAllQuestions(): Flow<PagingData<Question>> {
        return Pager(
            config = PagingConfig(
                PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = 2 * PAGE_SIZE
            ),
            pagingSourceFactory = {
                localDataSource.getAllQuestions()
            }
        ).flow
    }

    override suspend fun deleteQuestion(question: Question) {
        return localDataSource.deleteQuestion(question)
    }

    override suspend fun upsertQuestion(question: Question) {
        return localDataSource.upsertQuestion(question)
    }

}