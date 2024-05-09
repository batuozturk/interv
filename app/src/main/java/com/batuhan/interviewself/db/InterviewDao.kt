package com.batuhan.interviewself.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewWithSteps
import com.batuhan.interviewself.data.model.InterviewStep

@Dao
interface InterviewDao {

    @Query("SELECT * FROM interview")
    fun getAllInterviews(): PagingSource<Int, Interview>

    @Upsert
    suspend fun upsertInterview(interview: Interview): Long

    @Upsert
    suspend fun upsertInterviewStep(interviewStep: InterviewStep)

    @Delete
    suspend fun deleteInterviewStep(interviewStep: InterviewStep)

    @Query("DELETE FROM interviewstep WHERE :id = interviewId")
    suspend fun deleteInterviewSteps(id: Long)

    @Delete
    suspend fun deleteInterview(interview: Interview)

    @Transaction
    @Query("SELECT * FROM interview WHERE :id = interviewId")
    suspend fun getInterviewWithSteps(id: Long): InterviewWithSteps

    @Upsert
    suspend fun upsertInterviewSteps(steps: List<InterviewStep>)
}