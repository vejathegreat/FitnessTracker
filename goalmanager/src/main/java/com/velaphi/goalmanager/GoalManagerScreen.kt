package com.velaphi.goalmanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.velaphi.core.data.ExerciseCategory
import com.velaphi.core.data.WorkoutExercise
import com.velaphi.core.data.WorkoutExerciseRepository
import com.velaphi.core.data.WorkoutGoal
import com.velaphi.core.data.WorkoutGoalRepository

@Composable
fun GoalManagerScreen() {
    val context = LocalContext.current
    val goalRepository = remember { WorkoutGoalRepository.getInstance(context) }
    val goals by goalRepository.goals.collectAsState()
    val allExercises = remember { WorkoutExerciseRepository.getWorkoutExercises() }
    var selectedCategory by remember { mutableStateOf<ExerciseCategory?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = stringResource(R.string.filter_by_category),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Button(
                    onClick = { selectedCategory = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedCategory == null) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(stringResource(R.string.all))
                }
            }
            
            items(ExerciseCategory.values()) { category ->
                Button(
                    onClick = { selectedCategory = category },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedCategory == category) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(category.name.replace("_", " "))
                }
            }
        }
        
        // Available Exercises Section
        Text(
            text = stringResource(R.string.available_exercises),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val filteredExercises = if (selectedCategory != null) {
                allExercises.filter { it.category == selectedCategory }
            } else {
                allExercises
            }
            
            items(filteredExercises) { exercise ->
                val isSelected = goals.any { it.exercise.id == exercise.id }
                
                AvailableExerciseCard(
                    exercise = exercise,
                    isSelected = isSelected,
                    onToggleGoal = { 
                        if (isSelected) {
                            goalRepository.deselectGoal(exercise)
                        } else {
                            goalRepository.selectGoal(exercise)
                        }
                    }
                )
            }
        }
    }
}



@Composable
private fun AvailableExerciseCard(
    exercise: WorkoutExercise,
    isSelected: Boolean,
    onToggleGoal: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = exercise.category.name.replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                // Muscle group chips with proper wrapping using custom layout
                WrappingChips(
                    items = exercise.muscleGroups.take(3).map { it.name.replace("_", " ") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Add/Remove goal button
            Button(
                onClick = onToggleGoal,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Remove else Icons.Default.Add,
                    contentDescription = if (isSelected) stringResource(R.string.remove_goal_description) else stringResource(R.string.add_goal_description),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (isSelected) stringResource(R.string.remove) else stringResource(R.string.add_goal))
            }
        }
    }
}

@Composable
private fun WrappingChips(
    items: List<String>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Bullet point
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    modifier = Modifier.size(4.dp)
                ) { }
                
                // Item text
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}