package com.batuhan.interviewself.presentation.interview

import com.batuhan.interviewself.data.model.Interview

interface InterviewListEventHandler {

    fun deleteInterview(interview: Interview)

    fun undoDeleteInterview()
}