package com.collegereview.campuschronicles.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.collegereview.campuschronicles.domain.models.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onNavigateBack: () -> Unit,
    onSaveSurvey: (Survey, () -> Unit) -> Unit,
) {
    var surveyTitle by remember { mutableStateOf("") }
    var selectedTriggerAction by remember { mutableStateOf(GameAction.UPGRADE_LIBRARY) }
    var expandedTrigger by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    
    val questions = remember { mutableStateListOf(Question(text = "", type = ResponseType.LIKERT_5)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin: Survey Builder") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, enabled = !isSaving) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E293B),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = surveyTitle,
                onValueChange = { surveyTitle = it },
                label = { Text("Survey Name (e.g. Central Library Feedback)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFF5B8DD9),
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = Color(0xFF5B8DD9),
                    unfocusedBorderColor = Color.Gray
                )
            )

            // Trigger Action Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedTrigger && !isSaving,
                onExpandedChange = { if (!isSaving) expandedTrigger = !expandedTrigger }
            ) {
                OutlinedTextField(
                    value = selectedTriggerAction.getDisplayName(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Trigger Event (Game Action)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTrigger) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    enabled = !isSaving,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF5B8DD9)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedTrigger,
                    onDismissRequest = { expandedTrigger = false }
                ) {
                    GameAction.entries.forEach { action ->
                        DropdownMenuItem(
                            text = { Text(action.getDisplayName()) },
                            onClick = {
                                selectedTriggerAction = action
                                expandedTrigger = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
            
            Text("Questions", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            questions.forEachIndexed { index, question ->
                QuestionItem(
                    index = index,
                    question = question,
                    enabled = !isSaving,
                    onQuestionChange = { updated -> questions[index] = updated },
                    onRemove = { if (questions.size > 1) questions.removeAt(index) }
                )
            }

            OutlinedButton(
                onClick = { questions.add(Question(id = UUID.randomUUID().toString(), text = "", type = ResponseType.LIKERT_5)) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF5B8DD9)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ Add Question", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (surveyTitle.isNotBlank() && questions.all { it.text.isNotBlank() }) {
                        isSaving = true
                        onSaveSurvey(
                            Survey(
                                surveyId = "SURV_" + UUID.randomUUID().toString().take(6).uppercase(),
                                title = surveyTitle,
                                triggerAction = selectedTriggerAction,
                                questions = questions.toList()
                            )
                        ) {
                            isSaving = false
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isSaving && surveyTitle.isNotBlank() && questions.all { it.text.isNotBlank() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Survey", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionItem(
    index: Int,
    question: Question,
    enabled: Boolean = true,
    onQuestionChange: (Question) -> Unit,
    onRemove: () -> Unit
) {
    var expandedType by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Question ${index + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = onRemove, enabled = enabled) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red.copy(alpha = 0.7f))
                }
            }

            OutlinedTextField(
                value = question.text,
                onValueChange = { onQuestionChange(question.copy(text = it)) },
                label = { Text("Question Text") },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = Color(0xFF5B8DD9),
                    unfocusedLabelColor = Color.Gray,
                    focusedBorderColor = Color(0xFF5B8DD9),
                    unfocusedBorderColor = Color.Gray
                )
            )

            ExposedDropdownMenuBox(
                expanded = expandedType && enabled,
                onExpandedChange = { if (enabled) expandedType = !expandedType }
            ) {
                OutlinedTextField(
                    value = question.type.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    enabled = enabled,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF5B8DD9)
                    )
                )
                ExposedDropdownMenu(
                    expanded = expandedType,
                    onDismissRequest = { expandedType = false }
                ) {
                    ResponseType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.name) },
                            onClick = {
                                onQuestionChange(question.copy(type = type))
                                expandedType = false
                            }
                        )
                    }
                }
            }

            if (question.type == ResponseType.MULTIPLE_CHOICE) {
                var optionsText by remember { mutableStateOf(question.options.joinToString("\n")) }
                OutlinedTextField(
                    value = optionsText,
                    onValueChange = {
                        optionsText = it
                        onQuestionChange(question.copy(options = it.split("\n").filter { line -> line.isNotBlank() }))
                    },
                    label = { Text("Options (One per line)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = enabled,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = Color(0xFF5B8DD9),
                        unfocusedLabelColor = Color.Gray,
                        focusedBorderColor = Color(0xFF5B8DD9),
                        unfocusedBorderColor = Color.Gray
                    )
                )
            }
        }
    }
}
