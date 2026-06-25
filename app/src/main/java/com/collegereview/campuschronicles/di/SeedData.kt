package com.collegereview.campuschronicles.di

import com.collegereview.campuschronicles.data.local.entities.QuestionEntity

object SeedData {
    fun getDefaultQuestions(): List<QuestionEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // --- Library ---
            QuestionEntity(
                questionCode = "LIB_001",
                questionTitle = "Resource Availability",
                questionText = "How satisfied are you with the availability of the latest academic journals in the Central Library?",
                category = "RESOURCES",
                responseType = "LIKERT_5",
                mappedGameAction = "UPGRADE_LIBRARY",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            ),
            QuestionEntity(
                questionCode = "LIB_002",
                questionTitle = "Study Environment",
                questionText = "The library provides a quiet and conducive environment for focused study. Do you agree?",
                category = "INFRASTRUCTURE",
                responseType = "LIKERT_5",
                mappedGameAction = "UPGRADE_LIBRARY",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            ),

            // --- Lab ---
            QuestionEntity(
                questionCode = "LAB_001",
                questionTitle = "Lab Equipment",
                questionText = "Are the high-performance computing clusters in Tech Park 1 sufficient for your project needs?",
                category = "RESOURCES",
                responseType = "BINARY",
                mappedGameAction = "UPGRADE_LAB",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            ),

            // --- Canteen ---
            QuestionEntity(
                questionCode = "CAN_001",
                questionTitle = "Food Quality",
                questionText = "Rate the variety and nutritional value of meals served at the JAVA Canteen.",
                category = "WELLBEING",
                responseType = "RATING_10",
                mappedGameAction = "UPGRADE_CANTEEN",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            ),

            // --- Sports ---
            QuestionEntity(
                questionCode = "SPO_001",
                questionTitle = "Facilities Maintenance",
                questionText = "The indoor courts in Tech Park 2 are well-maintained. Your feedback?",
                category = "INFRASTRUCTURE",
                responseType = "OPEN_TEXT",
                mappedGameAction = "UPGRADE_SPORTS",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            ),

            // --- Admin ---
            QuestionEntity(
                questionCode = "ADM_001",
                questionTitle = "Administrative Support",
                questionText = "How efficient is the student support desk at Vendhar Square for resolving grievances?",
                category = "MANAGEMENT",
                responseType = "LIKERT_5",
                mappedGameAction = "UPGRADE_ADMIN",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            ),

            // --- Medical ---
            QuestionEntity(
                questionCode = "MED_001",
                questionTitle = "Healthcare Access",
                questionText = "The SRM Medical Centre is easily accessible during emergencies. Do you agree?",
                category = "WELLBEING",
                responseType = "BINARY",
                mappedGameAction = "UPGRADE_MEDICAL",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            ),

            // --- Hostel ---
            QuestionEntity(
                questionCode = "HOS_001",
                questionTitle = "Hostel Facilities",
                questionText = "The Nelson Mandela Hostel provides adequate laundry and recreational facilities. Your rating?",
                category = "INFRASTRUCTURE",
                responseType = "RATING_10",
                mappedGameAction = "UPGRADE_HOSTEL",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            ),

            // --- Auditorium ---
            QuestionEntity(
                questionCode = "AUD_001",
                questionTitle = "Event Infrastructure",
                questionText = "The audio-visual systems at T.P. Ganesan Auditorium are world-class. Your feedback?",
                category = "INFRASTRUCTURE",
                responseType = "OPEN_TEXT",
                mappedGameAction = "UPGRADE_AUDITORIUM",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            ),

            // --- Research ---
            QuestionEntity(
                questionCode = "RES_001",
                questionTitle = "Research Grants",
                questionText = "Are you satisfied with the transparency of the research grant allocation process at the NRC?",
                category = "MANAGEMENT",
                responseType = "LIKERT_5",
                mappedGameAction = "UPGRADE_RESEARCH",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            ),

            // --- Innovation ---
            QuestionEntity(
                questionCode = "INN_001",
                questionTitle = "Innovation Support",
                questionText = "Does the Hi-Tech Building provide sufficient mentorship for student startups?",
                category = "RESOURCES",
                responseType = "BINARY",
                mappedGameAction = "UPGRADE_INNOVATION",
                targetGroup = "ALL",
                coinsReward = 100,
                xpReward = 50,
                isActive = true,
                createdAt = now,
                weight = 1.0f
            )
        )
    }
}
