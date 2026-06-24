package com.collegereview.campuschronicles.data.repository

import android.util.Log
import com.collegereview.campuschronicles.data.local.database.*
import com.collegereview.campuschronicles.data.local.entities.*
import com.collegereview.campuschronicles.di.SeedData
import com.collegereview.campuschronicles.domain.models.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val buildingDao: BuildingDao,
    private val gameStateDao: GameStateDao,
    private val questionDao: QuestionDao,
    private val responseDao: ResponseDao,
    private val eventDao: EventDao,
    private val userDao: UserDao,
    private val surveyDao: SurveyDao
) {
    // ── Buildings ──────────────────────────────────────────────────────────
    fun getBuildings(userId: String): Flow<List<Building>> =
        buildingDao.getBuildingsByUser(userId).map { list ->
            list.map { it.toDomain() }
        }

    suspend fun placeBuilding(building: Building): Long =
        buildingDao.insertBuilding(building.toEntity())

    suspend fun upgradeBuilding(building: Building) {
        val baseCost = buildingCatalog.find { it.type == building.buildingType }?.unlockCost ?: 100
        val newLevel = (building.level + 1).coerceAtMost(20)
        val costToReachNext = calculateUpgradeCost(baseCost, newLevel)
        
        buildingDao.updateBuilding(
            building.toEntity().copy(
                level = newLevel,
                health = 100,
                lastUpgradedAt = System.currentTimeMillis(),
                costToUpgrade = costToReachNext
            )
        )
    }

    suspend fun updateBuildingHealth(buildingId: Long, health: Int) {
        val building = buildingDao.getBuildingById(buildingId) ?: return
        buildingDao.updateBuilding(building.copy(health = health.coerceIn(0, 100)))
    }

    // ── Game State ─────────────────────────────────────────────────────────
    fun observeGameState(userId: String): Flow<GameState?> =
        gameStateDao.observeGameState(userId).map { it?.toDomain() }

    suspend fun getGameState(userId: String): GameState? =
        gameStateDao.getGameStateForUser(userId)?.toDomain()

    suspend fun createInitialGameState(userId: String): GameState {
        val state = GameState(userId = userId, totalCoins = 50000, xp = 500)
        gameStateDao.insertGameState(state.toEntity())
        return state
    }

    suspend fun updateCoins(userId: String, delta: Int) {
        val state = gameStateDao.getGameStateForUser(userId) ?: return
        val newCoins = (state.totalCoins + delta).coerceAtLeast(0)
        gameStateDao.updateGameState(state.copy(totalCoins = newCoins, lastSavedAt = System.currentTimeMillis()))
    }

    suspend fun processDailyLogin(userId: String): Int? {
        val state = gameStateDao.getGameStateForUser(userId) ?: return null
        val now = System.currentTimeMillis()
        
        // Check if already claimed today
        if (isSameDay(state.lastClaimDate, now)) return null
        
        val isConsecutive = isYesterday(state.lastClaimDate, now)
        val newStreak = if (isConsecutive) (state.streakCount % 7) + 1 else 1
        val reward = getDailyReward(newStreak)
        
        gameStateDao.updateGameState(state.copy(
            totalCoins = state.totalCoins + reward,
            streakCount = newStreak,
            lastClaimDate = now,
            lastSavedAt = now
        ))
        return reward
    }

    suspend fun collectPassiveIncome(userId: String): Int? {
        val state = gameStateDao.getGameStateForUser(userId) ?: return null
        val now = System.currentTimeMillis()
        
        if (isSameDay(state.lastRevenueCollectionDate, now)) return null
        
        val buildings = buildingDao.getBuildingsByUserSync(userId)
        val totalRevenue = buildings.sumOf { getDailyRevenue(it.level) }
        
        if (totalRevenue > 0) {
            gameStateDao.updateGameState(state.copy(
                totalCoins = state.totalCoins + totalRevenue,
                lastRevenueCollectionDate = now,
                lastSavedAt = now
            ))
            return totalRevenue
        }
        return null
    }

    private fun isSameDay(millis1: Long, millis2: Long): Boolean {
        if (millis1 == 0L) return false
        val day1 = millis1 / (24 * 60 * 60 * 1000)
        val day2 = millis2 / (24 * 60 * 60 * 1000)
        return day1 == day2
    }

    private fun isYesterday(millis1: Long, millis2: Long): Boolean {
        if (millis1 == 0L) return false
        val day1 = millis1 / (24 * 60 * 60 * 1000)
        val day2 = millis2 / (24 * 60 * 60 * 1000)
        return day2 == day1 + 1
    }

    suspend fun updateXP(userId: String, delta: Int) {
        val state = gameStateDao.getGameStateForUser(userId) ?: return
        val newXp = (state.xp + delta).coerceAtLeast(0)
        
        // TASK 6: XP Leveling System
        // Level 1: 0 XP, Level 2: 100 XP, Level 3: 250 XP, Level 4: 500 XP, Level 5: 1000 XP
        val newSemester = when {
            newXp >= 1000 -> 5
            newXp >= 500 -> 4
            newXp >= 250 -> 3
            newXp >= 100 -> 2
            else -> 1
        }

        gameStateDao.updateGameState(state.copy(xp = newXp, semesterNumber = newSemester))
    }

    suspend fun updateCampusRating(userId: String) {
        val state = gameStateDao.getGameStateForUser(userId) ?: return
        // Recalculate rating from all responses
        val allResponses = responseDao.getAllResponsesSync() 
        if (allResponses.isEmpty()) return
        
        val values = allResponses.mapNotNull { resp: ResponseEntity ->
            // Try to extract a numeric value from "A. Strongly Disagree", "B. Disagree", etc.
            when {
                resp.responseValue.startsWith("A") -> 1f
                resp.responseValue.startsWith("B") -> 2f
                resp.responseValue.startsWith("C") -> 3f
                resp.responseValue.startsWith("D") -> 4f
                resp.responseValue.startsWith("E") -> 5f
                else -> resp.responseValue.toFloatOrNull()
            }
        }
        
        if (values.isEmpty()) return
        
        val avg = values.average().toFloat()
        val rating = if (avg.isNaN()) 0f else ((avg - 1f) / 4f * 5f).coerceIn(0f, 5f)
        gameStateDao.updateGameState(state.copy(campusRating = rating))
    }

    // ── Questions ──────────────────────────────────────────────────────────
    fun getAllQuestions(): Flow<List<SurveyQuestion>> =
        questionDao.getAllQuestions().map { list -> list.map { it.toDomain() } }

    fun getUnansweredQuestions(userId: String): Flow<List<SurveyQuestion>> {
        return observeGameState(userId).map { state ->
            val completed = state?.completedQuestions ?: emptyList()
            // This is a bit inefficient to do in map, but for small sets it's fine
            questionDao.getAllQuestionsSync().map { it.toDomain() }.filter { it.questionCode !in completed }
        }
    }

    suspend fun seedQuestionsIfEmpty() {
        // Seed if questions are missing or very few (handles updates to SeedData)
        if (questionDao.getQuestionCount() < 10) {
            questionDao.insertQuestions(SeedData.getDefaultQuestions())
        }
    }

    suspend fun forceReSeedQuestions() {
        questionDao.deleteAllQuestions()
        questionDao.insertQuestions(SeedData.getDefaultQuestions())
    }

    suspend fun insertQuestion(question: SurveyQuestion) {
        Log.d("GameRepository", "Inserting legacy question: ${question.questionCode}")
        questionDao.insertQuestion(question.toEntity())
    }

    // ── Surveys ────────────────────────────────────────────────────────────
    fun getAllSurveys(): Flow<List<Survey>> =
        surveyDao.getAllSurveys().map { list -> 
            Log.d("GameRepository", "Fetching all surveys, count: ${list.size}")
            list.map { it.toDomain() } 
        }

    suspend fun insertSurvey(survey: Survey) {
        try {
            Log.d("GameRepository", "Attempting to save survey: ${survey.title} (${survey.surveyId})")
            surveyDao.insertSurvey(survey.toEntity())
            Log.d("GameRepository", "Survey saved successfully")
        } catch (e: Exception) {
            Log.e("GameRepository", "Failed to save survey", e)
            throw e
        }
    }

    suspend fun getSurveysForAction(action: GameAction): List<Survey> {
        Log.d("GameRepository", "Querying surveys for action: ${action.name}")
        val results = surveyDao.getSurveysForAction(action.name).map { it.toDomain() }
        Log.d("GameRepository", "Found ${results.size} matching surveys")
        return results
    }

    // ── Events ─────────────────────────────────────────────────────────────
    fun getPendingEvents(userId: String): Flow<List<GameEvent>> =
        eventDao.getPendingEvents(userId).map { list -> list.map { it.toDomain() } }

    suspend fun resolveEvent(eventId: Long) =
        eventDao.markEventResolved(eventId)

    suspend fun seedEventsIfEmpty(userId: String) {
        if (eventDao.getPendingEventCount(userId) == 0) {
            eventDao.insertEvents(getDefaultEvents(userId))
        }
    }

    // ── Users ──────────────────────────────────────────────────────────────
    suspend fun getUser(userId: String): User? =
        userDao.getUserById(userId)?.toDomain()

    suspend fun saveUser(user: User) =
        userDao.insertUser(user.toEntity())

    fun getLeaderboard(): Flow<List<User>> =
        userDao.getLeaderboard().map { list -> list.map { it.toDomain() } }

    // ── Responses ──────────────────────────────────────────────────────────
    fun getAllResponses(): Flow<List<SurveyResponse>> =
        responseDao.getAllResponses().map { list -> list.map { it.toDomain() } }
}

