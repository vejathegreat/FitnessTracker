package com.velaphi.workouttracker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velaphi.core.domain.WorkoutState
import com.velaphi.core.viewmodel.WorkoutViewModel
import com.velaphi.core.data.WorkoutGoal

@Composable
fun WorkoutStartCard(
    viewModel: WorkoutViewModel,
    onStartWorkout: () -> Unit,
    onStopWorkout: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val workoutState by viewModel.workoutState.collectAsState()
    val workoutDuration by viewModel.workoutDuration.collectAsState()
    val workoutGoals by viewModel.workoutGoals.collectAsState()
    
    val activeGoal = workoutGoals.find { it.isActive }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (workoutState == WorkoutState.ACTIVE) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = if (workoutState == WorkoutState.ACTIVE) "Workout in Progress" else "Start Workout",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Active exercise info
            if (workoutState == WorkoutState.ACTIVE && activeGoal != null) {
                Text(
                    text = "Current: ${activeGoal.exercise.name}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            // Timer display
            if (workoutState == WorkoutState.ACTIVE) {
                Text(
                    text = viewModel.formatDuration(workoutDuration),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Text(
                    text = "00:00",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            
            // Start/Stop button
            Button(
                onClick = if (workoutState == WorkoutState.ACTIVE) onStopWorkout else onStartWorkout,
                enabled = if (workoutState == WorkoutState.ACTIVE) true else enabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (workoutState == WorkoutState.ACTIVE) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .height(56.dp)
                    .fillMaxWidth()
            ) {
                Icon(
                    imageVector = if (workoutState == WorkoutState.ACTIVE) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (workoutState == WorkoutState.ACTIVE) "Stop Workout" else "Start Workout",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (workoutState == WorkoutState.ACTIVE) "Stop Workout" else "Start Workout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Info text
            if (workoutState == WorkoutState.IDLE) {
                Text(
                    text = if (workoutGoals.isNotEmpty()) 
                        "Tap to start your workout goals" 
                    else 
                        "Add workout goals below to enable workout tracking",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
