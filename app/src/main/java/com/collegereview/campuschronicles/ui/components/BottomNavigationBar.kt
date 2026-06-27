package com.collegereview.campuschronicles.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.collegereview.campuschronicles.ui.navigation.Screen
import com.collegereview.campuschronicles.ui.navigation.bottomNavItems

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E293B).copy(alpha = 0.95f))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        bottomNavItems.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val isMain = screen == Screen.Upgrade // Let's keep Upgrade as the central "main" button style for now as per current UI
            
            NavIcon(
                screen = screen,
                isSelected = isSelected,
                isMain = isMain,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Map.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun NavIcon(
    screen: Screen,
    isSelected: Boolean,
    isMain: Boolean,
    onClick: () -> Unit
) {
    val activeColor = if (isMain) Color(0xFFF59E0B) else Color(0xFF3B82F6)
    val inactiveColor = Color(0xFF94A3B8)
    val color = if (isSelected) activeColor else inactiveColor
    
    val size = if (isMain) 64.dp else 56.dp
    val iconSize = if (isMain) 32.dp else 24.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset(y = if (isMain) (-16).dp else 0.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isMain) Color(0xFFF59E0B) else Color(0xFF334155))
                .border(2.dp, if (isSelected && !isMain) activeColor else Color.Transparent, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                screen.icon,
                contentDescription = screen.title,
                tint = if (isMain) Color.White else color,
                modifier = Modifier.size(iconSize)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            screen.title.uppercase(),
            color = if (isSelected) Color.White else inactiveColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}