package com.collegereview.campuschronicles.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.collegereview.campuschronicles.domain.models.QuestionPresentationModel
import com.collegereview.campuschronicles.domain.models.ResponseType

@Composable
fun CampusDecisionDialog(
    question: QuestionPresentationModel,
    currentIndex: Int = 1,
    totalCount: Int = 1,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit,
    onSpeak: (String) -> Unit
) {
    val progress = currentIndex.toFloat() / totalCount
    
    LaunchedEffect(question) {
        onSpeak(question.narrativeText)
    }

    val angelStage = when {
        progress >= 1f -> "👑 Campus Legend"
        progress >= 0.8f -> "✨ Guardian Angel"
        progress >= 0.6f -> "😇 Young Angel"
        progress >= 0.4f -> "🪽 Small Wings"
        progress >= 0.2f -> "👶 Baby Angel"
        else -> "🥚 New Spirit"
    }
    
    val angelEmoji = when {
        progress >= 1f -> "👑"
        progress >= 0.8f -> "👼"
        progress >= 0.6f -> "😇"
        progress >= 0.4f -> "🪽"
        progress >= 0.2f -> "👶"
        else -> "🥚"
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.95f) // Use percentage instead of fixed width to avoid clipping
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .border(3.dp, Color(0xFF3B82F6), RoundedCornerShape(16.dp))
        ) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3B82F6))
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "CAMPUS DECISION",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                // Left Sidebar: The Angel
                Column(
                    modifier = Modifier
                        .width(100.dp)
                        .background(Color(0xFFF1F5F9))
                        .padding(vertical = 24.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        angelEmoji,
                        fontSize = 48.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        angelStage,
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    IconButton(
                        onClick = { 
                            Log.d("TTS", "Speaker button clicked for: ${question.narrativeText}")
                            onSpeak(question.narrativeText) 
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = Color(0xFF3B82F6)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .height(120.dp)
                            .width(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFCBD5E1))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(progress)
                                .align(Alignment.BottomCenter)
                                .background(Color(0xFF3B82F6))
                        )
                    }
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = Color(0xFF64748B),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Main Content: The Question
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (totalCount > 1) {
                        Text(
                            "Question $currentIndex of $totalCount",
                            color = Color(0xFF64748B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // If there's a title in the question, we can show it here as a sub-header
                    if (question.questionTitle != null) {
                        Text(
                            question.questionTitle.uppercase(),
                            color = Color(0xFF3B82F6),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Text(
                        question.narrativeText,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))

                    when (question.responseType) {
                        ResponseType.OPEN_TEXT -> {
                            var textValue by remember { mutableStateOf("") }
                            OutlinedTextField(
                                value = textValue,
                                onValueChange = { textValue = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                placeholder = { Text("Enter your feedback...") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            val isLast = currentIndex == totalCount
                            Button(
                                onClick = { if (textValue.isNotBlank()) onSubmit(textValue) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                shape = RoundedCornerShape(12.dp),
                                enabled = textValue.isNotBlank()
                            ) {
                                Text(
                                    if (isLast) "FINISH SURVEY" else "SUBMIT RESPONSE", 
                                    color = Color.White, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        ResponseType.RATING_10 -> {
                            var rating by remember { mutableIntStateOf(0) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (rating > 0) "Rating: $rating / 10" else "Select Rating",
                                    color = Color(0xFF1E3A8A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                // Two rows of 5 stars
                                repeat(2) { row ->
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        repeat(5) { index ->
                                            val starValue = (row * 5) + index + 1
                                            IconButton(
                                                onClick = { rating = starValue },
                                                modifier = Modifier.size(40.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (rating >= starValue) Icons.Default.Star else Icons.Outlined.StarBorder,
                                                    contentDescription = "Rate $starValue",
                                                    tint = if (rating >= starValue) Color(0xFFF59E0B) else Color.Gray,
                                                    modifier = Modifier.size(32.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                val isLast = currentIndex == totalCount
                                Button(
                                    onClick = { if (rating > 0) onSubmit(rating.toString()) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = rating > 0
                                ) {
                                    Text(
                                        if (isLast) "FINISH SURVEY" else "SUBMIT RATING", 
                                        color = Color.White, 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        ResponseType.STARS -> {
                            var rating by remember { mutableIntStateOf(0) }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (rating > 0) "Rating: $rating / 5" else "Select Rating",
                                    color = Color(0xFF1E3A8A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    repeat(5) { index ->
                                        val starValue = index + 1
                                        IconButton(
                                            onClick = { rating = starValue },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (rating >= starValue) Icons.Default.Star else Icons.Outlined.StarBorder,
                                                contentDescription = "Rate $starValue",
                                                tint = if (rating >= starValue) Color(0xFFF59E0B) else Color.Gray,
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                val isLast = currentIndex == totalCount
                                Button(
                                    onClick = { if (rating > 0) onSubmit(rating.toString()) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = rating > 0
                                ) {
                                    Text(
                                        if (isLast) "FINISH SURVEY" else "SUBMIT STARS", 
                                        color = Color.White, 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        else -> {
                            val options = when (question.responseType) {
                                ResponseType.LIKERT_5 -> listOf(
                                    "😠 Strongly Disagree",
                                    "🙁 Disagree",
                                    "😐 Neutral",
                                    "🙂 Agree",
                                    "😀 Strongly Agree"
                                )

                                ResponseType.BINARY -> listOf("✅ Yes", "❌ No")
                                ResponseType.MULTIPLE_CHOICE -> question.options.ifEmpty { 
                                    listOf("Option A", "Option B", "Option C", "Option D") 
                                }
                                else -> listOf("A. Suboptimal", "B. Average", "C. Excellent")
                            }

                            options.forEach { opt ->
                                Button(
                                    onClick = { onSubmit(opt) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .height(50.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDBEAFE)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        opt,
                                        color = Color(0xFF1E3A8A),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

