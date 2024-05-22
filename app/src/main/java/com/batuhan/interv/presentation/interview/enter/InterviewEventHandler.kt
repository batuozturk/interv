package com.batuhan.interv.presentation.interview.enter

interface InterviewEventHandler {

    fun updateCurrentStep(step:Int)

    fun initalizeSteps()

    fun upsertInterviewStep(answer: String)

    fun configureCall(event: InterviewEvent)
}