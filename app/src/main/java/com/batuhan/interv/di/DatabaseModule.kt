package com.batuhan.interv.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.batuhan.interv.db.InterviewselfDatabase
import com.batuhan.interv.db.QuestionConverter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val migration_1_2 = object : Migration(1,2){
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE interviewStep ADD COLUMN suggestedAnswer TEXT DEFAULT NULL")
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): InterviewselfDatabase {
        return Room.databaseBuilder(context, InterviewselfDatabase::class.java, "interviewself").addTypeConverter(QuestionConverter()).addMigrations(
            migration_1_2).build()
    }
}