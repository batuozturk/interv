package com.batuhan.interviewself.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class InterviewWithSteps(
    @Embedded val interview: Interview?= null,
    @Relation(
        InterviewStep::class,
        parentColumn = "interviewId",
        entityColumn = "interviewId"
    ) val steps: List<InterviewStep>? = null
)
