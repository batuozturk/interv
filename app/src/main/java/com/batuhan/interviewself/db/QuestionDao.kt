package com.batuhan.interviewself.db

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Upsert
import androidx.sqlite.db.SimpleSQLiteQuery
import com.batuhan.interviewself.data.model.Question

@Dao
interface QuestionDao {

    @RawQuery(observedEntities = [Question::class])
    fun getAllQuestions(query: SimpleSQLiteQuery): PagingSource<Int, Question>

    @Upsert
    suspend fun upsertQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)
}