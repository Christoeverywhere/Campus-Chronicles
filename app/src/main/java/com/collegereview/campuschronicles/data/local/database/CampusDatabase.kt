package com.collegereview.campuschronicles.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.collegereview.campuschronicles.data.local.entities.*

@Database(
    entities = [
        BuildingEntity::class,
        GameStateEntity::class,
        QuestionEntity::class,
        EventEntity::class,
        UserEntity::class,
        ResponseEntity::class,
        SurveyEntity::class
    ],
    version = 7,
    exportSchema = false
)
abstract class CampusDatabase : RoomDatabase() {
    abstract fun buildingDao(): BuildingDao
    abstract fun gameStateDao(): GameStateDao
    abstract fun questionDao(): QuestionDao
    abstract fun eventDao(): EventDao
    abstract fun userDao(): UserDao
    abstract fun responseDao(): ResponseDao
    abstract fun surveyDao(): SurveyDao
}
