package com.batuhan.interviewself.presentation.interview.detail

import com.batuhan.interviewself.data.model.Interview

interface InterviewDetailEventHandler {

    fun shareInterview(interview: Interview)

    fun deleteInterview(interview: Interview)

    fun retryInterview(interview: Interview)

    fun getInterviewWithSteps(interviewId: Long)

    fun setInterviewWithStepsAsInitial()
}