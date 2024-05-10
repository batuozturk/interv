package com.batuhan.interviewself.presentation.interview

import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewFilterType

interface InterviewListEventHandler {

    fun deleteInterview(interview: Interview)

    fun undoDeleteInterview()

    fun filterByText(filterText: String)

    fun filter(filterType: InterviewFilterType)
}