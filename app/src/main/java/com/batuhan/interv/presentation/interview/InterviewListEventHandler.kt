package com.batuhan.interv.presentation.interview

import com.batuhan.interv.data.model.Interview
import com.batuhan.interv.data.model.InterviewFilterType

interface InterviewListEventHandler {

    fun deleteInterview(interview: Interview)

    fun undoDeleteInterview()

    fun filterByText(filterText: String)

    fun filter(filterType: InterviewFilterType)
}