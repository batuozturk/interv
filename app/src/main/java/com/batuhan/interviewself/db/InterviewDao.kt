package com.batuhan.interviewself.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewResult
import com.batuhan.interviewself.data.model.InterviewStep

@Dao
interface InterviewDao {

    @Query("SELECT * FROM interview")
    fun getAllInterviews(): PagingSource<Long, Interview>

    @Upsert
    suspend fun upsertInterview(interview: Interview)

    @Upsert
    suspend fun upsertInterviewStep(interviewStep: InterviewStep)

    @Delete
    suspend fun deleteInterview(interview: Interview)

    @Transaction
    @Query("SELECT * FROM interview WHERE :id = interviewId")
    suspend fun getInterviewResult(id: Long): InterviewResult
}