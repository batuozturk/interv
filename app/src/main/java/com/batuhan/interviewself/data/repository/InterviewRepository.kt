package com.batuhan.interviewself.data.repository

import androidx.paging.PagingData
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewFilterType
import com.batuhan.interviewself.data.model.InterviewWithSteps
import com.batuhan.interviewself.data.model.InterviewStep
import kotlinx.coroutines.flow.Flow

interface InterviewRepository {
    fun getAllInterviews(searchText: String, filterType: InterviewFilterType): Flow<PagingData<Interview>>

    suspend fun deleteInterview(question: Interview)

    suspend fun upsertInterview(question: Interview): Long

    suspend fun getInterviewWithSteps(interviewId: Long): InterviewWithSteps

    suspend fun upsertInterviewStep(interviewStep: InterviewStep)

    suspend fun deleteInterviewStep(interviewStep: InterviewStep)

    suspend fun deleteInterviewSteps(id: Long)

    suspend fun upsertInterviewSteps(steps: List<InterviewStep>)

    fun getInterviewSteps(interviewId: Long): Flow<PagingData<InterviewStep>>
}