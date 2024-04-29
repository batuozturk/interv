package com.batuhan.interviewself.data.source

import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.db.InterviewselfDatabase
import com.batuhan.interviewself.db.QuestionDao
import javax.inject.Inject

class QuestionLocalDataSource @Inject constructor(private val database: InterviewselfDatabase) {

    fun getAllQuestions() = database.questionDao.getAllQuestions()

    suspend fun deleteQuestion(question: Question) = database.questionDao.deleteQuestion(question)

    suspend fun upsertQuestion(question: Question) = database.questionDao.upsertQuestion(question)

}