package com.batuhan.interviewself.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity
data class Question(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("questionId") val questionId: Long,
    @ColumnInfo("question") val question: String?,
    @ColumnInfo("answer") val answer: String?
)
