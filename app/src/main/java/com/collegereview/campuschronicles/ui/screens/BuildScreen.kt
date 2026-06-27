package com.collegereview.campuschronicles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.collegereview.campuschronicles.GameViewModel
import com.collegereview.campuschronicles.domain.models.BuildingInfo
import com.collegereview.campuschronicles.domain.models.buildingCatalog

@Composable
fun BuildScreen(viewModel: GameViewModel) {
    val buildings by viewModel.buildings.collectAsState()
    val gameState by viewModel.gameState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .padding(16.dp)
            .padding(top = 32.dp)
    ) {
        Text(
            "Campus Development",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Expand your campus with new facilities.",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            items(buildingCatalog) { info ->
                val isBuilt = buildings.any { it.buildingType == info.type }
                BuildingBuildCard(
                    info = info,
                    isBuilt = isBuilt,
                    canAfford = (gameState?.totalCoins ?: 0) >= info.unlockCost,
                    onBuild = { viewModel.onBuildingClicked(info) }
                )
            }
        }
    }
}

@Composable
fun BuildingBuildCard(
    info: BuildingInfo,
    isBuilt: Boolean,
    canAfford: Boolean,
    onBuild: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(info.displayName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(info.description, color = Color(0xFF94A3B8), fontSize = 12.sp, lineHeight = 16.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${info.unlockCost}", color = if (canAfford || isBuilt) Color.White else Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
            
            Button(
                onClick = onBuild,
                enabled = !isBuilt && canAfford,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBuilt) Color(0xFF10B981) else Color(0xFF3B82F6),
                    disabledContainerColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isBuilt) {
                    Icon(Icons.Default.Check, contentDescription = null)
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("BUILD")
                    }
                }
            }
        }
    }
}