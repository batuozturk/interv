package com.batuhan.interviewself.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.batuhan.interviewself.data.model.Interview
import com.batuhan.interviewself.data.model.InterviewStep
import com.batuhan.interviewself.data.model.Question

private const val DB_VERSION = 1
@TypeConverters(QuestionConverter::class)
@Database(
    version = DB_VERSION,
    entities = [Question::class, Interview::class, InterviewStep::class],
    exportSchema = false
)
abstract class InterviewselfDatabase: RoomDatabase() {

    abstract val questionDao: QuestionDao

    abstract val interviewDao: InterviewDao
}