// ── Entity ↔ Domain Mappers ───────────────────────────────────────────────────

fun BuildingEntity.toDomain() = Building(
    id = id, buildingType = BuildingType.valueOf(buildingType),
    name = name, level = level, health = health,
    xPosition = xPosition, yPosition = yPosition,
    lastUpgradedAt = lastUpgradedAt, isUnlocked = isUnlocked,
    costToUpgrade = costToUpgrade, userId = userId
)

fun Building.toEntity() = BuildingEntity(
    id = id, buildingType = buildingType.name,
    name = name, level = level, health = health,
    xPosition = xPosition, yPosition = yPosition,
    lastUpgradedAt = lastUpgradedAt, isUnlocked = isUnlocked,
    costToUpgrade = costToUpgrade, userId = userId
)

fun GameStateEntity.toDomain() = GameState(
    id = id, userId = userId, totalCoins = totalCoins,
    xp = xp,
    campusRating = campusRating, semesterNumber = semesterNumber,
    totalBuildings = totalBuildings, lastSavedAt = lastSavedAt,
    lastClaimDate = lastClaimDate,
    streakCount = streakCount,
    lastRevenueCollectionDate = lastRevenueCollectionDate,
    completedQuestions = try {
        Json.decodeFromString(completedQuestionsJson)
    } catch (e: Exception) { emptyList() }
)

