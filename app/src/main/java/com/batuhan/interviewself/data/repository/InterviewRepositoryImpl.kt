package com.batuhan.interviewself.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewResult
import com.batuhan.interviewself.data.source.InterviewLocalDataSource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InterviewRepositoryImpl @Inject constructor(private val localDataSource: InterviewLocalDataSource) :
    InterviewRepository {

    companion object {
        private const val PAGE_SIZE = 20
    }

    override fun getAllInterviews(): Flow<PagingData<Interview>> {
        return Pager(
            config = PagingConfig(
                PAGE_SIZE,
                enablePlaceholders = false,
                prefetchDistance = 2 * PAGE_SIZE
            ),
            pagingSourceFactory = {
                localDataSource.getAllInterviews()
            }
        ).flow
    }

    override suspend fun deleteInterview(question: Interview) {
        return localDataSource.deleteInterview(question)
    }

    override suspend fun upsertInterview(question: Interview) {
        return localDataSource.upsertInterview(question)
    }

    override suspend fun getInterviewResult(interviewId: Long): InterviewResult {
        return localDataSource.getInterviewResult(interviewId)
    }

}