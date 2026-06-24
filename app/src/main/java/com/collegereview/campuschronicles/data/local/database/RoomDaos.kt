package com.collegereview.campuschronicles.data.local.database

import androidx.room.*
import com.collegereview.campuschronicles.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BuildingDao {
    @Query("SELECT * FROM buildings WHERE userId = :userId")
    fun getBuildingsByUser(userId: String): Flow<List<BuildingEntity>>

    @Query("SELECT * FROM buildings WHERE userId = :userId")
    suspend fun getBuildingsByUserSync(userId: String): List<BuildingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuilding(building: BuildingEntity): Long

    @Update
    suspend fun updateBuilding(building: BuildingEntity)

    @Query("SELECT * FROM buildings WHERE id = :buildingId")
    suspend fun getBuildingById(buildingId: Long): BuildingEntity?
}

@Dao
interface GameStateDao {
    @Query("SELECT * FROM game_state WHERE userId = :userId LIMIT 1")
    fun observeGameState(userId: String): Flow<GameStateEntity?>

    @Query("SELECT * FROM game_state WHERE userId = :userId LIMIT 1")
    suspend fun getGameStateForUser(userId: String): GameStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGameState(state: GameStateEntity)

    @Update
    suspend fun updateGameState(state: GameStateEntity)

    @Query("UPDATE game_state SET completedQuestionsJson = '[]' WHERE userId = :userId")
    suspend fun clearCompletedQuestions(userId: String)
}

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions")
    fun getAllQuestions(): Flow<List<QuestionEntity>>

    @Query("SELECT * FROM questions")
    suspend fun getAllQuestionsSync(): List<QuestionEntity>

    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getQuestionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Query("SELECT * FROM questions WHERE mappedGameAction = :action AND (targetGroup = :userGroup OR targetGroup = 'ALL')")
    suspend fun getQuestionsForAction(action: String, userGroup: String): List<QuestionEntity>

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()
}

@Dao
interface SurveyDao {
    @Query("SELECT * FROM surveys")
    fun getAllSurveys(): Flow<List<SurveyEntity>>

    @Query("SELECT * FROM surveys WHERE triggerAction = :action")
    suspend fun getSurveysForAction(action: String): List<SurveyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSurvey(survey: SurveyEntity)

    @Query("DELETE FROM surveys")
    suspend fun deleteAllSurveys()
}

@Dao
interface EventDao {
    @Query("SELECT * FROM events WHERE userId = :userId AND isResolved = 0")
    fun getPendingEvents(userId: String): Flow<List<EventEntity>>

    @Query("UPDATE events SET isResolved = 1, resolvedAt = :time WHERE id = :eventId")
    suspend fun markEventResolved(eventId: Long, time: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM events WHERE userId = :userId AND isResolved = 0")
    suspend fun getPendingEventCount(userId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<EventEntity>)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users ORDER BY totalScore DESC")
    fun getLeaderboard(): Flow<List<UserEntity>>
}

@Dao
interface ResponseDao {
    @Query("SELECT * FROM responses WHERE synced = 0")
    suspend fun getUnsyncedResponses(): List<ResponseEntity>

    @Query("SELECT * FROM responses")
    suspend fun getAllResponsesSync(): List<ResponseEntity>

    @Query("SELECT * FROM responses")
    fun getAllResponses(): Flow<List<ResponseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResponse(response: ResponseEntity)
}
