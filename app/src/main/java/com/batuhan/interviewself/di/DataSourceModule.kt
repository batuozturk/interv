package com.batuhan.interviewself.di

import com.batuhan.interviewself.data.source.InterviewLocalDataSource
import com.batuhan.interviewself.data.source.QuestionLocalDataSource
import com.batuhan.interviewself.db.InterviewDao
import com.batuhan.interviewself.db.QuestionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object DataSourceModule {

    @Provides
    @ViewModelScoped
    fun provideInterviewDataSource(interviewDao: InterviewDao): InterviewLocalDataSource {
        return InterviewLocalDataSource(interviewDao)
    }

    @Provides
    @ViewModelScoped
    fun provideQuestionLocalDataSource(questionDao: QuestionDao): QuestionLocalDataSource {
        return QuestionLocalDataSource(questionDao)
    }
}