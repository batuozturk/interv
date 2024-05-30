package com.batuhan.interv.presentation.interview.create

import com.batuhan.interv.data.model.Interview

interface CreateInterviewEventHandler {

    fun createInterview()

    fun initializeInterview()

    fun updateCurrentSetup(interviewField: InterviewField)

    fun cancelInterview(interview: Interview)

    fun setInterviewAsInitial()

}