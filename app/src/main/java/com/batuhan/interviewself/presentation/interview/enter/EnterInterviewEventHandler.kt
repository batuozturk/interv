package com.batuhan.interviewself.presentation.interview.enter

interface EnterInterviewEventHandler {

    fun updateCurrentStep(step:Int)

    fun initalizeSteps()

    fun upsertInterviewStep(answer: String)
}