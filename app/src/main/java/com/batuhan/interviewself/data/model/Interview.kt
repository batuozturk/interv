package com.batuhan.interviewself.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Interview(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("interviewId") val interviewId: Long,
    @ColumnInfo("interviewName") val interviewName: String?,
    @ColumnInfo("questionDuration") val questionDuration: Int?,
    @ColumnInfo("interviewType") val interviewType: InterviewType?,
    @ColumnInfo("step") val step: Int?,
    @ColumnInfo("totalStep") val totalStep: Int?,
    @ColumnInfo("timestamp") val timestamp: Long?,
    @ColumnInfo("completed") val completed: Boolean? = false
)

@Entity
data class InterviewStep(
    @PrimaryKey(autoGenerate = true) val interviewStepId: Long,
    @ColumnInfo("interviewId") val interviewId: Long,
    @ColumnInfo("step") val step: Int?,
    @ColumnInfo("sentenceToTalk") val sentenceToTalk: String?,
    @ColumnInfo("question") val question: Question,
    @ColumnInfo("answer") val answer: String,
)

enum class InterviewType {
    VIDEO, PHONE_CALL
}
