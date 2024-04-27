package com.batuhan.interviewself.data.repository

import androidx.paging.PagingData
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewResult
import kotlinx.coroutines.flow.Flow

interface InterviewRepository {
    fun getAllInterviews(): Flow<PagingData<Interview>>

    suspend fun deleteInterview(question: Interview)

    suspend fun upsertInterview(question: Interview)

    suspend fun getInterviewResult(interviewId: Long): InterviewResult
}