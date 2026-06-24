package com.collegereview.campuschronicles.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buildings")
data class BuildingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buildingType: String,
    val name: String,
    val level: Int,
    val health: Int,
    val xPosition: Int,
    val yPosition: Int,
    val lastUpgradedAt: Long,
    val isUnlocked: Boolean,
    val costToUpgrade: Int,
    val userId: String
)

@Entity(tableName = "game_state")
data class GameStateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val totalCoins: Int,
    val xp: Int = 0,
    val campusRating: Float,
    val semesterNumber: Int,
    val totalBuildings: Int,
    val lastSavedAt: Long,
    val lastClaimDate: Long = 0,
    val streakCount: Int = 0,
    val lastRevenueCollectionDate: Long = 0,
    val completedQuestionsJson: String
)

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionTitle: String? = null,
    val questionText: String,
    val questionCode: String,
    val category: String,
    val responseType: String,
    val mappedGameAction: String,
    val targetGroup: String,
    val correctAnswer: String? = null,
    val coinsReward: Int = 100,
    val xpReward: Int = 50,
    val isActive: Boolean,
    val createdAt: Long,
    val weight: Float
)

@Entity(tableName = "surveys")
data class SurveyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val surveyId: String,
    val title: String,
    val triggerAction: String,
    val questionsJson: String,
    val createdAt: Long
)

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val eventCode: String,
    val title: String,
    val description: String,
    val eventType: String,
    val optionA: String,
    val optionB: String,
    val optionAMappedQuestion: String,
    val optionBMappedQuestion: String,
    val coinsRewardA: Int,
    val coinsRewardB: Int,
    val ratingImpactA: Float,
    val ratingImpactB: Float,
    val isResolved: Boolean = false,
    val resolvedAt: Long = 0L,
    val userId: String
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val email: String,
    val userGroup: String,
    val department: String,
    val collegeId: String,
    val totalScore: Int,
    val campusLevel: Int,
    val lastActiveAt: Long,
    val isAdmin: Boolean
)

@Entity(tableName = "responses")
data class ResponseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: Long,
    val questionCode: String,
    val userId: String,
    val userGroup: String,
    val responseValue: String,
    val gameActionTaken: String,
    val buildingContext: String,
    val eventContext: String? = null,
    val recordedAt: Long = System.currentTimeMillis(),
    val sessionId: String,
    val synced: Boolean
)
