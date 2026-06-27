package com.collegereview.campuschronicles.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.collegereview.campuschronicles.domain.models.Building
import com.collegereview.campuschronicles.domain.models.BuildingInfo
import com.collegereview.campuschronicles.domain.models.getDailyRevenue
import com.collegereview.campuschronicles.domain.models.getUpgradeRequirements

@Composable
fun BuildingDetailDialog(
    info: BuildingInfo,
    state: Building?,
    totalCoins: Int,
    totalXp: Int,
    onAction: () -> Unit,
    onDismiss: () -> Unit
) {
    val isBuilt = state != null
    val level = state?.level ?: 0
    val requirements = if (isBuilt) getUpgradeRequirements(level + 1) else null
    val cost = if (isBuilt) requirements?.coins ?: 0 else info.unlockCost
    val xpRequired = requirements?.xp ?: 0
    
    val canAffordCoins = totalCoins >= cost
    val canAffordXp = totalXp >= xpRequired
    
    val isMaxLevel = level >= 20
    val currentRevenue = if (isBuilt) getDailyRevenue(level) else 0
    val nextRevenue = if (!isMaxLevel) getDailyRevenue(level + 1) else 0

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (isBuilt) "UPGRADE FACILITY" else "NEW CONSTRUCTION",
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    info.displayName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )

                if (isBuilt) {
                    Surface(
                        color = Color(0xFF3B82F6),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            "CURRENT LEVEL: $level",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        "Daily Revenue: $currentRevenue coins",
                        color = Color(0xFF10B981),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (!isMaxLevel) {
                        Text(
                            "Next Level: $nextRevenue coins/day",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    info.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Requirements Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        // Coins Requirement
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("COINS", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "$cost",
                                    color = if (canAffordCoins) Color.White else Color(0xFFEF4444),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // XP Requirement
                        if (isBuilt && !isMaxLevel) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("XP REQ", color = Color(0xFF64748B), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ArrowUpward,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "$xpRequired",
                                        color = if (canAffordXp) Color.White else Color(0xFFEF4444),
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = onAction,
                        enabled = !isMaxLevel,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isBuilt) Color(0xFFF59E0B) else Color(0xFF3B82F6),
                            disabledContainerColor = Color(0xFF334155)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (isMaxLevel) {
                            Text("MAX LEVEL REACHED")
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (isBuilt) Icons.Default.ArrowUpward else Icons.Default.Build,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(if (isBuilt) "UPGRADE" else "CONSTRUCT")
                            }
                        }
                    }
                }
                
                if (!isMaxLevel) {
                    if (!canAffordCoins) {
                        Text(
                            "NEED ${cost - totalCoins} MORE COINS",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    } else if (!canAffordXp) {
                        Text(
                            "NEED ${xpRequired - totalXp} MORE XP",
                            color = Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
