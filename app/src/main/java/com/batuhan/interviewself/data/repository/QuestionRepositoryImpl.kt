package com.batuhan.interviewself.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.data.model.QuestionFilterType
import com.batuhan.interviewself.data.source.QuestionLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor(private val localDataSource: QuestionLocalDataSource) :
    QuestionRepository {

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun getAllQuestions(searchText: String, filterType: QuestionFilterType): Flow<PagingData<Question>> {
        return Pager(
            config = PagingConfig(
                PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = 2 * PAGE_SIZE
            ),
            pagingSourceFactory = {
                localDataSource.getAllQuestions(searchText, filterType)
            }
        ).flow
    }

    override suspend fun deleteQuestion(question: Question) {
        return localDataSource.deleteQuestion(question)
    }

    override suspend fun upsertQuestion(question: Question) {
        return localDataSource.upsertQuestion(question)
    }

    override suspend fun getAllQuestionsAsList(langCode: String): List<Question>? {
        return localDataSource.getAllQuestionsAsList(langCode)
    }

    override suspend fun upsertQuestions(list: List<Question>) {
        return localDataSource.upsertQuestions(list)
    }

}