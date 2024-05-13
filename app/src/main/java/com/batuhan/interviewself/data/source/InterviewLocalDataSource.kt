package com.batuhan.interviewself.data.source

import androidx.sqlite.db.SimpleSQLiteQuery
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewFilterType
import com.batuhan.interviewself.data.model.InterviewStep
import com.batuhan.interviewself.data.model.createQuery
import com.batuhan.interviewself.db.InterviewselfDatabase
import javax.inject.Inject

class InterviewLocalDataSource @Inject constructor(private val database: InterviewselfDatabase) {
    fun getAllInterviews(
        searchText: String,
        filterType: InterviewFilterType,
    ) = database.interviewDao.getAllInterviews(
        SimpleSQLiteQuery(
            filterType.createQuery(searchText),
            searchText.takeIf { it.isNotBlank() && it.isNotEmpty() }
                ?.let { arrayOf(it + "%") },
        ),
    )

    suspend fun deleteInterview(interview: Interview) = database.interviewDao.deleteInterview(interview)

    suspend fun upsertInterview(interview: Interview) = database.interviewDao.upsertInterview(interview)

    suspend fun getInterviewWithSteps(interviewId: Long) = database.interviewDao.getInterviewWithSteps(interviewId)

    suspend fun upsertInterviewStep(interviewStep: InterviewStep) = database.interviewDao.upsertInterviewStep(interviewStep)

    suspend fun deleteInterviewStep(interviewStep: InterviewStep) = database.interviewDao.deleteInterviewStep(interviewStep)

    suspend fun deleteInterviewSteps(id:Long) = database.interviewDao.deleteInterviewSteps(id)

    suspend fun upsertInterviewSteps(steps: List<InterviewStep>) = database.interviewDao.upsertInterviewSteps(steps)

    fun getInterviewSteps(interviewId: Long) = database.interviewDao.getInterviewSteps(interviewId)
}