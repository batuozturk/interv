package com.batuhan.interv.presentation.interview.detail

import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.model.InterviewStep

interface InterviewDetailEventHandler {

    fun shareInterview(interview: Interview)

    fun deleteInterview(interview: Interview)

    fun retryInterview(interview: Interview, isTablet: Boolean)

    fun getInterviewWithSteps(interviewId: Long)

    fun setInterviewWithStepsAsInitial()

    fun upsertInterviewSteps(interviewId: Long, steps: List<InterviewStep>, isTablet: Boolean)
}