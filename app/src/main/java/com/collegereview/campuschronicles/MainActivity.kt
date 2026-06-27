package com.collegereview.campuschronicles

import android.util.Log
import android.speech.tts.TextToSpeech
import android.widget.Toast
import java.util.Locale
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.collegereview.campuschronicles.data.repository.GameRepository
import com.collegereview.campuschronicles.domain.models.*
import com.collegereview.campuschronicles.engine.QuestionOrchestrator
import com.collegereview.campuschronicles.ui.admin.AdminScreen
import com.collegereview.campuschronicles.ui.components.BuildingDetailDialog
import com.collegereview.campuschronicles.ui.components.BottomNavigationBar
import com.collegereview.campuschronicles.ui.components.CampusDecisionDialog
import com.collegereview.campuschronicles.ui.navigation.Screen
import com.collegereview.campuschronicles.ui.screens.*
import com.collegereview.campuschronicles.ui.theme.CampusChroniclesTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class GameViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: GameRepository,
    private val orchestrator: QuestionOrchestrator
) : ViewModel() {
    private val userId: String
    private val sessionId = UUID.randomUUID().toString()

    init {
        val prefs = context.getSharedPreferences("campus_chronicles_prefs", Context.MODE_PRIVATE)
        var savedId = prefs.getString("user_id", null)
        if (savedId == null) {
            savedId = UUID.randomUUID().toString()
            prefs.edit().putString("user_id", savedId).apply()
        }
        userId = savedId

        viewModelScope.launch {
            if (repository.getUser(userId) == null) {
                repository.saveUser(User(id = userId, displayName = "Campus Manager", email = ""))
            }
            if (repository.getGameState(userId) == null) {
                repository.createInitialGameState(userId)
                repository.forceReSeedQuestions()
                repository.seedEventsIfEmpty(userId)
            } else {
                // Testing boost: Ensure existing users have at least 50000 Coins and 500 XP
                repository.updateCoins(userId, 50000)
                repository.updateXP(userId, 500)
                // Force seed to ensure latest questions are available
                repository.forceReSeedQuestions()
            }
            
            // Process economy tasks
            val loginReward = repository.processDailyLogin(userId)
            if (loginReward != null) {
                _rewardTitle.value = "DAILY REWARD"
                _rewardMessage.value = "Welcome back! You earned $loginReward coins for your login streak."
            }
            
            val revenue = repository.collectPassiveIncome(userId)
            if (revenue != null) {
                // If both happen, we might need a way to show multiple rewards or combine them
                val msg = if (_rewardMessage.value != null) {
                    _rewardMessage.value + "\n\nAlso, your buildings generated $revenue coins in revenue!"
                } else {
                    "Your buildings generated $revenue coins in revenue!"
                }
                _rewardTitle.value = "CAMPUS REVENUE"
                _rewardMessage.value = msg
            }
        }
    }

    val gameState: StateFlow<GameState?> = repository.observeGameState(userId)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val buildings: StateFlow<List<Building>> = repository.getBuildings(userId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val events: StateFlow<List<GameEvent>> = repository.getPendingEvents(userId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val unansweredQuestions: StateFlow<List<SurveyQuestion>> = repository.getUnansweredQuestions(userId)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _currentQuestion = MutableStateFlow<QuestionPresentationModel?>(null)
    val currentQuestion: StateFlow<QuestionPresentationModel?> = _currentQuestion

    private val _questionQueue = MutableStateFlow<List<QuestionPresentationModel>>(emptyList())
    val questionQueue: StateFlow<List<QuestionPresentationModel>> = _questionQueue

    private val _totalQuestionsInBatch = MutableStateFlow(0)
    val totalQuestionsInBatch: StateFlow<Int> = _totalQuestionsInBatch

    private var currentBuildingInfo: BuildingInfo? = null

    private val _selectedBuilding = MutableStateFlow<Pair<BuildingInfo, Building?>?>(null)
    val selectedBuilding: StateFlow<Pair<BuildingInfo, Building?>?> = _selectedBuilding

    private val _rewardMessage = MutableStateFlow<String?>(null)
    val rewardMessage: StateFlow<String?> = _rewardMessage

    private val _rewardTitle = MutableStateFlow<String>("RESULTS")
    val rewardTitle: StateFlow<String> = _rewardTitle

    private var pendingAction: (() -> Unit)? = null

    fun onBuildingClicked(buildingInfo: BuildingInfo) {
        val buildingState = buildings.value.find { it.buildingType == buildingInfo.type }
        _selectedBuilding.value = buildingInfo to buildingState
    }

    fun dismissBuildingDetail() {
        _selectedBuilding.value = null
    }

    fun startBuildingAction(buildingInfo: BuildingInfo) {
        _selectedBuilding.value = null
        currentBuildingInfo = buildingInfo
        viewModelScope.launch {
            try {
                // Try to find surveys first
                val surveyBatch = orchestrator.getSurveyQuestionsForAction(buildingInfo.gameAction)
                if (surveyBatch.isNotEmpty()) {
                    _totalQuestionsInBatch.value = surveyBatch.size
                    _questionQueue.value = surveyBatch
                    _currentQuestion.value = surveyBatch.first()
                } else {
                    // Fallback to legacy single questions
                    val batch = orchestrator.getQuestionsForActionBatch(buildingInfo.gameAction, "ALL", userId, 10)
                    if (batch.isNotEmpty()) {
                        _totalQuestionsInBatch.value = batch.size
                        _questionQueue.value = batch
                        _currentQuestion.value = batch.first()
                    } else {
                        placeOrUpgradeBuilding(buildingInfo, isFromQuestion = false)
                    }
                }
            } catch (e: Exception) {
                _rewardTitle.value = "ERROR"
                _rewardMessage.value = "Failed to start action: ${e.message}"
            }
        }
    }

    private fun placeOrUpgradeBuilding(buildingInfo: BuildingInfo, isFromQuestion: Boolean) {
        viewModelScope.launch {
            try {
                val currentBuildings = buildings.value
                val existing = currentBuildings.find { it.buildingType == buildingInfo.type }
                
                // Re-fetch state to get latest XP/Coins after survey
                val state = repository.getGameState(userId) ?: return@launch
                val currentCoins = state.totalCoins
                val currentXp = state.xp

                val feedbackPrefix = if (isFromQuestion) "Feedback Submitted! (+25 Coins, +10 XP)\n\n" else ""

                if (existing != null) {
                    val requirements = getUpgradeRequirements(existing.level + 1)
                    if (currentCoins >= requirements.coins && currentXp >= requirements.xp) {
                        repository.upgradeBuilding(existing)
                        repository.updateCoins(userId, -requirements.coins)
                        _rewardTitle.value = "BUILDING UPGRADED"
                        _rewardMessage.value = "${feedbackPrefix}${buildingInfo.displayName} is now Level ${existing.level + 1}!"
                    } else {
                        _rewardTitle.value = if (isFromQuestion) "FEEDBACK RECEIVED" else "NOT ENOUGH RESOURCES"
                        val reason = if (currentCoins < requirements.coins) {
                            "Need ${requirements.coins - currentCoins} more coins."
                        } else {
                            "Need ${requirements.xp - currentXp} more XP."
                        }
                        _rewardMessage.value = "${feedbackPrefix}Upgrade Failed: $reason"
                    }
                } else {
                    if (currentCoins >= buildingInfo.unlockCost) {
                        repository.placeBuilding(
                            Building(
                                buildingType = buildingInfo.type,
                                name = buildingInfo.displayName,
                                userId = userId,
                                costToUpgrade = getUpgradeRequirements(2).coins
                            )
                        )
                        repository.updateCoins(userId, -buildingInfo.unlockCost)
                        _rewardTitle.value = "CONSTRUCTION COMPLETE"
                        _rewardMessage.value = "${feedbackPrefix}${buildingInfo.displayName} has been built!"
                    } else {
                        _rewardTitle.value = if (isFromQuestion) "FEEDBACK RECEIVED" else "NOT ENOUGH COINS"
                        _rewardMessage.value = "${feedbackPrefix}Construction Failed: Need ${buildingInfo.unlockCost - currentCoins} more coins."
                    }
                }
            } catch (e: Exception) {
                _rewardTitle.value = "ERROR"
                _rewardMessage.value = "Action failed: ${e.message}"
            }
        }
    }

    fun submitQuestionResponse(responseValue: String) {
        viewModelScope.launch {
            try {
                val q = _currentQuestion.value ?: return@launch
                
                Log.d("GameViewModel", "Submitting response for question ${q.questionCode}: $responseValue")
                orchestrator.recordResponse(
                    questionId = q.questionId,
                    questionCode = q.questionCode,
                    responseValue = responseValue,
                    gameAction = q.gameContext,
                    buildingContext = q.buildingName,
                    userId = userId,
                    userGroup = "ALL",
                    sessionId = sessionId
                )
                
                // Immediately grant rewards
                repository.updateCoins(userId, q.coinsReward)
                repository.updateXP(userId, q.xpReward)
                repository.updateCampusRating(userId)
                
                val remaining = _questionQueue.value.drop(1)
                _questionQueue.value = remaining
                
                if (remaining.isNotEmpty()) {
                    _currentQuestion.value = remaining.first()
                } else {
                    Log.d("GameViewModel", "Survey batch complete. Building context: ${currentBuildingInfo?.displayName ?: "NONE"}")
                    _currentQuestion.value = null
                    if (currentBuildingInfo != null) {
                        placeOrUpgradeBuilding(currentBuildingInfo!!, isFromQuestion = true)
                    } else {
                        // Handle surveys completed from the Tasks screen or generic context
                        _rewardTitle.value = "ASSESSMENT COMPLETE"
                        _rewardMessage.value = "Thank you for your feedback! You've earned ${q.coinsReward} Coins and ${q.xpReward} XP for helping improve the campus."
                    }
                    currentBuildingInfo = null
                    _totalQuestionsInBatch.value = 0
                }
                
            } catch (e: Exception) {
                _rewardTitle.value = "ERROR"
                _rewardMessage.value = "Submission failed: ${e.message}"
            }
        }
    }

    fun dismissReward() {
        _rewardMessage.value = null
    }

    fun dismissQuestion() {
        _currentQuestion.value = null
        _questionQueue.value = emptyList()
        _totalQuestionsInBatch.value = 0
        currentBuildingInfo = null
    }

    fun addNewQuestion(question: SurveyQuestion) {
        viewModelScope.launch {
            repository.insertQuestion(question)
        }
    }

    fun addNewSurvey(survey: Survey, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                repository.insertSurvey(survey)
                onComplete?.invoke()
            } catch (e: Exception) {
                _rewardTitle.value = "ERROR"
                _rewardMessage.value = "Failed to save survey: ${e.message}"
            }
        }
    }
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var ttsInitStatus: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        initializeTts()

        enableEdgeToEdge()
        setContent {
            CampusChroniclesTheme {
                MainContent(onSpeak = { text ->
                    if (isTtsReady && tts != null) {
                        Log.d("TTS", "Speaking: $text")
                        val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "dec_id")
                        if (result == TextToSpeech.ERROR) {
                            Log.e("TTS", "Speak failed, attempting re-init")
                            isTtsReady = false
                            initializeTts()
                        }
                    } else {
                        val statusMsg = when (ttsInitStatus) {
                            null -> "Initializing..."
                            TextToSpeech.ERROR -> "Engine Error"
                            else -> "Status: $ttsInitStatus"
                        }
                        Log.w("TTS", "TTS not ready. Status: $statusMsg")
                        
                        if (ttsInitStatus == TextToSpeech.ERROR) {
                            Toast.makeText(this, "Voice Engine Error. Try checking your device TTS settings.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Voice engine is warming up...", Toast.LENGTH_SHORT).show()
                        }
                        
                        // If it failed before, try to re-init
                        if (ttsInitStatus == TextToSpeech.ERROR || tts == null) {
                            initializeTts()
                        }
                    }
                })
            }
        }
    }

    private fun initializeTts() {
        Log.d("TTS", "Starting TTS initialization")
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e("TTS", "Error cleaning up TTS: ${e.message}")
        }

        // Use applicationContext for initialization as it can be more stable
        tts = TextToSpeech(applicationContext) { status ->
            ttsInitStatus = status
            if (status == TextToSpeech.SUCCESS) {
                Log.d("TTS", "Initialization successful")
                Handler(Looper.getMainLooper()).post {
                    tts?.let { engine ->
                        // Log available engines for debugging
                        try {
                            val engines = engine.engines
                            Log.d("TTS", "Available engines: ${engines.map { it.name }}")
                        } catch (e: Exception) {
                            Log.e("TTS", "Error listing engines: ${e.message}")
                        }

                        // Try default language, fallback to US
                        val result = engine.setLanguage(Locale.getDefault())
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.w("TTS", "Default language not supported, trying US...")
                            engine.setLanguage(Locale.US)
                        }
                        isTtsReady = true
                        Log.d("TTS", "TTS ready and configured")
                    } ?: run {
                        Log.e("TTS", "TTS instance is null in callback!")
                    }
                }
            } else {
                Log.e("TTS", "Initialization failed with status: $status")
                isTtsReady = false
                // Check if any engines are even available
                try {
                    val engines = tts?.engines
                    if (engines.isNullOrEmpty()) {
                        Log.e("TTS", "CRITICAL: No TTS engines found on this device!")
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(this, "No voice engine found. Please install Google Speech Services.", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TTS", "Error checking engines: ${e.message}")
                }
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@Composable
fun MainContent(onSpeak: (String) -> Unit) {
    val navController = rememberNavController()
    val viewModel: GameViewModel = hiltViewModel()
    
    val gameState by viewModel.gameState.collectAsState()
    val currentQuestion by viewModel.currentQuestion.collectAsState()
    val questionQueue by viewModel.questionQueue.collectAsState()
    val totalQuestions by viewModel.totalQuestionsInBatch.collectAsState()
    val selectedBuilding by viewModel.selectedBuilding.collectAsState()
    val rewardMessage by viewModel.rewardMessage.collectAsState()
    val rewardTitle by viewModel.rewardTitle.collectAsState()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            NavHost(
                navController = navController,
                startDestination = Screen.Map.route,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Screen.Map.route) {
                    MapScreen(viewModel = viewModel, onAdminClick = { navController.navigate(Screen.Admin.route) })
                }
                composable(Screen.Build.route) {
                    BuildScreen(viewModel = viewModel)
                }
                composable(Screen.Upgrade.route) {
                    UpgradeScreen(viewModel = viewModel)
                }
                composable(Screen.Tasks.route) {
                    TasksScreen(viewModel = viewModel)
                }
                composable(Screen.Social.route) {
                    SocialScreen(viewModel = viewModel)
                }
                composable(Screen.Admin.route) {
                    AdminScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onSaveSurvey = { survey, onComplete -> 
                            viewModel.addNewSurvey(survey, onComplete) 
                        }
                    )
                }
            }

            // Global Overlays
            selectedBuilding?.let { (info, state) ->
                BuildingDetailDialog(
                    info = info,
                    state = state,
                    totalCoins = gameState?.totalCoins ?: 0,
                    totalXp = gameState?.xp ?: 0,
                    onAction = { viewModel.startBuildingAction(info) },
                    onDismiss = { viewModel.dismissBuildingDetail() }
                )
            }

            currentQuestion?.let { question ->
                val currentIndex = totalQuestions - questionQueue.size + 1
                CampusDecisionDialog(
                    question = question,
                    currentIndex = currentIndex,
                    totalCount = totalQuestions,
                    onSubmit = { viewModel.submitQuestionResponse(it) },
                    onDismiss = { viewModel.dismissQuestion() },
                    onSpeak = onSpeak
                )
            }

            rewardMessage?.let { message ->
                AlertDialog(
                    onDismissRequest = { viewModel.dismissReward() },
                    confirmButton = {
                        TextButton(onClick = { viewModel.dismissReward() }) {
                            Text("AWESOME", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold)
                        }
                    },
                    title = { Text(rewardTitle, fontWeight = FontWeight.ExtraBold) },
                    text = { Text(message) },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = Color.White
                )
            }
        }
    }
}
