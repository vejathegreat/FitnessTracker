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
import com.velaphi.workouttracker.components.WorkoutGoalsList
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import com.velaphi.core.domain.WorkoutState
import com.velaphi.workouttracker.components.WorkoutCompletionDialog
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
            println("WorkoutTrackerScreen: Context is null or app not active, skipping receiver registration")
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
                println("WorkoutTrackerScreen: Broadcast receiver registered successfully with RECEIVER_NOT_EXPORTED")
            } else {
                // For older versions, use the old method
                ContextCompat.registerReceiver(
                    context,
                    receiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
                )
                println("WorkoutTrackerScreen: Broadcast receiver registered successfully with old method")
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
                println("WorkoutTrackerScreen: Fallback receiver registration successful")
            } catch (fallbackException: Exception) {
                println("WorkoutTrackerScreen: Failed to register receiver even with fallback: ${fallbackException.message}")
            }
        }
        
        onDispose {
            try {
                context.unregisterReceiver(receiver)
                println("WorkoutTrackerScreen: Broadcast receiver unregistered successfully")
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
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Workout Tracker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Notification permission status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
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
                        contentDescription = "Notifications",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Notification permission requested",
                        style = MaterialTheme.typography.bodySmall,
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