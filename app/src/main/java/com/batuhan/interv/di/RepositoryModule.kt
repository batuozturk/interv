package com.batuhan.interv.di

import com.batuhan.interv.data.repository.InterviewRepository
import com.batuhan.interv.data.repository.InterviewRepositoryImpl
import com.batuhan.interv.data.repository.QuestionRepository
import com.batuhan.interv.data.repository.QuestionRepositoryImpl
import com.batuhan.interv.data.source.InterviewLocalDataSource
import com.batuhan.interv.data.source.QuestionLocalDataSource
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