package com.batuhan.interv.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class InterviewWithSteps(
    @Embedded val interview: Interview?= null,
    @Relation(
        InterviewStep::class,
        parentColumn = "interviewId",
        entityColumn = "interviewId"
    ) val steps: List<InterviewStep>? = null
)
