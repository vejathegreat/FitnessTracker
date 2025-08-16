package com.velaphi.workouttracker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velaphi.workouttracker.WorkoutState
import com.velaphi.core.data.WorkoutGoal
import com.velaphi.core.data.WorkoutExercise

@Composable
fun WorkoutGoalsList(
    goals: List<WorkoutGoal>,
    workoutState: WorkoutState,
    onStartWorkout: (WorkoutExercise) -> Unit,
    onStopWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (goals.isEmpty()) return
    
    Column(modifier = modifier) {
        Text(
            text = "Your Workout Goals",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(goals) { goal ->
                WorkoutGoalCard(
                    goal = goal,
                    workoutState = workoutState,
                    onStartWorkout = onStartWorkout,
                    onStopWorkout = onStopWorkout
                )
            }
        }
    }
}

@Composable
private fun WorkoutGoalCard(
    goal: WorkoutGoal,
    workoutState: WorkoutState,
    onStartWorkout: (WorkoutExercise) -> Unit,
    onStopWorkout: () -> Unit
) {
    val isActive = goal.isActive && workoutState == WorkoutState.ACTIVE
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                    contentDescription = "Priority ${goal.priority}",
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = goal.exercise.category.name.replace("_", " "),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (goal.priority > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Priority ${goal.priority}",
                        fontSize = 12.sp,
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isActive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = if (isActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isActive) "Stop" else "Start",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isActive) "Stop" else "Start")
            }
        }
    }
}
