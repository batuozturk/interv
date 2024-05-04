package com.batuhan.interviewself.presentation.interview.create.addstep

import com.batuhan.interviewself.data.model.InterviewStep
import com.batuhan.interviewself.data.model.Question

interface AddStepEventHandler {

    fun addStep(question: Question)

    fun deleteStep(interviewStep: InterviewStep)


    fun getInterviewWithSteps(interviewId: Long)

}