fun GameState.toEntity() = GameStateEntity(
    id = id, userId = userId, totalCoins = totalCoins,
    xp = xp,
    campusRating = campusRating, semesterNumber = semesterNumber,
    totalBuildings = totalBuildings, lastSavedAt = lastSavedAt,
    lastClaimDate = lastClaimDate,
    streakCount = streakCount,
    lastRevenueCollectionDate = lastRevenueCollectionDate,
    completedQuestionsJson = Json.encodeToString(completedQuestions)
)

fun QuestionEntity.toDomain() = SurveyQuestion(
    id = id, questionTitle = questionTitle, questionText = questionText, questionCode = questionCode,
    category = try { QuestionCategory.valueOf(category) } catch (e: Exception) { QuestionCategory.INFRASTRUCTURE },
    responseType = try { ResponseType.valueOf(responseType) } catch (e: Exception) { ResponseType.LIKERT_5 },
    mappedGameAction = try { GameAction.valueOf(mappedGameAction) } catch (e: Exception) { GameAction.BUILDING_INSPECTION },
    targetGroup = try { UserGroup.valueOf(targetGroup) } catch (e: Exception) { UserGroup.ALL },
    correctAnswer = correctAnswer,
    coinsReward = coinsReward,
    xpReward = xpReward,
    isActive = isActive, createdAt = createdAt, weight = weight
)

