package com.batuhan.interviewself.data.source

import androidx.sqlite.db.SimpleSQLiteQuery
import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.data.model.QuestionFilterType
import com.batuhan.interviewself.data.model.createQuery
import com.batuhan.interviewself.db.InterviewselfDatabase
import javax.inject.Inject

class QuestionLocalDataSource @Inject constructor(private val database: InterviewselfDatabase) {

    fun getAllQuestions(
        searchText: String,
        filterType: QuestionFilterType,
    ) = database.questionDao.getAllQuestions(
        SimpleSQLiteQuery(
            filterType.createQuery(searchText),
            searchText.takeIf { it.isNotBlank() && it.isNotEmpty() }
                ?.let { arrayOf(it + "%") },
        )
    )

    suspend fun deleteQuestion(question: Question) = database.questionDao.deleteQuestion(question)

    suspend fun upsertQuestion(question: Question) = database.questionDao.upsertQuestion(question)

}