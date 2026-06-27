package com.collegereview.campuschronicles.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.collegereview.campuschronicles.domain.models.GameState

@Composable
fun TopHUD(state: GameState, onAdminClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile & Admin trigger
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(Color(0xFF1E293B).copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .clickable { onAdminClick() }
                .padding(end = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF3B82F6))
                    .border(2.dp, Color(0xFFF59E0B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = "Avatar", tint = Color.White)
            }
            Spacer(Modifier.width(8.dp))
            Column {
                Text("Lvl ${state.semesterNumber}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("Campus Manager", color = Color(0xFF94A3B8), fontSize = 10.sp)
            }
        }

        // Resources
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ResourcePill(Icons.Default.MonetizationOn, Color(0xFFF59E0B), "${state.totalCoins}")
            ResourcePill(Icons.Default.EmojiEvents, Color(0xFF8B5CF6), "${state.xp} XP")
            ResourcePill(Icons.Default.Star, Color(0xFF10B981), String.format("%.1f", state.campusRating))
        }
    }
}

@Composable
fun ResourcePill(icon: ImageVector, color: Color, text: String) {
    Row(
        modifier = Modifier
            .background(Color(0xFF1E293B).copy(alpha = 0.9f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}