package com.collegereview.campuschronicles.domain.models
import kotlinx.serialization.Serializable

// ─── Enums ───────────────────────────────────────────────────────────────────
@Serializable
enum class BuildingType {
    LIBRARY, LAB, CANTEEN, SPORTS, ADMIN_BLOCK,
    MEDICAL, HOSTEL, AUDITORIUM, RESEARCH_CENTER, INNOVATION_HUB
}
@Serializable
enum class UserGroup { FACULTY, STUDENT, STAFF, ALL }
@Serializable
enum class QuestionCategory {
    INFRASTRUCTURE, DIGITAL_TOOLS, TEACHING,
    STUDENTS, WELLBEING, MANAGEMENT, RESOURCES
}
@Serializable
enum class ResponseType { LIKERT_5, BINARY, RATING_10, OPEN_TEXT, STARS, MULTIPLE_CHOICE }
@Serializable
enum class GameAction {
    UPGRADE_LIBRARY, UPGRADE_LAB, UPGRADE_CANTEEN, UPGRADE_SPORTS,
    UPGRADE_ADMIN, UPGRADE_MEDICAL, UPGRADE_HOSTEL, UPGRADE_AUDITORIUM,
    UPGRADE_RESEARCH, UPGRADE_INNOVATION,
    BUDGET_ALLOCATION, HIRE_FACULTY, PLACE_BUILDING,
    RESOLVE_EVENT_POSITIVE, RESOLVE_EVENT_NEGATIVE,
    BUILDING_INSPECTION, SEMESTER_REVIEW, RESOURCE_REQUEST,
    POLICY_VOTE, MAINTENANCE_ACTION, DAILY_LOGIN, LEVEL_COMPLETE
}
@Serializable
enum class EventType {
    BUDGET_CRISIS, FACULTY_REQUEST, STUDENT_COMPLAINT,
    INFRASTRUCTURE_ISSUE, ACHIEVEMENT_UNLOCK, POLICY_DECISION, INSPECTION_ALERT
}
// ─── Domain Models ────────────────────────────────────────────────────────────
data class Building(
    val id: Long = 0,
    val buildingType: BuildingType,
    val name: String,
    val level: Int = 1,
    val health: Int = 100,
    val xPosition: Int = 0,
    val yPosition: Int = 0,
    val lastUpgradedAt: Long = System.currentTimeMillis(),
    val isUnlocked: Boolean = false,
    val costToUpgrade: Int = 100,
    val userId: String = ""
)

data class SurveyQuestion(
    val id: Long = 0,
    val questionTitle: String? = null,
    val questionText: String,
    val questionCode: String,
    val category: QuestionCategory,
    val responseType: ResponseType = ResponseType.LIKERT_5,
    val mappedGameAction: GameAction,
    val targetGroup: UserGroup = UserGroup.ALL,
    val correctAnswer: String? = null,
    val coinsReward: Int = 100,
    val xpReward: Int = 50,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val weight: Float = 1.0f
)

