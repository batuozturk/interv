package com.batuhan.interviewself.data.source

import com.batuhan.interviewself.data.model.Question
import com.batuhan.interviewself.db.QuestionDao
import javax.inject.Inject

class QuestionLocalDataSource @Inject constructor(private val questionDao: QuestionDao) {

    fun getAllQuestions() = questionDao.getAllQuestions()

    suspend fun deleteQuestion(question: Question) = questionDao.deleteQuestion(question)

    suspend fun upsertQuestion(question: Question) = questionDao.upsertQuestion(question)



}