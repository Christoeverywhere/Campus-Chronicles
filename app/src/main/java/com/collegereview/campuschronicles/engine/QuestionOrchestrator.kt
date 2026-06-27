package com.collegereview.campuschronicles.engine

import com.collegereview.campuschronicles.data.local.database.GameStateDao
import com.collegereview.campuschronicles.data.local.database.QuestionDao
import com.collegereview.campuschronicles.data.local.database.ResponseDao
import com.collegereview.campuschronicles.data.local.database.SurveyDao
import com.collegereview.campuschronicles.data.local.entities.ResponseEntity
import com.collegereview.campuschronicles.domain.models.GameAction
import com.collegereview.campuschronicles.domain.models.Question
import com.collegereview.campuschronicles.domain.models.QuestionPresentationModel
import com.collegereview.campuschronicles.domain.models.ResponseType
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import javax.inject.Inject
import javax.inject.Singleton

import android.util.Log

@Singleton
class QuestionOrchestrator @Inject constructor(
    private val questionDao: QuestionDao,
    private val responseDao: ResponseDao,
    private val gameStateDao: GameStateDao,
    private val surveyDao: SurveyDao
) {
    // Returns a question to show for a given game action, or null if none available
    suspend fun triggerAction(
        action: GameAction,
        userGroup: String,
        userId: String,
        sessionId: String
    ): QuestionPresentationModel? {
        Log.d("QuestionOrchestrator", "Triggering action: $action for user: $userId")

        // Get matching questions from DB
        val allQuestions = questionDao.getQuestionsForAction(action.name, userGroup)
        Log.d("QuestionOrchestrator", "Found ${allQuestions.size} total questions for action $action")

        if (allQuestions.isEmpty()) return null

        // Get completed questions
        val gameState = gameStateDao.getGameStateForUser(userId) ?: return null
        val completed = try {
            Json.decodeFromString<List<String>>(gameState.completedQuestionsJson)
        } catch (e: Exception) { emptyList() }

        // Filter unanswered
        val unanswered = allQuestions.filter { it.questionCode !in completed }
        Log.d("QuestionOrchestrator", "Remaining unanswered questions: ${unanswered.size}")

        if (unanswered.isEmpty()) return null

        // Selection strategy: Randomly select one unanswered question
        val question = unanswered.random()

        Log.d("QuestionOrchestrator", "Selected Question ID: ${question.id}, Code: ${question.questionCode}, Text: ${question.questionText}")

        return mapToPresentation(question, action)
    }

    suspend fun getQuestionsForActionBatch(
        action: GameAction,
        userGroup: String,
        userId: String,
        maxCount: Int = 10
    ): List<QuestionPresentationModel> {
        val allQuestions = questionDao.getQuestionsForAction(action.name, userGroup)
        if (allQuestions.isEmpty()) return emptyList()

        val gameState = gameStateDao.getGameStateForUser(userId) ?: return emptyList()
        val completed = try {
            Json.decodeFromString<List<String>>(gameState.completedQuestionsJson)
        } catch (e: Exception) { emptyList() }

        val unanswered = allQuestions.filter { it.questionCode !in completed }
        
        return unanswered.take(maxCount).map { mapToPresentation(it, action) }
    }

    suspend fun getSurveyQuestionsForAction(
        action: GameAction
    ): List<QuestionPresentationModel> {
        Log.d("QuestionOrchestrator", "Fetching survey questions for action: $action")
        val surveys = surveyDao.getSurveysForAction(action.name)
        if (surveys.isEmpty()) {
            Log.d("QuestionOrchestrator", "No surveys found for action $action")
            return emptyList()
        }

        // Just take the most recent survey for now
        val surveyEntity = surveys.last()
        Log.d("QuestionOrchestrator", "Found survey: ${surveyEntity.title} (ID: ${surveyEntity.surveyId})")
        
        val questions: List<Question> = try {
            Json.decodeFromString(surveyEntity.questionsJson)
        } catch (e: Exception) {
            Log.e("QuestionOrchestrator", "Failed to decode survey questions JSON", e)
            emptyList()
        }

        Log.d("QuestionOrchestrator", "Survey contains ${questions.size} questions")

        return questions.map { q ->
            QuestionPresentationModel(
                questionId = 0, // Using 0 for surveys as they aren't in the questions table
                questionCode = q.id,
                questionTitle = surveyEntity.title,
                narrativeText = q.text,
                responseType = q.type,
                gameContext = action.name,
                buildingName = action.toBuildingName(),
                coinsReward = 75,
                xpReward = 30,
                options = q.options
            )
        }
    }

    private fun mapToPresentation(
        question: com.collegereview.campuschronicles.data.local.entities.QuestionEntity, 
        action: GameAction
    ): QuestionPresentationModel {
        return QuestionPresentationModel(
            questionId = question.id,
            questionCode = question.questionCode,
            questionTitle = question.questionTitle,
            narrativeText = question.questionText,
            responseType = try {
                ResponseType.valueOf(question.responseType)
            } catch (e: Exception) {
                ResponseType.LIKERT_5
            },
            gameContext = action.name,
            buildingName = action.toBuildingName(),
            correctAnswer = question.correctAnswer,
            coinsReward = question.coinsReward,
            xpReward = question.xpReward,
            options = emptyList()
        )
    }

    suspend fun recordResponse(
        questionId: Long,
        questionCode: String,
        responseValue: String,
        gameAction: String,
        buildingContext: String,
        userId: String,
        userGroup: String,
        sessionId: String
    ) {
        Log.d("QuestionOrchestrator", "Recording response for question $questionCode: $responseValue")
        // Save response
        responseDao.insertResponse(
            ResponseEntity(
                questionId = questionId,
                questionCode = questionCode,
                userId = userId,
                userGroup = userGroup,
                responseValue = responseValue,
                gameActionTaken = gameAction,
                buildingContext = buildingContext,
                sessionId = sessionId,
                synced = false
            )
        )
        // Mark question as completed in session
        val gameState = gameStateDao.getGameStateForUser(userId) ?: return
        val completed = try {
            Json.decodeFromString<List<String>>(gameState.completedQuestionsJson).toMutableList()
        } catch (e: Exception) { mutableListOf() }
        if (questionCode !in completed) {
            completed.add(questionCode)
            gameStateDao.updateGameState(
                gameState.copy(
                    completedQuestionsJson = Json.encodeToString(completed),
                    lastSavedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun GameAction.toBuildingName(): String = when (this) {
        GameAction.UPGRADE_LIBRARY -> "Central Library"
        GameAction.UPGRADE_LAB -> "Tech Park 1"
        GameAction.UPGRADE_CANTEEN -> "JAVA Canteen"
        GameAction.UPGRADE_SPORTS -> "Tech Park 2"
        GameAction.UPGRADE_ADMIN -> "Vendhar Square"
        GameAction.UPGRADE_MEDICAL -> "SRM Medical Centre"
        GameAction.UPGRADE_HOSTEL -> "Nelson Mandela Hostel"
        GameAction.UPGRADE_AUDITORIUM -> "T.P. Ganesan Auditorium"
        GameAction.UPGRADE_RESEARCH -> "NRC Building"
        GameAction.UPGRADE_INNOVATION -> "Hi-Tech Building"
        else -> "Campus"
    }
}
