package com.velaphi.workouttracker.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velaphi.core.domain.WorkoutState
import com.velaphi.core.viewmodel.WorkoutViewModel
import com.velaphi.workouttracker.R

@Composable
fun WorkoutStartCard(
    viewModel: WorkoutViewModel,
    onStopWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val workoutState by viewModel.workoutState.collectAsState()
    val workoutDuration by viewModel.workoutDuration.collectAsState()
    val workoutGoals by viewModel.workoutGoals.collectAsState()
    
    val activeGoal = workoutGoals.find { it.isActive }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = if (workoutState == WorkoutState.ACTIVE) stringResource(R.string.workout_in_progress) else stringResource(R.string.workout_timer),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Active exercise info
            if (workoutState == WorkoutState.ACTIVE && activeGoal != null) {
                Text(
                    text = stringResource(R.string.current_exercise, activeGoal.exercise.name),
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
                    text = stringResource(R.string.timer_default),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            
            // Start/Stop button - Only show when workout is active
            if (workoutState == WorkoutState.ACTIVE) {
                Button(
                    onClick = onStopWorkout,
                    enabled = true,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .height(56.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = stringResource(R.string.stop_workout),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.stop_workout),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Info text
            if (workoutState == WorkoutState.IDLE) {
                Text(
                    text = if (workoutGoals.isNotEmpty()) 
                        stringResource(R.string.start_workout_from_goals)
                    else 
                        stringResource(R.string.add_workout_goals_below),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
