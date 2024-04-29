package com.batuhan.interviewself.data.source

import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewStep
import com.batuhan.interviewself.db.InterviewDao
import javax.inject.Inject

class InterviewLocalDataSource @Inject constructor(private val interviewDao: InterviewDao) {

    fun getAllInterviews() = interviewDao.getAllInterviews()

    suspend fun deleteInterview(interview: Interview) = interviewDao.deleteInterview(interview)

    suspend fun upsertInterview(interview: Interview) = interviewDao.upsertInterview(interview)

    suspend fun getInterviewWithSteps(interviewId: Long) = interviewDao.getInterviewWithSteps(interviewId)

    suspend fun upsertInterviewStep(interviewStep: InterviewStep) = interviewDao.upsertInterviewStep(interviewStep)

    suspend fun deleteInterviewStep(interviewStep: InterviewStep) = interviewDao.deleteInterviewStep(interviewStep)

    suspend fun deleteInterviewSteps(id:Long) = interviewDao.deleteInterviewSteps(id)
}