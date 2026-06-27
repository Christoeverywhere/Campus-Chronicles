package com.collegereview.campuschronicles.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
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
import com.collegereview.campuschronicles.domain.models.Building
import com.collegereview.campuschronicles.domain.models.buildingCatalog
import com.collegereview.campuschronicles.domain.models.getUpgradeRequirements

@Composable
fun UpgradeScreen(viewModel: GameViewModel) {
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
            "Campus Upgrades",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Improve your facilities to boost campus rating.",
            color = Color(0xFF94A3B8),
            fontSize = 14.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        if (buildings.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No buildings to upgrade. Build something first!", color = Color(0xFF94A3B8))
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(buildings) { building ->
                    val req = getUpgradeRequirements(building.level + 1)
                    val canAfford = (gameState?.totalCoins ?: 0) >= req.coins && (gameState?.xp ?: 0) >= req.xp
                    
                    UpgradeCard(
                        building = building,
                        canAfford = canAfford,
                        onUpgrade = { 
                            val info = buildingCatalog.find { it.type == building.buildingType }
                            info?.let { viewModel.onBuildingClicked(it) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun UpgradeCard(
    building: Building,
    canAfford: Boolean,
    onUpgrade: () -> Unit
) {
    val req = getUpgradeRequirements(building.level + 1)
    val upgradeCost = req.coins
    
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(building.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        color = Color(0xFF3B82F6),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            "LVL ${building.level}",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text("Next level: ${building.level + 1} (Req: ${req.xp} XP)", color = Color(0xFF94A3B8), fontSize = 12.sp)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("$upgradeCost", color = if (canAfford) Color.White else Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            }
            
            Button(
                onClick = onUpgrade,
                enabled = building.level < 20,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF59E0B),
                    disabledContainerColor = Color(0xFF334155)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (building.level >= 20) {
                    Text("MAX")
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = null)
                        Text("UPGRADE")
                    }
                }
            }
        }
    }
}
