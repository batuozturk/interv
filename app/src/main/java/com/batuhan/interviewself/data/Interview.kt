package com.batuhan.interviewself.data

data class Interview(
    val interviewId: Int,
    val interviewName: String,
    val questionDuration: Int,
    val questions: Question,
    val interviewType: InterviewType,
)

enum class InterviewType {
    VIDEO, PHONE_CALL
}
