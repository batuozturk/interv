package com.batuhan.interviewself.presentation.interview.detail

import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewStep

interface InterviewDetailEventHandler {

    fun shareInterview(interview: Interview)

    fun deleteInterview(interview: Interview)

    fun retryInterview(interview: Interview)

    fun getInterviewWithSteps(interviewId: Long)

    fun setInterviewWithStepsAsInitial()

    fun upsertInterviewSteps(interviewId: Long, steps: List<InterviewStep>)
}