package com.collegereview.campuschronicles.di

import android.content.Context
import androidx.room.Room
import com.collegereview.campuschronicles.data.local.database.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): CampusDatabase {
        return Room.databaseBuilder(
            context,
            CampusDatabase::class.java,
            "campus_chronicles_db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideBuildingDao(database: CampusDatabase): BuildingDao = database.buildingDao()

    @Provides
    fun provideGameStateDao(database: CampusDatabase): GameStateDao = database.gameStateDao()

    @Provides
    fun provideQuestionDao(database: CampusDatabase): QuestionDao = database.questionDao()

    @Provides
    fun provideEventDao(database: CampusDatabase): EventDao = database.eventDao()

    @Provides
    fun provideUserDao(database: CampusDatabase): UserDao = database.userDao()

    @Provides
    fun provideResponseDao(database: CampusDatabase): ResponseDao = database.responseDao()

    @Provides
    fun provideSurveyDao(database: CampusDatabase): SurveyDao = database.surveyDao()
}
