package com.batuhan.interv.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.errorprone.annotations.Keep
import com.squareup.moshi.JsonClass

@Keep
@Entity
@JsonClass(generateAdapter = true)
data class Question(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("questionId") val questionId: Long? = null,
    @ColumnInfo("question") val question: String?,
    @ColumnInfo("langCode") val langCode: String?
)
