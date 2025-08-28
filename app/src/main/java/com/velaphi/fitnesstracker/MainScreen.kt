package com.velaphi.fitnesstracker

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.velaphi.core.navigation.BottomNavItem
import com.velaphi.goalmanager.GoalManagerScreen
import com.velaphi.mealplan.MealPlanScreen
import com.velaphi.workoutsummary.WorkoutSummaryScreen
import com.velaphi.workouttracker.WorkoutTrackerScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    navController: androidx.navigation.NavHostController,
    onSignOut: () -> Unit = {},
    initialScreen: String? = null
) {
    val items = listOf(
        BottomNavItem.Tracker,
        BottomNavItem.Goals,
        BottomNavItem.Summary,
        BottomNavItem.Meals
    )

    var selectedTab by remember { mutableStateOf(initialScreen ?: BottomNavItem.Goals.route) }

    LaunchedEffect(initialScreen) {
        if (initialScreen != null) {
            selectedTab = initialScreen
            println("MainScreen: initialScreen set to $initialScreen, selectedTab now $selectedTab")
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = selectedTab == item.route,
                        onClick = {
                            selectedTab = item.route
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
                
                // Add sign out button
                NavigationBarItem(
                    selected = false,
                    onClick = onSignOut,
                    icon = { Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out") },
                    label = { Text("Sign Out") }
                )
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            "goals" -> GoalManagerScreen()
            "meals" -> MealPlanScreen()
            "summary" -> WorkoutSummaryScreen()
            "tracker" -> WorkoutTrackerScreen(navController)
            else -> GoalManagerScreen()
        }
    }
}
