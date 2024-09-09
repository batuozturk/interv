package com.batuhan.interv.presentation.interview.create.addstep

import com.batuhan.interv.data.model.InterviewStep
import com.batuhan.interv.data.model.Question

interface AddStepEventHandler {

    fun addStep(question: Question)

    fun deleteStep(interviewStep: InterviewStep)

    fun search(searchText: String)

}