fun SurveyQuestion.toEntity() = QuestionEntity(
    id = id, questionTitle = questionTitle, questionText = questionText, questionCode = questionCode,
    category = category.name, responseType = responseType.name,
    mappedGameAction = mappedGameAction.name, targetGroup = targetGroup.name,
    correctAnswer = correctAnswer,
    coinsReward = coinsReward,
    xpReward = xpReward,
    isActive = isActive, createdAt = createdAt, weight = weight
)

fun SurveyEntity.toDomain() = Survey(
    id = id,
    surveyId = surveyId,
    title = title,
    triggerAction = try { GameAction.valueOf(triggerAction) } catch (e: Exception) { GameAction.BUILDING_INSPECTION },
    questions = try { Json.decodeFromString(questionsJson) } catch (e: Exception) { emptyList() },
    createdAt = createdAt
)

fun Survey.toEntity() = SurveyEntity(
    id = id,
    surveyId = surveyId,
    title = title,
    triggerAction = triggerAction.name,
    questionsJson = Json.encodeToString(questions),
    createdAt = createdAt
)

fun EventEntity.toDomain() = GameEvent(
    id = id, eventCode = eventCode, title = title,
    description = description,
    eventType = try { EventType.valueOf(eventType) } catch (e: Exception) { EventType.FACULTY_REQUEST },
    optionA = optionA, optionB = optionB,
    optionAMappedQuestion = optionAMappedQuestion,
    optionBMappedQuestion = optionBMappedQuestion,
    coinsRewardA = coinsRewardA, coinsRewardB = coinsRewardB,
    ratingImpactA = ratingImpactA, ratingImpactB = ratingImpactB,
    isResolved = isResolved, resolvedAt = resolvedAt, userId = userId
)

fun UserEntity.toDomain() = User(
    id = id, displayName = displayName, email = email,
    userGroup = try { UserGroup.valueOf(userGroup) } catch (e: Exception) { UserGroup.STUDENT },
    department = department, collegeId = collegeId,
    totalScore = totalScore, campusLevel = campusLevel,
    lastActiveAt = lastActiveAt, isAdmin = isAdmin
)

fun User.toEntity() = UserEntity(
    id = id, displayName = displayName, email = email,
    userGroup = userGroup.name, department = department,
    collegeId = collegeId, totalScore = totalScore,
    campusLevel = campusLevel, lastActiveAt = lastActiveAt,
    isAdmin = isAdmin
)

fun ResponseEntity.toDomain() = SurveyResponse(
    id = id, questionId = questionId, questionCode = questionCode,
    userId = userId, userGroup = userGroup, responseValue = responseValue,
    gameActionTaken = gameActionTaken, buildingContext = buildingContext,
    eventContext = eventContext, recordedAt = recordedAt,
    sessionId = sessionId, synced = synced
)

