package com.batuhan.interviewself.db

import androidx.room.TypeConverter
import com.batuhan.interviewself.data.model.Question
import com.squareup.moshi.Moshi

class QuestionConverter {

    @TypeConverter
    fun fromJson(question: String) =
        Moshi.Builder().build().adapter(Question::class.java).fromJson(question)

    @TypeConverter
    fun toJson(question: Question) =
        Moshi.Builder().build().adapter(Question::class.java).toJson(question)
}