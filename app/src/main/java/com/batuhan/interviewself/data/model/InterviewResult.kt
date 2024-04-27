package com.batuhan.interviewself.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class InterviewResult(
    @Embedded val interview: Interview?,
    @Relation(
        InterviewStep::class,
        parentColumn = "interviewId",
        entityColumn = "interviewId"
    ) val steps: List<InterviewStep>?
)
