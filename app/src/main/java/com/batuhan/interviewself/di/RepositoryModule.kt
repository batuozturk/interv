package com.batuhan.interviewself.di

import com.batuhan.interviewself.data.repository.InterviewRepository
import com.batuhan.interviewself.data.repository.InterviewRepositoryImpl
import com.batuhan.interviewself.data.repository.QuestionRepository
import com.batuhan.interviewself.data.repository.QuestionRepositoryImpl
import com.batuhan.interviewself.data.source.InterviewLocalDataSource
import com.batuhan.interviewself.data.source.QuestionLocalDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object RepositoryModule {

    @Provides
    @ViewModelScoped
    fun provideInterviewRepository(interviewLocalDataSource: InterviewLocalDataSource): InterviewRepository {
        return InterviewRepositoryImpl(interviewLocalDataSource)
    }

    @Provides
    @ViewModelScoped
    fun provideQuestionRepository(questionLocalDataSource: QuestionLocalDataSource): QuestionRepository {
        return QuestionRepositoryImpl(questionLocalDataSource)
    }
}