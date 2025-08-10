package com.velaphi.fitnesstracker

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.velaphi.workouttracker.WorkoutTrackerScreen
import com.velaphi.workoutsummary.WorkoutSummaryScreen
import com.velaphi.goalmanager.GoalManagerScreen
import com.velaphi.mealplan.MealPlanScreen

sealed class BottomNavItem(val label: String, val route: String, val icon: ImageVector) {
    object Goals : BottomNavItem("Goals", "goals", Icons.Default.Flag)
    object Meals : BottomNavItem("Meals", "meals", Icons.Default.Fastfood)
    object Summary : BottomNavItem("Summary", "summary", Icons.Default.Assessment)
    object Tracker : BottomNavItem("Tracker", "tracker", Icons.Default.FitnessCenter)
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        BottomNavItem.Tracker,
        BottomNavItem.Goals,
        BottomNavItem.Summary,
        BottomNavItem.Meals
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Goals.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Goals.route) { GoalManagerScreen() }
            composable(BottomNavItem.Meals.route) { MealPlanScreen() }
            composable(BottomNavItem.Summary.route) { WorkoutSummaryScreen() }
            composable(BottomNavItem.Tracker.route) { WorkoutTrackerScreen() }
        }
    }
}
