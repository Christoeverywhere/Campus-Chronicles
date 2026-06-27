package com.collegereview.campuschronicles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.collegereview.campuschronicles.GameViewModel
import com.collegereview.campuschronicles.domain.models.GameEvent
import com.collegereview.campuschronicles.domain.models.SurveyQuestion
import com.collegereview.campuschronicles.domain.models.buildingCatalog

@Composable
fun QuestionTaskCard(question: SurveyQuestion, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF10B981))
                Spacer(Modifier.width(8.dp))
                Text(question.category.name, color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(question.questionText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(Modifier.height(12.dp))
            Text("Building: ${question.mappedGameAction.name.replace("UPGRADE_", "").replace("_", " ")}", color = Color(0xFF94A3B8), fontSize = 12.sp)
        }
    }
}

@Composable
fun TasksScreen(viewModel: GameViewModel) {
    val events by viewModel.events.collectAsState()
    val unanswered by viewModel.unansweredQuestions.collectAsState()
    val gameState by viewModel.gameState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .padding(top = 32.dp)
    ) {
        Text(
            "Campus Tasks",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Complete these tasks to earn rewards and improve campus life.",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                SectionHeader("Pending Assessments", unanswered.size)
            }
            
            if (unanswered.isEmpty()) {
                item {
                    Text("No pending assessments. Good job!", color = Color(0xFF94A3B8), modifier = Modifier.padding(16.dp))
                }
            } else {
                items(unanswered) { question ->
                    QuestionTaskCard(
                        question = question,
                        onClick = {
                            val info = buildingCatalog.find { it.gameAction == question.mappedGameAction }
                            info?.let { viewModel.onBuildingClicked(it) }
                        }
                    )
                }
            }

            item {
                SectionHeader("Active Events", events.size)
            }

            item {
                SectionHeader("Completed Quizzes", gameState?.completedQuestions?.size ?: 0)
            }
            
            gameState?.completedQuestions?.let { completed ->
                items(completed) { questionCode ->
                    CompletedTaskCard(questionCode)
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6)),
            contentAlignment = Alignment.Center
        ) {
            Text("$count", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun EventTaskCard(event: GameEvent) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF3B82F6))
                Spacer(Modifier.width(8.dp))
                Text(event.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(event.description, color = Color(0xFF94A3B8), fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { /* Handle action */ },
                    label = { Text("ACTION REQUIRED", color = Color.White) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFF3B82F6).copy(alpha = 0.2f))
                )
            }
        }
    }
}

@Composable
fun CompletedTaskCard(questionCode: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(questionCode, color = Color.White, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Text("Assessment completed", color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
    }
}