@Serializable
data class Survey(
    val id: Long = 0,
    val surveyId: String = "",
    val title: String,
    val triggerAction: GameAction,
    val questions: List<Question> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class Question(
    val id: String = "",
    val text: String,
    val type: ResponseType,
    val options: List<String> = emptyList()
)

data class SurveyResponse(
    val id: Long = 0,
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
    val synced: Boolean = false
)

data class User(
    val id: String,
    val displayName: String,
    val email: String,
    val userGroup: UserGroup = UserGroup.STUDENT,
    val department: String = "",
    val collegeId: String = "",
    val totalScore: Int = 0,
    val campusLevel: Int = 1,
    val lastActiveAt: Long = System.currentTimeMillis(),
    val isAdmin: Boolean = false
)

data class GameState(
    val id: Long = 0,
    val userId: String,
    val totalCoins: Int = 1000,
    val xp: Int = 500,
    val campusRating: Float = 0f,
    val semesterNumber: Int = 1,
    val totalBuildings: Int = 0,
    val lastSavedAt: Long = System.currentTimeMillis(),
    val lastClaimDate: Long = 0,
    val streakCount: Int = 0,
    val lastRevenueCollectionDate: Long = 0,
    val completedQuestions: List<String> = emptyList()
)

data class GameEvent(
    val id: Long = 0,
    val eventCode: String,
    val title: String,
    val description: String,
    val eventType: EventType,
    val optionA: String,
    val optionB: String,
    val optionAMappedQuestion: String,
    val optionBMappedQuestion: String,
    val coinsRewardA: Int = 100,
    val coinsRewardB: Int = 50,
    val ratingImpactA: Float = 0.2f,
    val ratingImpactB: Float = 0.1f,
    val isResolved: Boolean = false,
    val resolvedAt: Long = 0L,
    val userId: String = ""
)

data class QuestionPresentationModel(
    val questionId: Long,
    val questionCode: String,
    val questionTitle: String? = null,
    val narrativeText: String,
    val responseType: ResponseType,
    val gameContext: String,
    val buildingName: String = "",
    val eventTitle: String = "",
    val correctAnswer: String? = null,
    val coinsReward: Int = 100,
    val xpReward: Int = 50,
    val options: List<String> = emptyList()
)

data class BuildingInfo(
    val type: BuildingType,
    val displayName: String,
    val description: String,
    val unlockCost: Int,
    val gameAction: GameAction
)

// ─── Building Catalog ─────────────────────────────────────────────────────────

val buildingCatalog = listOf(
    BuildingInfo(BuildingType.LIBRARY, "Central Library", "Heart of academic resources and research.", 50, GameAction.UPGRADE_LIBRARY),
    BuildingInfo(BuildingType.LAB, "Tech Park 1", "Engineering and Science laboratories.", 100, GameAction.UPGRADE_LAB),
    BuildingInfo(BuildingType.CANTEEN, "JAVA Canteen", "Iconic student hub and eatery.", 150, GameAction.UPGRADE_CANTEEN),
    BuildingInfo(BuildingType.SPORTS, "Tech Park 2", "Modern IT and software facilities.", 200, GameAction.UPGRADE_SPORTS),
    BuildingInfo(BuildingType.ADMIN_BLOCK, "Vendhar Square", "The symbolic heart of the campus.", 250, GameAction.UPGRADE_ADMIN),
    BuildingInfo(BuildingType.MEDICAL, "SRM Medical Centre", "Health and wellbeing for the community.", 300, GameAction.UPGRADE_MEDICAL),
    BuildingInfo(BuildingType.HOSTEL, "Nelson Mandela Hostel", "Home for students across the globe.", 350, GameAction.UPGRADE_HOSTEL),
    BuildingInfo(BuildingType.AUDITORIUM, "T.P. Ganesan Auditorium", "One of Asia's largest convention centres.", 400, GameAction.UPGRADE_AUDITORIUM),
    BuildingInfo(BuildingType.RESEARCH_CENTER, "NRC Building", "Nanotechnology Research Centre hub.", 500, GameAction.UPGRADE_RESEARCH),
    BuildingInfo(BuildingType.INNOVATION_HUB, "Hi-Tech Building", "Advanced computing and innovation labs.", 600, GameAction.UPGRADE_INNOVATION)
)

fun BuildingType.toGameAction(): GameAction = when (this) {
    BuildingType.LIBRARY -> GameAction.UPGRADE_LIBRARY
    BuildingType.LAB -> GameAction.UPGRADE_LAB
    BuildingType.CANTEEN -> GameAction.UPGRADE_CANTEEN
    BuildingType.SPORTS -> GameAction.UPGRADE_SPORTS
    BuildingType.ADMIN_BLOCK -> GameAction.UPGRADE_ADMIN
    BuildingType.MEDICAL -> GameAction.UPGRADE_MEDICAL
    BuildingType.HOSTEL -> GameAction.UPGRADE_HOSTEL
    BuildingType.AUDITORIUM -> GameAction.UPGRADE_AUDITORIUM
    BuildingType.RESEARCH_CENTER -> GameAction.UPGRADE_RESEARCH
    BuildingType.INNOVATION_HUB -> GameAction.UPGRADE_INNOVATION
}

fun GameAction.getDisplayName(): String = when (this) {
    GameAction.UPGRADE_LIBRARY -> "📚 Central Library"
    GameAction.UPGRADE_LAB -> "🔬 Tech Park 1"
    GameAction.UPGRADE_CANTEEN -> "🍴 JAVA Canteen"
    GameAction.UPGRADE_SPORTS -> "🏆 Tech Park 2"
    GameAction.UPGRADE_ADMIN -> "🏛️ Vendhar Square"
    GameAction.UPGRADE_MEDICAL -> "🏥 SRM Medical Centre"
    GameAction.UPGRADE_HOSTEL -> "🏨 Nelson Mandela Hostel"
    GameAction.UPGRADE_AUDITORIUM -> "🎭 T.P. Ganesan Auditorium"
    GameAction.UPGRADE_RESEARCH -> "🧬 NRC Building"
    GameAction.UPGRADE_INNOVATION -> "💡 Hi-Tech Building"
    else -> this.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
}

data class UpgradeRequirements(
    val coins: Int,
    val xp: Int
)

fun getUpgradeRequirements(nextLevel: Int): UpgradeRequirements {
    return when (nextLevel) {
        2 -> UpgradeRequirements(50, 20)
        3 -> UpgradeRequirements(100, 40)
        4 -> UpgradeRequirements(150, 60)
        5 -> UpgradeRequirements(200, 80)
        6 -> UpgradeRequirements(300, 120)
        7 -> UpgradeRequirements(400, 160)
        8 -> UpgradeRequirements(550, 220)
        9 -> UpgradeRequirements(700, 280)
        10 -> UpgradeRequirements(900, 360)
        11 -> UpgradeRequirements(1100, 450)
        12 -> UpgradeRequirements(1350, 550)
        13 -> UpgradeRequirements(1600, 650)
        14 -> UpgradeRequirements(1900, 750)
        15 -> UpgradeRequirements(2250, 900)
        16 -> UpgradeRequirements(2600, 1050)
        17 -> UpgradeRequirements(3000, 1200)
        18 -> UpgradeRequirements(3500, 1400)
        19 -> UpgradeRequirements(4000, 1600)
        20 -> UpgradeRequirements(4500, 1800)
        else -> UpgradeRequirements(99999, 99999)
    }
}

fun getDailyRevenue(level: Int): Int {
    return when (level) {
        1 -> 5
        2 -> 8
        3 -> 12
        4 -> 16
        5 -> 20
        6 -> 25
        7 -> 30
        8 -> 35
        9 -> 40
        10 -> 50
        11 -> 60
        12 -> 70
        13 -> 80
        14 -> 90
        15 -> 100
        16 -> 120
        17 -> 140
        18 -> 160
        19 -> 180
        20 -> 200
        else -> 0
    }
}

fun getDailyReward(day: Int): Int {
    return when (day) {
        1 -> 50
        2 -> 75
        3 -> 100
        4 -> 125
        5 -> 150
        6 -> 175
        7 -> 250
        else -> 50
    }
}

fun calculateUpgradeCost(baseCost: Int, currentLevel: Int): Int {
    return getUpgradeRequirements(currentLevel + 1).coins
}