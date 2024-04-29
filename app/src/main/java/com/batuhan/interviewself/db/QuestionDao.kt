package com.batuhan.interviewself.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.batuhan.interviewself.data.model.Question

@Dao
interface QuestionDao {

    @Query("SELECT * FROM question")
    fun getAllQuestions(): PagingSource<Int, Question>

    @Upsert
    suspend fun upsertQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)
}