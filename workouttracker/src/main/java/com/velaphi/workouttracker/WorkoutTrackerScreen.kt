package com.velaphi.workouttracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.velaphi.workouttracker.components.WorkoutStartCard
import com.velaphi.workouttracker.components.SelectedExercisesSummary
import com.velaphi.workouttracker.components.WorkoutGoalsList
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import com.velaphi.core.domain.WorkoutState

@Composable
fun WorkoutTrackerScreen(navController: NavController? = null) {
    val viewModel: WorkoutTrackerViewModel = viewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val selectedExercises by viewModel.selectedExercises.collectAsState()
    val workoutGoals by viewModel.workoutGoals.collectAsState()
    val workoutState by viewModel.workoutState.collectAsState()
    
    // Initialize repository and check workout status
    LaunchedEffect(Unit) {
        viewModel.initializeRepository(context)
        viewModel.checkWorkoutStatus(context)
    }
    
    // Handle app lifecycle to sync with service only when necessary
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (viewModel.workoutState.value == WorkoutState.ACTIVE) {
                        // If workout is active, just sync the current time without restarting
                        viewModel.syncWithService(context)
                    } else {
                        // Only check status if we don't already have an active workout
                        viewModel.checkWorkoutStatus(context)
                    }
                }
                else -> {}
            }
        }
        
        lifecycleOwner.lifecycle.addObserver(observer)
        
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Workout Tracker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        

        
        // Timer Card - Moved to top
        WorkoutStartCard(
            viewModel = viewModel,
            onStartWorkout = {
                viewModel.startWorkout(context)
            },
            onStopWorkout = {
                viewModel.stopWorkout(context)
            },
            enabled = workoutGoals.isNotEmpty(), // Only enable when goals are selected
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Workout Goals List (if goals are set)
        if (workoutGoals.isNotEmpty()) {
            WorkoutGoalsList(
                goals = workoutGoals,
                workoutState = workoutState,
                onStartWorkout = { exercise ->
                    viewModel.startWorkout(context, exercise)
                },
                onStopWorkout = {
                    viewModel.stopWorkout(context)
                },
                modifier = Modifier.padding(bottom = 16.dp)
            )
        } else {
            // No goals selected - show clickable placeholder to add goals
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable {
                        if (navController != null) {
                            // Instead of programmatic navigation, just refresh goals
                            // User should use the Goals tab in bottom navigation
                            viewModel.forceRefreshGoals()
                        } else {
                            // Fallback when navigation is not available
                            viewModel.forceRefreshGoals()
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (navController != null) {
                            "➕ Add Workout Goals"
                        } else {
                            "➕ Refresh Goals"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (navController != null) {
                            "Use the Goals tab below to select exercises"
                        } else {
                            "Tap to refresh goals from storage"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    if (navController != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Use Goals tab below",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        // Selected Exercises Summary
        SelectedExercisesSummary(
            selectedExercises = selectedExercises,
            onExerciseRemoved = { exercise ->
                viewModel.deselectExercise(exercise)
            }
        )
        
        // Note: Exercise selection and filtering has been moved to the Goal Manager screen
        Text(
            text = if (navController != null) {
                "💡 Tip: Use the Goals tab below to select exercises. The Tracker tab will show your selected goals."
            } else {
                "💡 Tip: Go to Goal Manager to select and filter exercises for your workout goals"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
    }
}