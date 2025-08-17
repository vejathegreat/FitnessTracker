package com.velaphi.workouttracker

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Star
import com.velaphi.core.domain.WorkoutState
import com.velaphi.workouttracker.components.WorkoutCompletionDialog
import com.velaphi.core.data.WorkoutGoal
import com.velaphi.core.data.WorkoutExercise
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.Manifest

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.core.content.ContextCompat
import androidx.compose.material3.Icon
import androidx.compose.ui.res.stringResource

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WorkoutTrackerScreen(navController: NavController? = null) {
    val viewModel: WorkoutTrackerViewModel = viewModel()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val workoutGoals by viewModel.workoutGoals.collectAsState()
    val workoutState by viewModel.workoutState.collectAsState()
    
    // Workout completion dialog state
    val lastWorkoutData by viewModel.lastWorkoutData.collectAsState()
    val showCompletionDialogState = remember { mutableStateOf(false) }
    val showCompletionDialog by showCompletionDialogState
    
    // Show completion dialog when workout data is available
    LaunchedEffect(lastWorkoutData) {
        if (lastWorkoutData != null) {
            showCompletionDialogState.value = true
        }
    }
    
    // Initialize repository and check workout status
    LaunchedEffect(Unit) {
        viewModel.initializeRepository(context)
        viewModel.initializeSessionRepository(context)
        viewModel.checkWorkoutStatus(context)
    }
    
    // Request notification permission
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            println("Notification permission granted")
        } else {
            println("Notification permission denied")
        }
    }
    
    // Request permission when screen is first displayed
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                println("WorkoutTrackerScreen: Notification permission request launched")
            } catch (e: Exception) {
                println("WorkoutTrackerScreen: Failed to request notification permission: ${e.message}")
            }
        }
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
    
    // Handle workout stopped broadcasts from notifications
    DisposableEffect(Unit) {
        // Only register receiver if we have a valid context, the app is active, and there might be an active workout
        if (!lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            println("WorkoutTrackerScreen: Context null, skipping receiver registration.")
            return@DisposableEffect onDispose { }
        }
        
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == "com.velaphi.workouttracker.WORKOUT_STOPPED") {
                    // Refresh workout status when stopped from notification
                    viewModel.checkWorkoutStatus(context ?: return)
                }
            }
        }
        
        val filter = IntentFilter("com.velaphi.workouttracker.WORKOUT_STOPPED")
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // For API 33+, use the new flag
                context.registerReceiver(receiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED)
                println("WorkoutTrackerScreen: Broadcast receiver registered successfully.")
            } else {
                // For older versions, use the old method
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                println("WorkoutTrackerScreen: Broadcast receiver registered using old method.")
            }
        } catch (e: Exception) {
            // Fallback to old method if new method fails
            println("WorkoutTrackerScreen: Using fallback receiver registration: ${e.message}")
            try {
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                println("WorkoutTrackerScreen: Fallback receiver registration successful.")
            } catch (fallbackException: Exception) {
                println("WorkoutTrackerScreen: Failed to register receiver fallback: ${fallbackException.message}")
            }
        }
        
        onDispose {
            try {
                context.unregisterReceiver(receiver)
                println("WorkoutTrackerScreen: Broadcast receiver unregistered successfully.")
            } catch (e: Exception) {
                println("WorkoutTrackerScreen: Error unregistering receiver: ${e.message}")
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.workout_tracker_title),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        // Notification permission status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.notifications),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.notification_permission_requested),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Timer Card - Moved to top
        WorkoutStartCard(
            viewModel = viewModel,
            onStopWorkout = {
                viewModel.stopWorkout(context)
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Workout Goals Section
        if (workoutGoals.isNotEmpty()) {
            Text(
                text = stringResource(R.string.your_workout_goals),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                workoutGoals.forEach { goal ->
                    WorkoutGoalCard(
                        goal = goal,
                        workoutState = workoutState,
                        onStartWorkout = { exercise ->
                            viewModel.startWorkout(context, exercise)
                        },
                        onStopWorkout = {
                            viewModel.stopWorkout(context)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // No goals selected - show clickable placeholder to add goals
            val addGoalsText = if (navController != null) {
                stringResource(R.string.add_workout_goals)
            } else {
                stringResource(R.string.add_workout_goals)
            }
            
            val addGoalsDescription = if (navController != null) {
                stringResource(R.string.use_goals_tab_below)
            } else {
                stringResource(R.string.select_exercises_below)
            }
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
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
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = stringResource(R.string.navigate_to_goals),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = addGoalsText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = addGoalsDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.use_bottom_navigation_tabs),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
        
        // Note: Exercise selection and filtering has been moved to the Goal Manager screen
        val tipText = if (navController != null) {
            stringResource(R.string.tip_use_goals_tab)
        } else {
            stringResource(R.string.tip_go_to_goal_manager)
        }
        
        Text(
            text = tipText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )
    }
    
    // Workout Completion Dialog
    if (showCompletionDialog && lastWorkoutData != null) {
        WorkoutCompletionDialog(
            exerciseName = lastWorkoutData!!.exerciseName,
            duration = lastWorkoutData!!.duration,
            caloriesBurned = lastWorkoutData!!.caloriesBurned,
            onDismiss = {
                showCompletionDialogState.value = false
                viewModel.clearLastWorkoutData()
            },
            onComplete = { session ->
                viewModel.saveWorkoutSession(session)
                showCompletionDialogState.value = false
            }
        )
    }
}

@Composable
private fun WorkoutGoalCard(
    goal: WorkoutGoal,
    workoutState: WorkoutState,
    onStartWorkout: (WorkoutExercise) -> Unit,
    onStopWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isActive = goal.isActive && workoutState == WorkoutState.ACTIVE
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            if (goal.priority > 0) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = stringResource(R.string.priority, goal.priority),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            // Exercise details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = goal.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.exercise.category.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (goal.priority > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.priority, goal.priority),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Start/Stop button
            Button(
                onClick = {
                    if (isActive) {
                        onStopWorkout()
                    } else {
                        onStartWorkout(goal.exercise)
                    }
                },
                enabled = if (workoutState == WorkoutState.ACTIVE) isActive else true,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.error
                    else if (workoutState == WorkoutState.ACTIVE) MaterialTheme.colorScheme.surfaceVariant
                    else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isActive) stringResource(R.string.stop) else stringResource(R.string.start),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isActive) stringResource(R.string.stop) else stringResource(R.string.start),
                    color = if (workoutState == WorkoutState.ACTIVE && !isActive) 
                        MaterialTheme.colorScheme.onSurfaceVariant 
                    else 
                        MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}