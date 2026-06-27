package com.collegereview.campuschronicles.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Map : Screen("map", "Map", Icons.Default.Map)
    object Build : Screen("build", "Build", Icons.Default.Handyman)
    object Upgrade : Screen("upgrade", "Upgrade", Icons.Default.Upgrade)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.Assignment)
    object Social : Screen("social", "Social", Icons.Default.Group)
    object Admin : Screen("admin", "Admin", Icons.Default.Settings)
}

val bottomNavItems = listOf(
    Screen.Build,
    Screen.Upgrade,
    Screen.Tasks,
    Screen.Map,
    Screen.Social
)