fun getDefaultEvents(userId: String) = listOf(
    EventEntity(eventCode = "EVT_001", title = "The Research Renaissance",
        description = "Professor Chen reports the research centre needs new equipment. As Campus Manager, what's your call?",
        eventType = "FACULTY_REQUEST",
        optionA = "Invest in cutting-edge research tools (+150₡)",
        optionB = "Reallocate research funds to teaching (-50₡)",
        optionAMappedQuestion = "INF_003", optionBMappedQuestion = "INF_003",
        coinsRewardA = 150, coinsRewardB = -50,
        ratingImpactA = 0.3f, ratingImpactB = 0.1f, userId = userId),
    EventEntity(eventCode = "EVT_002", title = "The Digital Classroom Crisis",
        description = "Tech report: 60% of smart boards have software issues. How do you respond?",
        eventType = "INFRASTRUCTURE_ISSUE",
        optionA = "Emergency upgrade: new digital teaching tools (-200₡)",
        optionB = "Schedule gradual upgrades over the semester (-50₡)",
        optionAMappedQuestion = "DIG_002", optionBMappedQuestion = "DIG_003",
        coinsRewardA = -200, coinsRewardB = -50,
        ratingImpactA = 0.4f, ratingImpactB = 0.1f, userId = userId),
    EventEntity(eventCode = "EVT_003", title = "Student Wellness Alert",
        description = "The student wellness report is in. The medical centre reports low utilisation. What action do you take?",
        eventType = "STUDENT_COMPLAINT",
        optionA = "Launch a campus wellness awareness campaign (+100₡)",
        optionB = "Improve sports and recreation facilities (-100₡)",
        optionAMappedQuestion = "STU_001", optionBMappedQuestion = "HOL_001",
        coinsRewardA = 100, coinsRewardB = -100,
        ratingImpactA = 0.2f, ratingImpactB = 0.3f, userId = userId),
    EventEntity(eventCode = "EVT_004", title = "The AI Teaching Debate",
        description = "The faculty senate is debating AI tool adoption for grading. Cast your vote as Campus Manager:",
        eventType = "POLICY_DECISION",
        optionA = "Endorse AI grading and feedback tools (+50₡)",
        optionB = "Keep traditional assessment methods (0₡)",
        optionAMappedQuestion = "DIG_004", optionBMappedQuestion = "DIG_004",
        coinsRewardA = 50, coinsRewardB = 0,
        ratingImpactA = 0.2f, ratingImpactB = 0.0f, userId = userId),
    EventEntity(eventCode = "EVT_005", title = "Library Expansion Proposal",
        description = "Students are demanding more study spaces. The library needs an expansion. Your decision?",
        eventType = "STUDENT_COMPLAINT",
        optionA = "Approve library expansion (-300₡)",
        optionB = "Optimise existing spaces (+50₡)",
        optionAMappedQuestion = "CAM_002", optionBMappedQuestion = "CAM_002",
        coinsRewardA = -300, coinsRewardB = 50,
        ratingImpactA = 0.4f, ratingImpactB = 0.1f, userId = userId),
    EventEntity(eventCode = "EVT_006", title = "Faculty Development Day",
        description = "HR proposes a professional development day for all faculty. Do you approve?",
        eventType = "FACULTY_REQUEST",
        optionA = "Approve full-day faculty training (+80₡)",
        optionB = "Offer optional online modules only (+20₡)",
        optionAMappedQuestion = "INF_001", optionBMappedQuestion = "DIG_001",
        coinsRewardA = 80, coinsRewardB = 20,
        ratingImpactA = 0.3f, ratingImpactB = 0.1f, userId = userId),
    EventEntity(eventCode = "EVT_007", title = "Campus Safety Inspection",
        description = "An official safety inspection is due. How do you prepare?",
        eventType = "INSPECTION_ALERT",
        optionA = "Full campus audit and immediate repairs (-150₡)",
        optionB = "Address only flagged areas (-50₡)",
        optionAMappedQuestion = "WRK_002", optionBMappedQuestion = "SAF_001",
        coinsRewardA = -150, coinsRewardB = -50,
        ratingImpactA = 0.5f, ratingImpactB = 0.2f, userId = userId)
)