package com.batuhan.interviewself.presentation.interview.create

import com.batuhan.interviewself.data.model.Interview

interface CreateInterviewEventHandler {

    fun createInterview()

    fun initializeInterview()

    fun updateCurrentSetup(interviewField: InterviewField)

    fun cancelInterview(interview: Interview)

    fun setInterviewAsInitial()

}