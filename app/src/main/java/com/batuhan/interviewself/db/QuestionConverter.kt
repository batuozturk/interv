package com.batuhan.interviewself.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.batuhan.interviewself.data.model.Question
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@ProvidedTypeConverter
class QuestionConverter {

    @TypeConverter
    fun fromJson(question: String) =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(Question::class.java).fromJson(question)

    @TypeConverter
    fun toJson(question: Question) =
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build().adapter(Question::class.java).toJson(question)
}