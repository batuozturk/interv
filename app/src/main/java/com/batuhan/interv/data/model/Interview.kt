package com.batuhan.interv.data.model

import androidx.annotation.StringRes
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.batuhan.interv.R
import com.squareup.moshi.JsonClass

@Entity
@JsonClass(generateAdapter = true)
data class Interview(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("interviewId") val interviewId: Long? = null,
    @ColumnInfo("interviewName") val interviewName: String? = null,
    @ColumnInfo("questionDuration") val questionDuration: Int? = null,
    @ColumnInfo("interviewType") val interviewType: InterviewType? = null,
    @ColumnInfo("step") val step: Int? = null,
    @ColumnInfo("totalStep") val totalStep: Int? = null,
    @ColumnInfo("timestamp") val timestamp: Long? = null,
    @ColumnInfo("completed") val completed: Boolean? = false,
    @ColumnInfo("langCode") val langCode: String? = null
)

@Entity
@JsonClass(generateAdapter = true)
data class InterviewStep(
    @PrimaryKey(autoGenerate = true) val interviewStepId: Long? = null,
    @ColumnInfo("interviewId") val interviewId: Long? = null,
    @ColumnInfo("step") val step: Int? = null,
    @ColumnInfo("sentenceToTalk") val sentenceToTalk: String? = null,
    @ColumnInfo("question") val question: Question? = null,
    @ColumnInfo("answer") val answer: String? = null,
)

enum class InterviewType(
    @StringRes val text: Int,
) {
    VIDEO(R.string.type_video),
    PHONE_CALL(R.string.type_phone_call),
}

fun findType(code: String?) = code?.let { LanguageType.entries.indexOf(LanguageType.entries.find { it.code == code }) } ?: 0
