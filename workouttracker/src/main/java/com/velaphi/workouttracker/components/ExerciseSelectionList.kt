package com.velaphi.workouttracker.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.velaphi.core.data.ExerciseCategory
import com.velaphi.core.data.WorkoutExercise
import com.velaphi.core.data.WorkoutExerciseRepository

@Composable
fun ExerciseSelectionList(
    selectedExercises: List<WorkoutExercise>,
    onExerciseSelected: (WorkoutExercise) -> Unit,
    onExerciseDeselected: (WorkoutExercise) -> Unit,
    modifier: Modifier = Modifier
) {
    val exercises = remember { WorkoutExerciseRepository.getWorkoutExercises() }
    var selectedCategory by remember { mutableStateOf<ExerciseCategory?>(null) }
    
    Column(modifier = modifier) {
        // Category filter chips
        Text(
            text = "Filter by Category:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .padding(horizontal = 16.dp),
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
                    Text("All")
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
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Exercise list
        Text(
            text = "Available Exercises:",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp), // Constrain the height to prevent infinite constraints
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filteredExercises = if (selectedCategory != null) {
                exercises.filter { it.category == selectedCategory }
            } else {
                exercises
            }
            
            items(filteredExercises) { exercise ->
                val isSelected = selectedExercises.any { it.id == exercise.id }
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = exercise.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = exercise.category.name.replace("_", " "),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                exercise.muscleGroups.forEach { muscleGroup ->
                                    Text(
                                        text = muscleGroup.name.replace("_", " "),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceVariant,
                                                shape = MaterialTheme.shapes.small
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                if (isSelected) {
                                    onExerciseDeselected(exercise)
                                } else {
                                    onExerciseSelected(exercise)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(if (isSelected) "Remove" else "Add")
                        }
                    }
                }
            }
        }
    }
}
