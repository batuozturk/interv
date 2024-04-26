package com.batuhan.interviewself.data

data class InterviewResult(
    val interviewResults: List<InterviewResultItem>,
    val date: Long,
    val interviewResultId: Int,
    val interviewResultName: String
)

data class InterviewResultItem(
    val question: Question,
    val givenAnswer: String,
)
