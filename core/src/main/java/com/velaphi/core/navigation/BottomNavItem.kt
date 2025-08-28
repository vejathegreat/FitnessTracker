package com.velaphi.core.navigation

import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val label: String, val route: String, val icon: ImageVector) {
    object Goals : BottomNavItem("Goals", "goals", androidx.compose.material.icons.Icons.Default.Flag)
    object Meals : BottomNavItem("Meals", "meals", androidx.compose.material.icons.Icons.Default.Fastfood)
    object Summary : BottomNavItem("Summary", "summary", androidx.compose.material.icons.Icons.Default.Assessment)
    object Tracker : BottomNavItem("Tracker", "tracker", androidx.compose.material.icons.Icons.Default.FitnessCenter)
}


