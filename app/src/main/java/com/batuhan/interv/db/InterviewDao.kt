package com.batuhan.interv.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Transaction
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.model.InterviewWithSteps
import com.batuhan.interv.data.model.InterviewStep

@Dao
interface InterviewDao {

    @RawQuery(observedEntities = [Interview::class])
    fun getAllInterviews(query: SimpleSQLiteQuery): PagingSource<Int, Interview>

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

    @Query("SELECT * FROM interviewstep WHERE :interviewId = interviewId")
    fun getInterviewSteps(interviewId: Long): PagingSource<Int, InterviewStep>
}